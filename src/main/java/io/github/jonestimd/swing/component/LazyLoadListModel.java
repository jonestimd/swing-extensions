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

import javax.swing.ListModel;

/**
 * A list model that supports loading the list items lazily.
 * @param <T> the class of the list items
 */
public interface LazyLoadListModel<T> extends ListModel<T>, Iterable<T> {
    /**
     * Replace the items in the list.
     * @param items the new items
     */
    void setElements(Collection<? extends T> items);

    /**
     * Append an item to the list.
     * @param item the item to append
     */
    void addElement(T item);

    /**
     * Append items to the end of the list.
     * @param items the items to append
     */
    void addAllElements(Collection<? extends T> items);

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

    /**
     * Check if an item is in the list.
     * @return true if the item is in the list
     */
    boolean contains(T item);
}
