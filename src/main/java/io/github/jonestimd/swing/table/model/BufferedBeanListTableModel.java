// Copyright (c) 2019 Timothy D. Jones
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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * This class overrides {@link BeanListTableModel} to add change tracking.  Changes to cells are queued
 * by {@link #setValueAt(Object, int, int)}. The following methods are used to update pending changes for rows:
 * <ul>
 * <li>{@link #queueAdd(Object)}</li>
 * <li>{@link #queueAdd(int, Object)} (cancelled by {@link #queueDelete(Object)}</li>
 * <li>{@link #queueDelete(Object)}</li>
 * <li>{@link #undoChangedAt(int, int)}</li>
 * <li>{@link #undoDelete(int)}</li>
 * </ul>
 * The {@link #commit()} method should be called after all pending changes have been saved.
 * The {@link #revert()} method is used to revert all pending changes.
 */
public class BufferedBeanListTableModel<T> extends BeanListTableModel<T> implements ChangeBufferTableModel<T> {
    private ChangeTracker<T> changeTracker = new ChangeTracker<T>(false) {
        @Override
        protected void revertItemChange(Object originalValue, T item, int index) {
            BufferedBeanListTableModel.super.setValue(originalValue, indexOf(item), index);
        }

        @Override
        protected void itemUpdated(T item) {
            fireTableRowUpdated(item);
        }

        @Override
        protected void itemDeleted(T item) {
            removeRow(item);
        }
    };

    @SafeVarargs
    public BufferedBeanListTableModel(ColumnAdapter<? super T, ?>... columnAdapters) {
        super(Arrays.asList(columnAdapters));
    }

    public BufferedBeanListTableModel(List<? extends ColumnAdapter<? super T, ?>> columnAdapters, Iterable<? extends TableDataProvider<T>> dataProviders) {
        super(columnAdapters, dataProviders);
    }

    @Override
    protected void setValue(Object value, int rowIndex, int columnIndex) {
        T row = getRow(rowIndex);
        changeTracker.setValue(row, columnIndex, getValue(row, columnIndex), value);
        super.setValue(value, rowIndex, columnIndex);
    }

    @Override
    public Stream<T> getChangedRows() {
        return changeTracker.getChanges();
    }

    @Override
    public List<T> getPendingAdds() {
        return changeTracker.getAdds();
    }

    @Override
    public List<T> getPendingDeletes() {
        return changeTracker.getDeletes();
    }

    @Override
    public Stream<T> getPendingUpdates() {
        return changeTracker.getUpdates();
    }

    @Override
    public void revert() {
        changeTracker.revert();
    }

    private void fireTableRowUpdated(T bean) {
        int rowIndex = indexOf(bean);
        fireTableRowsUpdated(rowIndex, rowIndex);
    }

    @Override
    public void commit() {
        changeTracker.commit();
    }

    @Override
    public boolean isChanged() {
        return !changeTracker.isEmpty();
    }

    @Override
    public boolean isChangedAt(int rowIndex, int columnIndex) {
        return changeTracker.isChanged(getRow(rowIndex), columnIndex);
    }

    @Override
    public void undoChangedAt(int rowIndex, int columnIndex) {
        changeTracker.undoChange(getRow(rowIndex), columnIndex);
    }

    public boolean isPendingAdd(int rowIndex) {
        return changeTracker.isPendingAdd(getRow(rowIndex));
    }

    @Override
    public boolean isPendingDelete(int rowIndex) {
        return changeTracker.isPendingDelete(getRow(rowIndex));
    }

    @Override
    public void undoDelete(int rowIndex) {
        changeTracker.undoDelete(getRow(rowIndex));
    }

    @Override
    public void setBeans(Collection<T> beans) {
        changeTracker.reset();
        super.setBeans(beans);
    }

    /**
     * Override to merge pending changes with new bean.  Copies changed column values from the old bean to the
     * new bean.  If the row was a pending add or delete then it will still be a pending add or delete.
     */
    @Override
    public void setRow(int row, T bean) {
        T oldBean = getBean(row);
        if (changeTracker.isPendingDelete(oldBean)) changeTracker.pendingDelete(bean);
        else if (changeTracker.isPendingAdd(oldBean)) changeTracker.pendingAdd(bean);
        else changeTracker.getChangeIndexes(oldBean).forEach(column -> copyColumn(column, oldBean, bean));
        changeTracker.resetItem(oldBean);
        super.setRow(row, bean);
    }

    @SuppressWarnings("unchecked")
    private void copyColumn(int column, T oldBean, T newBean) {
        Object changeValue = getValue(oldBean, column);
        Object value = getValue(newBean, column);
        if (!Objects.equals(value, changeValue)) {
            ((ColumnAdapter) beanTableAdapter.getColumnAdapter(column)).setValue(newBean, changeValue);
            changeTracker.setValue(newBean, column, value, changeValue);
        }
    }

    /**
     * Override to disable editing on a pending delete.
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return !isPendingDelete(rowIndex) && super.isCellEditable(rowIndex, columnIndex);
    }

    public void queueAdd(T bean) {
        queueAdd(getRowCount(), bean);
    }

    public void queueAdd(int row, T bean) {
        changeTracker.pendingAdd(bean);
        addRow(row, bean);
    }

    private void cancelAdd(T bean) {
        changeTracker.resetItem(bean);
        removeRow(bean);
    }

    /**
     * @return true if the delete was queued, false if the row was an unsaved addition and was deleted immediately.
     */
    public boolean queueDelete(T bean) {
        if (changeTracker.isPendingAdd(bean)) {
            cancelAdd(bean);
            return false;
        }
        changeTracker.pendingDelete(bean);
        int index = indexOf(bean);
        fireTableRowsUpdated(index, index);
        return true;
    }
}