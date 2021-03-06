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

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import io.github.jonestimd.swing.table.sort.SectionTableRowSorter;
import io.github.jonestimd.util.JavaPredicates;

/**
 * A {@link SectionTableModel} that stores the bean groups in a {@link ListMultimap}.  Groups are displayed in ascending
 * order based on the group name.
 * @param <G> the type of the group keys
 * @param <T> the class representing a row in the table
 * @see SectionTableRowSorter
 */
public class BeanListMultimapTableModel<G, T> extends AbstractTableModel implements ColumnIdentifier, SectionTableModel<T>, BeanTableModel<T> {
    private static final int GROWTH_FACTOR = 10;
    private final BeanTableAdapter<T> beanTableAdapter;
    private final Function<T, G> groupingFunction;
    private final Function<G, String> groupNameFunction;
    private final ListMultimap<G, T> groups = ArrayListMultimap.create();
    private final Comparator<G> groupOrdering;
    private final List<G> sortedGroups = new ArrayList<>();
    private int[] groupOffsets = new int[GROWTH_FACTOR];

    /**
     * Create a new model.
     * @param columnAdapters provides access to column values on the row beans
     * @param tableDataProviders supplemental data providers
     * @param groupingFunction provides the group key for a row
     * @param groupNameFunction provides the display name for a group
     */
    public BeanListMultimapTableModel(List<? extends ColumnAdapter<? super T, ?>> columnAdapters,
                                      Iterable<? extends TableDataProvider<T>> tableDataProviders,
                                      Function<T, G> groupingFunction, Function<G, String> groupNameFunction) {
        this.beanTableAdapter = new BeanTableAdapter<>(this, columnAdapters, tableDataProviders);
        this.groupingFunction = groupingFunction;
        this.groupNameFunction = groupNameFunction;
        groupOrdering = Comparator.comparing(groupNameFunction);
    }

    @Override
    public boolean isSectionRow(int rowIndex) {
        return Arrays.binarySearch(groupOffsets, 0, sortedGroups.size(), rowIndex) >= 0;
    }

    @Override
    public int getSectionRow(int rowIndex) {
        return groupOffsets[getGroupNumber(rowIndex)];
    }

    @Override
    public String getSectionName(int rowIndex) {
        int groupIndex = getGroupNumber(rowIndex);
        return groupNameFunction.apply(sortedGroups.get(groupIndex));
    }

    @Override
    public int getGroupNumber(int rowIndex) {
        int i = Arrays.binarySearch(groupOffsets, 0, sortedGroups.size(), rowIndex);
        return i >= 0 ? i : -i - 2;
    }

    @Override
    public List<T> getGroup(int groupNumber) {
        return groups.get(sortedGroups.get(groupNumber));
    }

    public void setBeans(Collection<T> beans) {
        setBeans(Multimaps.index(beans, groupingFunction::apply));
    }

    public void setBeans(Multimap<G, T> beans) {
        groups.clear();
        groups.putAll(beans);
        sortedGroups.clear();
        sortedGroups.addAll(groups.keySet());
        sortedGroups.sort(groupOrdering);

        groupOffsets = new int[sortedGroups.size() + GROWTH_FACTOR];
        int groupIndex = 0;
        for (G group : sortedGroups) {
            groupOffsets[groupIndex+1] = groupOffsets[groupIndex] + groups.get(group).size() + 1;
            groupIndex++;
        }
        fireTableDataChanged();
        beanTableAdapter.setBeans(beans.values());
    }

    @Override
    public void updateBeans(Collection<T> beans, BiPredicate<T, T> isEqual) {
        for (T bean : beans) {
            int index = indexOf(item -> isEqual.test(bean, item));
            if (index < 0) put(groupingFunction.apply(bean), bean);
            else setBean(index, bean);
        }
    }

    public List<T> getBeans() {
        return Lists.newArrayList(groups.values());
    }

    public List<G> getSections() {
        return Collections.unmodifiableList(sortedGroups);
    }

    public List<T> getBeans(G group) {
        return Collections.unmodifiableList(groups.get(group));
    }

    @Override
    public int getBeanCount() {
        return groups.size();
    }

    @Override
    public T getBean(int rowIndex) {
        if (isSectionRow(rowIndex)) {
            return null;
        }
        int groupNumber = getGroupNumber(rowIndex);
        return groups.get(sortedGroups.get(groupNumber)).get(rowIndex - groupOffsets[groupNumber] - 1);
    }

    protected void setBean(int rowIndex, T bean) {
        int groupNumber = getGroupNumber(rowIndex);
        groups.get(sortedGroups.get(groupNumber)).set(rowIndex - groupOffsets[groupNumber] - 1, bean);
        fireTableRowsUpdated(rowIndex, rowIndex);
    }

    @Override
    public Object getValue(T bean, int columnIndex) {
        return beanTableAdapter.getValue(bean, columnIndex);
    }

    public void addBean(T bean) {
        put(groupingFunction.apply(bean), bean);
    }

    /**
     * Add a bean to a group.
     */
    public void put(G group, T bean) {
        groups.put(group, bean);
        if (! sortedGroups.contains(group)) {
            sortedGroups.add(group);
            sortedGroups.sort(groupOrdering);
            int groupIndex = sortedGroups.indexOf(group);
            groupAdded(groupIndex, 1);
        }
        else {
            int groupIndex = sortedGroups.indexOf(group);
            groupChanged(groupIndex, 1);
            fireTableRowsInserted(groupOffsets[groupIndex+1]-1, groupOffsets[groupIndex+1]-1);
        }
        beanTableAdapter.addBean(bean);
    }

