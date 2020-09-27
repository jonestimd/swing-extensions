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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.swing.JList;
import javax.swing.ListModel;

import io.github.jonestimd.beans.ObservableBean;

/**
 * Interface for the model used by {@link FilterComboBox}. The {@link #getSize()} and {@link #getElementAt(int)}
 * methods support displaying the filtered list in a {@link JList} and, therefore, operate on the filtered list.
 * All other methods inherited from {@link LazyLoadListModel} operate on the unfiltered list.
 * @param <T> class of the list items
 */
public interface FilterComboBoxModel<T> extends LazyLoadListModel<T>, ObservableBean {
    String FILTER = "filter";
    String SELECTED_ITEM = "selectedItem";

    /**
     * Replace the items in the list.
     * @param items the new items
     * @param keepSelection if true then the selected item is not changed, if false then the selected item is cleared
     *        if it is not in {@code items}
     */
    void setElements(Collection<? extends T> items, boolean keepSelection);

    /**
     * Add an item to the unfiltered list.
     */
    void insertElementAt(T item, int index);

    /**
     * Remove an item from the unfiltered list.
     */
    void removeElementAt(int index);

    /**
     * Update the filtered items.
     * @param search the text to match
     */
    void setFilter(String search);

    /**
     * @return the item that matches {@code text} (ignoring case)
     */
    Optional<T> findElement(String text);

    /**
     * @return the items that match the filter
     */
    List<T> getMatches();

    /**
     * @return the unfiltered list
     */
    List<T> getElements();

    /**
     * Implementation of {@link ListModel} for displaying the items in a {@link JList}.
     * @return the size of the filtered list.
     */
    @Override
    int getSize();

    /**
     * Implementation of {@link ListModel} for displaying the items in a {@link JList}.
     * @param index the index of the item in the filtered list.
     * @return an item from the filtered list.
     */
    @Override
    T getElementAt(int index);

    T getSelectedItem();

    void setSelectedItem(T item);

    /**
     * @return the index of the selected item in the filtered list or -1 if it is not in the list
     */
    int getSelectedItemIndex();

    /**
     * @return the display value for the selected item
     */
    String getSelectedItemText();

    /**
     * @return the function used to convert an item to its display value
     */
    Function<T, String> getFormat();
}
