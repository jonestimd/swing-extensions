// The MIT License (MIT)
//
// Copyright (c) 2020 Timothy D. Jones
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
package io.github.jonestimd.swing.component;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import javax.swing.AbstractListModel;

public class ContainsFilterComboBoxModel<T> extends AbstractListModel<T> implements FilterComboBoxModel<T> {
    private final List<T> unfilteredItems = new ArrayList<>();
    private final List<T> filteredItems = new ArrayList<>();
    private String filter = "";
    private T selectedItem = null;
    private final Function<T, String> format;
    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    public ContainsFilterComboBoxModel(Function<T, String> format) {
        this(new ArrayList<>(), format);
    }

    public ContainsFilterComboBoxModel(List<T> unfilteredItems, Function<T, String> format) {
        this.unfilteredItems.addAll(unfilteredItems);
        this.filteredItems.addAll(unfilteredItems);
        this.format = format;
    }

    @Override
    public Function<T, String> getFormat() {
        return format;
    }

    public List<T> getMatches() {
        return Collections.unmodifiableList(filteredItems);
    }

    /**
     * Get the unfiltered list.
     */
    public List<T> getElements() {
        return Collections.unmodifiableList(unfilteredItems);
    }

    /**
     * Set the unfiltered list and clear selected item if it is not in the new list.
     */
    @Override
    public void setElements(Collection<? extends T> items) {
        setElements(items, false);
    }

    @Override
    public void setElements(Collection<? extends T> elements, boolean keepSelection) {
        unfilteredItems.clear();
        unfilteredItems.addAll(elements);
        if (!keepSelection && !unfilteredItems.contains(selectedItem)) setSelectedItem(null);
        applyFilter();
    }

    /**
     * @return {@code true} if the unfiltered list contains the item
     */
    @Override
    public boolean contains(T item) {
        return unfilteredItems.contains(item);
    }

    @Override
    public void setFilter(String search) {
        String oldValue = this.filter;
        this.filter = search.toLowerCase();
        applyFilter();
        changeSupport.firePropertyChange(FILTER, oldValue, filter);
    }

    protected void applyFilter() {
        filteredItems.clear();
        unfilteredItems.stream().filter(this::isMatch).forEach(filteredItems::add);
        fireContentsChanged(this, 0, Integer.MAX_VALUE);
    }

    private boolean isMatch(T item) {
        return format.apply(item).toLowerCase().contains(filter);
    }

    @Override
    public void addElement(T item) {
        unfilteredItems.add(item);
        if (isMatch(item)) {
            int index = filteredItems.size();
            filteredItems.add(item);
            fireIntervalAdded(this, index, index);
        }
    }

    @Override
    public void addMissingElements(Collection<? extends T> items) {
        int index = 0;
        for (T element : items) {
            if (unfilteredItems.indexOf(element) < 0) {
                unfilteredItems.add(index, element);
            }
            index++;
        }
        applyFilter();
    }

    /**
     * Get the index of an item in the unfiltered list.
     */
    @Override
    public int indexOf(Object item) {
        return unfilteredItems.indexOf(item);
    }

    @Override
    public T getSelectedItem() {
        return selectedItem;
    }

    @Override
    public void setSelectedItem(T anItem) {
        T oldValue = this.selectedItem;
        this.selectedItem = anItem;
        changeSupport.firePropertyChange(SELECTED_ITEM, oldValue, selectedItem);
    }

    @Override
    public int getSelectedItemIndex() {
        return filteredItems.indexOf(selectedItem);
    }

    @Override
    public String getSelectedItemText() {
        return selectedItem == null ? "" : format.apply(selectedItem);
    }

    /**
     * @return an {@link Iterator} for the filtered list.
     */
    @Override
    public Iterator<T> iterator() {
        return getMatches().iterator();
    }

    /**
     * @return the size of the filtered list.
     */
    @Override
    public int getSize() {
        return filteredItems.size();
    }

    /**
     * @param index the index of the item in the filtered list.
     * @return an item from the filtered list.
     */
    @Override
    public T getElementAt(int index) {
        return filteredItems.get(index);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(propertyName, listener);
    }
}
