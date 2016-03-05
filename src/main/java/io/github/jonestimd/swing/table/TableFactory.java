// Copyright (c) 2016 Timothy D. Jones
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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableRowSorter;

import io.github.jonestimd.swing.component.ComboBoxCellEditor;
import io.github.jonestimd.swing.table.model.BeanListMultimapTableModel;
import io.github.jonestimd.swing.table.model.BeanListTableModel;
import io.github.jonestimd.swing.table.model.BeanTableModel;
import io.github.jonestimd.swing.table.model.ColumnAdapter;
import io.github.jonestimd.swing.table.model.ValidatedBeanListTableModel;
import io.github.jonestimd.swing.validation.ValidatingTextCellEditor;
import io.github.jonestimd.swing.validation.Validator;
import io.github.jonestimd.util.Streams;

public class TableFactory {
    private final TableInitializer tableInitializer;

    public TableFactory(TableInitializer tableInitializer) {
        this.tableInitializer = tableInitializer;
    }

    public <B, M extends BeanListTableModel<B>> DecoratedTable<B, M> createSortedTable(M model, int ... sortColumns) {
        DecoratedTable<B, M> table = new DecoratedTable<>(model);
        table.setRowSorter(newRowSorter(model, sortColumns));
        return initialize(table);
    }

    @SuppressWarnings("unchecked")
    public <B, M extends ValidatedBeanListTableModel<B>> DecoratedTable<B, M> createValidatedTable(M model, int ... sortColumns) {
        DecoratedTable<B, M> table = createSortedTable(model, sortColumns);
        Validator<String> validationAdapter = value -> table.isEditing() ?
                model.validateAt(table.convertRowIndexToModel(table.getEditingRow()), table.convertColumnIndexToModel(table.getEditingColumn()), value) : null;
        for (int columnIndex = 0; columnIndex < model.getColumnCount(); columnIndex++) {
            Class<?> columnClass = model.getColumnClass(columnIndex);
            ColumnAdapter<? super B, ?> columnIdentifier = model.getColumnIdentifier(columnIndex);
            if (columnClass == String.class) {
                table.getColumn(columnIdentifier).setCellEditor(new ValidatingTextCellEditor(table, validationAdapter));
            }
            else if (Enum.class.isAssignableFrom(columnClass)) {
                        table.getColumn(columnIdentifier).setCellEditor(createEnumCellEditor((Class<? extends Enum<?>>) columnClass));
            }
        }
        table.setCellSelectionEnabled(true);
        return table;
    }

    protected <B, M extends BeanTableModel<B>, T extends DecoratedTable<B, M>> T initialize(T table) {
        return tableInitializer.initialize(table);
    }

    protected RowSorter<BeanListTableModel<?>> newRowSorter(BeanListTableModel<?> model, int ... sortColumns) {
        TableRowSorter<BeanListTableModel<?>> sorter = new TableRowSorter<>(model);
        sorter.setSortsOnUpdates(true);
        if (sortColumns.length > 0) {
            sorter.setSortKeys(Streams.map(sortColumns, column -> new SortKey(column, SortOrder.ASCENDING)));
        }
        return sorter;
    }

    public <G, T, M extends BeanListMultimapTableModel<G, T>> SectionTable<T, M> createTable(M model) {
        return tableInitializer.initialize(new SectionTable<>(model));
    }

    public static TableCellEditor createEnumCellEditor(Class<? extends Enum<?>> enumType) { // TODO use TranslatingComboBoxCellEditor
        return new ComboBoxCellEditor(new JComboBox<>(enumType.getEnumConstants()));
    }

    public static void addDoubleClickHandler(JTable table, Consumer<MouseEvent> handler) {
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                super.mouseClicked(event);
                if (event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() == 2) {
                    handler.accept(event);
                }
            }
        });
    }
}