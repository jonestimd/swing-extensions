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
import java.util.List;
import java.util.function.Function;

import io.github.jonestimd.util.Streams;

public class ContainsFilterComboBoxModel<T> extends BeanListComboBoxModel<T> implements FilterComboBoxModel<T> {
    private List<T> unfilteredItems;
    private final Function<T, String> format;

    public ContainsFilterComboBoxModel(Format format) {
        this(new ArrayList<>(), format);
    }

    public ContainsFilterComboBoxModel(List<T> unfilteredItems, Format format) {
        this(unfilteredItems, format::format);
    }

    public ContainsFilterComboBoxModel(List<T> unfilteredItems, Function<T, String> format) {
        super(unfilteredItems);
        this.unfilteredItems = unfilteredItems;
        this.format = format;
    }

    @Override
    public void applyFilter(String search) {
        final String filter = search.toLowerCase();
        setElements(Streams.filter(unfilteredItems, item -> format.apply(item).toLowerCase().contains(filter)), false);
    }

    public String formatItem(T item) {
        return format.apply((item));
    }
}