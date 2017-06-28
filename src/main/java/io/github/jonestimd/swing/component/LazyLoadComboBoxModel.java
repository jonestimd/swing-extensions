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

import java.util.Collection;

import javax.swing.ComboBoxModel;

/**
 * A combo box model that supports loading the list items lazily.
 * @param <T> the class of the combo box items
 */
public interface LazyLoadComboBoxModel<T> extends ComboBoxModel<T>, Iterable<T> {
    /**
     * Replace the items in the list.
     * @param items the new items
     * @param keepSelection if true then the selected item is not changed, if false then the selected item is cleared
     *        if it is not in {@code items}
     */
    void setElements(Collection<? extends T> items, boolean keepSelection);

    /**
     * Append an item to the list.
     * @param item the item to append
     */
    void addElement(T item);

    /**
     * Add items to the list, skipping items that are already in the list.
     * @param items the items to add
     */
    void addMissingElements(Collection<? extends T> items);

    /**
     * Get the index of an item in the list.
     * @return the index of the item or -1 if it is not in the list
     */
    int indexOf(Object item);
}
