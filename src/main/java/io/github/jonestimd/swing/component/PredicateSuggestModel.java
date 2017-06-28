// The MIT License (MIT)
//
// Copyright (c) 2017 Timothy D. Jones
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.swing.AbstractListModel;

import io.github.jonestimd.util.JavaPredicates;
import io.github.jonestimd.util.Streams;

/**
 * An implementation of {@link SuggestModel} that suggests the items from a list that match a predicate.
 * @param <T> the class of the list items
 */
public class PredicateSuggestModel<T> extends AbstractListModel<T> implements SuggestModel<T>, LazyLoadComboBoxModel<T> {
    private final BiPredicate<T, String> rawFilter;
    private Predicate<T> filter;
    private final List<T> unfilteredItems = new ArrayList<>();
    private final Comparator<? super T> comparator;
    private List<T> filteredItems = new ArrayList<>();
    private T selectedItem;

    /**
     * Create a model that suggests the items that contain the input text, ignoring case.
     * @param converter a function for converting the list items to strings
     * @param comparator a comparator for ordering the list items
     */
    public static <T> PredicateSuggestModel<T> ignoreCase(Function<T, String> converter, Comparator<? super T> comparator) {
        return new PredicateSuggestModel<>((item, text) -> converter.apply(item).toLowerCase().contains(text.toLowerCase()), comparator);
    }

    /**
     * Create a model that suggests the items that contain the input text.
     * @param converter a function for converting the list items to strings
     * @param comparator a comparator for ordering the list items
     */
    public PredicateSuggestModel(Function<T, String> converter, Comparator<? super T> comparator) {
        this((item, text) -> converter.apply(item).contains(text), comparator);
    }

    /**
     * Create a model that suggests the items that match a predicate.
     * @param filter a predicate for suggesting items
     * @param comparator a comparator for ordering the list items
     */
    public PredicateSuggestModel(BiPredicate<T, String> filter, Comparator<? super T> comparator) {
        this(filter, Collections.emptyList(), comparator);
    }

    /**
     * Create a model that suggests the items that match a predicate.
     * @param filter a predicate for suggesting items
     * @param items the unfiltered list of items
     * @param comparator a comparator for ordering the list items
     */
    public PredicateSuggestModel(BiPredicate<T, String> filter, Collection<T> items, Comparator<? super T> comparator) {
        this.rawFilter = filter;
        this.comparator = comparator;
        updateSuggestions("");
        setElements(items, false);
    }

    @Override
    public T updateSuggestions(String editorText) {
        this.filter = editorText.isEmpty() ? JavaPredicates.alwaysTrue() : item -> rawFilter.test(item, editorText);
        applyFilter();
        return filteredItems.isEmpty() ? null : filteredItems.get(0);
    }

    private void applyFilter() {
        if (! filteredItems.isEmpty()) fireIntervalRemoved(this, 0, filteredItems.size()-1);
        this.filteredItems = Streams.filter(unfilteredItems, filter);
        if (! filteredItems.isEmpty()) fireIntervalAdded(this, 0, filteredItems.size()-1);
    }

    /**
     * Set the list of available choices.
     * @param items the unfiltered list of choices
     * @param allowSelectionNotInList if true then the selected item is not changed, if false then the selected item
     *        is cleared if it is not in list of filtered {@code items}
     */
    @Override
    public void setElements(Collection<? extends T> items, boolean allowSelectionNotInList) {
        this.unfilteredItems.clear();
        this.unfilteredItems.addAll(items);
        this.unfilteredItems.sort(comparator);
        applyFilter();
        if (! allowSelectionNotInList && ! filteredItems.contains(selectedItem)) {
            setSelectedItem(null);
        }
    }

    /**
     * Add items to the list of available choices, skipping items that are already in the list.
     * @param items the items to add
     */
    @Override
    public void addMissingElements(Collection<? extends T> items) {
        for (T element : unfilteredItems) {
            if (unfilteredItems.indexOf(element) < 0) {
                unfilteredItems.add(element);
            }
        }
        unfilteredItems.sort(comparator);
        applyFilter();
    }

    /**
     * Add an item to the list of available choices.
     * @param item the item to append to the list
     */
    @Override
    public void addElement(T item) {
        unfilteredItems.add(item);
        unfilteredItems.sort(comparator);
        if (filter.test(item)) applyFilter();
    }

    /**
     * Get the index of an item in the list of available choices.
     * @param item the item to find
     * @return the index of the item or -1 if it is not in the list of available choices
     */
    @Override
    public int indexOf(Object item) {
        return unfilteredItems.indexOf(item);
    }

    /**
     * Create an iterator for the filtered items.
     * @return an iterator for the filtered items
     */
    @Override
    public Iterator<T> iterator() {
        return Collections.unmodifiableList(filteredItems).iterator();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setSelectedItem(Object anItem) {
        this.selectedItem = (T) anItem;
    }

    @Override
    public Object getSelectedItem() {
        return selectedItem;
    }

    /**
     * Get the number of items matching the filter.
     * @return the number of items matching the filter
     */
    @Override
    public int getSize() {
        return filteredItems.size();
    }

    /**
     * Get an item that matches the filter.
     * @param index the index of the item in the filtered list
     * @return the item at the specified index
     */
    @Override
    public T getElementAt(int index) {
        return filteredItems.get(index);
    }
}
