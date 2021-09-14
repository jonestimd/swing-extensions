// The MIT License (MIT)
//
// Copyright (c) 2021 Timothy D. Jones
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
package io.github.jonestimd.swing.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import io.github.jonestimd.swing.table.model.HeaderDetailTableModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MixedRowTableColumnTest {
    private static final String HEADER = "header";
    private static final String SUB_HEADER = "sub-column 1";
    @Mock
    private MixedRowTable<?, ?> table;
    @Mock
    private HeaderDetailTableModel<?> tableModel;
    @Mock
    private JTableHeader tableHeader;
    @Mock
    private DefaultTableCellRenderer mockRenderer;
    @Mock
    private Component rendererComponent;

    @Before
    public void setupRenderer() throws Exception {
        when(mockRenderer.getTableCellRendererComponent(table, HEADER, false, false, 0, 0)).thenReturn(rendererComponent);

        doReturn(tableModel).when(table).getModel();
        when(tableModel.getRowTypeIndex(anyInt())).then((i) -> i.getArguments()[0]);
        when(table.convertRowIndexToModel(anyInt())).then((i) -> i.getArguments()[0]);
        when(tableModel.getRowCount()).thenReturn(10);
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
        MixedRowTableColumn mixedColumn = new MixedRowTableColumn(newColumn(mockRenderer));
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
        MixedRowTableColumn mixedColumn = new MixedRowTableColumn(newColumn(null));
        mixedColumn.addSubColumn(subColumn(SUB_HEADER));
        when(table.getTableHeader()).thenReturn(tableHeader);
        when(tableHeader.getDefaultRenderer()).thenReturn(mockRenderer);

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
        MixedRowTableColumn mixedColumn = new MixedRowTableColumn(newColumn(mockRenderer));
        mixedColumn.addSubColumn(new TableColumn(-1));
        JList<Object> jList = (JList<Object>) mixedColumn.getHeaderRenderer().getTableCellRendererComponent(table, HEADER, false, false, -1, 0);

        jList.getCellRenderer().getListCellRendererComponent(jList, null, 0, false, false);

        verify(mockRenderer).getTableCellRendererComponent(table, " ", false, false, 0, 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void showsIconOnFirstHeaderRow() throws Exception {
        final JLabel renderer = newDefaultTableCellHeaderRenderer();
        MixedRowTableColumn mixedColumn = new MixedRowTableColumn(newColumn(mockRenderer));
        mixedColumn.addSubColumn(new TableColumn(-1));
        when(mockRenderer.getTableCellRendererComponent(table, HEADER, false, false, 0, 0)).thenReturn(renderer);
        JList<Object> jList = (JList<Object>) mixedColumn.getHeaderRenderer().getTableCellRendererComponent(table, HEADER, false, false, -1, 0);

        jList.getCellRenderer().getListCellRendererComponent(jList, HEADER, 0, false, false);

        assertThat(renderer.getIcon()).isNotNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void doesNotShowIconOnHeaderSubrows() throws Exception {
        final JLabel renderer = newDefaultTableCellHeaderRenderer();
        MixedRowTableColumn mixedColumn = new MixedRowTableColumn(newColumn(mockRenderer));
        mixedColumn.addSubColumn(new TableColumn(-1));
        when(mockRenderer.getTableCellRendererComponent(table, SUB_HEADER, false, false, 1, 0)).thenReturn(renderer);
        JList<Object> jList = (JList<Object>) mixedColumn.getHeaderRenderer().getTableCellRendererComponent(table, HEADER, false, false, -1, 0);

        jList.getCellRenderer().getListCellRendererComponent(jList, SUB_HEADER, 1, false, false);

        assertThat(renderer.getIcon()).isNull();
    }

    @Test
    public void cellRenderer_returnsDefaultTableCellRenderer() throws Exception {
        TableCellRenderer expectedRenderer = new DefaultTableCellRenderer();
        when(table.getDefaultRenderer(any())).thenReturn(expectedRenderer);
        MixedRowTableColumn mixedColumn = new MixedRowTableColumn(subColumn(SUB_HEADER));

        Component cellRenderer = mixedColumn.getCellRenderer().getTableCellRendererComponent(table, "", false, false, 0, 0);

        assertThat(cellRenderer).isSameAs(expectedRenderer);
    }

    @Test
    public void cellRenderer_setsPropertiesOnHeaderCell() throws Exception {
        final JLabel renderer = newDefaultTableCellHeaderRenderer();
        MixedRowTableColumn mixedColumn = new MixedRowTableColumn(newColumn(mockRenderer));
        when(mockRenderer.getTableCellRendererComponent(table, HEADER, false, false, 0, 0)).thenReturn(renderer);
        Font font = renderer.getFont().deriveFont(Font.ITALIC);

        TableCellRenderer cellRenderer = mixedColumn.getCellRenderer();
        ((JComponent) cellRenderer).setForeground(Color.cyan);
        ((JComponent) cellRenderer).setBackground(Color.gray);
        ((JComponent) cellRenderer).setFont(font);

        assertThat(cellRenderer.getTableCellRendererComponent(table, HEADER, false, false, 0, 0)).isSameAs(renderer);
        verify(mockRenderer).setForeground(Color.cyan);
        verify(mockRenderer).setBackground(Color.gray);
        verify(mockRenderer).setFont(font);
    }

    @Test
    public void cellRenderer_setsPropertiesOnSubrowCell() throws Exception {
        final JLabel renderer = newDefaultTableCellHeaderRenderer();
        MixedRowTableColumn mixedColumn = new MixedRowTableColumn(newColumn(new DefaultTableCellRenderer()));
        mixedColumn.addSubColumn(newColumn(mockRenderer));
        when(mockRenderer.getTableCellRendererComponent(table, SUB_HEADER, false, false, 1, 0)).thenReturn(renderer);
        Font font = renderer.getFont().deriveFont(Font.ITALIC);

        TableCellRenderer cellRenderer = mixedColumn.getCellRenderer();
        ((JComponent) cellRenderer).setForeground(Color.cyan);
        ((JComponent) cellRenderer).setBackground(Color.gray);
        ((JComponent) cellRenderer).setFont(font);

        assertThat(cellRenderer.getTableCellRendererComponent(table, SUB_HEADER, false, false, 1, 0)).isSameAs(renderer);
        verify(mockRenderer).setForeground(Color.cyan);
        verify(mockRenderer).setBackground(Color.gray);
        verify(mockRenderer).setFont(font);
    }

    private JLabel newDefaultTableCellHeaderRenderer() {
        final JLabel renderer = new JLabel();
        renderer.setIcon(new ImageIcon());
        return renderer;
    }

    private TableColumn newColumn(TableCellRenderer headerRenderer) {
        TableColumn column = new TableColumn(1);
        column.setHeaderValue(HEADER);
        column.setHeaderRenderer(headerRenderer);
        column.setCellRenderer(headerRenderer);
        return column;
    }

    private TableColumn subColumn(String headerValue) {
        final TableColumn column = new TableColumn(-1);
        column.setHeaderValue(headerValue);
        return column;
    }
}
