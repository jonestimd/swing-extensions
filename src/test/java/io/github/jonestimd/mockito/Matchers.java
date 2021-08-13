// The MIT License (MIT)
//
// Copyright (c) 2021 Timothy D. Jones
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
package io.github.jonestimd.mockito;

import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.swing.event.ListDataEvent;
import javax.swing.event.TableModelEvent;

import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

public class Matchers {
    @SafeVarargs
    public static <T> Collection<T> containsOnly(T... items) {
        return containsOnly(Arrays.asList(items));
    }

    public static <T> Collection<T> isEmpty() {
        return containsOnly(Collections.emptyList());
    }

    public static <T> Collection<T> containsOnly(final Collection<T> items) {
        final Set<T> uniqueItems = new HashSet<>(items);
        return Mockito.argThat(arg -> arg.size() == uniqueItems.size() && arg.containsAll(uniqueItems));
    }

    public static TableModelEvent matches(final TableModelEvent example) {
        return Mockito.argThat(actual -> actual.getSource() == example.getSource() &&
                actual.getType() == example.getType() &&
                actual.getColumn() == example.getColumn() &&
                actual.getFirstRow() == example.getFirstRow() &&
                actual.getLastRow() == example.getLastRow());
    }

    public static PropertyChangeEvent matches(final PropertyChangeEvent example) {
        return Mockito.argThat(new ArgumentMatcher<PropertyChangeEvent>() {
            @Override
            public boolean matches(PropertyChangeEvent actual) {
                return actual.getSource() == example.getSource() &&
                        actual.getPropertyName().equals(example.getPropertyName()) &&
                        Objects.equals(actual.getOldValue(), example.getOldValue()) &&
                        Objects.equals(actual.getNewValue(), example.getNewValue());
            }

            public String toString() {
                return example.toString();
            }
        });
    }

    public static ListDataEvent listDataEvent(Object source, int type, int index0, int index1) {
        return Mockito.argThat(actual -> actual.getSource() == source &&
                actual.getType() == type &&
                actual.getIndex0() == index0 &&
                actual.getIndex1() == index1);
    }
}
