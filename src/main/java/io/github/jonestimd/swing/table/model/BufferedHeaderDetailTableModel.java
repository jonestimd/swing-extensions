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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import io.github.jonestimd.swing.validation.BeanPropertyValidator;
import io.github.jonestimd.util.Streams;

/**
 * This class overrides {@link HeaderDetailTableModel} to add change tracking and validation.  Changes to cells are queued
 * by {@link #setValueAt}. The following methods are used to queue pending changes for rows:
 * <ul>
 *     <li>{@link #queueAdd(Object)}</li>
 *     <li>{@link #queueAdd(int, Object)}</li>
 *     <li>{@link #queueAppendSubRow(int)}</li>
 *     <li>{@link #queueDelete(int)}</li>
 * </ul>
 * The {@link #commit()} method should be called after all pending changes have been saved.
 * The {@link #revert()} method is used to revert all pending changes.
 */
public class BufferedHeaderDetailTableModel<H> extends HeaderDetailTableModel<H>
    implements ChangeBufferTableModel<H>, ValidatedTableModel
{
    private final Table<Integer, Integer, String> errors = HashBasedTable.create();

    private ChangeTracker<ChangeRow<H>> changeTracker = new ChangeTracker<ChangeRow<H>>(true) {
        @Override
        protected void revertItemChange(Object originalValue, ChangeRow<H> row, int index) {
            int rowIndex = rowIndexOf(row.header) + detailAdapter.detailIndex(row.header, row.detail) + 1;
            setCellValue(originalValue, rowIndex, index);
        }

        @Override
        protected void itemUpdated(ChangeRow<H> row) {
            fireTableRowsUpdated(row);
        }

        @Override
        protected void itemDeleted(ChangeRow<H> row) {
            if (row.detail == null) {
                BufferedHeaderDetailTableModel.super.removeBean(row.header);
            }
            else {
                removeSubRow(row);
            }
        }
    };

    protected BufferedHeaderDetailTableModel(DetailAdapter<H> detailAdapter, Function<? super H, ?> idFunction) {
        super(detailAdapter, idFunction);
    }

    public BufferedHeaderDetailTableModel(DetailAdapter<H> detailAdapter,
                                          List<? extends ColumnAdapter<H, ?>> columnAdapters,
                                          List<? extends List<? extends ColumnAdapter<?, ?>>> detailColumnAdapters) {
        this(detailAdapter, Function.identity(), columnAdapters, detailColumnAdapters);
    }

    public BufferedHeaderDetailTableModel(DetailAdapter<H> detailAdapter, Function<? super H, ?> idFunction,
                                          List<? extends ColumnAdapter<H, ?>> columnAdapters,
                                          List<? extends List<? extends ColumnAdapter<?, ?>>> detailColumnAdapters) {
        super(detailAdapter, idFunction, columnAdapters, detailColumnAdapters);
    }

    /**
     * Overridden to reset change tracking and validation.
     */
    @Override
    public void setBeans(Collection<H> beans) {
        changeTracker.reset();
        errors.clear();
        super.setBeans(beans);
        for (int i = 0; i < getBeanCount(); i++) {
            updateGroupValidation(i);
        }
    }

    @Override
    public boolean queueDelete(H bean) {
        return queueDelete(rowIndexOf(bean));
    }

    /**
     * Mark a row as a pending delete or remove the row if it is a pending addition.  If the specified row is the
     * header of a group then the operation is applied to the entire group.
     * @return true if the delete was queued or false if the row was an unsaved addition and was deleted immediately.
     */
    public boolean queueDelete(int rowIndex) {
        int subRowIndex = getSubRowIndex(rowIndex);
        H bean = getBeanAtRow(rowIndex);
        if (subRowIndex > 0 && isPendingAdd(rowIndex)) {
            changeTracker.resetItem(new ChangeRow<>(bean, detailAdapter.getDetail(bean, subRowIndex-1)));
            detailAdapter.removeDetail(bean, subRowIndex-1);
            fireTableRowsDeleted(rowIndex, rowIndex);
            return false;
        }
        if (isPendingAdd(rowIndex)) {
            removeBean(bean);
            return false;
        }
        queueDelete(bean, subRowIndex);
        return true;
    }

    private void queueDelete(H bean, int subRowIndex) {
        if (subRowIndex == 0) {
            changeTracker.cancelDeletes(new HeaderPredicate(bean));
        }
        ChangeRow<H> row = new ChangeRow<>(bean, subRowIndex == 0 ? null : detailAdapter.getDetail(bean, subRowIndex-1));
        changeTracker.pendingDelete(row);
        fireTableRowsUpdated(row);
    }

    /**
     * Append an unsaved group.
     */
    public void queueAdd(H bean) {
        queueAdd(getBeanCount(), bean);
    }

    /**
     * Insert an unsaved group.
     */
    public void queueAdd(int groupNumber, H bean) {
        changeTracker.pendingAdd(new ChangeRow<>(bean, null));
        addBean(groupNumber, bean);
    }

    /**
     * Append a detail row to a group.
     * @param currentRow the index of a row in the group.
     * @return the index of the new row.
     */
    public int queueAppendSubRow(int currentRow) {
        int index = getGroupNumber(currentRow);
        H bean = getBean(index);
        int subRowCount = detailAdapter.appendDetail(bean);
        fireSubRowInserted(bean, subRowCount);
        return getLeadRowForGroup(index) + subRowCount;
    }

    /**
     * Append a saved group.
     */
    public void addBean(H bean) {
        addBean(getBeanCount(), bean);
    }

    /**
     * Insert a saved group.
     */
    @Override
    public void addBean(int index, H bean) {
        super.addBean(index, bean);
        int firstRow = getLeadRowForGroup(index);
        int subRowCount = detailAdapter.getDetailCount(bean);
        updateGroupValidation(index);
        fireTableRowsUpdated(firstRow, firstRow + subRowCount);
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

    /**
     * Overridden to reset pending changes and validation for the group.
     */
    @Override
    public void setBean(int index, H bean) {
        H oldBean = getBean(index);
        resetChanges(oldBean);
        super.setBean(index, bean);
        updateGroupValidation(index);
    }

    /**
     * Overridden to update validation.
     */
    @Override
    public void fireTableRowsDeleted(int firstRow, int lastRow) {
        for (int i = firstRow; i <= lastRow; i++) {
            errors.row(i).clear();
        }
        shiftErrors(firstRow, firstRow - lastRow - 1);
        super.fireTableRowsDeleted(firstRow, lastRow);
    }

    private H resetChanges(H bean) {
        changeTracker.resetItems(new HeaderPredicate(bean));
        return bean;
    }

    @Override
    public void removeBean(H bean) {
        super.removeBean(bean);
        resetChanges(bean);
    }

    // TODO visible for testing
    protected void removeSubRow(H bean, Object subRow) {
        removeSubRow(new ChangeRow<>(bean, subRow));
    }

    private void removeSubRow(ChangeRow<H> row) {
        int rowIndex = rowIndexOf(row.header) + detailAdapter.detailIndex(row.header, row.detail) + 1;
        detailAdapter.removeDetail(row.header, row.detail);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }

    /**
     * Overridden to update validation.
     */
    @Override
    public void fireTableRowsInserted(int firstRow, int lastRow) {
        shiftErrors(firstRow, lastRow - firstRow + 1);
        super.fireTableRowsInserted(firstRow, lastRow);
    }

    /**
     * Mark the sub-row as an unsaved addition unless the header bean is already an unsaved addition.
     */
    protected void fireSubRowInserted(H bean, int subRowIndex) {
        int index = indexOf(bean);
        if (! changeTracker.isPendingAdd(new ChangeRow<>(bean, null))) {
            changeTracker.pendingAdd(new ChangeRow<>(bean, detailAdapter.getDetail(bean, subRowIndex-1)));
        }
        int rowIndex = getLeadRowForGroup(index) + subRowIndex;
        fireTableRowsInserted(rowIndex, rowIndex);
    }

    /**
     * Update the errors for all rows in a group without firing change events.
     */
    protected void updateGroupValidation(int groupNumber) {
        int headerRow = getLeadRowForGroup(groupNumber);
        H bean = getBean(groupNumber);
        for (int subRow = 0; subRow <= detailAdapter.getDetailCount(bean); subRow++) {
            updateRowValidation(headerRow + subRow);
        }
    }

    /**
     * Update the errors for a row without firing change events.
     */
    protected void updateRowValidation(int rowIndex) {
        for (int columnIndex = 0; columnIndex < getColumnCount(); columnIndex++) {
            validateCell(rowIndex, columnIndex);
        }
    }

    /**
     * @return true if the cell validation changed
     */
    protected boolean validateCell(int rowIndex, int columnIndex) {
        String validation = validateAt(rowIndex, columnIndex, getValueAt(rowIndex, columnIndex));
        if (validation == null) {
            return errors.remove(rowIndex, columnIndex) != null;
        }
        return errors.put(rowIndex, columnIndex, validation) == null;
    }

    @Override
    public String validateAt(int rowIndex, int columnIndex) {
        return errors.get(rowIndex, columnIndex);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <V> String validateAt(int rowIndex, int columnIndex, V value) {
        int subRowIndex = getSubRowIndex(rowIndex);
        if (subRowIndex == 0) {
            ColumnAdapter<H, ?> columnAdapter = getColumnAdapter(columnIndex);
            if (columnAdapter instanceof BeanPropertyValidator) {
                BeanPropertyValidator validator = (BeanPropertyValidator) columnAdapter;
                return validator.validate(getGroupNumber(rowIndex), value, getBeans());
            }
        }
        else {
            H bean = getBeanAtRow(rowIndex);
            int detailTypeIndex = detailAdapter.getDetailTypeIndex(bean, subRowIndex-1);
            ColumnAdapter<Object,Object> columnAdapter = getDetailColumnAdapter(detailTypeIndex, columnIndex);
            if (columnAdapter instanceof BeanPropertyValidator) {
                BeanPropertyValidator validator = (BeanPropertyValidator) columnAdapter;
                return validator.validate(subRowIndex-1, value, detailAdapter.getDetails(bean, detailTypeIndex));
            }
        }
        return null;
    }

    public boolean isNoErrors() {
        return errors.isEmpty();
    }

    /**
     * Override so that pending deletes can not be edited.
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return !isPendingDelete(rowIndex) && super.isCellEditable(rowIndex, columnIndex);
    }

    /**
     * Overridden to update change tracking and validation.
     */
    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        Object currentValue = getValueAt(rowIndex, columnIndex);
        try {
            if (! Objects.equals(currentValue, value)) {
                changeTracker.setValue(newChangeKey(rowIndex), columnIndex, currentValue, value);
                setCellValue(value, rowIndex, columnIndex);
            }
        } catch (Exception ex) {
            throw new UnsupportedOperationException(ex);
        }
    }

    /**
     * Overridden to update validation.
     */
    @Override
    public void fireTableCellUpdated(int row, int column) {
        validateCell(row, column);
        super.fireTableCellUpdated(row, column);
    }

    @Override
    protected void setCellValue(Object value, int rowIndex, int columnIndex) {
        super.setCellValue(value, rowIndex, columnIndex);
        // validate header row
        if (isSubRow(rowIndex)) {
            validateRow(getLeadRowForGroup(getGroupNumber(rowIndex)));
        }
    }

    private void validateRow(int rowIndex) {
        for (int columnIndex = 0; columnIndex < getColumnCount(); columnIndex++) {
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }

    @Override
    public Stream<H> getChangedRows() {
        return changeTracker.getChanges().map(ChangeRow::getHeader);
    }

    @Override
    public List<H> getPendingAdds() {
        return Streams.map(changeTracker.getAdds(), ChangeRow::getHeader);
    }

    @Override
    public List<H> getPendingDeletes() {
        return Streams.map(changeTracker.getDeletes(), ChangeRow::getHeader);
    }

    @Override
    public Stream<H> getPendingUpdates() {
        return changeTracker.getUpdates().map(ChangeRow::getHeader);
    }

    @Override
    public boolean isChanged() {
        return ! changeTracker.isEmpty();
    }

    /**
     * @return true if there are pending changes for the group.
     */
    public boolean isChanged(H bean) {
        return getChangedRows().anyMatch(bean::equals);
    }

    @Override
    public boolean isChangedAt(int rowIndex, int columnIndex) {
        return changeTracker.isPendingAdd(new ChangeRow<>(getBeanAtRow(rowIndex), null)) ||
            isPendingDelete(rowIndex) || changeTracker.isChanged(newChangeKey(rowIndex), columnIndex);
    }

    @Override
    public void undoChangedAt(int rowIndex, int columnIndex) {
        changeTracker.undoChange(newChangeKey(rowIndex), columnIndex);
    }

    public boolean isPendingAdd(int rowIndex) {
        return changeTracker.isPendingAdd(new ChangeRow<>(getBeanAtRow(rowIndex), null)) ||
            changeTracker.isPendingAdd(newChangeKey(rowIndex));
    }

    @Override
    public boolean isPendingDelete(int rowIndex) {
        return changeTracker.isPendingDelete(new ChangeRow<>(getBeanAtRow(rowIndex), null)) ||
            changeTracker.isPendingDelete(newChangeKey(rowIndex));
    }

    @Override
    public void undoDelete(int rowIndex) {
        changeTracker.undoDelete(newChangeKey(rowIndex));
    }

    public boolean isPendingDelete(H bean) {
        return isPendingDelete(rowIndexOf(bean));
    }

    @Override
    public void revert() {
        changeTracker.revert();
    }

    @Override
    public void commit() {
        changeTracker.commit();
    }

    private void fireTableRowsUpdated(ChangeRow<H> row) {
        int firstRow = rowIndexOf(row.header);
        if (row.detail == null) {
            fireTableRowsUpdated(firstRow, firstRow + detailAdapter.getDetailCount(row.header));
        }
        else {
            firstRow += detailAdapter.detailIndex(row.header, row.detail) + 1;
            fireTableRowsUpdated(firstRow, firstRow);
        }
    }

    private ChangeRow<H> newChangeKey(int rowIndex) {
        int beanIndex = getGroupNumber(rowIndex);
        int subRowIndex = rowIndex - getLeadRowForGroup(beanIndex);
        H bean = getBean(beanIndex);
        return new ChangeRow<>(bean, subRowIndex == 0 ? null : detailAdapter.getDetail(bean, subRowIndex-1));
    }

    private class HeaderPredicate implements Predicate<ChangeRow<H>> {
        private final H bean;

        public HeaderPredicate(H bean) {
            this.bean = bean;
        }

        @Override
        public boolean test(ChangeRow<H> input) {
            return input.header == bean;
        }
    }

    private static class ChangeRow<H> {
        public final H header;
        // header row = null
        public final Object detail;

        public ChangeRow(H header, Object detail) {
            this.header = header;
            this.detail = detail;
        }

        public H getHeader() {
            return header;
        }

        @SuppressWarnings("unchecked")
        public boolean equals(Object obj) {
            ChangeRow<H> that = (ChangeRow<H>) obj;
            return this.header == that.header && this.detail == that.detail;
        }

        public int hashCode() {
            return 29 * header.hashCode() + (detail == null ? 0 : detail.hashCode());
        }

        public String toString() {
            return String.format("ChangeRow[%s subrow=%s]", header, detail);
        }
    }
}