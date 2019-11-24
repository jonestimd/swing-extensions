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
package io.github.jonestimd.swing.table.sort;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.event.TableModelEvent;

import io.github.jonestimd.swing.table.DecoratedTable;
import io.github.jonestimd.swing.table.model.BeanTableModel;

/**
 * This class provides a comparator for sorting rows in a {@link DecoratedTable} that uses a {@link BeanTableModel}.
 * @param <Bean> The class of the beans in the table model
 * @param <VTM> The class that maps the table row index to the model row index
 */
public class TableRowComparator<Bean, VTM extends ViewToModel<Bean>> implements Comparator<VTM> {
    private static final Comparator<ViewToModel<?>> MODEL_INDEX_COMPARATOR = Comparator.comparingInt(ViewToModel::getModelIndex);
    private final Logger logger = Logger.getLogger(TableRowComparator.class.getName());
    private final DecoratedTable<Bean, ? extends BeanTableModel<Bean>> table;
    private final List<SortKey> sortKeys = new ArrayList<>();
    private Comparator<? super VTM> delegate = MODEL_INDEX_COMPARATOR;

    public TableRowComparator(DecoratedTable<Bean, ? extends BeanTableModel<Bean>> table) {
        this.table = table;
    }

    @Override
    public int compare(VTM row1, VTM row2) {
        return delegate.compare(row1, row2);
    }

    public List<SortKey> getSortKeys() {
        return Collections.unmodifiableList(sortKeys);
    }

    public void setSortKeys(List<? extends SortKey> keys) {
        this.sortKeys.clear();
        if (keys != null && ! keys.isEmpty()) {
            this.sortKeys.addAll(keys);
            updateDelegate();
        }
    }

    public void toggleSortOrder(int column) {
        if (sortKeys.stream().noneMatch(isColumn(column))) {
            setSortKeys(Collections.singletonList(new SortKey(column, SortOrder.ASCENDING)));
        }
        else {
            sortKeys.replaceAll(key -> key.getColumn() == column ? new SortKey(column, invert(key.getSortOrder())) : key);
            updateDelegate();
        }
    }

    private Predicate<SortKey> isColumn(int column) {
        return key -> key.getColumn() == column;
    }

    public boolean isEmpty() {
        return sortKeys.isEmpty();
    }

    public boolean isSorted(int column) {
        return !isEmpty() && (column == TableModelEvent.ALL_COLUMNS || sortKeys.stream().anyMatch(isColumn(column)));
    }

    public void reset() {
        sortKeys.clear();
        updateDelegate();
    }

    private void updateDelegate() {
        if (sortKeys.isEmpty()) {
            delegate = MODEL_INDEX_COMPARATOR;
        }
        else {
            delegate = sortKeys.stream().map(this::columnComparator).reduce(Comparator::thenComparing).get();
        }
    }

    private SortOrder invert(SortOrder order) {
        return SortOrder.values()[1 - order.ordinal()];
    }

    protected Comparator<VTM> columnComparator(SortKey sortKey) {
        return new ColumnComparator(sortKey);
    }

    private class ColumnComparator implements Comparator<VTM> {
        private final SortOrder sortOrder;
        private final Comparator<? super Bean> comparator;

        private ColumnComparator(SortKey sortKey) {
            this.sortOrder = sortKey.getSortOrder();
            BeanTableModel<Bean> model = table.getModel();
            if (!Comparable.class.isAssignableFrom(model.getColumnClass(sortKey.getColumn()))) {
                logger.log(Level.WARNING, "using toString() for {0}", model.getColumnClass(sortKey.getColumn()).getSimpleName());
                comparator = Comparator.comparing(asString(sortKey.getColumn()), Comparator.nullsFirst(Collator.getInstance()));
            }
            else {
                comparator = Comparator.comparing(asComparable(sortKey.getColumn()), Comparator.nullsFirst(Comparator.naturalOrder()));
            }
        }

        @Override
        public int compare(VTM r1, VTM r2) {
            if (sortOrder == SortOrder.UNSORTED) {
                return r1.getModelIndex() - r2.getModelIndex();
            }
            BeanTableModel<Bean> model = table.getModel();
            return sortOrder == SortOrder.DESCENDING
                    ? comparator.compare(r2.getBean(model), r1.getBean(model))
                    : comparator.compare(r1.getBean(model), r2.getBean(model));
        }
    }

    @SuppressWarnings("unchecked")
    private Function<Bean, Comparable<? super Comparable<?>>> asComparable(int column) {
        return row -> (Comparable<? super Comparable<?>>) table.getModel().getValue(row, column);
    }

    private Function<Bean, String> asString(int column) {
        return row -> Objects.toString(table.getModel().getValue(row, column), null);
    }
}
