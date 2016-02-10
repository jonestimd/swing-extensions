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
package io.github.jonestimd.swing.table.sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.swing.RowSorter;
import javax.swing.event.TableModelEvent;

import io.github.jonestimd.swing.table.DecoratedTable;
import io.github.jonestimd.swing.table.model.BeanTableModel;
import io.github.jonestimd.util.JavaPredicates;
import io.github.jonestimd.util.Streams;

public abstract class BeanModelRowSorter<BEAN, MODEL extends BeanTableModel<BEAN>, V extends ViewToModel<BEAN>> extends RowSorter<MODEL> {
    private final Function<V, BEAN> getBean = new Function<V, BEAN>() {
        @Override
        public BEAN apply(V input) {
            return input.getBean(table.getModel());
        }
    };
    private DecoratedTable<BEAN, MODEL> table;
    private final TableRowComparator<BEAN, V> comparator;
    private Predicate<BEAN> modelFiter = null;
    private Predicate<V> viewFiter = JavaPredicates.alwaysTrue();
    protected List<V> viewToModel;
    protected int[] modelToView;

    protected BeanModelRowSorter(DecoratedTable<BEAN, MODEL> table, TableRowComparator<BEAN, V> comparator) {
        this.table = table;
        this.comparator = comparator;
    }

    public MODEL getModel() {
        return table.getModel();
    }

    public void setRowFilter(Predicate<BEAN> rowFilter) {
        modelFiter = rowFilter;
        viewFiter = rowFilter == null ? JavaPredicates.alwaysTrue() : createViewFilter(rowFilter);
        sort(buildViewToModel());
    }

    protected Predicate<V> createViewFilter(Predicate<BEAN> rowFilter) {
        return JavaPredicates.onResult(getBean, rowFilter);
    }

    public void toggleSortOrder(int column) {
        comparator.toggleSortOrder(column);
        sort(viewToModel == null ? buildViewToModel() : viewToModelAsInts());
    }

    public int convertRowIndexToModel(int index) {
        return viewToModel == null || index < 0 || index >= viewToModel.size() ? index : viewToModel.get(index).getModelIndex();
    }

    public int convertRowIndexToView(int index) {
        return modelToView == null ? index : modelToView[index];
    }

    public void setSortKeys(List<? extends SortKey> keys) {
        if (keys != null && ! keys.isEmpty()) {
            comparator.setSortKeys(keys);
            sort(viewToModel == null ? buildViewToModel() : viewToModelAsInts());
        }
        else {
            reset();
        }
        fireSortOrderChanged();
    }

    public List<? extends SortKey> getSortKeys() {
        return comparator.getSortKeys();
    }

    public int getViewRowCount() {
        return viewToModel == null ? getModelRowCount() : viewToModel.size();
    }

    public int getModelRowCount() {
        return table.getModel().getRowCount();
    }

    public void modelStructureChanged() {
        reset();
    }

    protected void reset() {
        comparator.reset();
        viewToModel = null;
        modelToView = null;
    }

    public void allRowsChanged() {
        if (! comparator.isEmpty() || modelFiter != null) {
            sort(buildViewToModel());
        }
    }

    public void rowsInserted(int firstRow, int endRow) {
        if (! comparator.isEmpty() || viewToModel != null) {
            int[] oldViewToModel = viewToModelAsInts();
            int deltaRows = endRow - firstRow + 1;
            updateModelIndex(firstRow, deltaRows);
            List<V> addedRows = newModelRows(firstRow, endRow);
            Collections.sort(addedRows, comparator);
            insertInOrder(addedRows.stream().filter(viewFiter)::iterator);
            buildModelToView();
            fireRowSorterChanged(oldViewToModel);
        }
    }

    private void insertInOrder(Iterable<V> addedRows) {
        int viewIndex = 0;
        for (V added : addedRows) {
            while (viewIndex < viewToModel.size() && comparator.compare(added, viewToModel.get(viewIndex)) > 0) {
                viewIndex++;
            }
            viewToModel.add(viewIndex++, added);
        }
    }

    private int[] viewToModelAsInts() {
        if (viewToModel == null || viewToModel.isEmpty()) {
            return null;
        }
        int[] modelIndexes = new int[viewToModel.size()];
        for (int i = 0; i < viewToModel.size(); i++) {
            modelIndexes[i] = viewToModel.get(i).getModelIndex();
        }
        return modelIndexes;
    }

    protected abstract List<V> newModelRows(int firstRow, int lastRow);

    protected void updateModelIndex(int firstRow, int deltaRows) {
        for (int i = firstRow; i < modelToView.length; i++) {
            if (isVisible(i)) {
                viewToModel.get(modelToView[i]).updateModelIndex(deltaRows);
            }
        }
    }

    protected boolean isVisible(int modelRow) {
        return modelToView[modelRow] != -1;
    }

    public void rowsDeleted(int firstRow, int endRow) {
        if (! comparator.isEmpty() || viewToModel != null) {
            int[] oldViewToModel = viewToModelAsInts();
            int deltaRows = endRow - firstRow + 1;
            updateModelIndex(firstRow, -deltaRows);
            viewToModel.subList(modelToView[firstRow], modelToView[endRow]+1).clear();
            viewToModel.removeIf(viewFiter.negate());
            buildModelToView();
            fireRowSorterChanged(oldViewToModel);
        }
    }

    public void rowsUpdated(int firstRow, int endRow) {
        rowsUpdated(firstRow, endRow, TableModelEvent.ALL_COLUMNS);
    }

    public void rowsUpdated(int firstRow, int endRow, int column) {
        boolean filterChange = modelFiter != null && isFilterChange(firstRow, endRow);
        if (comparator.isSorted(column) || filterChange) {
            modelToView = null;
            sort(filterChange ? buildViewToModel() : viewToModelAsInts());
        }
    }

    private boolean isFilterChange(int firstRow, int endRow) {
        boolean filterChange = false;
        for (int i = firstRow; i <= endRow && ! filterChange; i++) {
            filterChange = (modelToView[i] == -1) == modelFiter.test(getBean(i));
        }
        return filterChange;
    }

    protected BEAN getBean(int rowIndex) {
        return table.getModel().getBean(rowIndex);
    }

    private void sort(int[] oldViewToModel) {
        if (! comparator.isEmpty()) {
            Collections.sort(viewToModel, comparator);
            postSort();
        }
        buildModelToView();
        fireRowSorterChanged(oldViewToModel);
    }

    protected abstract void postSort();

    private int[] buildViewToModel() {
        int[] oldViewToModel = viewToModelAsInts();
        viewToModel = getModelRowCount() == 0 ? new ArrayList<>() :
                Streams.filter(newModelRows(0, getModelRowCount() - 1), viewFiter);
        return oldViewToModel;
    }

    private void buildModelToView() {
        modelToView = new int[getModelRowCount()];
        Arrays.fill(modelToView, -1);
        for (int i = 0; i < viewToModel.size(); i++) {
            modelToView[viewToModel.get(i).getModelIndex()] = i;
        }
    }
}
