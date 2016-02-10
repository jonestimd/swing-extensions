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
import java.util.List;
import java.util.function.Predicate;

import io.github.jonestimd.swing.table.DecoratedTable;
import io.github.jonestimd.swing.table.model.BeanTableModel;
import io.github.jonestimd.swing.table.model.SectionTableModel;
import io.github.jonestimd.swing.table.sort.SectionTableRowSorter.ViewRow;

public class SectionTableRowSorter<BEAN, MODEL extends SectionTableModel<BEAN> & BeanTableModel<BEAN>> extends BeanModelRowSorter<BEAN, MODEL, ViewRow<BEAN>> {
    public static <T, M extends SectionTableModel<T> & BeanTableModel<T>> SectionTableRowSorter<T, M> create(DecoratedTable<T, M> table) {
        return new SectionTableRowSorter<>(table);
    }

    public SectionTableRowSorter(DecoratedTable<BEAN, MODEL> table) {
        super(table, new SectionTableRowComparator<>(table));
    }

    @Override
    protected Predicate<ViewRow<BEAN>> createViewFilter(final Predicate<BEAN> rowFilter) {
        final Predicate<ViewRow<BEAN>> viewFilter = super.createViewFilter(rowFilter);
        return input -> {
            if (input.sectionHeader) {
                return getModel().getGroup(input.groupNumber).stream().filter(rowFilter).findAny().isPresent();
            }
            return viewFilter.test(input);
        };
    }

    @Override
    public void rowsDeleted(int firstRow, int endRow) {
        if (isVisible(firstRow) && isVisible(endRow)) {
            super.rowsDeleted(firstRow, endRow);
        }
        else {
            allRowsChanged();
        }
    }

    protected void updateModelIndex(int firstRow, int deltaRows) {
        super.updateModelIndex(firstRow, deltaRows);
        if (firstRow < modelToView.length) {
            if (deltaRows < 0) {
                if (viewToModel.get(modelToView[firstRow]).sectionHeader) {
                    updateGroupNumber(firstRow, -1);
                }
            }
            else if (getModel().isSectionRow(firstRow)) {
                updateGroupNumber(firstRow, 1);
            }
        }
    }

    private void updateGroupNumber(int from, int deltaGroup) {
        for (int i = from; i < modelToView.length; i++) {
            if (isVisible(i)) {
                viewToModel.get(modelToView[i]).groupNumber += deltaGroup;
            }
        }
    }

    protected List<ViewRow<BEAN>> newModelRows(int firstRow, int lastRow) {
        List<ViewRow<BEAN>> modelRows = new ArrayList<>(lastRow - firstRow + 1);
        int sectionRow = getModel().getSectionRow(firstRow);
        if (modelToView != null && firstRow != sectionRow && ! isVisible(sectionRow)) {
            modelRows.add(new ViewRow<>(getModel(), sectionRow));
        }
        for (int i = firstRow; i <= lastRow; i++) {
            modelRows.add(new ViewRow<>(getModel(), i));
        }
        return modelRows;
    }

    @Override
    protected void postSort() {
    }

    protected static class ViewRow<BEAN> implements ViewToModel<BEAN> {
        private final boolean sectionHeader;
        private int groupNumber;
        private int modelIndex;

        public ViewRow(SectionTableModel tableModel, int modelIndex) {
            this.sectionHeader = tableModel.isSectionRow(modelIndex);
            this.groupNumber = tableModel.getGroupNumber(modelIndex);
            this.modelIndex = modelIndex;
        }

        public BEAN getBean(BeanTableModel<BEAN> tableModel) {
            return tableModel.getBean(modelIndex);
        }

        @Override
        public int getModelIndex() {
            return modelIndex;
        }

        @Override
        public void updateModelIndex(int delta) {
            modelIndex += delta;
        }

        public String toString() {
            return String.format("{%b, group=%d model=%d}", sectionHeader, groupNumber, modelIndex);
        }
    }

    protected static class SectionTableRowComparator<BEAN> extends TableRowComparator<BEAN, ViewRow<BEAN>> {
        public SectionTableRowComparator(DecoratedTable<BEAN, ? extends BeanTableModel<BEAN>> table) {
            super(table);
        }

        @Override
        public int compare(ViewRow<BEAN> row1, ViewRow<BEAN> row2) {
            if (row1.groupNumber == row2.groupNumber) {
                if (row1.sectionHeader) {
                    return row2.sectionHeader ? 0 : -1;
                }
                else if (row2.sectionHeader) {
                    return 1;
                }
                return super.compare(row1, row2);
            }
            return row1.groupNumber - row2.groupNumber;
        }
    }
}
