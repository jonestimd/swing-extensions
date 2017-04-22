package io.github.jonestimd.swing.table.model;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import static io.github.jonestimd.swing.table.model.TableModelEventMatcher.*;
import static java.util.Collections.*;
import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BufferedBeanListTableModelTest {
    private static final ColumnAdapter<TestBean, String> COLUMN_ADAPTER1 = new TestColumnAdapter<>("column1", String.class, TestBean::getColumn1, TestBean::setColumn1);
    private static final ColumnAdapter<TestBean, String> COLUMN_ADAPTER2 = new TestColumnAdapter<>("column2", String.class, TestBean::getColumn2, TestBean::setColumn2);
    private static final BiPredicate<TestBean, TestBean> ID_PREDICATE = (b1, b2) -> Objects.equals(b1.id, b2.id);
    private TableModelListener listener = mock(TableModelListener.class);
    private BufferedBeanListTableModel<TestBean> model = new BufferedBeanListTableModel<>(COLUMN_ADAPTER1, COLUMN_ADAPTER2);

    @Before
    public void setUp() {
        model.addTableModelListener(listener);
    }

    @Test
    public void testSetValue() throws Exception {
        final TestBean bean = new TestBean();
        model.setBeans(Lists.newArrayList(bean, new TestBean()));
        assertThat(model.isChanged()).isFalse();
        assertThat(model.getChangedRows().collect(Collectors.toList())).isEmpty();

        model.setValueAt("value1", 0, 0);
        assertThat(model.isChanged()).isTrue();
        assertThat(model.isChangedAt(0, 0)).isTrue();
        assertThat(model.isChangedAt(0, 1)).isFalse();
        assertThat(model.getPendingUpdates().collect(Collectors.toList())).containsExactly(bean);

        model.setValueAt("value2", 0, 1);
        assertThat(model.isChanged()).isTrue();
        assertThat(model.isChangedAt(0, 0)).isTrue();
        assertThat(model.isChangedAt(0, 1)).isTrue();
        assertThat(model.getPendingUpdates().collect(Collectors.toList())).containsExactly(bean);

        assertEquals("value1", model.getValueAt(0, 0));
        assertEquals("value2", model.getValueAt(0, 1));

        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).tableChanged(tableModelEvent(TableModelEvent.UPDATE, 0, Integer.MAX_VALUE, -1));
        inOrder.verify(listener).tableChanged(tableModelEvent(TableModelEvent.UPDATE, 0, 0, 0));
        inOrder.verify(listener).tableChanged(tableModelEvent(TableModelEvent.UPDATE, 0, 0, 1));
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
        inOrder.verify(listener).tableChanged(tableModelEvent(TableModelEvent.UPDATE, 0, Integer.MAX_VALUE, -1));
        // setValueAt()
        inOrder.verify(listener).tableChanged(tableModelEvent(TableModelEvent.UPDATE, 0, 0, 0));
        // pendingAdd()
        inOrder.verify(listener).tableChanged(tableModelEvent(TableModelEvent.INSERT, 2, 2, -1));
        // pendingDelete()
        inOrder.verify(listener).tableChanged(tableModelEvent(TableModelEvent.UPDATE, 1, 1, -1));
        // revert()
        inOrder.verify(listener).tableChanged(tableModelEvent(TableModelEvent.DELETE, 2, 2, -1)); // undo add
        inOrder.verify(listener).tableChanged(tableModelEvent(TableModelEvent.UPDATE, 1, 1, -1)); // undo delete
        inOrder.verify(listener).tableChanged(tableModelEvent(TableModelEvent.UPDATE, 0, 0, 0)); // undo setValue
        inOrder.verify(listener, times(2)).tableChanged(tableModelEvent(TableModelEvent.UPDATE, 0, 0, 1));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void testCommit() throws Exception {
        model.setBeans(Arrays.asList(new TestBean(), new TestBean()));
        assertThat(model.isChanged()).isFalse();

        model.setValueAt("value1", 0, 0);
        assertThat(model.getPendingUpdates().collect(Collectors.toList())).containsExactly(model.getBean(0));
        assertThat(model.isChanged()).isTrue();
        assertThat(model.isChangedAt(0, 0)).isTrue();
        assertThat(model.isChangedAt(0, 1)).isFalse();

        model.queueAdd(new TestBean());
        assertThat(model.getPendingAdds()).containsExactly(model.getBean(2));
        assertThat(model.isChangedAt(2, 0)).isTrue();
        assertThat(model.isChangedAt(2, 1)).isTrue();

        model.queueDelete(model.getRow(1));
        assertThat(model.getPendingDeletes()).containsExactly(model.getBean(1));
        assertThat(model.isChangedAt(1, 0)).isTrue();
        assertThat(model.isChangedAt(1, 1)).isTrue();

        List<TestBean> beans = model.getChangedRows().collect(Collectors.toList());
        assertThat(beans).hasSize(3);
        assertThat(beans.containsAll(model.getBeans())).isTrue();
        model.commit();
        assertThat(model.getPendingAdds()).isEmpty();
        assertThat(model.getPendingDeletes()).isEmpty();
        assertThat(model.getPendingUpdates().collect(Collectors.toList())).isEmpty();
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
        inOrder.verify(listener).tableChanged(tableModelEvent(TableModelEvent.UPDATE, 0, Integer.MAX_VALUE, -1));
        inOrder.verify(listener).tableChanged(tableModelEvent(TableModelEvent.UPDATE, 0, 0, 0));
        inOrder.verify(listener).tableChanged(tableModelEvent(TableModelEvent.INSERT, 2, 2, -1));
        inOrder.verify(listener).tableChanged(tableModelEvent(TableModelEvent.UPDATE, 1, 1, -1));
        inOrder.verify(listener).tableChanged(tableModelEvent(TableModelEvent.UPDATE, 2, 2, -1));
        inOrder.verify(listener).tableChanged(tableModelEvent(TableModelEvent.DELETE, 1, 1, -1));
        inOrder.verify(listener).tableChanged(tableModelEvent(TableModelEvent.UPDATE, 0, 0, -1));
        inOrder.verify(listener, times(2)).tableChanged(tableModelEvent(TableModelEvent.UPDATE, 0, 0, 1));
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
        inOrder.verify(listener).tableChanged(tableModelEvent(TableModelEvent.UPDATE, 0, Integer.MAX_VALUE, -1));
        inOrder.verify(listener).tableChanged(tableModelEvent(TableModelEvent.UPDATE, 0, 0, 0));
        inOrder.verify(listener).tableChanged(tableModelEvent(TableModelEvent.UPDATE, 0, 0, -1));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void testAddRow() throws Exception {
        model.setBeans(singletonList(new TestBean()));

        model.queueAdd(new TestBean());
        assertThat(model.getPendingAdds()).containsExactly(model.getBean(1));
        assertThat(model.isPendingAdd(1)).isTrue();
        assertThat(model.isChanged()).isTrue();
        assertThat(model.isChangedAt(0, 0)).isFalse();
        assertThat(model.isChangedAt(0, 1)).isFalse();
        assertThat(model.isChangedAt(1, 0)).isTrue();
        assertThat(model.isChangedAt(1, 1)).isTrue();

        model.queueAdd(0, new TestBean());
        assertThat(model.getPendingAdds()).containsExactly(model.getBean(2), model.getBean(0));
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
        assertEquals(2, beans.size());
        assertThat(beans.contains(model.getBeans().get(0))).isTrue();
        assertThat(beans.contains(model.getBeans().get(2))).isTrue();
        model.commit();
        assertThat(model.getPendingAdds()).isEmpty();
        assertThat(model.isChanged()).isFalse();
        assertThat(model.isChangedAt(0, 0)).isFalse();
        assertThat(model.isChangedAt(0, 1)).isFalse();
        assertThat(model.isChangedAt(2, 0)).isFalse();
        assertThat(model.isChangedAt(2, 1)).isFalse();

        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).tableChanged(tableModelEvent(TableModelEvent.UPDATE, 0, Integer.MAX_VALUE, -1));
        inOrder.verify(listener).tableChanged(tableModelEvent(TableModelEvent.INSERT, 1, 1, -1));
        inOrder.verify(listener).tableChanged(tableModelEvent(TableModelEvent.INSERT, 0, 0, -1));
        inOrder.verify(listener).tableChanged(tableModelEvent(TableModelEvent.UPDATE, 0, 0, 0));
        inOrder.verify(listener).tableChanged(tableModelEvent(TableModelEvent.UPDATE, 2, 2, -1));
        inOrder.verify(listener).tableChanged(tableModelEvent(TableModelEvent.UPDATE, 0, 0, -1));
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
        inOrder.verify(listener).tableChanged(tableModelEvent(TableModelEvent.UPDATE, 0, Integer.MAX_VALUE, -1));
        inOrder.verify(listener).tableChanged(tableModelEvent(TableModelEvent.INSERT, 1, 1, -1));
        inOrder.verify(listener).tableChanged(tableModelEvent(TableModelEvent.DELETE, 1, 1, -1));
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
        inOrder.verify(listener).tableChanged(tableModelEvent(TableModelEvent.UPDATE, 0, Integer.MAX_VALUE, -1));
        inOrder.verify(listener).tableChanged(tableModelEvent(TableModelEvent.UPDATE, 0, 0, -1));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void undoDelete() throws Exception {
        TestBean bean = new TestBean();
        model.setBeans(Arrays.asList(bean, new TestBean()));
        model.queueDelete(bean);
        assertThat(model.isCellEditable(0, 0)).isFalse();

        model.undoDelete(0);

        assertThat(model.isChanged()).isFalse();
        assertThat(model.isCellEditable(0, 0)).isTrue();
        assertThat(model.getBeans().contains(bean)).isTrue();
        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).tableChanged(tableModelEvent(TableModelEvent.UPDATE, 0, Integer.MAX_VALUE, -1));
        inOrder.verify(listener, times(2)).tableChanged(tableModelEvent(TableModelEvent.UPDATE, 0, 0, -1));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void undoDeleteDoesNothingIfNotPendingDelete() throws Exception {
        model.setBeans(singletonList(new TestBean()));

        model.undoDelete(0);

        verify(listener).tableChanged(tableModelEvent(TableModelEvent.UPDATE, 0, Integer.MAX_VALUE, -1));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void undoChangeAtRevertsChange() throws Exception {
        model.setBeans(singletonList(new TestBean()));
        model.setValueAt("x", 0, 0);
        assertThat(model.isChangedAt(0, 0)).isTrue();

        model.undoChangedAt(0, 0);

        assertThat(model.isChangedAt(0, 0)).isFalse();
        assertThat(model.getValueAt(0, 0)).isNull();
    }

    @Test
    public void setRowUpdatesPendingDelete() throws Exception {
        TestBean bean1 = new TestBean(1L, "a");
        model.setBeans(singleton(bean1));
        model.queueDelete(bean1);
        TestBean bean2 = new TestBean(1L, "b");
        reset(listener);

        model.setRow(0, bean2);

        assertThat(bean2.column1).isEqualTo("b");
        assertThat(bean2.column2).isNull();
        assertThat(model.getBeanCount()).isEqualTo(1);
        assertThat(model.getValueAt(0, 0)).isEqualTo("b");
        assertThat(model.isPendingDelete(0)).isTrue();
        assertThat(model.getChangedRows().count()).isEqualTo(1);
        assertThat(model.getChangedRows().anyMatch(bean1::equals)).isFalse();
        assertThat(model.getChangedRows().anyMatch(bean2::equals)).isTrue();
        verify(listener).tableChanged(tableModelEvent(TableModelEvent.UPDATE, 0, 0, -1));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void setRowUpdatesPendingAdd() throws Exception {
        TestBean bean1 = new TestBean(1L, "a");
        model.queueAdd(0, bean1);
        TestBean bean2 = new TestBean(1L, "b");
        reset(listener);

        model.setRow(0, bean2);

        assertThat(bean2.column1).isEqualTo("b");
        assertThat(bean2.column2).isNull();
        assertThat(model.getBeanCount()).isEqualTo(1);
        assertThat(model.getValueAt(0, 0)).isEqualTo("b");
        assertThat(model.isPendingAdd(0)).isTrue();
        assertThat(model.getChangedRows().count()).isEqualTo(1);
        assertThat(model.getChangedRows().anyMatch(bean1::equals)).isFalse();
        assertThat(model.getChangedRows().anyMatch(bean2::equals)).isTrue();
        verify(listener).tableChanged(tableModelEvent(TableModelEvent.UPDATE, 0, 0, -1));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void setRowRetainsPendingChange() throws Exception {
        TestBean bean1 = new TestBean(1L, null);
        model.setBeans(singleton(bean1));
        model.setValue("x", 0, 0);
        TestBean bean2 = new TestBean(1L, null);
        reset(listener);

        model.setRow(0, bean2);

        assertThat(bean2.column1).isEqualTo("x");
        assertThat(bean2.column2).isNull();
        assertThat(model.getBeanCount()).isEqualTo(1);
        assertThat(model.getValueAt(0, 0)).isEqualTo("x");
        assertThat(model.isChangedAt(0, 0)).isTrue();
        assertThat(model.getChangedRows().count()).isEqualTo(1);
        assertThat(model.getChangedRows().anyMatch(bean1::equals)).isFalse();
        assertThat(model.getChangedRows().anyMatch(bean2::equals)).isTrue();
        verify(listener).tableChanged(tableModelEvent(TableModelEvent.UPDATE, 0, 0, -1));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void setRowUpdatesOriginalValue() throws Exception {
        TestBean bean1 = new TestBean(1L, "a");
        model.setBeans(singleton(bean1));
        model.setValue("x", 0, 0);
        TestBean bean2 = new TestBean(1L, "y");
        reset(listener);
        model.setRow(0, bean2);

        model.revert();;

        assertThat(bean2.column1).isEqualTo("y");
        assertThat(model.getValueAt(0, 0)).isEqualTo("y");
        assertThat(model.isChangedAt(0, 0)).isFalse();
        assertThat(model.getChangedRows().count()).isEqualTo(0);
        verify(listener).tableChanged(tableModelEvent(TableModelEvent.UPDATE, 0, 0, -1));
        verify(listener).tableChanged(tableModelEvent(TableModelEvent.UPDATE, 0, 0, 0));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void setRowClearsObsoleteChange() throws Exception {
        TestBean bean1 = new TestBean(1L, "a");
        model.setBeans(singleton(bean1));
        model.setValue("x", 0, 0);
        TestBean bean2 = new TestBean(1L, "x");
        reset(listener);

        model.setRow(0, bean2);

        assertThat(bean2.column1).isEqualTo("x");
        assertThat(bean2.column2).isNull();
        assertThat(model.getBeanCount()).isEqualTo(1);
        assertThat(model.getValueAt(0, 0)).isEqualTo("x");
        assertThat(model.isChangedAt(0, 0)).isFalse();
        assertThat(model.getChangedRows().count()).isEqualTo(0);
        verify(listener).tableChanged(tableModelEvent(TableModelEvent.UPDATE, 0, 0, -1));
        verifyNoMoreInteractions(listener);
    }

    public static class TestBean {
        private final long id;
        private String column1;
        private String column2;

        public TestBean() {
            this(1L, null);
        }

        public TestBean(long id, String column1) {
            this.id = id;
            this.column1 = column1;
        }

        public String getColumn1() {
            return column1;
        }

        public void setColumn1(String column1) {
            this.column1 = column1;
        }

        public String getColumn2() {
            return column2;
        }

        public void setColumn2(String column2) {
            this.column2 = column2;
        }
    }
}