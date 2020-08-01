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
package io.github.jonestimd.swing.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractListModel;

import io.github.jonestimd.swing.component.LazyLoadListModel;

/**
 * A list model that contains beans.
 * @param <T> the class of the list items
 */
public class BeanListModel<T> extends AbstractListModel<T> implements LazyLoadListModel<T> {
    private final List<T> elements = new ArrayList<>();

    public BeanListModel() {
    }

    public BeanListModel(Collection<? extends T> elements) {
        this.elements.addAll(elements);
    }

    /**
     * Replace the items in the list.
     * @param items the new items
     */
    @Override
    public void setElements(Collection<? extends T> items) {
        this.elements.clear();
        this.elements.addAll(items);
        fireContentsChanged(this, 0, Integer.MAX_VALUE);
    }

    @Override
    public void addElement(T item) {
        int index = elements.size();
        elements.add(item);
        fireIntervalAdded(this, index, index);
    }

    @Override
    public void addMissingElements(Collection<? extends T> elements) {
        int index = getSize() > 0 && getElementAt(0) == null ? 1 : 0;
        for (T element : elements) {
            if (indexOf(element) < 0) {
                insertElementAt(element, index);
            }
            index++;
        }
    }

    /**
     * Insert an item in the list at a specific index.
     * @param item the item to insert
     * @param index the index at which to add the item
     */
    public void insertElementAt(T item, int index) {
        elements.add(index, item);
        fireIntervalAdded(this, index, index);
    }

    /**
     * Get the index of an item in the list.
     * @return the index of the item or -1 if it is not in the list
     */
    @Override
    public int indexOf(Object item) {
        return elements.indexOf(item);
    }

    /**
     * Clear the list of items.
     */
    public void removeAll() {
        int size = this.elements.size();
        if (size > 0) {
            this.elements.clear();
            fireIntervalRemoved(this, 0, size-1);
        }
    }

    /**
     * Remove an item from the list.
     * @param item the item to remove
     */
    public void removeElement(Object item) {
        int index = indexOf(item);
        if (index >= 0) {
            removeElementAt(index);
        }
    }

    /**
     * Remove an item from the list.
     * @param index the index of the it
     */
    public void removeElementAt(int index) {
        elements.remove(index);
        fireIntervalRemoved(this, index, index);
    }

    @Override
    public T getElementAt(int index) {
        return elements.get(index);
    }

    @Override
    public int getSize() {
        return elements.size();
    }

    @Override
    public boolean contains(T item) {
        return elements.contains(item);
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.unmodifiableList(elements).iterator();
    }
}
