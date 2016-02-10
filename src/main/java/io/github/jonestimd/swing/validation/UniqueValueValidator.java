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
package io.github.jonestimd.swing.validation;

import java.util.List;
import java.util.function.Function;

public abstract class UniqueValueValidator<T> implements BeanPropertyValidator<T, String> {

    private final String requiredMessage;
    private final String uniqueMessage;
    private final Function<? super T, String> beanAdapter;

    public UniqueValueValidator(Function<? super T, String> beanAdapter, String requiredMessage, String uniqueMessage) {
        this.beanAdapter = beanAdapter;
        this.requiredMessage = requiredMessage;
        this.uniqueMessage = uniqueMessage;
    }

    public String validate(int selectedIndex, String value, List<? extends T> items) {
        return validate(items.get(selectedIndex), value, items);
    }

    public String validate(T selectedItem, String value, Iterable<? extends T> items) {
        if (value == null || value.trim().length() == 0) {
            return requiredMessage;
        }
        if (isDuplicate(selectedItem, value, items)) {
            return uniqueMessage;
        }
        return null;
    }

    private boolean isDuplicate(T selectedItem, String value, Iterable<? extends T> items) {
        for (T item : items) {
            if (item != selectedItem && isSameGroup(item, selectedItem) && value.equalsIgnoreCase(beanAdapter.apply(item))) {
                return true;
            }
        }
        return false;
    }

    protected abstract boolean isSameGroup(T item, T selectedItem);
}