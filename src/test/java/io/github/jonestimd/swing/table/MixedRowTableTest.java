package io.github.jonestimd.swing.table;

import java.awt.Color;
import java.awt.Component;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.github.jonestimd.swing.ComponentDefaults;
import io.github.jonestimd.swing.table.model.BufferedHeaderDetailTableModel;
import io.github.jonestimd.swing.table.model.ColumnAdapter;
import io.github.jonestimd.swing.table.model.EmptyColumnAdapter;
import io.github.jonestimd.swing.table.model.SingleTypeDetailAdapter;
import io.github.jonestimd.swing.table.model.TestColumnAdapter;
import io.github.jonestimd.swing.table.sort.HeaderDetailTableRowSorter;
import org.junit.Test;

import static java.util.Collections.*;
import static org.fest.assertions.Assertions.*;

public class MixedRowTableTest {
    private Color evenBackground = ComponentDefaults.getColor("Table.alternateRowColor");
    private Color oddBackground = ComponentDefaults.getColor("Table.background");
    private final ColumnAdapter<TestSummaryBean, String> summaryColumnAdapter1 =
            new TestColumnAdapter<>("Summary Name", "summaryName", String.class, TestSummaryBean::getName, TestSummaryBean::setName);
    private final ColumnAdapter<TestSummaryBean, Integer> summaryColumnAdapter2 =
            new TestColumnAdapter<>("Summary Number", "summaryNumber", Integer.class, TestSummaryBean::getNumber, TestSummaryBean::setNumber);
    private final ColumnAdapter<TestDetailBean, Integer> detailColumnAdapter =
            new TestColumnAdapter<>("Detail Id", "detailId", Integer.class, TestDetailBean::getId, TestDetailBean::setId);
    private int nextId = 1;

    @Test
    public void testSetModel() throws Exception {
        MixedRowTable<TestSummaryBean, BufferedHeaderDetailTableModel<TestSummaryBean>> table = new MixedRowTable<>(newModel());
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();

        table.setModel(model);

        assertThat(table.getColumnModel().getColumnCount()).isEqualTo(2);
        assertThat(table.getColumnModel().getColumn(0).getHeaderValue()).isEqualTo("Summary Name");
        assertThat(((MixedRowTableColumn) table.getColumnModel().getColumn(0)).getSubColumn(0).getHeaderValue()).isEqualTo("Detail Id");
        MixedRowTableColumn column = (MixedRowTableColumn) table.getColumnModel().getColumn(0);
        assertThat(column.getHeaderValue()).isEqualTo("Summary Name");
        assertThat(column.getSubColumn(0).getHeaderValue()).isEqualTo("Detail Id");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetModelThrowsException() throws Exception {
        MixedRowTable<TestSummaryBean, BufferedHeaderDetailTableModel<TestSummaryBean>> table = new MixedRowTable<>(newModel());

        table.setModel(new DefaultTableModel());
    }

    @Test
    public void testConstructorWithModel() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();

        MixedRowTable<TestSummaryBean, BufferedHeaderDetailTableModel<TestSummaryBean>> table = new MixedRowTable<>(model);

        assertThat(table.getColumnModel().getColumnCount()).isEqualTo(2);
        assertThat(table.getColumnModel().getColumn(0).getHeaderValue()).isEqualTo("<html><center>Summary Name</center></html>");
        assertThat(((MixedRowTableColumn) table.getColumnModel().getColumn(0)).getSubColumn(0).getHeaderValue()).isEqualTo("Detail Id");
        MixedRowTableColumn column = (MixedRowTableColumn) table.getColumnModel().getColumn(0);
        assertThat(column.getHeaderValue()).isEqualTo("<html><center>Summary Name</center></html>");
        assertThat(column.getSubColumn(0).getHeaderValue()).isEqualTo("Detail Id");
    }

    @Test
    public void testAddColumnCreatesMixedRowTableColumn() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        MixedRowTable<TestSummaryBean, BufferedHeaderDetailTableModel<TestSummaryBean>> table = new MixedRowTable<>(model);

        TableColumn column = new TableColumn(0);
        column.setIdentifier("new column");
        table.addColumn(column);

