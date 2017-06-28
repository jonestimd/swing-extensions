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

import javax.swing.MutableComboBoxModel;

import io.github.jonestimd.swing.list.BeanListModel;

/**
 * A {@link MutableComboBoxModel} that supports lazy loading.
 * @param <T> the class of the beans in the model
 */
public class BeanListComboBoxModel<T> extends BeanListModel<T> implements LazyLoadComboBoxModel<T> {
    private T selectedElement;

    public BeanListComboBoxModel() {
    }

    public BeanListComboBoxModel(Collection<? extends T> elements) {
        super(elements);
    }

    /**
     * Replace the elements.
     * @param elements the new elements
     * @param keepSelection if true then the selected item is not changed, if false then the selected item is cleared
     *        if it is not in {@code elements}
     */
    public void setElements(Collection<? extends T> elements, boolean keepSelection) {
        super.setElements(elements);
        if (! keepSelection && selectedElement != null && ! contains(selectedElement)) {
            setSelectedItem(null);
        }
    }

    @Override
    public void removeAll() {
        super.removeAll();
        setSelectedItem(null);
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
}