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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.table.AbstractTableModel;

import io.github.jonestimd.collection.HashList;
import io.github.jonestimd.swing.table.sort.BeanModelRowSorter;

/**
 * A {@link MixedRowTableModel} for which the groups contain a header row followed by one or more detail rows.
 * Multiple detail types are supported.  A {@link DetailAdapter} is used to determine a detail's type and to access
 * the detail beans within a group.  A set of {@link ColumnAdapter}s must be provided for the header and for
 * each detail type.  The header {@link ColumnAdapter}s operate on the group bean and the detail {@link ColumnAdapter}s
 * operate on the detail bean returned by the {@link DetailAdapter}.
 * <p>
 * To improve performance, the row indices of the group beans are kept in a lookup table.  The key for this lookup
 * table is provided by a {@link Function}.  {@link Function#identity()} can be used for that purpose if
 * all group instances are guaranteed to be unique (i.e. multiple instances of a group can't exist) or if the group
 * class overrides {@link Object#equals(Object)} and {@link Object#hashCode()}.  <strong>Important:</strong>  The ID
 * of a group must not be modified after the group is added to the model, and any fields used for that ID should not
 * be editable in the UI.  See {@link HashList} for further details.
 * <p>
 * This class implements {@link BeanTableModel} for compatibility with {@link BeanModelRowSorter}.  The implementation
 * of the interface allows sorting and filtering of the group beans in the view.
 *
 * @param <T> the type of the group bean (typically the header)
 */
public class HeaderDetailTableModel<T> extends AbstractTableModel implements MixedRowTableModel, BeanTableModel<T> {
    private static final int GROWTH_FACTOR = 10;
    private final Logger logger = Logger.getLogger(HeaderDetailTableModel.class.getName());

    protected final DetailAdapter<T> detailAdapter;
    private final List<T> beans;
    private List<? extends ColumnAdapter<T, ?>> columnAdapters;
    private List<? extends List<? extends ColumnAdapter<?, ?>>> detailColumnAdapters;

    private int beanCount = 0;
    private int[] rowOffset = new int[GROWTH_FACTOR];
    private int rowCount = 0;

    /**
     * Partial constructor for sub-classes.  The {@link ColumnAdapter}s must be set to complete construction.
     * Mostly intended to allow {@link ColumnAdapter}s to be defined as non-static inner classes of the model.
     * @param detailAdapter the {@link DetailAdapter} for accessing detail rows
     * @param idFunction the {@link Function} for supplying the ID of a group.
     */
    protected HeaderDetailTableModel(DetailAdapter<T> detailAdapter, Function<? super T, ?> idFunction) {
        this.detailAdapter = detailAdapter;
        this.beans = new HashList<>(idFunction);
    }

    /**
     * @param detailAdapter the {@link DetailAdapter} for accessing detail rows
     * @param idFunction the {@link Function} for supplying the ID of a group.
     * @param columnAdapters the {@link ColumnAdapter}s for the group header row
     * @param detailColumnAdapters the {@link ColumnAdapter}s for the group detail rows
     */
    public HeaderDetailTableModel(DetailAdapter<T> detailAdapter, Function<? super T, ?> idFunction,
                                  List<? extends ColumnAdapter<T, ?>> columnAdapters,
                                  List<? extends List<? extends ColumnAdapter<?, ?>>> detailColumnAdapters) {
        this(detailAdapter, idFunction);
        setColumnAdapters(columnAdapters);
        setDetailColumnAdapters(detailColumnAdapters);
    }

    /**
     * Get a header {@link ColumnAdapter}.
     * @param index the column index
     */
    public ColumnAdapter<T, ?> getColumnAdapter(int index) {
        return columnAdapters.get(index);
    }

    /**
     * Set the header {@link ColumnAdapter}s.  Must be called before {@link #setDetailColumnAdapters(List)}.
     */
    protected void setColumnAdapters(List<? extends ColumnAdapter<T, ?>> columnAdapters) {
        this.columnAdapters = columnAdapters;
    }

