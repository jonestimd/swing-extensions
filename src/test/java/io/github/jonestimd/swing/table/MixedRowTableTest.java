package io.github.jonestimd.swing.table;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import io.github.jonestimd.swing.table.model.BufferedHeaderDetailTableModel;
import io.github.jonestimd.swing.table.model.ColumnAdapter;
import io.github.jonestimd.swing.table.model.SingleTypeDetailAdapter;
import org.junit.Test;

import static org.junit.Assert.*;

public class MixedRowTableTest {
    private int nextId = 1;

    @Test
    public void testSetModel() throws Exception {
        MixedRowTable<TestSummaryBean, BufferedHeaderDetailTableModel<TestSummaryBean>> table = new MixedRowTable<>(newModel());
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();

        table.setModel(model);

        assertEquals(1, table.getColumnModel().getColumnCount());
        assertEquals("Summary Name", table.getColumnModel().getColumn(0).getHeaderValue());
        assertEquals("Detail Id", ((MixedRowTableColumn) table.getColumnModel().getColumn(0)).getSubColumn(0).getHeaderValue());
        MixedRowTableColumn column = (MixedRowTableColumn) table.getColumnModel().getColumn(0);
        assertEquals("Summary Name", column.getHeaderValue());
        assertEquals("Detail Id", column.getSubColumn(0).getHeaderValue());
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

        assertEquals(1, table.getColumnModel().getColumnCount());
        assertEquals("<html><center>Summary Name</center></html>", table.getColumnModel().getColumn(0).getHeaderValue());
        assertEquals("Detail Id", ((MixedRowTableColumn) table.getColumnModel().getColumn(0)).getSubColumn(0).getHeaderValue());
        MixedRowTableColumn column = (MixedRowTableColumn) table.getColumnModel().getColumn(0);
        assertEquals("<html><center>Summary Name</center></html>", column.getHeaderValue());
        assertEquals("Detail Id", column.getSubColumn(0).getHeaderValue());
    }

    @Test
    public void testAddColumnCreatesMixedRowTableColumn() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        MixedRowTable<TestSummaryBean, BufferedHeaderDetailTableModel<TestSummaryBean>> table = new MixedRowTable<>(model);

        TableColumn column = new TableColumn(0);
        column.setIdentifier("new column");
        table.addColumn(column);

        assertEquals(2, table.getColumnModel().getColumnCount());
        Enumeration<TableColumn> columns = table.getColumnModel().getColumns();
        while (columns.hasMoreElements()) {
            assertEquals(MixedRowTableColumn.class, columns.nextElement().getClass());
        }
    }

    @Test
    public void testGetCellEditor() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        model.setBeans(Arrays.asList(new TestSummaryBean(new TestDetailBean(), new TestDetailBean())));
        MixedRowTable<TestSummaryBean, BufferedHeaderDetailTableModel<TestSummaryBean>> table = new MixedRowTable<>(model);

        assertEquals("io.github.jonestimd.swing.table.MixedRowTable$GenericCellEditor", table.getCellEditor(0, 0).getClass().getName());
        assertEquals("io.github.jonestimd.swing.table.MixedRowTable$NumberCellEditor", table.getCellEditor(1, 0).getClass().getName());
        assertEquals("io.github.jonestimd.swing.table.MixedRowTable$NumberCellEditor", table.getCellEditor(2, 0).getClass().getName());
    }

    @Test
    public void testGetCellRenderer() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel();
        model.setBeans(Arrays.asList(new TestSummaryBean(new TestDetailBean(), new TestDetailBean())));
        MixedRowTable<TestSummaryBean, BufferedHeaderDetailTableModel<TestSummaryBean>> table = new MixedRowTable<>(model);
        DefaultTableCellRenderer renderer1 = new DefaultTableCellRenderer();
        DefaultTableCellRenderer renderer2 = new DefaultTableCellRenderer();
        table.getColumnModel().getColumn(0).setCellRenderer(renderer1);
        ((MixedRowTableColumn) table.getColumnModel().getColumn(0)).getSubColumn(0).setCellRenderer(renderer2);

        assertSame(renderer1, table.getCellRenderer(0, 0));
        assertSame(renderer2, table.getCellRenderer(1, 0));
        assertSame(renderer2, table.getCellRenderer(2, 0));
    }

    @SuppressWarnings("unchecked")
    private BufferedHeaderDetailTableModel<TestSummaryBean> newModel() {
        return new BufferedHeaderDetailTableModel<>(new TestDetailAdapter(),
            Arrays.asList(new TestSummaryColumnAdapter()),
            Arrays.asList(Arrays.asList(new TestDetailColumnAdapter())));
    }

    private class TestDetailBean {
        private Integer id = nextId++;
    }

    private class TestSummaryBean {
        private List<TestDetailBean> details;
        private String name = "summary " + nextId++;
        private boolean editable = false;

        private TestSummaryBean(TestDetailBean ... details) {
            this.details = Arrays.asList(details);
        }
    }

    private class TestDetailColumnAdapter implements ColumnAdapter<TestDetailBean, Integer> {
        public String getColumnId() {
            return "detailId";
        }

        public String getResource(String columnId, String defaultValue) {
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
    }

    private class TestSummaryColumnAdapter implements ColumnAdapter<TestSummaryBean, String> {
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