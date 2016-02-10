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
package io.github.jonestimd.swing.table.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import io.github.jonestimd.swing.validation.BeanPropertyValidator;

/**
 * Extends {@link BufferedBeanListTableModel} to add validation.
 */
public class ValidatedBeanListTableModel<T> extends BufferedBeanListTableModel<T> implements ValidatedTableModel {
    private final Table<Integer, Integer, String> errors = HashBasedTable.create();

    public ValidatedBeanListTableModel(List<? extends ColumnAdapter<? super T, ?>> columnAdapters) {
        this(columnAdapters, Collections.<TableDataProvider<T>>emptyList());
    }

    public ValidatedBeanListTableModel(List<? extends ColumnAdapter<? super T, ?>> columnAdapters, Iterable<? extends TableDataProvider<T>> dataProviders) {
        super(columnAdapters, dataProviders);
    }

    public void setBeans(Collection<T> beans) {
        errors.clear();
        super.setBeans(beans);
        for (int i = 0; i < getRowCount(); i++) {
            validateRow(i);
        }
    }

    public void addRow(int rowIndex, T bean) {
        super.addRow(rowIndex, bean);
        shiftErrors(rowIndex, 1);
        fireTableRowsUpdated(rowIndex, rowIndex);
    }

    private void shiftErrors(int firstRow, int delta) {
        Table<Integer, Integer, String> updatedErrors = HashBasedTable.create();
        Iterator<Entry<Integer, Map<Integer, String>>> iterator = errors.rowMap().entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<Integer, Map<Integer, String>> entry = iterator.next();
            if (entry.getKey() >= firstRow) {
                updatedErrors.row(entry.getKey() + delta).putAll(entry.getValue());
                iterator.remove();
            }
        }
        errors.putAll(updatedErrors);

    }

    protected void validateRow(int rowIndex) {
        for (int columnIndex = 0; columnIndex < getColumnCount(); columnIndex++) {
            validateCell(rowIndex, columnIndex);
        }
    }

    protected void validateCell(int rowIndex, int columnIndex) {
        String validation = validateAt(rowIndex, columnIndex, getValueAt(rowIndex, columnIndex));
        if (validation == null) {
            errors.remove(rowIndex, columnIndex);
        }
        else {
            errors.put(rowIndex, columnIndex, validation);
        }
    }

    @Override
    public String validateAt(int rowIndex, int columnIndex) {
        return errors.get(rowIndex, columnIndex);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> String validateAt(int rowIndex, int columnIndex, V value) {
        ColumnAdapter<? super T, ?> columnAdapter = getColumnAdapter(columnIndex);
        if (columnAdapter instanceof BeanPropertyValidator) {
            BeanPropertyValidator<T, V> validator = (BeanPropertyValidator<T, V>) columnAdapter;
            return validator.validate(rowIndex, value, getBeans());
        }
        return null;
    }

    public boolean isNoErrors() {
        return errors.isEmpty();
    }

    public void fireTableCellUpdated(int rowIndex, int columnIndex) {
        validateCell(rowIndex, columnIndex);
        super.fireTableCellUpdated(rowIndex, columnIndex);
    }

    public void fireTableRowsUpdated(int firstRow, int lastRow) {
        for (int i = firstRow; i <= lastRow; i++) {
            validateRow(i);
        }
        super.fireTableRowsUpdated(firstRow, lastRow);
    }

    @Override
    public void fireTableRowsDeleted(int firstRow, int lastRow) {
        for (int i = firstRow; i <= lastRow; i++) {
            errors.row(i).clear();
        }
        shiftErrors(firstRow, firstRow - lastRow - 1);
        super.fireTableRowsDeleted(firstRow, lastRow);
    }
}