    /**
     * Set the detail {@link ColumnAdapter}s.  The list must contain a list of {@link ColumnAdapter}s for each
     * detail type.  The list should be ordered to correspond with {@link DetailAdapter#getDetailTypeIndex(Object, int)}.
     * @throws NullPointerException if called before {@link #setColumnAdapters(List)}
     * @throws IllegalArgumentException if the number of adapters for any detail type doesn't match the number of
     * header adapters
     */
    protected void setDetailColumnAdapters(List<? extends List<? extends ColumnAdapter<?, ?>>> detailColumnAdapters) {
        for (List<? extends ColumnAdapter<?, ?>> adapters : detailColumnAdapters) {
            if (adapters.size() != this.columnAdapters.size()) {
                throw new IllegalArgumentException("header and detail column adapter counts don't match");
            }
        }
        this.detailColumnAdapters = detailColumnAdapters;
    }

    @Override
    public int getRowTypeCount() {
        return detailColumnAdapters.size() + 1;
    }

    @Override
    public int getColumnCount() {
        return columnAdapters.size();
    }

    @Override
    public ColumnAdapter<T, ?> getColumnIdentifier(int columnIndex) {
        return columnAdapters.get(columnIndex);
    }

    /**
     * Get the column index for a header {@link ColumnAdapter}.
     * @param identifier a header {@link ColumnAdapter}
     */
    public int getColumnIndex(ColumnAdapter<T, ?> identifier) {
        return columnAdapters.indexOf(identifier);
    }

    /**
     * Get the column index for a detail {@link ColumnAdapter}.
     * @param subRowType the detail type index
     * @param adapter the detail {@link ColumnAdapter}
     */
    public int getDetailColumnIndex(int subRowType, ColumnAdapter<?, ?> adapter) {
        return detailColumnAdapters.get(subRowType).indexOf(adapter);
    }

    /**
     * Get the group beans.
     */
    public List<T> getBeans() {
        return Collections.unmodifiableList(beans);
    }

    /**
     * Get the number of groups.
     */
    @Override
    public int getBeanCount() {
        return beans.size();
    }

    /**
     * Replace the list of groups.  The groups will be added in iteration order.
     */
    public void setBeans(Collection<T> beans) {
        this.beans.clear();
        this.beans.addAll(beans);
        updateRowOffsets(0);
        fireTableDataChanged();
    }

    @Override
    public void updateBeans(Collection<T> beans, BiPredicate<T, T> isEqual) {
        for (T bean : beans) {
            int index = indexOf(item -> isEqual.test(bean, item));
            if (index < 0) addBean(getBeanCount(), bean);
            else setBean(index, bean);
        }
    }

    /**
     * Get the group bean at the specified index.
     * @param index the index of the group in the bean list
     */
    @Override
    public T getBean(int index) {
        return beans.get(index);
    }

    /**
     * Get the group at the specified row.
     * @param rowIndex the index of a row in the table
     */
    public T getBeanAtRow(int rowIndex) {
        final int index = getGroupNumber(rowIndex);
        return index >= 0 && index < beanCount ? beans.get(index) : null;
    }

    /**
     * Insert a group in the table at the specified group index.
     * @param index the insertion point in the list of groups
     * @param bean the group to add
     */
    public void addBean(int index, T bean) {
        beans.add(index, bean);
        int firstRow = getLeadRowForGroup(index);
        int subRowCount = detailAdapter.getDetailCount(bean);
        fireTableRowsInserted(firstRow, firstRow + subRowCount);
    }

    /**
     * Overridden to update group row offsets.
     */
    @Override
    public void fireTableRowsInserted(int firstRow, int lastRow) {
        updateRowOffsets(firstRow == 0 ? 0 : getGroupNumber(firstRow - 1));
        super.fireTableRowsInserted(firstRow, lastRow);
    }

    /**
     * Overridden to update group row offsets.
     */
    @Override
    public void fireTableRowsDeleted(int firstRow, int lastRow) {
        updateRowOffsets(getGroupNumber(firstRow));
        super.fireTableRowsDeleted(firstRow, lastRow);
    }

