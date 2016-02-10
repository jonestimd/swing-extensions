package io.github.jonestimd.swing.table;

import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import org.junit.Test;

import static org.junit.Assert.*;

public class MixedRowTableColumnTest {
    @Test
    public void testConstructorCopiesValues() throws Exception {
        TableColumn column = new TableColumn(1);
        column.setIdentifier("identifier");
        column.setHeaderValue("header value");
        column.setCellEditor(new DefaultCellEditor(new JTextField()));
        column.setCellRenderer(new DefaultTableCellRenderer());

        MixedRowTableColumn mixedColumn = new MixedRowTableColumn(column);

        assertEquals(column.getModelIndex(), mixedColumn.getModelIndex());
        assertEquals(column.getIdentifier(), mixedColumn.getIdentifier());
        assertEquals(column.getHeaderValue(), mixedColumn.getHeaderValue());
        assertSame(column.getCellEditor(), mixedColumn.getCellEditor());
        assertSame(column.getCellRenderer(), mixedColumn.getCellRenderer());
        assertEquals("io.github.jonestimd.swing.table.MixedRowTableColumn$MixedRowHeaderRenderer", mixedColumn.getHeaderRenderer().getClass().getName());
    }
}
