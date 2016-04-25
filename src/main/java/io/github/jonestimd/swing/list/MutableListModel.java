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
package io.github.jonestimd.swing.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractListModel;

/**
 * A list model that allows the entire list to be replaced.
 * @param <T> the class of the list items
 */
public class MutableListModel<T> extends AbstractListModel<T> implements Iterable<T> {
    private List<T> elements = new ArrayList<T>();

    public void setElements(List<T> elements) {
        this.elements.clear();
        this.elements.addAll(elements);
        fireContentsChanged(this, 0, Integer.MAX_VALUE);
    }

    public void addElement(T element) {
        int index = elements.size();
        elements.add(element);
        fireIntervalAdded(this, index, index);
    }

    public void removeElement(int index) {
        elements.remove(index);
        fireIntervalRemoved(this, index, index);
    }

    public T getElementAt(int index) {
        return elements.get(index);
    }

    public int getSize() {
        return elements.size();
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.unmodifiableList(elements).iterator();
    }
}