    /**
     * Replace a group in the table.
     * @param index the index in the list of groups.
     * @param bean the replacement group
     */
    public void setBean(int index, T bean) {
        T oldBean = beans.set(index, bean);
        int oldDetailCount = detailAdapter.getDetailCount(oldBean);
        int firstRow = getLeadRowForGroup(index);
        int newDetailCount = detailAdapter.getDetailCount(bean);
        if (oldDetailCount > newDetailCount) {
            fireTableRowsDeleted(firstRow + newDetailCount + 1, firstRow + oldDetailCount);
        }
        else if (oldDetailCount < newDetailCount) {
            fireTableRowsInserted(firstRow + oldDetailCount + 1, firstRow + newDetailCount);
        }
        fireTableRowsUpdated(firstRow, firstRow + newDetailCount); // TODO move down to HeaderDetailTableModel?
    }

    /**
     * Remove a group from the table.
     * @param bean the group to remove
     */
    public void removeBean(T bean) {
        int beanIndex = indexOf(bean);
        if (beanIndex >= 0) {
            int firstRow = getLeadRowForGroup(beanIndex);
            beans.remove(beanIndex);
            fireTableRowsDeleted(firstRow, firstRow + detailAdapter.getDetailCount(bean));
        }
    }

    /**
     * Clear the table data.
     */
    public void removeAll(List<T> rowBeans) {
        rowBeans.forEach(this::removeBean);
    }

    /**
     * Update the group row offsets starting with the specified group.
     * @param beanIndex the index of the first group to be updated
     */
    protected void updateRowOffsets(int beanIndex) {
        rowCount = beanIndex == beanCount ? rowCount : rowOffset[beanIndex];
        beanCount = beans.size();
        if (beanCount > rowOffset.length) {
            rowOffset = Arrays.copyOf(rowOffset, beanCount + GROWTH_FACTOR);
        }
        for (int i=beanIndex; i<beanCount; i++) {
            rowOffset[i] = rowCount;
            rowCount += detailAdapter.getDetailCount(getBean(i)) + 1;
        }
    }

    @Override
    public int getRowCount(int beanIndex) {
        return detailAdapter.getDetailCount(beans.get(beanIndex)) + 1;
    }

    /**
     * Get thd index of a group in the list of groups.
     * @param bean the group
     */
    public int indexOf(T bean) {
        return beans.indexOf(bean);
    }

    public int indexOf(Predicate<T> condition) {
        return beans.stream().filter(condition).findFirst().map(this::indexOf).orElse(-1);
    }

    /**
     * Get the row index of the header of a group.
     * @param bean the group
     */
    public int rowIndexOf(T bean) {
        return getLeadRowForGroup(indexOf(bean));
    }

    @Override
    public int getLeadRowForGroup(int beanIndex) {
        return beanIndex < beanCount ? rowOffset[beanIndex] : rowCount;
    }

    @Override
    public int getRowCount() {
        return rowCount;
    }

    @Override
    public Class<?> getCellClass(int rowIndex, int columnIndex) {
        return getColumnClass(getRowTypeIndex(rowIndex), columnIndex);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnAdapters.get(columnIndex).getType();
    }

    @Override
    public Class<?> getColumnClass(int typeIndex, int columnIndex) {
        return typeIndex == 0 ? getColumnClass(columnIndex) : getDetailColumnAdapter(typeIndex-1, columnIndex).getType();
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnAdapters.get(columnIndex).getName();
    }

    @Override
    public String getColumnName(int typeIndex, int columnIndex) {
        return typeIndex == 0 ? getColumnName(columnIndex) : getDetailColumnAdapter(typeIndex-1, columnIndex).getName();
    }

    @Override
    public ColumnAdapter<?, ?> getColumnIdentifier(int typeIndex, int columnIndex) {
        return typeIndex == 0 ? getColumnIdentifier(columnIndex) : getDetailColumnAdapter(typeIndex-1, columnIndex);
    }

