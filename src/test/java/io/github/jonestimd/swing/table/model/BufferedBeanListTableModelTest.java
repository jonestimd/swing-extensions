package io.github.jonestimd.swing.table.model;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import static java.util.Collections.*;
import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BufferedBeanListTableModelTest {
    private static final ColumnAdapter<TestBean, String> COLUMN_ADAPTER1 = new TestColumnAdapter("column1") {
        public String getValue(TestBean row) {
            return row.column1;
        }

        public void setValue(TestBean row, String value) {
            row.column1 = value;
        }
    };
    private static final ColumnAdapter<TestBean, String> COLUMN_ADAPTER2 = new TestColumnAdapter("column2") {
        public String getValue(TestBean row) {
            return row.column2;
        }

        public void setValue(TestBean row, String value) {
            row.column2 = value;
        }
    };
    private TableModelListener listener = mock(TableModelListener.class);
    @SuppressWarnings("unchecked")
    private BufferedBeanListTableModel<TestBean> model = new BufferedBeanListTableModel<>(COLUMN_ADAPTER1, COLUMN_ADAPTER2);

    @Before
    public void setUp() {
        model.addTableModelListener(listener);
    }

    @Test
    public void testSetValue() throws Exception {
        model.setBeans(singletonList(new TestBean()));
        assertThat(model.isChanged()).isFalse();

        model.setValueAt("value1", 0, 0);
        assertThat(model.isChanged()).isTrue();
        assertThat(model.isChangedAt(0, 0)).isTrue();
        assertThat(model.isChangedAt(0, 1)).isFalse();

        model.setValueAt("value2", 0, 1);
        assertThat(model.isChanged()).isTrue();
        assertThat(model.isChangedAt(0, 0)).isTrue();
        assertThat(model.isChangedAt(0, 1)).isTrue();

        assertEquals("value1", model.getValueAt(0, 0));
        assertEquals("value2", model.getValueAt(0, 1));

        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 0, Integer.MAX_VALUE, -1));
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 0, 0, 0));
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 0, 0, 1));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void testRevert() throws Exception {
        model.setBeans(Arrays.asList(new TestBean(), new TestBean()));
        assertThat(model.isChanged()).isFalse();

        model.setValueAt("value1", 0, 0);
        assertThat(model.isChanged()).isTrue();
        assertThat(model.isChangedAt(0, 0)).isTrue();
        assertThat(model.isChangedAt(0, 1)).isFalse();

        model.queueAdd(new TestBean());
        assertThat(model.isChangedAt(2, 0)).isTrue();
        assertThat(model.isChangedAt(2, 1)).isTrue();

        model.queueDelete(model.getRow(1));
        assertThat(model.isChangedAt(1, 0));
        assertThat(model.isChangedAt(1, 1));

        model.revert();
        assertEquals(2, model.getBeans().size());
        assertThat(model.isChanged()).isFalse();
        assertThat(model.isChangedAt(0, 0)).isFalse();
        assertThat(model.isChangedAt(0, 1)).isFalse();
        assertThat(model.isChangedAt(1, 0)).isFalse();
        assertThat(model.isChangedAt(1, 1)).isFalse();

        model.setValueAt("value2", 0, 1);
        assertThat(model.isChanged()).isTrue();
        assertThat(model.isChangedAt(0, 0)).isFalse();
        assertThat(model.isChangedAt(0, 1)).isTrue();
        assertNull(model.getValueAt(0, 0));
        assertEquals("value2", model.getValueAt(0, 1));

        model.setValueAt(null, 0, 1);
        assertThat(model.isChanged()).isFalse();
        assertThat(model.isChangedAt(0, 0)).isFalse();
        assertThat(model.isChangedAt(0, 1)).isFalse();
        assertNull(model.getValueAt(0, 0));
        assertNull(model.getValueAt(0, 1));

        InOrder inOrder = inOrder(listener);
        // setBeans()
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 0, Integer.MAX_VALUE, -1));
        // setValueAt()
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 0, 0, 0));
        // pendingAdd()
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.INSERT, 2, 2, -1));
        // pendingDelete()
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 1, 1, -1));
        // revert()
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.DELETE, 2, 2, -1)); // undo add
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 1, 1, -1)); // undo delete
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 0, 0, 0)); // undo setValue
        inOrder.verify(listener, times(2)).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 0, 0, 1));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void testCommit() throws Exception {
        model.setBeans(Arrays.asList(new TestBean(), new TestBean()));
        assertThat(model.isChanged()).isFalse();

        model.setValueAt("value1", 0, 0);
        assertThat(model.isChanged()).isTrue();
        assertThat(model.isChangedAt(0, 0)).isTrue();
        assertThat(model.isChangedAt(0, 1)).isFalse();

        model.queueAdd(new TestBean());
        assertThat(model.isChangedAt(2, 0)).isTrue();
        assertThat(model.isChangedAt(2, 1)).isTrue();

        model.queueDelete(model.getRow(1));
        assertThat(model.isChangedAt(1, 0)).isTrue();
        assertThat(model.isChangedAt(1, 1)).isTrue();

        List<TestBean> beans = model.getChangedRows().collect(Collectors.toList());
        assertThat(beans).hasSize(3);
        assertThat(beans.containsAll(model.getBeans())).isTrue();
        model.commit();
        assertThat(model.getBeans()).hasSize(2);
        assertThat(model.getBeans()).containsOnly(beans.get(0), beans.get(1));
        assertThat(model.isChanged()).isFalse();
        assertThat(model.isChangedAt(0, 0)).isFalse();
        assertThat(model.isChangedAt(0, 1)).isFalse();
        assertThat(model.isChangedAt(1, 0)).isFalse();
        assertThat(model.isChangedAt(1, 1)).isFalse();

        model.setValueAt("value2", 0, 1);
        assertThat(model.isChanged()).isTrue();
        assertThat(model.isChangedAt(0, 0)).isFalse();
        assertThat(model.isChangedAt(0, 1)).isTrue();

        model.revert();
        assertThat(model.isChanged()).isFalse();
        assertThat(model.isChangedAt(0, 0)).isFalse();
        assertThat(model.isChangedAt(0, 1)).isFalse();

        assertEquals("value1", model.getValueAt(0, 0));
        assertNull(model.getValueAt(0, 1));
        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 0, Integer.MAX_VALUE, -1));
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 0, 0, 0));
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.INSERT, 2, 2, -1));
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 1, 1, -1));
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 2, 2, -1));
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.DELETE, 1, 1, -1));
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 0, 0, -1));
        inOrder.verify(listener, times(2)).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 0, 0, 1));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void testRemoveChangedRow() throws Exception {
        TestBean testBean = new TestBean();
        model.setBeans(Arrays.asList(testBean, new TestBean()));
        assertThat(model.isChanged()).isFalse();

        model.setValueAt("value1", 0, 0);
        assertThat(model.isChanged()).isTrue();
        assertThat(model.isChangedAt(0, 0)).isTrue();

        model.queueDelete(model.getRow(0));
        assertThat(model.isChanged()).isTrue();
        assertThat(model.isChangedAt(0, 0)).isTrue();
        assertThat(model.isChangedAt(0, 1)).isTrue();
        assertThat(model.isPendingDelete(0)).isTrue();

        List<TestBean> beans = model.getChangedRows().collect(Collectors.toList());
        assertThat(beans).containsExactly(testBean, testBean).as("both changed and to be deleted");
        assertThat(testBean.column1).isEqualTo("value1").as("still the changed value");
        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 0, Integer.MAX_VALUE, -1));
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 0, 0, 0));
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 0, 0, -1));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void testAddRow() throws Exception {
        model.setBeans(singletonList(new TestBean()));

        model.queueAdd(new TestBean());
        assertThat(model.isPendingAdd(1)).isTrue();
        assertThat(model.isChanged()).isTrue();
        assertThat(model.isChangedAt(0, 0)).isFalse();
        assertThat(model.isChangedAt(0, 1)).isFalse();
        assertThat(model.isChangedAt(1, 0)).isTrue();
        assertThat(model.isChangedAt(1, 1)).isTrue();

        model.queueAdd(0, new TestBean());
        assertThat(model.isChanged()).isTrue();
        assertThat(model.isChangedAt(0, 0)).isTrue();
        assertThat(model.isChangedAt(0, 1)).isTrue();
        assertThat(model.isChangedAt(1, 0)).isFalse();
        assertThat(model.isChangedAt(1, 1)).isFalse();
        assertThat(model.isChangedAt(2, 0)).isTrue();
        assertThat(model.isChangedAt(2, 1)).isTrue();

        model.setValueAt("value1", 0, 0);
        assertEquals("write-through on added rows", "value1", model.getBeans().get(0).column1);
        assertThat(model.isChangedAt(0, 0)).isTrue();

        List<TestBean> beans = model.getChangedRows().collect(Collectors.toList());
        model.commit();
        assertEquals(2, beans.size());
        assertThat(beans.contains(model.getBeans().get(0))).isTrue();
        assertThat(beans.contains(model.getBeans().get(2))).isTrue();
        assertThat(model.isChanged()).isFalse();
        assertThat(model.isChangedAt(0, 0)).isFalse();
        assertThat(model.isChangedAt(0, 1)).isFalse();
        assertThat(model.isChangedAt(2, 0)).isFalse();
        assertThat(model.isChangedAt(2, 1)).isFalse();

        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 0, Integer.MAX_VALUE, -1));
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.INSERT, 1, 1, -1));
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.INSERT, 0, 0, -1));
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 0, 0, 0));
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 2, 2, -1));
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 0, 0, -1));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void testCancelAddedRow() throws Exception {
        model.setBeans(singletonList(new TestBean()));

        model.queueAdd(new TestBean());
        assertThat(model.isPendingAdd(1)).isTrue();
        assertThat(model.isChanged()).isTrue();
        assertThat(model.isChangedAt(0, 0)).isFalse();
        assertThat(model.isChangedAt(0, 1)).isFalse();
        assertThat(model.isChangedAt(1, 0)).isTrue();
        assertThat(model.isChangedAt(1, 1)).isTrue();

        assertThat(model.queueDelete(model.getRow(1))).isFalse();

        assertThat(model.isChanged()).isFalse();
        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 0, Integer.MAX_VALUE, -1));
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.INSERT, 1, 1, -1));
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.DELETE, 1, 1, -1));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void testSetBeansClearsChanges() throws Exception {
        model.setBeans(Arrays.asList(new TestBean(), new TestBean()));
        assertThat(model.isChanged()).isFalse();

        model.queueAdd(new TestBean());
        assertThat(model.isPendingAdd(2)).isTrue();
        assertThat(model.isChanged()).isTrue();

        model.setValueAt("value1", 0, 0);
        assertThat(model.isChanged()).isTrue();

        assertThat(model.queueDelete(model.getRow(1))).isTrue();
        assertThat(model.isChanged()).isTrue();

        model.setBeans(singletonList(new TestBean()));
        assertThat(model.isChanged()).isFalse();
    }

    @Test
    public void testPendingDelete() throws Exception {
        TestBean bean = new TestBean();
        model.setBeans(singletonList(bean));
        assertThat(model.isCellEditable(0, 0)).isTrue();

        assertThat(model.queueDelete(bean)).isTrue();

        assertThat(model.isChanged()).isTrue();
        assertThat(model.isChangedAt(0, 0)).isTrue();
        assertThat(model.isChangedAt(0, 1)).isTrue();
        assertThat(model.isCellEditable(0, 0)).isFalse();
        assertThat(model.getBeans()).contains(bean);
        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 0, Integer.MAX_VALUE, -1));
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 0, 0, -1));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void testCancelDelete() throws Exception {
        TestBean bean = new TestBean();
        model.setBeans(Arrays.asList(bean, new TestBean()));
        model.queueDelete(bean);
        assertThat(model.isCellEditable(0, 0)).isFalse();

        model.cancelDelete(bean);

        assertThat(model.isChanged()).isFalse();
        assertThat(model.isCellEditable(0, 0)).isTrue();
        assertThat(model.getBeans().contains(bean)).isTrue();
        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 0, Integer.MAX_VALUE, -1));
        inOrder.verify(listener, times(2)).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 0, 0, -1));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void testCancelDeleteDoesNothingIfNotPendingDelete() throws Exception {
        model.setBeans(singletonList(new TestBean()));

        model.cancelDelete(new TestBean());

        verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 0, Integer.MAX_VALUE, -1));
        verifyNoMoreInteractions(listener);
    }

    public static class TestBean {
        private String column1;
        private String column2;
    }

    private static abstract class TestColumnAdapter implements ColumnAdapter<TestBean, String> {
        private String name;

        private TestColumnAdapter(String name) {
            this.name = name;
        }

        public String getColumnId() {
            return name;
        }

        public String getResource(String resourceId, String defaultValue) {
            return null;
        }

        public String getName() {
            return name;
        }

        public Class<String> getType() {
            return String.class;
        }

        public boolean isEditable(TestBean row) {
            return true;
        }
    }
}