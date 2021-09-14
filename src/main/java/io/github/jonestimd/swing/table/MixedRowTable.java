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
import java.awt.Rectangle;
import java.lang.reflect.Constructor;
import java.util.stream.IntStream;

import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import io.github.jonestimd.swing.table.model.BeanTableModel;
import io.github.jonestimd.swing.table.model.MixedRowTableModel;
import io.github.jonestimd.swing.table.sort.MixedRowTableRowSorter;

/**
 * A table that displays rows in groups.  The ordering of the rows within each group is constant but sorting and filtering
 * can be applied to the groups.  Row groups are visually separated by alternating background colors (rows within a group
 * have the same background color).
 * @param <Bean> the row group class
 * @param <Model> the table model class
 */
public class MixedRowTable<Bean, Model extends MixedRowTableModel & BeanTableModel<Bean>> extends DecoratedTable<Bean, Model> {
    public MixedRowTable(Model model) {
        super(model);
    }

    /**
     * Overridden to require a model that implements {@link MixedRowTableModel}.
     */
    @Override
    public void setModel(TableModel dataModel) {
        if (! (dataModel instanceof MixedRowTableModel)) {
            throw new IllegalArgumentException("Not a MixedRowTableModel");
        }
        super.setModel(dataModel);
    }

    /**
     * Adds a {@link MixedRowTableColumn} using {@code aColumn} for the first sub-row type and additional
     * column definitions for the remaining sub-row types.
     * @param aColumn the column definition for the first sub-row type.
     */
    @Override
    public void addColumn(TableColumn aColumn) {
        int modelColumn = aColumn.getModelIndex();
        MixedRowTableColumn column = new MixedRowTableColumn(aColumn);
        for (int i = 1; i < getModel().getRowTypeCount(); i++) {
            TableColumn subColumn = new TableColumn(modelColumn);
            subColumn.setHeaderValue(getModel().getColumnName(i, modelColumn));
            subColumn.setIdentifier(getModel().getColumnIdentifier(i, modelColumn));
            column.addSubColumn(subColumn);
        }
        super.addColumn(column);
    }

    /**
     * Overridden to use editors that handle multiple value types in a single column.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void createDefaultEditors() {
        super.createDefaultEditors();
        defaultEditorsByColumnClass.put(Object.class, new GenericCellEditor());
        defaultEditorsByColumnClass.put(Number.class, new NumberCellEditor());
    }

    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        MixedRowTableModel model = getModel();
        int typeIndex = model.getRowTypeIndex(convertRowIndexToModel(row));
        if (typeIndex > 0) {
            TableCellEditor editor = ((MixedRowTableColumn) getColumnModel().getColumn(column)).getSubColumn(typeIndex-1).getCellEditor();
            return editor == null ? getDefaultEditor(getCellClass(row, column)) : editor;
        }
        return super.getCellEditor(row, column);
    }

    /**
     * Get the value class for a cell.
     * @return the cell's value class
     */
    public Class<?> getCellClass(int row, int column) {
        return getModel().getCellClass(convertRowIndexToModel(row), convertColumnIndexToModel(column));
    }

    @Override
    protected Color getRowBackground(int row) {
        int viewGroup = -1;
        if (getRowSorter() instanceof MixedRowTableRowSorter) {
            viewGroup = ((MixedRowTableRowSorter)getRowSorter()).getViewGroup(row);
        }
        if (viewGroup < 0) {
            viewGroup = getModel().getGroupNumber(convertRowIndexToModel(row));
        }
        return super.getRowBackground(viewGroup);
    }

    @Override
    protected IntStream getBeanIndexes(int[] viewIndexes) {
        return super.getBeanIndexes(viewIndexes).map(getModel()::getGroupNumber).distinct();
    }

    /**
     * Select the first column of the specified row.
     * @param viewIndex the view index of the row
     */
    public void selectRowAt(int viewIndex) {
        getSelectionModel().setSelectionInterval(viewIndex, viewIndex);
        getColumnModel().getSelectionModel().setSelectionInterval(0, 0);
        scrollToSelectedGroup();
    }

    /**
     * Scroll so that the selected group is visible.
     */
    protected void scrollToSelectedGroup() {
        int groupNumber = getModel().getGroupNumber(convertRowIndexToModel(getSelectionModel().getLeadSelectionIndex()));
        int leadRow = getModel().getLeadRowForGroup(groupNumber);
        Rectangle cellRect = getCellRect(convertRowIndexToView(leadRow), getColumnModel().getSelectionModel().getLeadSelectionIndex(), true);
        cellRect.height *= getModel().getRowCount(groupNumber);
        scrollRectToVisible(cellRect);
    }

    /**
     * Get the model index of the first selected row.
     */
    public int getLeadSelectionModelIndex() {
        return convertRowIndexToModel(getSelectionModel().getLeadSelectionIndex());
    }

    private static final Class<?>[] CELL_VALUE_CONSTRUCTOR_ARG_TYPES = { String.class };

    /**
     * Copy of JTable.GenericCellEditor that handles multiple value types in a single column.
     */
    private class GenericCellEditor extends DefaultCellEditor {
        private Constructor<?> constructor;
        private Object editorValue;

        public GenericCellEditor() {
            super(new JTextField());
            getComponent().setName("Table.editor");
        }

        public boolean stopCellEditing() {
            String s = (String)super.getCellEditorValue();
            if ("".equals(s)) {
                if (constructor.getDeclaringClass() == String.class) {
                    editorValue = s;
                }
                super.stopCellEditing();
            }

            try {
                editorValue = constructor.newInstance(s);
            }
            catch (Exception e) {
                ((JComponent)getComponent()).setBorder(new LineBorder(Color.red));
                return false;
            }
            return super.stopCellEditing();
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.editorValue = null;
            ((JComponent)getComponent()).setBorder(new LineBorder(Color.black));
            try {
                // fixed to get correct value class
                Class<?> type = getModel().getCellClass(convertRowIndexToModel(row), convertColumnIndexToModel(column));
                if (type == Object.class) {
                    type = String.class;
                }
                constructor = type.getConstructor(CELL_VALUE_CONSTRUCTOR_ARG_TYPES);
            }
            catch (Exception e) {
                return null;
            }
            return super.getTableCellEditorComponent(table, value, isSelected, row, column);
        }

        public Object getCellEditorValue() {
            return editorValue;
        }
    }

    /**
     * Copy of JTable.NumberCellEditor that handles multiple value types in a single column.
     */
    private class NumberCellEditor extends GenericCellEditor {
        public NumberCellEditor() {
            ((JTextField)getComponent()).setHorizontalAlignment(JTextField.RIGHT);
        }
    }
}