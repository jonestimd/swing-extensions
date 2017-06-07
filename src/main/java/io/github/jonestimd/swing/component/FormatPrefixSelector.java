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

import java.text.Format;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.ListModel;

/**
 * A {@link PrefixSelector} that uses a format to convert the beans to strings.
 * @param <T> the class of the beans in the model
 */
public class FormatPrefixSelector<T> implements PrefixSelector<T> {
    private final Format format;
    private final Comparator<? super T> selectionOrdering;

    private static <T> Comparator<T> formatComparator(final Format format) {
        return (o1, o2) -> format.format(o1).compareToIgnoreCase(format.format(o2));
    }

    public FormatPrefixSelector(Format format) {
        this(formatComparator(format), format);
    }

    public FormatPrefixSelector(Format format, Comparator<? super T> selectionOrdering) {
        this(selectionOrdering.thenComparing(formatComparator(format)), format);
    }

    private FormatPrefixSelector(Comparator<? super T> selectionOrdering, Format format) {
        this.format = format;
        this.selectionOrdering = selectionOrdering;
    }

    public T selectMatch(ListModel<T> model, String prefix) {
        List<T> matches = getMatches(model, prefix.toUpperCase());
        if (!matches.isEmpty()) {
            return matches.stream().min(selectionOrdering).get();
        }
        return null;
    }

    private List<T> getMatches(ListModel<T> model, String prefix) {
        List<T> matches = new ArrayList<>();
        for (int i = 0; i < model.getSize(); i++) {
            T item = model.getElementAt(i);
            if (item != null && format.format(item).toUpperCase().startsWith(prefix)) {
                matches.add(item);
            }
        }
        return matches;
    }
}