    /**
     * Remove a bean from a group.  If the group is empty then the group is also removed.
     */
    public void remove(int rowIndex) {
        T bean = getBean(rowIndex);
        int groupIndex = getGroupNumber(rowIndex);
        G group = sortedGroups.get(groupIndex);
        if (groups.remove(group, bean)) {
            if (groups.get(group).isEmpty()) {
                groupRemoved(groupIndex, rowIndex - 1, 1);
            }
            else {
                groupChanged(groupIndex, -1);
                fireTableRowsDeleted(rowIndex, rowIndex);
            }
            beanTableAdapter.removeBean(bean);
        }
    }

    /**
     * Remove an entire group.
     * @return the removed beans.
     */
    public List<T> removeAll(G group) {
        int groupIndex = sortedGroups.indexOf(group);
        int groupOffset = groupOffsets[groupIndex];
        List<T> beans = groups.removeAll(group);
        groupRemoved(groupIndex, groupOffset, beans.size());
        return beans;
    }

    /**
     * Add beans to a group.
     */
    public void putAll(G group, Collection<? extends T> beans) {
        groups.putAll(group, beans);
        if (! sortedGroups.contains(group)) {
            sortedGroups.add(group);
            sortedGroups.sort(groupOrdering);
            int groupIndex = sortedGroups.indexOf(group);
            groupAdded(groupIndex, beans.size());
        }
        else {
            int groupIndex = sortedGroups.indexOf(group);
            groupChanged(groupIndex, beans.size());
            fireTableRowsInserted(groupOffsets[groupIndex+1]-beans.size(), groupOffsets[groupIndex+1]-1);
        }
    }

    private void groupRemoved(int groupIndex, int groupOffset, int size) {
        groupChanged(groupIndex, -size - 1);
        sortedGroups.remove(groupIndex);
        System.arraycopy(groupOffsets, groupIndex + 1, groupOffsets, groupIndex, groupOffsets.length - groupIndex - 1);
        fireTableRowsDeleted(groupOffset, groupOffset + size);
    }

    private void groupAdded(int groupIndex, int groupSize) {
        if (sortedGroups.size()+1 > groupOffsets.length) {
            groupOffsets = Arrays.copyOf(groupOffsets, groupOffsets.length + GROWTH_FACTOR);
        }
        for (int i = sortedGroups.size(); i > groupIndex; i--) {
            groupOffsets[i] = groupOffsets[i-1] + 1 + groupSize;
        }
        fireTableRowsInserted(groupOffsets[groupIndex], groupOffsets[groupIndex]+groupSize);
    }

    private void groupChanged(int groupIndex, int delta) {
        for (int i = groupIndex+1; i <= sortedGroups.size(); i++) {
            groupOffsets[i] += delta;
        }
    }

    @Override
    public ColumnAdapter<? super T, ?> getColumnIdentifier(int modelIndex) {
        return beanTableAdapter.getColumnAdapter(modelIndex);
    }

    @Override
    public String getColumnName(int modelIndex) {
        return beanTableAdapter.getColumnName(modelIndex);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return beanTableAdapter.getColumnClass(columnIndex);
    }

    @Override
    public int getRowCount() {
        return sortedGroups.size() + groups.values().size();
    }

    @Override
    public int getColumnCount() {
        return beanTableAdapter.getColumnCount();
    }

    // TODO call from fireEvent methods instead of from sub-class
    protected void notifyDataProviders(T row, String columnId, Object oldValue) {
        beanTableAdapter.notifyDataProviders(row, indexOf(row::equals), columnId, oldValue);
    }

    /**
     * Find the index of a bean matching a predicate.
     * @return the index of the first matching bean or -1 if none match.
     */
    public int indexOf(Predicate<T> predicate) {
        return groups.entries().stream().filter(JavaPredicates.onResult(Entry::getValue, predicate))
                .findFirst().map(this::indexOf)
                .orElse(-1);
    }

    private int indexOf(Entry<G, T> entry) {
        int groupIndex = sortedGroups.indexOf(entry.getKey());
        return groupOffsets[groupIndex] + groups.get(entry.getKey()).indexOf(entry.getValue()) + 1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (isSectionRow(rowIndex)) {
            return null;
        }
        return beanTableAdapter.getValue(getBean(rowIndex), columnIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return beanTableAdapter.isCellEditable(getBean(rowIndex), columnIndex);
    }

    @Override
    public Cursor getCursor(MouseEvent event, JTable table, int rowIndex, int columnIndex) {
        T bean = getBean(rowIndex);
        return bean == null ? null : beanTableAdapter.getColumnAdapter(columnIndex).getCursor(event, table, bean);
    }

    @Override
    public void handleClick(MouseEvent event, JTable table, int rowIndex, int columnIndex) {
        T bean = getBean(rowIndex);
        if (bean != null) beanTableAdapter.getColumnAdapter(columnIndex).handleClick(event, table, bean);
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (isSectionRow(rowIndex)) {
            throw new IllegalArgumentException("Can't edit section row");
        }
        beanTableAdapter.setValue(aValue, getBean(rowIndex), rowIndex, columnIndex);
        fireTableCellUpdated(rowIndex, columnIndex);
    }
}
