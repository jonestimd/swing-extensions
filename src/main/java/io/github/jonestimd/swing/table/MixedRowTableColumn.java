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

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import io.github.jonestimd.swing.table.model.MixedRowTableModel;

/**
 * A table column for a {@link MixedRowTable}.  Provides a header value for each sub-row of the table.
 */
public class MixedRowTableColumn extends TableColumn {
    private List<TableColumn> subColumns = new ArrayList<>();

    public MixedRowTableColumn(TableColumn column) {
        super(column.getModelIndex());
        this.identifier = column.getIdentifier();
        this.headerValue = column.getHeaderValue();
        this.cellEditor = column.getCellEditor();
        this.cellRenderer = new MixedRowCellRenderer(column.getCellRenderer());
        this.headerRenderer = new MixedRowHeaderRenderer(column.getHeaderRenderer());
    }

    public void addSubColumn(TableColumn column) {
        subColumns.add(column);
    }

    public int getSubColumnCount() {
        return subColumns.size();
    }

    public TableColumn getSubColumn(int typeIndex) {
        return subColumns.get(typeIndex);
    }

    protected class MixedRowCellRenderer extends DefaultTableCellRenderer {
        private TableCellRenderer cellRenderer;

        protected MixedRowCellRenderer(TableCellRenderer cellRenderer) {
            this.cellRenderer = cellRenderer;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            int modelRow = table.convertRowIndexToModel(row);
            int modelColumn = table.convertColumnIndexToModel(column);
            TableCellRenderer renderer = getRenderer(table, modelRow, modelColumn);
            if (renderer instanceof JComponent) {
                JComponent component = (JComponent) renderer;
                component.setBackground(getBackground());
                component.setForeground(getForeground());
                component.setFont(getFont());
            }
            return renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }

        protected TableCellRenderer getRenderer(JTable table, int row, int column) {
            if (row < table.getModel().getRowCount() && table instanceof MixedRowTable) {
                MixedRowTableModel model = (MixedRowTableModel) table.getModel();
                int typeIndex = model.getRowTypeIndex(row);
                return getRenderer(table, typeIndex, model.getColumnClass(typeIndex, column));
            }
            return getRenderer(table, 0, table.getColumnClass(column));
        }

        protected TableCellRenderer getRenderer(JTable table, int typeIndex, Class<?> columnClass) {
            TableCellRenderer renderer = typeIndex == 0 ? cellRenderer : getSubColumn(typeIndex-1).getCellRenderer();
            return renderer != null ? renderer : table.getDefaultRenderer(columnClass);
        }
    }

    private class MixedRowHeaderRenderer extends JList<Object> implements TableCellRenderer {
        private final TableCellRenderer headerCellRenderer;
        private final CellRenderer renderer = new CellRenderer();

        private MixedRowHeaderRenderer(TableCellRenderer headerCellRenderer) {
            this.headerCellRenderer = headerCellRenderer;
            setCellRenderer(renderer);
            setModel(new RendererModel());
        }

        public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            renderer.table = t;
            renderer.column = column;
            renderer.tableRenderer = headerCellRenderer == null ? t.getTableHeader().getDefaultRenderer() : headerCellRenderer;
            return this;
        }
    }

    private static class CellRenderer implements ListCellRenderer<Object> {
        private JTable table;
        private int column;
        private TableCellRenderer tableRenderer;

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value == null) {
                value = " ";
            }
            Component component = tableRenderer.getTableCellRendererComponent(table, value, isSelected, cellHasFocus, index, column);
            if (index > 0 && component instanceof JLabel) {
                ((JLabel) component).setIcon(null);
            }
            return component;
        }
    }

    private class RendererModel extends AbstractListModel<Object> {
        public int getSize() {
            return subColumns.size() + 1;
        }

        public Object getElementAt(int rowIndex) {
            return rowIndex == 0 ? getHeaderValue() : subColumns.get(rowIndex - 1).getHeaderValue();
        }
    }
}