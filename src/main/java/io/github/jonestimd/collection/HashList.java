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
package io.github.jonestimd.collection;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * List implementation with improved performance for {@link List#indexOf(Object)}.  The {@link #indexOf(Object)} method
 * uses a map of {@code ID} to index instead of searching the list for the value.  Items in the list for which the
 * {@code ID} {@link Function} returns {@code null} are allowed and can safely be assigned a non-{@code null} {@code ID}
 * after being added to the list.  Otherwise, the {@code ID} of an item in the list should not change.
 */
public class HashList<T, ID> extends AbstractList<T> {
    private final Function<? super T, ID> idFunction;
    private final List<T> delegate;
    private final Map<ID, Integer> indexes = new HashMap<>();
    private final Map<T, Integer> nullIndexes = new HashMap<>();

    public HashList(Function<? super T, ID> idFunction) {
        this.idFunction = idFunction;
        delegate = new ArrayList<>();
    }

    public HashList(Function<? super T, ID> idFunction, int initialCapacity) {
        this.idFunction = idFunction;
        this.delegate = new ArrayList<>(initialCapacity);
    }

    public HashList(List<T> list, Function<? super T, ID> idFunction) {
        this(idFunction, list.size());
        addAll(list);
    }

    @Override
    public T get(int index) {
        return delegate.get(index);
    }

    @Override
    public T set(int index, T element) {
        final T oldElement = delegate.set(index, element);
        removeIndex(oldElement);
        checkForDuplicate(element);
        setIndex(element, index);
        return oldElement;
    }

    @Override
    public void add(int index, T element) {
        checkForDuplicate(element);
        delegate.add(index, element);
        updateIndexes(index);
    }

    private void checkForDuplicate(T element) {
        if (indexes.containsKey(idFunction.apply(element)) || nullIndexes.containsKey(element)) {
            throw new IllegalStateException("Duplicate value");
        }
    }

    private void updateIndexes(int fromIndex) {
        for (int i = fromIndex; i < delegate.size(); i++) {
            setIndex(delegate.get(i), i);
        }
    }

    private Integer setIndex(T element, int index) {
        final ID id = idFunction.apply(element);
        if (id == null) {
            return nullIndexes.put(element, index);
        }
        return indexes.put(id, index);
    }

    @Override
    public T remove(int index) {
        T element = delegate.remove(index);
        removeIndex(element);
        updateIndexes(index);
        return element;
    }

    private void removeIndex(T element) {
        indexes.remove(idFunction.apply(element));
        nullIndexes.remove(element);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    @SuppressWarnings("unchecked")
    public int indexOf(Object o) {
        return indexes.getOrDefault(idFunction.apply((T) o), nullIndexes.getOrDefault(o, -1));
    }

    @Override
    public int lastIndexOf(Object o) {
        return indexOf(o);
    }
}
