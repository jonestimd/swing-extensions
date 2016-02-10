package io.github.jonestimd.swing.table.model;

import java.util.Arrays;
import java.util.List;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import com.google.common.collect.Lists;
import io.github.jonestimd.swing.validation.BeanPropertyValidator;
import org.junit.Test;
import org.mockito.InOrder;

import static java.util.Collections.*;
import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BufferedHeaderDetailTableModelTest {
    private final TestDetailColumnAdapter detailColumnAdapter = new TestDetailColumnAdapter();
    private int nextId = 1;
    private TableModelListener listener = mock(TableModelListener.class);
    private TestSummaryColumnAdapter summaryColumnAdapter = new TestSummaryColumnAdapter();

    @Test
    public void testSetBeans() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        List<TestSummaryBean> beans = Arrays.asList(
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()),
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()));
        model.addTableModelListener(listener);

        model.setBeans(beans);

        verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 0, Integer.MAX_VALUE, -1));
        assertThat(model.getRowCount()).isEqualTo(6);
    }

    @Test
    public void testSetBeansUpdatesErrors() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        List<TestSummaryBean> beans = Arrays.asList(
            new TestSummaryBean("error value", new TestDetailBean(), new TestDetailBean()),
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()));
        beans.get(1).details.get(0).id = -1;

        model.setBeans(beans);
        assertEquals("error value", model.validateAt(0, 0));
        assertEquals("ID less than 0", model.validateAt(4, 0));

        model.setBeans(Arrays.asList(
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()),
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean())));

        assertNull(model.validateAt(0, 0));
        assertNull(model.validateAt(4, 0));
    }

    @Test
    public void testGetRowCount() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        List<TestSummaryBean> beans = Arrays.asList(
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()),
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()));
        model.setBeans(beans);

        assertEquals(6, model.getRowCount());
    }

    @Test
    public void testGetGroupNumber() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        List<TestSummaryBean> beans = Arrays.asList(
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()),
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()));
        model.setBeans(beans);

        assertEquals(0, model.getGroupNumber(0));
        assertEquals(0, model.getGroupNumber(1));
        assertEquals(0, model.getGroupNumber(2));
        assertEquals(1, model.getGroupNumber(3));
        assertEquals(1, model.getGroupNumber(4));
        assertEquals(1, model.getGroupNumber(5));
    }

    @Test
    public void testGetCellClass() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        List<TestSummaryBean> beans = Arrays.asList(
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()),
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()));
        model.setBeans(beans);

        assertEquals(String.class, model.getCellClass(0, 0));
        assertEquals(Integer.class, model.getCellClass(1, 0));
        assertEquals(Integer.class, model.getCellClass(2, 0));
        assertEquals(String.class, model.getCellClass(3, 0));
        assertEquals(Integer.class, model.getCellClass(4, 0));
        assertEquals(Integer.class, model.getCellClass(5, 0));
    }

    @Test
    public void testGetValueAt() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        List<TestSummaryBean> beans = Arrays.asList(
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()),
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()));
        model.setBeans(beans);

        assertEquals(beans.get(0).name, model.getValueAt(0, 0));
        assertEquals(beans.get(0).details.get(0).id, model.getValueAt(1, 0));
        assertEquals(beans.get(0).details.get(1).id, model.getValueAt(2, 0));
        assertEquals(beans.get(1).name, model.getValueAt(3, 0));
        assertEquals(beans.get(1).details.get(0).id, model.getValueAt(4, 0));
        assertEquals(beans.get(1).details.get(1).id, model.getValueAt(5, 0));
    }

    @Test
    public void testIsCellEditable() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        List<TestSummaryBean> beans = Arrays.asList(
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()),
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()));
        model.setBeans(beans);

        assertThat(model.isCellEditable(0, 0)).isFalse();
        assertThat(model.isCellEditable(1, 0)).isTrue();
        assertThat(model.isCellEditable(2, 0)).isTrue();
        assertThat(model.isCellEditable(3, 0)).isFalse();
        assertThat(model.isCellEditable(4, 0)).isTrue();
        assertThat(model.isCellEditable(5, 0)).isTrue();

        model.queueDelete(3);

        assertThat(model.isCellEditable(3, 0)).isFalse();
        assertThat(model.isCellEditable(4, 0)).isFalse();
        assertThat(model.isCellEditable(5, 0)).isFalse();
    }

    @Test
    public void testSetValueAt() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        List<TestSummaryBean> beans = Arrays.asList(
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()),
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()));
        beans.get(0).editable = true;
        beans.get(1).editable = true;
        model.setBeans(beans);
        model.addTableModelListener(listener);

        model.setValueAt("new name", 0, 0);
        model.setValueAt(98, 1, 0);
        model.setValueAt(-1, 2, 0);
        model.setValueAt("xxx", 3, 0);
        model.setValueAt(99, 5, 0);

        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 0, 0, 0));
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 1, 1, 0));
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 0, 0, 0));
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 2, 2, 0));
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 0, 0, 0));
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 3, 3, 0));
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 5, 5, 0));
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 3, 3, 0));
        verifyNoMoreInteractions(listener);
        assertEquals("new name", beans.get(0).name);
        assertEquals(98, beans.get(0).details.get(0).id.intValue());
        assertEquals(-1, beans.get(0).details.get(1).id.intValue());
        assertEquals("ID less than 0", model.validateAt(2, 0));
        assertEquals(99, beans.get(1).details.get(1).id.intValue());
    }

    @Test
    public void testIsSubRow() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        List<TestSummaryBean> beans = Arrays.asList(
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()),
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()));

        model.setBeans(beans);

        assertThat(model.isSubRow(0)).isFalse();
        assertThat(model.isSubRow(1)).isTrue();
        assertThat(model.isSubRow(2)).isTrue();
        assertThat(model.isSubRow(3)).isFalse();
        assertThat(model.isSubRow(4)).isTrue();
        assertThat(model.isSubRow(5)).isTrue();
    }

    @Test
    public void testGetSubRowColumnClass() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();

        assertEquals(String.class, model.getColumnClass(0, 0));
        assertEquals(Integer.class, model.getColumnClass(1, 0));
    }

    @Test
    public void testGetSubRowColumnName() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();

        assertEquals("Summary Name", model.getColumnName(0, 0));
        assertEquals("Detail Id", model.getColumnName(1, 0));
    }

    @Test
    public void testAppendBean() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        List<TestSummaryBean> beans = Arrays.asList(
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()),
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()));
        model.setBeans(beans);
        model.addTableModelListener(listener);

        model.addBean(new TestSummaryBean("error value", new TestDetailBean(), new TestDetailBean()));

        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.INSERT, 6, 8, -1));
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 6, 8, -1));
        verifyNoMoreInteractions(listener);
        assertEquals(9, model.getRowCount());
        assertEquals("error value", model.validateAt(6, 0));
    }

    @Test
    public void testInsertBean() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        List<TestSummaryBean> beans = Arrays.asList(
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()),
            new TestSummaryBean("error value", new TestDetailBean(), new TestDetailBean()));
        model.setBeans(beans);
        assertEquals("error value", model.validateAt(3, 0));
        model.addTableModelListener(listener);

        TestSummaryBean bean = new TestSummaryBean(new TestDetailBean(), new TestDetailBean(), new TestDetailBean());
        model.addBean(1, bean);

        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.INSERT, 3, 6, -1));
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 3, 6, -1));
        verifyNoMoreInteractions(listener);
        assertEquals(10, model.getRowCount());
        assertEquals(beans.get(0).name, model.getValueAt(0, 0));
        assertEquals(bean.name, model.getValueAt(3, 0));
        assertEquals(beans.get(1).name, model.getValueAt(7, 0));
        assertNull(model.validateAt(6, 0));
        assertEquals("error value", model.validateAt(7, 0));
    }

    @Test
    public void testSetBeanWithSameDetails() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        List<TestSummaryBean> beans = Arrays.asList(
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()),
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()));
        model.setBeans(beans);
        model.addTableModelListener(listener);

        TestSummaryBean bean = new TestSummaryBean(new TestDetailBean(), new TestDetailBean());
        model.setBean(1, bean);

        verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 3, 5, -1));
        verifyNoMoreInteractions(listener);
        assertEquals(6, model.getRowCount());
        assertEquals(bean.name, model.getValueAt(3, 0));
    }

    @Test
    public void testSetBeanWithMoreDetails() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        List<TestSummaryBean> beans = Arrays.asList(
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()),
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()),
            new TestSummaryBean("error value", new TestDetailBean(), new TestDetailBean()));
        model.setBeans(beans);
        assertEquals("error value", model.validateAt(6, 0));
        model.addTableModelListener(listener);

        TestSummaryBean bean = new TestSummaryBean(new TestDetailBean(), new TestDetailBean(), new TestDetailBean());
        model.setBean(1, bean);

        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.INSERT, 6, 6, -1));
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 3, 6, -1));
        verifyNoMoreInteractions(listener);
        assertEquals(10, model.getRowCount());
        assertEquals(bean.name, model.getValueAt(3, 0));
        assertNull(model.validateAt(6, 0));
        assertEquals("error value", model.validateAt(7, 0));
    }

    @Test
    public void testSetBeanWithFewerDetails() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        List<TestSummaryBean> beans = Arrays.asList(
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()),
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean(), new TestDetailBean()),
            new TestSummaryBean("error value", new TestDetailBean(), new TestDetailBean()));
        model.setBeans(beans);
        assertEquals("error value", model.validateAt(7, 0));
        model.addTableModelListener(listener);

        TestSummaryBean bean = new TestSummaryBean(new TestDetailBean());
        model.setBean(1, bean);

        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.DELETE, 5, 6, -1));
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 3, 4, -1));
        verifyNoMoreInteractions(listener);
        assertEquals(8, model.getRowCount());
        assertEquals(bean.name, model.getValueAt(3, 0));
        assertNull(model.validateAt(7, 0));
        assertEquals("error value", model.validateAt(5, 0));
    }

    @Test
    public void testRemoveBean() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        List<TestSummaryBean> beans = Arrays.asList(
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()),
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean(), new TestDetailBean()),
            new TestSummaryBean("error value", new TestDetailBean(), new TestDetailBean()));
        model.setBeans(beans);
        assertEquals("error value", model.validateAt(7, 0));
        model.addTableModelListener(listener);

        model.removeBean(beans.get(1));

        verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.DELETE, 3, 6, -1));
        verifyNoMoreInteractions(listener);
        assertEquals(6, model.getRowCount());
        assertEquals("error value", model.validateAt(3, 0));
    }

    @Test
    public void testRemoveNonexistingBean() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        List<TestSummaryBean> beans = Arrays.asList(
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()),
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()));
        model.setBeans(beans);
        model.addTableModelListener(listener);

        model.removeBean(new TestSummaryBean());

        assertEquals(6, model.getRowCount());
        verifyZeroInteractions(listener);
    }

    @Test
    public void testRemoveAll() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        List<TestSummaryBean> beans = Arrays.asList(
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()),
            new TestSummaryBean("error value", new TestDetailBean(), new TestDetailBean(), new TestDetailBean()),
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()));
        model.setBeans(beans);
        assertEquals("error value", model.validateAt(3, 0));
        model.addTableModelListener(listener);

        model.removeAll(beans.subList(1, 3));

        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.DELETE, 3, 6, -1));
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.DELETE, 3, 5, -1));
        verifyNoMoreInteractions(listener);
        assertEquals(3, model.getRowCount());
        assertEquals(beans.get(0).name, model.getValueAt(0, 0));
        assertNull(model.validateAt(3, 0));
    }

    @Test
    public void testQueueAdd() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        model.addTableModelListener(listener);
        model.addTableModelListener(event -> assertThat(model.getPendingAdds().size()).isEqualTo(1));

        model.queueAdd(new TestSummaryBean(new TestDetailBean()));

        assertThat(model.isPendingAdd(0));
        assertThat(model.isPendingAdd(1));
        assertThat(model.isChanged()).isTrue();
        assertThat(model.isChangedAt(0, 0)).isTrue();
        assertThat(model.isChangedAt(1, 0)).isTrue();
        verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.INSERT, 0, 1, -1));
    }

    @Test
    public void testQueueDeleteHeader() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        List<TestSummaryBean> beans = Arrays.asList(
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()),
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()));
        model.setBeans(beans);
        model.addTableModelListener(listener);

        assertThat(model.queueDelete(3)).isTrue();

        assertThat(model.getChangedRows().iterator()).hasSize(1);
        assertThat(model.isPendingDelete(0)).isFalse();
        assertThat(model.isPendingDelete(1)).isFalse();
        assertThat(model.isPendingDelete(2)).isFalse();
        assertThat(model.isPendingDelete(3)).isTrue();
        assertThat(model.isPendingDelete(4)).isTrue();
        assertThat(model.isPendingDelete(5)).isTrue();
        verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 3, 5, -1));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void testQueueDeleteHeaderRemovesPendingDeleteDetail() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        List<TestSummaryBean> beans = Arrays.asList(
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()),
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()));
        model.setBeans(beans);
        model.queueDelete(4);
        model.addTableModelListener(listener);

        assertThat(model.queueDelete(3)).isTrue();

        assertThat(model.getChangedRows().iterator()).hasSize(1);
        assertThat(model.isPendingDelete(0)).isFalse();
        assertThat(model.isPendingDelete(1)).isFalse();
        assertThat(model.isPendingDelete(2)).isFalse();
        assertThat(model.isPendingDelete(3)).isTrue();
        assertThat(model.isPendingDelete(4)).isTrue();
        assertThat(model.isPendingDelete(5)).isTrue();
        verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 3, 5, -1));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void testQueueDeleteHeaderRetainsPendingAddDetail() throws Exception { // TODO handle on commit
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        List<TestSummaryBean> beans = Arrays.asList(
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()),
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()));
        model.setBeans(beans);
        model.queueAppendSubRow(0);
        model.addTableModelListener(listener);

        assertThat(model.queueDelete(0)).isTrue();

        assertThat(model.isPendingAdd(3)).isTrue();
        assertThat(beans.get(0).details).hasSize(3);
        assertThat(model.getChangedRows().iterator()).hasSize(2);
        assertThat(model.isPendingDelete(0)).isTrue();
        assertThat(model.isPendingDelete(1)).isTrue();
        assertThat(model.isPendingDelete(2)).isTrue();
        assertThat(model.isPendingDelete(3)).isTrue();
        assertThat(model.isPendingDelete(4)).isFalse();
        assertThat(model.isPendingDelete(5)).isFalse();
        assertThat(model.isPendingDelete(6)).isFalse();
        verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 0, 3, -1));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void testQueueDeleteHeaderRetainsPendingChangeDetail() throws Exception { // TODO handle on commit
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        List<TestSummaryBean> beans = Arrays.asList(
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()),
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()));
        model.setBeans(beans);
        final int newDetailId = 500;
        model.setValueAt(newDetailId, 1, 0);
        model.addTableModelListener(listener);

        assertThat(model.queueDelete(0)).isTrue();

        assertThat(model.getValueAt(1, 0)).isEqualTo(newDetailId);
        assertThat(model.getChangedRows().iterator()).hasSize(2);
        assertThat(model.isPendingDelete(0)).isTrue();
        assertThat(model.isPendingDelete(1)).isTrue();
        assertThat(model.isPendingDelete(2)).isTrue();
        assertThat(model.isPendingDelete(3)).isFalse();
        assertThat(model.isPendingDelete(4)).isFalse();
        assertThat(model.isPendingDelete(5)).isFalse();
        verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 0, 2, -1));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void testQueueDeleteDetail() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        List<TestSummaryBean> beans = Arrays.asList(
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()),
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()));
        model.setBeans(beans);
        model.addTableModelListener(listener);

        assertThat(model.queueDelete(4)).isTrue();

        assertThat(model.isPendingDelete(0)).isFalse();
        assertThat(model.isPendingDelete(1)).isFalse();
        assertThat(model.isPendingDelete(2)).isFalse();
        assertThat(model.isPendingDelete(3)).isFalse();
        assertThat(model.isPendingDelete(4)).isTrue();
        assertThat(model.isPendingDelete(5)).isFalse();
        verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 4, 4, -1));
    }

    @Test
    public void testQueueDeleteDetailCancelsPendingAddDetail() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        List<TestSummaryBean> beans = Arrays.asList(
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()),
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()));
        model.setBeans(beans);
        int lastDetailId = (Integer) model.getValueAt(2, 0);
        int newSubRow = model.queueAppendSubRow(0);
        model.addTableModelListener(listener);

        assertThat(model.queueDelete(newSubRow)).isFalse();

        assertThat(model.getRowCount()).isEqualTo(6);
        assertThat(model.isChanged()).isFalse();
        assertThat(model.isPendingDelete(0)).isFalse();
        assertThat(model.isPendingDelete(1)).isFalse();
        assertThat(model.isPendingDelete(2)).isFalse();
        assertThat(model.isPendingDelete(3)).isFalse();
        assertThat(model.isPendingDelete(4)).isFalse();
        assertThat(model.isPendingDelete(5)).isFalse();
        assertThat(model.getValueAt(2, 0)).isEqualTo(lastDetailId);
        verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.DELETE, newSubRow, newSubRow, -1));
    }

    @Test
    public void testQueueDeleteDetailRetainsPendingChangeDetail() throws Exception { // TODO handle on commit
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        List<TestSummaryBean> beans = Arrays.asList(
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()),
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()));
        model.setBeans(beans);
        final int newDetailId = 500;
        model.setValueAt(newDetailId, 1, 0);
        model.addTableModelListener(listener);

        assertThat(model.queueDelete(1)).isTrue();

        assertThat(model.getValueAt(1, 0)).isEqualTo(newDetailId);
        assertThat(model.getChangedRows().iterator()).hasSize(2);
        assertThat(model.isPendingDelete(0)).isFalse();
        assertThat(model.isPendingDelete(1)).isTrue();
        assertThat(model.isPendingDelete(2)).isFalse();
        assertThat(model.isPendingDelete(3)).isFalse();
        assertThat(model.isPendingDelete(4)).isFalse();
        assertThat(model.isPendingDelete(5)).isFalse();
        verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 1, 1, -1));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void getColumnIndex() throws Exception {
        assertThat(newModel().getColumnIndex(summaryColumnAdapter)).isEqualTo(0);
    }

    @Test
    public void getDetailColumnIndex() throws Exception {
        assertThat(newModel().getDetailColumnIndex(0, detailColumnAdapter)).isEqualTo(0);
    }

    private BufferedHeaderDetailTableModel<TestSummaryBean> newModel() {
        return new BufferedHeaderDetailTableModel<>(new TestDetailAdapter(),
            singletonList(summaryColumnAdapter),
            singletonList(singletonList(detailColumnAdapter)));
    }

    private class TestDetailBean {
        private Integer id = nextId++;
    }

    private class TestSummaryBean {
        private List<TestDetailBean> details;
        private String name;
        private boolean editable = false;

        private TestSummaryBean(TestDetailBean... details) {
            this("summary " + nextId++, details);
        }

        private TestSummaryBean(String name, TestDetailBean... details) {
            this.name = name;
            this.details = Lists.newArrayList(details);
        }
    }

    private class TestDetailColumnAdapter implements ColumnAdapter<TestDetailBean, Integer>, BeanPropertyValidator<TestDetailBean, Integer> {
        public String getColumnId() {
            return "detailId";
        }

        public String getResource(String resourceId, String defaultValue) {
            return null;
        }

        public String getName() {
            return "Detail Id";
        }

        public Class<Integer> getType() {
            return Integer.class;
        }

        public Integer getValue(TestDetailBean row) {
            return row.id;
        }

        public boolean isEditable(TestDetailBean row) {
            return true;
        }

        public void setValue(TestDetailBean row, Integer value) {
            row.id = value;
        }

        public String validate(int selectedIndex, Integer propertyValue, List<? extends TestDetailBean> beans) {
            return propertyValue < 0 ? "ID less than 0" : null;
        }
    }

    private class TestSummaryColumnAdapter implements ColumnAdapter<TestSummaryBean, String>, BeanPropertyValidator<TestSummaryBean, String> {
        public String getColumnId() {
            return "summaryName";
        }

        public String getResource(String resourceId, String defaultValue) {
            return null;
        }

        public String getName() {
            return "Summary Name";
        }

        public Class<String> getType() {
            return String.class;
        }

        public String getValue(TestSummaryBean row) {
            return row.name;
        }

        public boolean isEditable(TestSummaryBean row) {
            return row.editable;
        }

        public void setValue(TestSummaryBean row, String value) {
            row.name = value;
        }

        public String validate(int selectedIndex, String propertyValue, List<? extends TestSummaryBean> beans) {
            return "error value".equals(propertyValue) ? "error value" : null;
        }
    }

    private class TestDetailAdapter extends SingleTypeDetailAdapter<TestSummaryBean> {
        public List<?> getDetails(TestSummaryBean bean, int subRowTypeIndex) {
            return bean.details;
        }

        @Override
        public int appendDetail(TestSummaryBean bean) {
            bean.details.add(new TestDetailBean());
            return bean.details.size();
        }
    }
}