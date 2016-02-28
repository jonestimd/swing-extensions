package io.github.jonestimd.swing.table;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import sun.swing.table.DefaultTableCellHeaderRenderer;

import static org.fest.assertions.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MixedRowTableColumnTest {
    private static final String HEADER = "header";
    private static final String SUB_HEADER = "sub-column 1";
    @Mock
    private MixedRowTable<?, ?> table;
    @Mock
    private JTableHeader tableHeader;
    @Mock
    private TableCellRenderer headerRenderer;
    @Mock
    private Component rendererComponent;

    @Before
    public void setupRenderer() throws Exception {
        when(headerRenderer.getTableCellRendererComponent(table, HEADER, false, false, 0, 0)).thenReturn(rendererComponent);
    }

    @Test
    public void testConstructorCopiesValues() throws Exception {
        TableColumn column = new TableColumn(1);
        column.setIdentifier("identifier");
        column.setHeaderValue("header value");
        column.setCellEditor(new DefaultCellEditor(new JTextField()));
        column.setCellRenderer(new DefaultTableCellRenderer());

        MixedRowTableColumn mixedColumn = new MixedRowTableColumn(column);

        assertThat(mixedColumn.getModelIndex()).isEqualTo(column.getModelIndex());
        assertThat(mixedColumn.getIdentifier()).isEqualTo(column.getIdentifier());
        assertThat(mixedColumn.getSubColumnCount()).isEqualTo(0);
        assertThat(mixedColumn.getHeaderValue()).isEqualTo(column.getHeaderValue());
        assertThat(mixedColumn.getCellEditor()).isSameAs(column.getCellEditor());
        assertThat(mixedColumn.getCellRenderer()).isSameAs(column.getCellRenderer());
        assertThat(mixedColumn.getHeaderRenderer().getClass().getName()).isEqualTo("io.github.jonestimd.swing.table.MixedRowTableColumn$MixedRowHeaderRenderer");
    }

    @Test
    public void addSubColumn() throws Exception {
        MixedRowTableColumn mixedColumn = new MixedRowTableColumn(new TableColumn(1));
        TableColumn subColumn1 = new TableColumn(-1);
        TableColumn subColumn2 = new TableColumn(-1);

        mixedColumn.addSubColumn(subColumn1);
        mixedColumn.addSubColumn(subColumn2);

        assertThat(mixedColumn.getSubColumn(0)).isSameAs(subColumn1);
        assertThat(mixedColumn.getSubColumn(1)).isSameAs(subColumn2);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void usesColumnHeaderRenderer() throws Exception {
        MixedRowTableColumn mixedColumn = new MixedRowTableColumn(headerColumn(headerRenderer));
        mixedColumn.addSubColumn(subColumn(SUB_HEADER));

        Component component = mixedColumn.getHeaderRenderer().getTableCellRendererComponent(table, HEADER, false, false, -1, 0);

        assertThat(component).isInstanceOf(JList.class);
        JList<Object> jList = (JList<Object>) component;
        assertThat(jList.getModel().getSize()).isEqualTo(2);
        assertThat(jList.getModel().getElementAt(0)).isEqualTo(HEADER);
        assertThat(jList.getModel().getElementAt(1)).isEqualTo(SUB_HEADER);
        assertThat(jList.getCellRenderer().getListCellRendererComponent(jList, HEADER, 0, false, false)).isSameAs(rendererComponent);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void usesTableHeaderRenderer() throws Exception {
        MixedRowTableColumn mixedColumn = new MixedRowTableColumn(headerColumn(null));
        mixedColumn.addSubColumn(subColumn(SUB_HEADER));
        when(table.getTableHeader()).thenReturn(tableHeader);
        when(tableHeader.getDefaultRenderer()).thenReturn(headerRenderer);

        Component component = mixedColumn.getHeaderRenderer().getTableCellRendererComponent(table, HEADER, false, false, -1, 0);

        assertThat(component).isInstanceOf(JList.class);
        JList<Object> jList = (JList<Object>) component;
        assertThat(jList.getModel().getSize()).isEqualTo(2);
        assertThat(jList.getModel().getElementAt(0)).isEqualTo(HEADER);
        assertThat(jList.getModel().getElementAt(1)).isEqualTo(SUB_HEADER);
        assertThat(jList.getCellRenderer().getListCellRendererComponent(jList, HEADER, 0, false, false)).isSameAs(rendererComponent);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void usesBlankStringForNullHeader() throws Exception {
        MixedRowTableColumn mixedColumn = new MixedRowTableColumn(headerColumn(headerRenderer));
        mixedColumn.addSubColumn(new TableColumn(-1));
        JList<Object> jList = (JList<Object>) mixedColumn.getHeaderRenderer().getTableCellRendererComponent(table, HEADER, false, false, -1, 0);

        jList.getCellRenderer().getListCellRendererComponent(jList, null, 0, false, false);

        verify(headerRenderer).getTableCellRendererComponent(table, " ", false, false, 0, 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void showsIconOnFirstHeaderRow() throws Exception {
        final DefaultTableCellHeaderRenderer renderer = newDefaultTableCellHeaderRenderer();
        MixedRowTableColumn mixedColumn = new MixedRowTableColumn(headerColumn(headerRenderer));
        mixedColumn.addSubColumn(new TableColumn(-1));
        when(headerRenderer.getTableCellRendererComponent(table, HEADER, false, false, 0, 0)).thenReturn(renderer);
        JList<Object> jList = (JList<Object>) mixedColumn.getHeaderRenderer().getTableCellRendererComponent(table, HEADER, false, false, -1, 0);

        jList.getCellRenderer().getListCellRendererComponent(jList, HEADER, 0, false, false);

        assertThat(renderer.getIcon()).isNotNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void doesNotShowIconOnHeaderSubrows() throws Exception {
        final DefaultTableCellHeaderRenderer renderer = newDefaultTableCellHeaderRenderer();
        MixedRowTableColumn mixedColumn = new MixedRowTableColumn(headerColumn(headerRenderer));
        mixedColumn.addSubColumn(new TableColumn(-1));
        when(headerRenderer.getTableCellRendererComponent(table, SUB_HEADER, false, false, 1, 0)).thenReturn(renderer);
        JList<Object> jList = (JList<Object>) mixedColumn.getHeaderRenderer().getTableCellRendererComponent(table, HEADER, false, false, -1, 0);

        jList.getCellRenderer().getListCellRendererComponent(jList, SUB_HEADER, 1, false, false);

        assertThat(renderer.getIcon()).isNull();
    }

    private DefaultTableCellHeaderRenderer newDefaultTableCellHeaderRenderer() {
        final DefaultTableCellHeaderRenderer renderer = new DefaultTableCellHeaderRenderer();
        renderer.setIcon(new ImageIcon());
        return renderer;
    }

    private TableColumn headerColumn(TableCellRenderer renderer) {
        TableColumn column = new TableColumn(1);
        column.setHeaderValue(HEADER);
        column.setHeaderRenderer(renderer);
        return column;
    }

    private TableColumn subColumn(String headerValue) {
        final TableColumn column = new TableColumn(-1);
        column.setHeaderValue(headerValue);
        return column;
    }
}
