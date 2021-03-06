// The MIT License (MIT)
//
// Copyright (c) 2017 Timothy D. Jones
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import io.github.jonestimd.swing.table.action.UndoAction;
import io.github.jonestimd.swing.table.model.BeanTableModel;
import io.github.jonestimd.swing.table.model.ChangeBufferTableModel;
import io.github.jonestimd.swing.table.model.ColumnAdapter;
import io.github.jonestimd.swing.table.model.MixedRowTableModel;
import io.github.jonestimd.swing.table.model.ValidatedTableModel;

/**
 * Initializes table cell renderers and editors.
 */
public class TableInitializer {
    public static final String UNDO_CHANGE_ACTION_KEY = "io.github.jonestimd.swing.table.undoChange";

    private final Map<Class<?>, TableCellRenderer> tableCellRenderers;
    private final Map<Class<?>, Supplier<TableCellEditor>> tableCellEditors;
    private final Map<String, TableCellRenderer> columnRenderers;
    private final Map<String, Supplier<TableCellEditor>> columnEditors;
    private final BiFunction<JTable, ColumnConfiguration, ColumnWidthCalculator> columnWidthCalculatorBuilder;

    public TableInitializer(Map<Class<?>, TableCellRenderer> tableCellRenderers,
                            Map<Class<?>, Supplier<TableCellEditor>> tableCellEditors,
                            Map<String, TableCellRenderer> columnRenderers,
                            Map<String, Supplier<TableCellEditor>> columnEditors) {
        this(tableCellRenderers, tableCellEditors, columnRenderers, columnEditors, ValueClassColumnWidthCalculator::new);
    }

    public TableInitializer(Map<Class<?>, TableCellRenderer> tableCellRenderers,
                            Map<Class<?>, Supplier<TableCellEditor>> tableCellEditors,
                            Map<String, TableCellRenderer> columnRenderers,
                            Map<String, Supplier<TableCellEditor>> columnEditors,
            BiFunction<JTable, ColumnConfiguration, ColumnWidthCalculator> columnWidthCalculatorBuilder) {
        this.tableCellRenderers = tableCellRenderers;
        this.tableCellEditors = tableCellEditors;
        this.columnRenderers = columnRenderers;
        this.columnEditors = columnEditors;
        this.columnWidthCalculatorBuilder = columnWidthCalculatorBuilder;
    }

    private String getColumnResource(Object columnId, String resourceId) {
        if (columnId instanceof ColumnAdapter) {
            return ((ColumnAdapter<?,?>) columnId).getResource(resourceId, null);
        }
        return null;
    }

    public <B, M extends BeanTableModel<B>, T extends DecoratedTable<B, M>> T initialize(final T table) {
        applyDefaultRenders(table);
        applyDefaultEditors(table);
        TableColumnModel columnModel = table.getColumnModel();
        columnModel.addColumnModelListener(new TableColumnModelListener() {
            public void columnSelectionChanged(ListSelectionEvent e) {
            }

            public void columnRemoved(TableColumnModelEvent e) {
            }

            public void columnMoved(TableColumnModelEvent e) {
            }

            public void columnMarginChanged(ChangeEvent e) {
            }

            public void columnAdded(TableColumnModelEvent e) {
                TableColumnModel source = (TableColumnModel) e.getSource();
                initializeColumn(source.getColumn(e.getToIndex()));
            }
        });
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            initializeColumn(columnModel.getColumn(i));
        }
        BeanListColumnConfiguration configuration = new BeanListColumnConfiguration(table);
        new ColumnResizeHandler(table, configuration, columnWidthCalculatorBuilder.apply(table, configuration));
        setDecorators(table);
        addActions(table);
        return table;
    }

    private void addActions(JTable table) {
        table.getActionMap().put(UNDO_CHANGE_ACTION_KEY, new UndoAction());
        table.getInputMap().put(KeyStroke.getKeyStroke("control Z"), UNDO_CHANGE_ACTION_KEY);
    }

    private void setDecorators(DecoratedTable<?, ?> table) {
        List<TableDecorator> decorators = new ArrayList<>();
        if (table.getModel() instanceof ChangeBufferTableModel) {
            decorators.add(new UnsavedChangeDecorator());
        }
        if (table.getModel() instanceof ValidatedTableModel) {
            decorators.add(new ValidationDecorator());
        }
        table.setDecorators(decorators);
    }

    private void applyDefaultRenders(JTable table) {
        for (Entry<Class<?>, TableCellRenderer> entry : tableCellRenderers.entrySet()) {
            table.setDefaultRenderer(entry.getKey(), entry.getValue());
        }
    }

    protected void applyDefaultEditors(JTable table) {
        int columnCount = table.getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            applyDefaultEditor(table, table.getColumnClass(i));
        }
        if (table instanceof MixedRowTable) {
            MixedRowTableModel model = (MixedRowTableModel) table.getModel();
            for (int type = 1; type < model.getRowTypeCount(); type++) {
                for (int column = 0; column < table.getColumnCount(); column++) {
                    applyDefaultEditor(table, model.getColumnClass(type, column));
                }
            }
        }
    }

    protected void applyDefaultEditor(JTable table, Class<?> columnClass) {
        Supplier<TableCellEditor> supplier = tableCellEditors.get(columnClass);
        if (supplier != null) {
            table.setDefaultEditor(columnClass, supplier.get());
        }
    }

    protected void initializeColumn(TableColumn column) {
        if (column.getCellRenderer() == null) column.setCellRenderer(getColumnRenderer(column.getIdentifier()));
        if (column.getCellEditor() == null) column.setCellEditor(getColumnEditor(column.getIdentifier()));
        if (column instanceof MixedRowTableColumn) {
            MixedRowTableColumn mixedRowColumn = (MixedRowTableColumn) column;
            for (int i = 0; i < mixedRowColumn.getSubColumnCount(); i++) {
                TableColumn subColumn = mixedRowColumn.getSubColumn(i);
                Object alternateColumnId = subColumn.getIdentifier();
                subColumn.setCellRenderer(getColumnRenderer(alternateColumnId));
                subColumn.setCellEditor(getColumnEditor(alternateColumnId));
            }
        }
    }

    protected TableCellRenderer getColumnRenderer(Object columnId) {
        return columnRenderers == null ? null : columnRenderers.get(getColumnResource(columnId, ".renderer"));
    }

    protected TableCellEditor getColumnEditor(Object columnId) {
        String editorName = getColumnResource(columnId, ".editor");
        if (editorName == null || columnEditors == null) return null;
        Supplier<TableCellEditor> supplier = columnEditors.get(editorName);
        return supplier == null ? null : supplier.get();
    }

    /**
     * Set the minimum, preferred and maximum widths on a column.
     * @param column the table column
     * @param width the width for the column
     */
    public static void setFixedWidth(TableColumn column, int width) {
        column.setMinWidth(width);
        column.setPreferredWidth(width);
        column.setMaxWidth(width);
    }
}