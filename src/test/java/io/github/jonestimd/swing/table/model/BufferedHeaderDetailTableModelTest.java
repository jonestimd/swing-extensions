package io.github.jonestimd.swing.table.model;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import com.google.common.collect.Lists;
import io.github.jonestimd.swing.validation.BeanPropertyValidator;
import org.junit.Test;
import org.mockito.InOrder;

import static java.util.Collections.*;
import static org.fest.assertions.Assertions.*;
import static org.mockito.Mockito.*;

public class BufferedHeaderDetailTableModelTest {
    private final TestDetailColumnAdapter detailColumnAdapter = new TestDetailColumnAdapter();
    private int nextId = 1;
    private TableModelListener listener = mock(TableModelListener.class);
    private TestSummaryColumnAdapter summaryColumnAdapter = new TestSummaryColumnAdapter();

    private final DetailAdapter<TestSummaryBean> detailAdapter = new SingleTypeDetailAdapter<TestSummaryBean>() {
        public List<?> getDetails(TestSummaryBean bean, int subRowTypeIndex) {
            return bean.details;
        }

        @Override
        public int appendDetail(TestSummaryBean bean) {
            bean.details.add(new TestDetailBean());
            return bean.details.size();
        }
    };

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
        assertThat(model.validateAt(0, 0)).isEqualTo("error value");
        assertThat(model.validateAt(4, 0)).isEqualTo("ID less than 0");
        assertThat(model.isNoErrors()).isFalse();

