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
import java.util.Comparator;
import java.util.List;

import io.github.jonestimd.swing.table.DecoratedTable;
import io.github.jonestimd.swing.table.MixedRowTable;
import io.github.jonestimd.swing.table.model.BeanTableModel;
import io.github.jonestimd.swing.table.model.BufferedHeaderDetailTableModel;
import io.github.jonestimd.swing.table.sort.HeaderDetailTableRowSorter.HeaderDetailViewToModel;

public class HeaderDetailTableRowSorter<H, M extends BufferedHeaderDetailTableModel<H>> extends BeanModelRowSorter<H, M, HeaderDetailViewToModel<H>> implements MixedRowTableRowSorter {
    public HeaderDetailTableRowSorter(MixedRowTable<H, M> table) {
        super(table, new HeaderDetailTableRowComparator<>(table));
    }

    protected HeaderDetailTableRowSorter(MixedRowTable<H, M> table, HeaderDetailTableRowComparator<H> rowComparator) {
        super(table, rowComparator);
    }

    @Override
    public int getViewGroup(int viewIndex) {
        return viewToModel == null ? getModel().getGroupNumber(viewIndex) : viewToModel.get(viewIndex).viewGroup;
    }

    @Override
    public int nextViewGroup(int viewIndex) {
        int viewGroup = getViewGroup(viewIndex);
        while (viewIndex < getViewRowCount()-1 && viewGroup == getViewGroup(++viewIndex));
        return viewIndex;
    }

    @Override
    protected void updateModelIndex(int firstRow, int deltaRows) {
        super.updateModelIndex(firstRow, deltaRows);
        if (deltaRows < 0) {
            ModelChange change = new ModelChange(firstRow, -deltaRows + firstRow - 1, true);
            if (change.isBeanChange()) {
                updateBeanIndex(firstRow, -change.deltaBeans());
            }
        }
    }

    private void updateBeanIndex(int from, int deltaBeans) {
        for (int i = from; i < modelToView.length; i++) {
            if (isVisible(i)) {
                viewToModel.get(modelToView[i]).beanIndex += deltaBeans;
                viewToModel.get(modelToView[i]).viewGroup += deltaBeans;
            }
        }
    }

    @Override
    public void rowsDeleted(int firstRow, int endRow) {
        if (isVisible(firstRow) && isVisible(endRow)) {
            super.rowsDeleted(firstRow, endRow);
        } else {
            allRowsChanged();
        }
    }

    @Override
    public void rowsUpdated(int firstRow, int endRow) {
        if (new ModelChange(firstRow, endRow, false).isBeanChange()) {
            super.rowsUpdated(firstRow, endRow);
        }
    }

    @Override
    public void rowsUpdated(int firstRow, int endRow, int column) {
        if (new ModelChange(firstRow, endRow, false).isBeanChange()) {
            super.rowsUpdated(firstRow, endRow, column);
        }
    }

    @Override
    protected H getBean(int rowIndex) {
        return super.getBean(getModel().getGroupNumber(rowIndex));
    }

    @Override
    protected void postSort() {
        int viewGroup = -1;
        int beanIndex = -1;
        for (HeaderDetailViewToModel<H> row : viewToModel) {
            if (beanIndex != row.beanIndex) {
                beanIndex = row.beanIndex;
                viewGroup++;
            }
            row.viewGroup = viewGroup;
        }
    }

    @Override
    protected List<HeaderDetailViewToModel<H>> newModelRows(int firstRow, int endRow) {
        ModelChange change = new ModelChange(firstRow, endRow, false);
        if (modelToView != null && change.isBeanChange()) {
            updateBeanIndex(firstRow, change.deltaBeans());
        }
        return newModelRows(change.firstBean, change.endBean, change.firstDetail, endRow - firstRow + 1);
    }

    private List<HeaderDetailViewToModel<H>> newModelRows(int firstBean, int lastBean, int firstDetail, int rowCount) {
        List<HeaderDetailViewToModel<H>> modelRows = new ArrayList<>(rowCount);
        for (int i = firstBean; i <= lastBean; i++) {
            for (int j = firstDetail; j < getModel().getRowCount(i) && rowCount > 0; j++, rowCount--) {
                modelRows.add(new HeaderDetailViewToModel<>(i, getModel().getLeadRowForGroup(i) + j, i));
            }
            firstDetail = 0;
        }
        return modelRows;
    }

    public static class HeaderDetailViewToModel<BEAN> implements ViewToModel<BEAN> {
        private int beanIndex;
        private int modelIndex;
        private int viewGroup;

        public HeaderDetailViewToModel(int beanIndex, int modelIndex, int viewGroup) {
            this.beanIndex = beanIndex;
            this.modelIndex = modelIndex;
            this.viewGroup = viewGroup;
        }

        @Override
        public BEAN getBean(BeanTableModel<BEAN> tableModel) {
            return tableModel.getBean(beanIndex);
        }

        public int getBeanIndex() {
            return beanIndex;
        }

        @Override
        public int getModelIndex() {
            return modelIndex;
        }

        @Override
        public void updateModelIndex(int delta) {
            modelIndex += delta;
        }

        public int getViewGroup() {
            return viewGroup;
        }

        public String toString() {
            return String.format("{bean=%d model=%d group=%d", beanIndex, modelIndex, viewGroup);
        }
    }

    protected class ModelChange {
        protected final int firstBean;
        protected final int endBean;
        protected final int firstDetail;

        protected ModelChange(int firstRow, int endRow, boolean delete) {
            if (delete) {
                this.firstBean = viewToModel.get(modelToView[firstRow]).beanIndex;
                this.firstDetail = firstRow - getModel().getLeadRowForGroup(firstBean);
                this.endBean = viewToModel.get(modelToView[endRow]).beanIndex;
            }
            else {
                this.firstBean = getModel().getGroupNumber(firstRow);
                this.firstDetail = getModel().getSubRowIndex(firstRow);
                this.endBean = getModel().getGroupNumber(endRow);
            }
            if (firstRow > endRow || firstBean != endBean && firstDetail > 0) {
                throw new IllegalArgumentException(String.format("Invalid update range: %d, %d", firstRow, endRow));
            }
        }

        public boolean isBeanChange() {
            return firstDetail == 0;
        }

        public int deltaBeans() {
            return endBean - firstBean + 1;
        }
    }

    protected static class HeaderDetailTableRowComparator<H> extends TableRowComparator<H, HeaderDetailViewToModel<H>> {
        private final Comparator<HeaderDetailViewToModel<H>> beanComparator = (row1, row2) ->
                row1.beanIndex == row2.beanIndex ? row1.getModelIndex() - row2.getModelIndex() : 0;

        public HeaderDetailTableRowComparator(DecoratedTable<H, ? extends BeanTableModel<H>> table) {
            super(table);
        }

        @Override
        protected Comparator<HeaderDetailViewToModel<H>> columnComparator(SortKey sortKey) {
            return beanComparator.thenComparing(super.columnComparator(sortKey));
        }
    }
}
