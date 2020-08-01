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

import java.text.Format;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import javax.swing.AbstractListModel;

import io.github.jonestimd.util.Streams;

public class ContainsFilterComboBoxModel<T> extends AbstractListModel<T> implements FilterComboBoxModel<T> {
    private List<T> unfilteredItems;
    private List<T> filteredItems;
    private String filter = "";
    private T selectedItem = null;
    private final Function<T, String> format;

    public ContainsFilterComboBoxModel(Format format) {
        this(new ArrayList<>(), format);
    }

    public ContainsFilterComboBoxModel(List<T> unfilteredItems, Format format) {
        this(unfilteredItems, format::format);
    }

    public ContainsFilterComboBoxModel(List<T> unfilteredItems, Function<T, String> format) {
        this.unfilteredItems = unfilteredItems;
        this.format = format;
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

    /**
     * Set unfiltered list.
     */
    @Override
    public void setElements(Collection<? extends T> elements, boolean keepSelection) {
        unfilteredItems = new ArrayList<>(elements);
        applyFilter();
    }

    @Override
    public boolean contains(T item) {
        return false;
    }

    @Override
    public void setFilter(String search) {
        this.filter = search.toLowerCase();
        applyFilter();
    }

    protected void applyFilter() {
        this.filteredItems = Streams.filter(unfilteredItems, this::isMatch);
        fireContentsChanged(this, 0, Integer.MAX_VALUE);
    }

    private boolean isMatch(T item) {
        return format.apply(item).toLowerCase().contains(filter);
    }

    @Override
    public String getSelectedItemText() {
        return selectedItem == null ? "" : format.apply(selectedItem);
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
        int index = getSize() > 0 && getElementAt(0) == null ? 1 : 0;
        for (T element : items) {
            if (unfilteredItems.indexOf(element) < 0) {
                unfilteredItems.add(index, element);
            }
            index++;
        }
        applyFilter();
    }

    /**
     * Get the index of an item in the filtered list.
     */
    @Override
    public int indexOf(Object item) {
        return filteredItems.indexOf(item);
    }

    @Override
    public T getSelectedItem() {
        return selectedItem;
    }

    @Override
    public void setSelectedItem(T anItem) {
        this.selectedItem = anItem;
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
}