        model.setBeans(Arrays.asList(
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()),
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean())));

        assertThat(model.validateAt(0, 0)).isNull();
        assertThat(model.validateAt(4, 0)).isNull();
        assertThat(model.isNoErrors()).isTrue();
    }

    @Test
    public void validateAtIgnoresUnvalidatedColumns() throws Exception {
        ColumnAdapter<TestSummaryBean, String> summaryAdapter = new TestColumnAdapter<>("Name", String.class, TestSummaryBean::getName);
        ColumnAdapter<TestDetailBean, Integer> detailAdapter = new TestColumnAdapter<>("Id", Integer.class, TestDetailBean::getId);
        BufferedHeaderDetailTableModel<TestSummaryBean> model = new BufferedHeaderDetailTableModel<>(this.detailAdapter,
                singletonList(summaryAdapter), singletonList(singletonList(detailAdapter)));
        model.addBean(new TestSummaryBean(new TestDetailBean()));

        assertThat(model.validateAt(0, 0, "don't care")).isNull();
        assertThat(model.validateAt(1, 0, -99)).isNull();
    }

    @Test
    public void testGetRowCount() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        List<TestSummaryBean> beans = Arrays.asList(
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()),
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()));
        model.setBeans(beans);

        assertThat(model.getRowCount()).isEqualTo(6);
    }

    @Test
    public void testGetGroupNumber() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        List<TestSummaryBean> beans = Arrays.asList(
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()),
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()));
        model.setBeans(beans);

        assertThat(model.getGroupNumber(0)).isEqualTo(0);
        assertThat(model.getGroupNumber(1)).isEqualTo(0);
        assertThat(model.getGroupNumber(2)).isEqualTo(0);
        assertThat(model.getGroupNumber(3)).isEqualTo(1);
        assertThat(model.getGroupNumber(4)).isEqualTo(1);
        assertThat(model.getGroupNumber(5)).isEqualTo(1);
    }

    @Test
    public void testGetCellClass() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        List<TestSummaryBean> beans = Arrays.asList(
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()),
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()));
        model.setBeans(beans);

        assertThat(model.getCellClass(0, 0)).isEqualTo(String.class);
        assertThat(model.getCellClass(1, 0)).isEqualTo(Integer.class);
        assertThat(model.getCellClass(2, 0)).isEqualTo(Integer.class);
        assertThat(model.getCellClass(3, 0)).isEqualTo(String.class);
        assertThat(model.getCellClass(4, 0)).isEqualTo(Integer.class);
        assertThat(model.getCellClass(5, 0)).isEqualTo(Integer.class);
    }

    @Test
    public void testGetValueAt() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        List<TestSummaryBean> beans = Arrays.asList(
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()),
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()));
        model.setBeans(beans);

        assertThat(model.getValueAt(0, 0)).isEqualTo(beans.get(0).name);
        assertThat(model.getValueAt(1, 0)).isEqualTo(beans.get(0).details.get(0).id);
        assertThat(model.getValueAt(2, 0)).isEqualTo(beans.get(0).details.get(1).id);
        assertThat(model.getValueAt(3, 0)).isEqualTo(beans.get(1).name);
        assertThat(model.getValueAt(4, 0)).isEqualTo(beans.get(1).details.get(0).id);
        assertThat(model.getValueAt(5, 0)).isEqualTo(beans.get(1).details.get(1).id);
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
        assertThat(model.isChanged(beans.get(0))).isFalse();
        assertThat(model.isChangedAt(0, 0)).isFalse();
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
        assertThat(beans.get(0).name).isEqualTo("new name");
        assertThat(beans.get(0).details.get(0).id.intValue()).isEqualTo(98);
        assertThat(beans.get(0).details.get(1).id.intValue()).isEqualTo(-1);
        assertThat(model.validateAt(2, 0)).isEqualTo("ID less than 0");
        assertThat(beans.get(1).details.get(1).id.intValue()).isEqualTo(99);
        assertThat(model.isChanged(beans.get(0))).isTrue();
        assertThat(model.isChangedAt(0, 0)).isTrue();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setValueAtThrowsUnsupportedOperationException() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        model.setBeans(Lists.newArrayList(new TestSummaryBean(new TestDetailBean())));

        model.setValueAt("not an integer", 1, 0);
    }

    @Test
    public void setValueAtIgnoresSameValue() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        TestSummaryBean bean = new TestSummaryBean(new TestDetailBean());
        model.setBeans(Lists.newArrayList(bean));

        model.setValueAt(bean.name, 0, 0);

        assertThat(model.isChangedAt(0, 0)).isFalse();
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

        assertThat(model.getColumnClass(0, 0)).isEqualTo(String.class);
        assertThat(model.getColumnClass(1, 0)).isEqualTo(Integer.class);
    }

    @Test
    public void testGetSubRowColumnName() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();

        assertThat(model.getColumnName(0, 0)).isEqualTo("Summary Name");
        assertThat(model.getColumnName(1, 0)).isEqualTo("Detail Id");
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
        assertThat(model.getRowCount()).isEqualTo(9);
        assertThat(model.validateAt(6, 0)).isEqualTo("error value");
    }

    @Test
    public void testInsertBean() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        List<TestSummaryBean> beans = Arrays.asList(
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()),
            new TestSummaryBean("error value", new TestDetailBean(), new TestDetailBean()));
        model.setBeans(beans);
        assertThat(model.validateAt(3, 0)).isEqualTo("error value");
        model.addTableModelListener(listener);

        TestSummaryBean bean = new TestSummaryBean(new TestDetailBean(), new TestDetailBean(), new TestDetailBean());
        model.addBean(1, bean);

        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.INSERT, 3, 6, -1));
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 3, 6, -1));
        verifyNoMoreInteractions(listener);
        assertThat(model.getRowCount()).isEqualTo(10);
        assertThat(model.getValueAt(0, 0)).isEqualTo(beans.get(0).name);
        assertThat(model.getValueAt(3, 0)).isEqualTo(bean.name);
        assertThat(model.getValueAt(7, 0)).isEqualTo(beans.get(1).name);
        assertThat(model.validateAt(6, 0)).isNull();
        assertThat(model.validateAt(7, 0)).isEqualTo("error value");
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
        assertThat(model.getRowCount()).isEqualTo(6);
        assertThat(model.getValueAt(3, 0)).isEqualTo(bean.name);
    }

    @Test
    public void testSetBeanWithMoreDetails() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        List<TestSummaryBean> beans = Arrays.asList(
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()),
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()),
            new TestSummaryBean("error value", new TestDetailBean(), new TestDetailBean()));
        model.setBeans(beans);
        assertThat(model.validateAt(6, 0)).isEqualTo("error value");
        model.addTableModelListener(listener);

        TestSummaryBean bean = new TestSummaryBean(new TestDetailBean(), new TestDetailBean(), new TestDetailBean());
        model.setBean(1, bean);

        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.INSERT, 6, 6, -1));
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 3, 6, -1));
        verifyNoMoreInteractions(listener);
        assertThat(model.getRowCount()).isEqualTo(10);
        assertThat(model.getValueAt(3, 0)).isEqualTo(bean.name);
        assertThat(model.validateAt(6, 0)).isNull();
        assertThat(model.validateAt(7, 0)).isEqualTo("error value");
    }

    @Test
    public void testSetBeanWithFewerDetails() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        List<TestSummaryBean> beans = Arrays.asList(
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()),
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean(), new TestDetailBean()),
            new TestSummaryBean("error value", new TestDetailBean(), new TestDetailBean()));
        model.setBeans(beans);
        assertThat(model.validateAt(7, 0)).isEqualTo("error value");
        model.addTableModelListener(listener);

        TestSummaryBean bean = new TestSummaryBean(new TestDetailBean());
        model.setBean(1, bean);

        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.DELETE, 5, 6, -1));
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.UPDATE, 3, 4, -1));
        verifyNoMoreInteractions(listener);
        assertThat(model.getRowCount()).isEqualTo(8);
        assertThat(model.getValueAt(3, 0)).isEqualTo(bean.name);
        assertThat(model.validateAt(7, 0)).isNull();
        assertThat(model.validateAt(5, 0)).isEqualTo("error value");
    }

    @Test
    public void testRemoveBean() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        List<TestSummaryBean> beans = Arrays.asList(
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean()),
            new TestSummaryBean(new TestDetailBean(), new TestDetailBean(), new TestDetailBean()),
            new TestSummaryBean("error value", new TestDetailBean(), new TestDetailBean()));
        model.setBeans(beans);
        assertThat(model.validateAt(7, 0)).isEqualTo("error value");
        model.addTableModelListener(listener);

        model.removeBean(beans.get(1));

        verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.DELETE, 3, 6, -1));
        verifyNoMoreInteractions(listener);
        assertThat(model.getRowCount()).isEqualTo(6);
        assertThat(model.validateAt(3, 0)).isEqualTo("error value");
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

        assertThat(model.getRowCount()).isEqualTo(6);
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
        assertThat(model.validateAt(3, 0)).isEqualTo("error value");
        model.addTableModelListener(listener);

        model.removeAll(beans.subList(1, 3));

        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.DELETE, 3, 6, -1));
        inOrder.verify(listener).tableChanged(TableModelEventMatcher.tableModelEvent(TableModelEvent.DELETE, 3, 5, -1));
        verifyNoMoreInteractions(listener);
        assertThat(model.getRowCount()).isEqualTo(3);
        assertThat(model.getValueAt(0, 0)).isEqualTo(beans.get(0).name);
        assertThat(model.validateAt(3, 0)).isNull();
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
        assertThat(model.isPendingDelete(model.getBean(0))).isTrue();
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
        assertThat(model.isChanged(beans.get(0))).isFalse();
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
        model.getBeans().forEach(bean -> assertThat(model.isPendingDelete(bean)).isFalse());
        assertThat(model.isChanged(beans.get(0))).isTrue();
        assertThat(model.isChangedAt(1, 0)).isTrue();
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

    @Test
    public void undoChangedAtRevertsUpdate() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        model.setBeans(Lists.newArrayList(new TestSummaryBean(new TestDetailBean())));
        Object originalValue = model.getValueAt(1, 0);
        model.setValueAt(-99, 1, 0);

        model.undoChangedAt(1, 0);

        assertThat(model.getValueAt(1, 0)).isEqualTo(originalValue);
    }

    @Test
    public void undoDeleteSubrow() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        model.setBeans(Lists.newArrayList(new TestSummaryBean(new TestDetailBean())));
        model.queueDelete(1);

        model.undoDelete(1);

        assertThat(model.isPendingDelete(1)).isFalse();
    }

    @Test
    public void getPendingUpdatesReturnsSummaries() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        List<TestSummaryBean> beans = Lists.newArrayList(new TestSummaryBean(new TestDetailBean()), new TestSummaryBean(new TestDetailBean()));
        model.setBeans(beans);
        model.setValueAt(99, 1, 0);
        model.setValueAt("y", 2, 0);

        List<TestSummaryBean> updates = model.getPendingUpdates().collect(Collectors.toList());

        assertThat(updates).containsOnly(beans.toArray());
    }

    @Test
    public void revertChange() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        model.setBeans(Lists.newArrayList(new TestSummaryBean(new TestDetailBean())));
        Object originalValue = model.getValueAt(0, 0);
        model.setValueAt("summary X", 0, 0);

        model.revert();

        assertThat(model.getValueAt(0, 0)).isEqualTo(originalValue);
    }

    @Test
    public void queueDeleteRevertsAdd() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        model.setBeans(Lists.newArrayList(new TestSummaryBean(new TestDetailBean(), new TestDetailBean())));
        model.queueAdd(new TestSummaryBean(new TestDetailBean()));

        assertThat(model.queueDelete(3)).isFalse();

        assertThat(model.getRowCount()).isEqualTo(3);
        assertThat(model.getPendingAdds()).isEmpty();
        assertThat(model.getPendingDeletes()).isEmpty();
    }

    @Test
    public void revertDelete() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        model.setBeans(Lists.newArrayList(new TestSummaryBean(new TestDetailBean(), new TestDetailBean())));
        model.queueDelete(1);

        model.revert();

        assertThat(model.getPendingDeletes()).isEmpty();
        assertThat(model.getRowCount()).isEqualTo(3);
    }

    @Test
    public void revertAddSummary() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        model.setBeans(Lists.newArrayList(new TestSummaryBean(new TestDetailBean(), new TestDetailBean())));
        model.queueAdd(new TestSummaryBean(new TestDetailBean()));

        model.revert();

        assertThat(model.getPendingAdds()).isEmpty();
        assertThat(model.getRowCount()).isEqualTo(3);
    }

    @Test
    public void revertAddDetail() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        model.setBeans(Lists.newArrayList(new TestSummaryBean(new TestDetailBean(), new TestDetailBean())));
        model.queueAppendSubRow(1);

        model.revert();

        assertThat(model.getPendingAdds()).isEmpty();
        assertThat(model.getRowCount()).isEqualTo(3);
    }

    @Test
    public void commit() throws Exception {
        final TestSummaryBean added = new TestSummaryBean(new TestDetailBean());
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        model.setBeans(Lists.newArrayList(new TestSummaryBean(new TestDetailBean(), new TestDetailBean())));
        model.queueDelete(1);
        model.queueAdd(added);
        model.setValueAt("new header", 0, 0);

        model.commit();

        assertThat(model.isChanged()).isFalse();
        model.getBeans().forEach(bean -> assertThat(model.isChanged(bean)).isFalse());
        assertThat(model.getBean(1)).isSameAs(added);
        assertThat(model.isChangedAt(0, 0)).isFalse();
        assertThat(model.getValueAt(0, 0)).isEqualTo("new header");
        assertThat(model.getBean(0).details.size()).isEqualTo(1);
    }

    private BufferedHeaderDetailTableModel<TestSummaryBean> newModel() {
        return new BufferedHeaderDetailTableModel<>(detailAdapter,
            singletonList(summaryColumnAdapter),
            singletonList(singletonList(detailColumnAdapter)));
    }

    private class TestDetailBean {
        private Integer id = nextId++;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }
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

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    private class TestDetailColumnAdapter extends TestColumnAdapter<TestDetailBean, Integer> implements BeanPropertyValidator<TestDetailBean, Integer> {
        public TestDetailColumnAdapter() {
            super("Detail Id", "detailId", Integer.class, TestDetailBean::getId, TestDetailBean::setId);
        }

        public String validate(int selectedIndex, Integer propertyValue, List<? extends TestDetailBean> beans) {
            return propertyValue < 0 ? "ID less than 0" : null;
        }
    }

    private class TestSummaryColumnAdapter extends TestColumnAdapter<TestSummaryBean, String> implements BeanPropertyValidator<TestSummaryBean, String> {
        public TestSummaryColumnAdapter() {
            super("Summary Name", "summaryName", String.class, TestSummaryBean::getName, TestSummaryBean::setName);
        }

        public boolean isEditable(TestSummaryBean row) {
            return row.editable;
        }

        public String validate(int selectedIndex, String propertyValue, List<? extends TestSummaryBean> beans) {
            return "error value".equals(propertyValue) ? "error value" : null;
        }
    }
}