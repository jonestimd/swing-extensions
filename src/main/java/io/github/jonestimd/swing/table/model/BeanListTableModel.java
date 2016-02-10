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
import java.util.List;
import java.util.Objects;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import io.github.jonestimd.collection.IdentityArrayList;

/**
 * A {@link TableModel} that uses a list of beans for table rows and maps bean properties to table columns.
 * @param <T> the class representing a row in the table
 * @see ColumnAdapter
 */
public class BeanListTableModel<T> extends AbstractTableModel implements ColumnIdentifier, BeanTableModel<T> {
    private final BeanTableAdapter<T> beanTableAdapter;
    private List<T> beans = new IdentityArrayList<>();

    public BeanListTableModel(List<? extends ColumnAdapter<? super T, ?>> columnAdapters) {
        this(columnAdapters, Collections.<TableDataProvider<T>>emptyList());
    }

    public BeanListTableModel(List<? extends ColumnAdapter<? super T, ?>> columnAdapters, Iterable<? extends TableDataProvider<T>> dataProviders) {
        this.beanTableAdapter = new BeanTableAdapter<>(this, columnAdapters, dataProviders);
    }

    @Override
    public int getBeanCount() {
        return beans.size();
    }

    @Override
    public T getBean(int index) {
        return beans.get(index);
    }

    @Override
    public int getRowCount() {
        return beans.size();
    }

    protected ColumnAdapter<? super T, ?> getColumnAdapter(int index) {
        return beanTableAdapter.getColumnAdapter(index);
    }

    @Override
    public int getColumnCount() {
        return beanTableAdapter.getColumnCount();
    }

    @Override
    public String getColumnName(int columnIndex) {
        return beanTableAdapter.getColumnName(columnIndex);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return beanTableAdapter.getColumnClass(columnIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return beanTableAdapter.isCellEditable(getRow(rowIndex), columnIndex);
    }

    public T getRow(int index) {
        return beans.get(index);
    }

    public void setRow(int row, T bean) {
        beans.set(row, bean);
        fireTableRowsUpdated(row, row);
    }

    @Override
    public ColumnAdapter<? super T, ?> getColumnIdentifier(int columnIndex) {
        return beanTableAdapter.getColumnAdapter(columnIndex);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return getValue(getRow(rowIndex), columnIndex);
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        Object currentValue = getValueAt(rowIndex, columnIndex);
        if (! Objects.equals(currentValue, value)) {
            setValue(value, rowIndex, columnIndex);
        }
    }

    @SuppressWarnings("unchecked")
    protected void setValue(Object value, int rowIndex, int columnIndex) {
        beanTableAdapter.setValue(value, getRow(rowIndex), rowIndex, columnIndex);
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    protected void notifyDataProviders(T row, String columnId, Object oldValue) {
        beanTableAdapter.notifyDataProviders(row, indexOf(row), columnId, oldValue);
    }

    @Override
    public Object getValue(T row, int columnIndex) {
        return beanTableAdapter.getValue(row, columnIndex);
    }

    public List<T> getBeans() {
        return Collections.unmodifiableList(beans);
    }

    public void setBeans(Collection<T> beans) {
        this.beans.clear();
        this.beans.addAll(beans);
        beanTableAdapter.setBeans(beans);
        fireTableDataChanged();
    }

    public int indexOf(T bean) {
        return beans.indexOf(bean);
    }

    public void addRow(T bean) {
        addRow(beans.size(), bean);
    }

    public void addRow(int row, T bean) {
        beans.add(row, bean);
        beanTableAdapter.addBean(bean);
        fireTableRowsInserted(row, row);
    }

    public void removeRow(T bean) {
        int row = beans.indexOf(bean);
        if (row >= 0) {
        	removeRowAt(row);
        }
    }

    protected void removeRowAt(int index) {
        T bean = beans.remove(index);
        beanTableAdapter.removeBean(bean);
        fireTableRowsDeleted(index, index);
    }

    public void removeAll(Iterable<T> rowBeans) {
        for (T bean : rowBeans) {
            removeRow(bean);
        }
    }
}