        assertThat(table.getColumnModel().getColumnCount()).isEqualTo(3);
        Enumeration<TableColumn> columns = table.getColumnModel().getColumns();
        while (columns.hasMoreElements()) {
            assertThat(columns.nextElement().getClass()).isEqualTo(MixedRowTableColumn.class);
        }
    }

    @Test
    public void getCellEditorUsesDefaultEditor() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        model.setBeans(singletonList(new TestSummaryBean(new TestDetailBean(), new TestDetailBean())));
        MixedRowTable<TestSummaryBean, BufferedHeaderDetailTableModel<TestSummaryBean>> table = new MixedRowTable<>(model);

        assertThat(table.getCellEditor(0, 0).getClass().getName()).isEqualTo("io.github.jonestimd.swing.table.MixedRowTable$GenericCellEditor");
        assertThat(table.getCellEditor(1, 0).getClass().getName()).isEqualTo("io.github.jonestimd.swing.table.MixedRowTable$NumberCellEditor");
        assertThat(table.getCellEditor(2, 0).getClass().getName()).isEqualTo("io.github.jonestimd.swing.table.MixedRowTable$NumberCellEditor");
    }

    @Test
    public void getCellEditorUsesColumnEditor() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        model.setBeans(singletonList(new TestSummaryBean(new TestDetailBean(), new TestDetailBean())));
        MixedRowTable<TestSummaryBean, BufferedHeaderDetailTableModel<TestSummaryBean>> table = new MixedRowTable<>(model);
        MixedRowTableColumn column = (MixedRowTableColumn) table.getColumnModel().getColumn(0);
        final DefaultCellEditor cellEditor = new DefaultCellEditor(new JTextField());
        column.getSubColumn(0).setCellEditor(cellEditor);

        assertThat(table.getCellEditor(0, 0).getClass().getName()).isEqualTo("io.github.jonestimd.swing.table.MixedRowTable$GenericCellEditor");
        assertThat(table.getCellEditor(1, 0)).isSameAs(cellEditor);
        assertThat(table.getCellEditor(2, 0)).isSameAs(cellEditor);
    }

    @Test
    public void getCellRendererUsesDefaultRenderer() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        model.setBeans(singletonList(new TestSummaryBean(new TestDetailBean(), new TestDetailBean())));
        MixedRowTable<TestSummaryBean, BufferedHeaderDetailTableModel<TestSummaryBean>> table = new MixedRowTable<>(model);

        assertThat(table.getCellRenderer(0, 0)).isSameAs(table.getDefaultRenderer(String.class));
        assertThat(table.getCellRenderer(1, 0)).isSameAs(table.getDefaultRenderer(Integer.class));
        assertThat(table.getCellRenderer(2, 0)).isSameAs(table.getDefaultRenderer(Integer.class));
    }

    @Test
    public void getCellRendererUsesColumnRenderer() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        model.setBeans(singletonList(new TestSummaryBean(new TestDetailBean(), new TestDetailBean())));
        MixedRowTable<TestSummaryBean, BufferedHeaderDetailTableModel<TestSummaryBean>> table = new MixedRowTable<>(model);
        DefaultTableCellRenderer renderer1 = new DefaultTableCellRenderer();
        DefaultTableCellRenderer renderer2 = new DefaultTableCellRenderer();
        table.getColumnModel().getColumn(0).setCellRenderer(renderer1);
        ((MixedRowTableColumn) table.getColumnModel().getColumn(0)).getSubColumn(0).setCellRenderer(renderer2);

        assertThat(table.getCellRenderer(0, 0)).isSameAs(renderer1);
        assertThat(table.getCellRenderer(1, 0)).isSameAs(renderer2);
        assertThat(table.getCellRenderer(2, 0)).isSameAs(renderer2);
    }

    @Test
    public void getRowBackgroundWithoutSorter() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        model.setBeans(Lists.newArrayList(
                new TestSummaryBean(new TestDetailBean()),
                new TestSummaryBean(new TestDetailBean(), new TestDetailBean())));
        MixedRowTable<TestSummaryBean, BufferedHeaderDetailTableModel<TestSummaryBean>> table = new MixedRowTable<>(model);

        assertThat(table.getRowBackground(0)).isEqualTo(evenBackground);
        assertThat(table.getRowBackground(1)).isEqualTo(evenBackground);
        assertThat(table.getRowBackground(2)).isEqualTo(oddBackground);
        assertThat(table.getRowBackground(3)).isEqualTo(oddBackground);
        assertThat(table.getRowBackground(4)).isEqualTo(oddBackground);
    }

    @Test
    public void getRowBackgroundWithSorter() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        model.setBeans(Lists.newArrayList(
                new TestSummaryBean(new TestDetailBean()),
                new TestSummaryBean(new TestDetailBean(), new TestDetailBean())));
        MixedRowTable<TestSummaryBean, BufferedHeaderDetailTableModel<TestSummaryBean>> table = new MixedRowTable<>(model);
        HeaderDetailTableRowSorter<?, ?> sorter = new HeaderDetailTableRowSorter<>(table);
        sorter.setSortKeys(Lists.newArrayList(new SortKey(0, SortOrder.DESCENDING)));
        table.setRowSorter(sorter);

        assertThat(table.getRowBackground(0)).isEqualTo(evenBackground);
        assertThat(table.getRowBackground(1)).isEqualTo(evenBackground);
        assertThat(table.getRowBackground(2)).isEqualTo(evenBackground);
        assertThat(table.getRowBackground(3)).isEqualTo(oddBackground);
        assertThat(table.getRowBackground(4)).isEqualTo(oddBackground);
    }

    @Test
    public void selectRowAt() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        model.setBeans(Lists.newArrayList(new TestSummaryBean(new TestDetailBean())));
        MixedRowTable<TestSummaryBean, BufferedHeaderDetailTableModel<TestSummaryBean>> table = new MixedRowTable<>(model);
        table.setRowSelectionInterval(0, 0);
        table.setColumnSelectionInterval(1, 1);

        table.selectRowAt(1);

        assertThat(table.getSelectedRow()).isEqualTo(1);
        assertThat(table.getSelectedColumn()).isEqualTo(0);
        assertThat(table.getLeadSelectionModelIndex()).isEqualTo(1);
    }

    @Test
    public void editSummaryCell() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        model.setBeans(Lists.newArrayList(new TestSummaryBean(new TestDetailBean())));
        MixedRowTable<TestSummaryBean, BufferedHeaderDetailTableModel<TestSummaryBean>> table = new MixedRowTable<>(model);
        final TableCellEditor editor = table.getDefaultEditor(String.class);

        Component component = editor.getTableCellEditorComponent(table, "", false, 0, 0);
        ((JTextField) component).setText("new value");
        assertThat(editor.stopCellEditing()).isTrue();

        assertThat(editor.getCellEditorValue()).isEqualTo("new value");
    }

    @Test
    public void editSummaryCellEmptyString() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        model.setBeans(Lists.newArrayList(new TestSummaryBean(new TestDetailBean())));
        MixedRowTable<TestSummaryBean, BufferedHeaderDetailTableModel<TestSummaryBean>> table = new MixedRowTable<>(model);
        final TableCellEditor editor = table.getDefaultEditor(String.class);

        Component component = editor.getTableCellEditorComponent(table, "old value", false, 0, 0);
        ((JTextField) component).setText("");
        assertThat(editor.stopCellEditing()).isTrue();

        assertThat(editor.getCellEditorValue()).isEqualTo("");
    }

    @Test
    public void editSubRowCell() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        model.setBeans(Lists.newArrayList(new TestSummaryBean(new TestDetailBean())));
        MixedRowTable<TestSummaryBean, BufferedHeaderDetailTableModel<TestSummaryBean>> table = new MixedRowTable<>(model);
        final TableCellEditor editor = table.getDefaultEditor(String.class);

        Component component = editor.getTableCellEditorComponent(table, "", false, 1, 0);
        ((JTextField) component).setText("99");
        assertThat(editor.stopCellEditing()).isTrue();

        assertThat(editor.getCellEditorValue()).isEqualTo(99);
    }

    @Test
    public void editSubRowCellInvalidValue() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        model.setBeans(Lists.newArrayList(new TestSummaryBean(new TestDetailBean())));
        MixedRowTable<TestSummaryBean, BufferedHeaderDetailTableModel<TestSummaryBean>> table = new MixedRowTable<>(model);
        final TableCellEditor editor = table.getDefaultEditor(String.class);

        Component component = editor.getTableCellEditorComponent(table, "", false, 1, 0);
        ((JTextField) component).setText("");
        assertThat(editor.stopCellEditing()).isFalse();

        assertThat(editor.getCellEditorValue()).isNull();
    }

    @SuppressWarnings("unchecked")
    private BufferedHeaderDetailTableModel<TestSummaryBean> newModel() {
        return new BufferedHeaderDetailTableModel<>(new TestDetailAdapter(),
                ImmutableList.of(summaryColumnAdapter1, summaryColumnAdapter2),
                singletonList(ImmutableList.of(detailColumnAdapter, new EmptyColumnAdapter<>("dummy1"))));
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
        private String name = "summary " + nextId++;
        private Integer number;

        private TestSummaryBean(TestDetailBean ... details) {
            this.details = Arrays.asList(details);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getNumber() {
            return number;
        }

        public void setNumber(Integer number) {
            this.number = number;
        }
    }

    private class TestDetailAdapter extends SingleTypeDetailAdapter<TestSummaryBean> {
        public List<?> getDetails(TestSummaryBean bean, int subRowTypeIndex) {
            return bean.details;
        }

        @Override
        public int appendDetail(TestSummaryBean bean) {
            throw new UnsupportedOperationException();
        }
    }
}