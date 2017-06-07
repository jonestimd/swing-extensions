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
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.ListModel;

/**
 * A {@link ListModel} that contains beans.
 * @param <T> the class of the beans in the model
 */
public class BeanListModel<T> extends AbstractListModel<T> implements ComboBoxModel<T>, Iterable<T> {
    private final List<T> elements = new ArrayList<>();
    private T selectedElement;

    public BeanListModel() {
    }

    public BeanListModel(Collection<? extends T> elements) {
        this.elements.addAll(elements);
    }

    /**
     * Replace the elements.
     * @param elements the new elements
     * @param keepSelection if true then the selected item is not changed, if false then the selected item is cleared
     *        if it is not in {@code elements}
     */
    public void setElements(Collection<? extends T> elements, boolean keepSelection) {
        removeAll();
        addAllElements(elements);
        if (! keepSelection && selectedElement != null && ! this.elements.contains(selectedElement)) {
            setSelectedItem(null);
        }
    }

    public void removeAllElements() {
        removeAll();
        setSelectedItem(null);
    }

    private void removeAll() {
        int size = this.elements.size();
        if (size > 0) {
            this.elements.clear();
            fireIntervalRemoved(this, 0, size-1);
        }
    }

    public void addElement(T obj) {
        insertElementAt(obj, elements.size());
    }

    public void addAllElements(Collection<? extends T> elements) {
        if (elements.size() > 0) {
            int size = this.elements.size();
            this.elements.addAll(elements);
            fireIntervalAdded(this, size, size+elements.size()-1);
        }
    }

    public void insertElementAt(T obj, int index) {
        elements.add(index, obj);
        fireIntervalAdded(this, index, index);
    }

    public int indexOf(T obj) {
        return elements.indexOf(obj);
    }

    public void removeElement(T obj) {
        int index = elements.indexOf(obj);
        if (elements.remove(obj)) {
            fireIntervalRemoved(this, index, index);
        }
    }

    public void removeElementAt(int index) {
        elements.remove(index);
        fireIntervalRemoved(this, index, index);
    }

    @Override
    public T getSelectedItem() {
        return selectedElement;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setSelectedItem(Object anItem) {
        if (this.selectedElement != anItem) {
            this.selectedElement = (T) anItem;
            fireContentsChanged(this, -1, -1);
        }
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
    public Iterator<T> iterator() {
        return Collections.unmodifiableList(elements).iterator();
    }
}