    /**
     * @return true if the row is a detail or false if it is a header
     */
    public boolean isSubRow(int rowIndex) {
        return Arrays.binarySearch(rowOffset, 0, beanCount, rowIndex) < 0;
    }

    @Override
    public int getGroupNumber(int rowIndex) {
        int i = Arrays.binarySearch(rowOffset, 0, beanCount, rowIndex);
        return i >= 0 ? i : -i - 2;
    }

    @Override
    public int getSubRowIndex(int rowIndex) {
        int i = Arrays.binarySearch(rowOffset, 0, beanCount, rowIndex);
        return i >= 0 ? 0 : rowIndex - rowOffset[-i - 2];
    }

    @Override
    public int getRowTypeIndex(int rowIndex) {
        int subRowIndex = getSubRowIndex(rowIndex);
        return subRowIndex == 0 ? 0 : detailAdapter.getDetailTypeIndex(getBeanAtRow(rowIndex), subRowIndex-1) + 1;
    }

    @SuppressWarnings("unchecked")
    protected ColumnAdapter<Object, Object> getDetailColumnAdapter(int typeIndex, int columnIndex) {
        return (ColumnAdapter<Object, Object>) detailColumnAdapters.get(typeIndex).get(columnIndex);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        int i = Arrays.binarySearch(rowOffset, 0, beanCount, rowIndex);
        if (i >= 0) {
            return getValue(beans.get(i), columnIndex);
        }
        int beanIndex = -i - 2;
        T bean = getBean(beanIndex);
        int subRowIndex = rowIndex - rowOffset[beanIndex];
        return getDetailValueAt(detailAdapter.getDetail(bean, subRowIndex - 1), getRowTypeIndex(rowIndex)-1, columnIndex);
    }

    /**
     * Get the detail value for the specified detail type and column.
     */
    protected Object getDetailValueAt(Object detail, int typeIndex, int columnIndex) {
        Object value = null;
        try {
            value = getDetailColumnAdapter(typeIndex, columnIndex).getValue(detail);
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, "Failed to get detail column value: {0}", ex.getMessage());
        }
        return value;
    }

    @Override
    public Object getValue(T bean, int columnIndex) {
        Object value = null;
        try {
            value = columnAdapters.get(columnIndex).getValue(bean);
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, "Failed to get column value: {0}", ex.getMessage());
        }
        return value;
    }

    /**
     * Delegates to the header/detail column adapters.
     * @return the result of {@link ColumnAdapter#isEditable(Object)}
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        int i = Arrays.binarySearch(rowOffset, 0, beanCount, rowIndex);
        if (i >= 0) {
            return columnAdapters.get(columnIndex).isEditable(beans.get(i));
        }
        int subRowIndex = rowIndex - rowOffset[-i - 2];
        return getDetailColumnAdapter(getRowTypeIndex(rowIndex)-1, columnIndex)
                .isEditable(detailAdapter.getDetail(getBean(-i - 2), subRowIndex-1));
    }

    /**
     * Set a value on a header or detail.
     * @param value the cell value
     * @param rowIndex the table row index
     * @param columnIndex the table column index
     */
    @SuppressWarnings("unchecked")
    protected void setCellValue(Object value, int rowIndex, int columnIndex) {
        int i = Arrays.binarySearch(rowOffset, 0, beanCount, rowIndex);
        if (i >= 0) {
            ((ColumnAdapter<T, Object>) columnAdapters.get(columnIndex)).setValue(beans.get(i), value);
        }
        else {
            int subRowIndex = rowIndex - rowOffset[-i - 2];
            getDetailColumnAdapter(getRowTypeIndex(rowIndex)-1, columnIndex)
                    .setValue(detailAdapter.getDetail(getBean(-i - 2), subRowIndex - 1), value);
        }
        fireTableCellUpdated(rowIndex, columnIndex);
        // validate other columns
        for (int column = 0; column < getColumnCount(); column++) {
            if (column != columnIndex) {
                fireTableCellUpdated(rowIndex, column);
            }
        }
    }
}
