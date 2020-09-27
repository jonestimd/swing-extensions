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
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.swing.AbstractListModel;

/**
 * An abstract {@link FilterComboBoxModel} that displays items matching a predicate.  Uses a {@link Function} to convert
 * the list items to a string for display.
 * @param <T> the class of the list items
 */
public abstract class AbstractFilterComboBoxModel<T> extends AbstractListModel<T> implements FilterComboBoxModel<T> {
    private final List<T> unfilteredItems = new ArrayList<>();
    private final List<T> filteredItems = new ArrayList<>();
    private String filter = "";
    private T selectedItem = null;
    private final Predicate<T> isMatch;
    private final Function<T, String> format;
    private final Comparator<T> ordering;
    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    /**
     * Create an empty model.
     * @param format the function for rendering the items
     * @param filterPredicate predicate for filtering the items based on the search text
     */
    public AbstractFilterComboBoxModel(Function<T, String> format, BiPredicate<T, String> filterPredicate) {
        this(new ArrayList<>(), format, filterPredicate);
    }

    /**
     * Create a model populated with a list of items.
     * @param unfilteredItems the list of available items
     * @param format the function for rendering the items
     * @param filterPredicate predicate for filtering the items based on the search text
     */
    public AbstractFilterComboBoxModel(List<T> unfilteredItems, Function<T, String> format, BiPredicate<T, String> filterPredicate) {
        this.isMatch = (item) -> filterPredicate.test(item, filter);
        this.unfilteredItems.addAll(unfilteredItems);
        this.filteredItems.addAll(unfilteredItems);
        this.format = format;
        this.ordering = Comparator.comparing(format.andThen(String::toUpperCase));
    }

    @Override
    public Function<T, String> getFormat() {
        return format;
    }

    public List<T> getMatches() {
        return Collections.unmodifiableList(filteredItems);
    }

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

    public Optional<T> findElement(String text) {
        return filteredItems.stream().filter((item) -> format.apply(item).equalsIgnoreCase(text)).findFirst();
    }

    /**
     * @return {@code true} if the unfiltered list contains the item
     */
    @Override
    public boolean contains(T item) {
        return unfilteredItems.contains(item);
    }

    public String getFilter() {
        return filter;
    }

    @Override
    public void setFilter(String filter) {
        String oldValue = this.filter;
        this.filter = filter;
        applyFilter();
        changeSupport.firePropertyChange(FILTER, oldValue, this.filter);
    }

    protected void applyFilter() {
        filteredItems.clear();
        unfilteredItems.stream().filter(isMatch).forEach(filteredItems::add);
        if (selectedItem != null && indexOf(selectedItem) < 0) filteredItems.add(selectedItem);
        filteredItems.sort(ordering);
        fireContentsChanged(this, 0, Integer.MAX_VALUE);
    }

    @Override
    public void addElement(T item) {
        insertElementAt(item, unfilteredItems.size());
    }

    @Override
    public void insertElementAt(T item, int index) {
        unfilteredItems.add(index, item);
        if (isMatch.test(item)) {
            int filteredIndex = filteredItems.size();
            filteredItems.add(item);
            fireIntervalAdded(this, filteredIndex, filteredIndex);
        }
    }

    @Override
    public void addMissingElements(Collection<? extends T> items) {
        int index = 0;
        for (T element : items) {
            if (!unfilteredItems.contains(element)) {
                unfilteredItems.add(index, element);
            }
            index++;
        }
        applyFilter();
    }

    @Override
    public void removeElementAt(int index) {
        int filteredIndex = filteredItems.indexOf(unfilteredItems.remove(index));
        if (filteredIndex >= 0) {
            filteredItems.remove(filteredIndex);
            fireIntervalRemoved(this, filteredIndex, filteredIndex);
        }
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
     * @return an {@link Iterator} for the unfiltered list.
     */
    @Override
    public Iterator<T> iterator() {
        return getElements().iterator();
    }

    /**
     * @return the size of the filtered list.
     */
    @Override
    public int getSize() {
        return filteredItems.size();
    }

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
