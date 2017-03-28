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
package io.github.jonestimd.util;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Streams {
    public static <T> Stream<T> of(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    public static <T> List<T> toList(Iterable<T> iterable) {
        return of(iterable).collect(Collectors.toList());
    }

    public static <T, R> List<R> map(Iterable<T> iterable, Function<? super T, ? extends R> transform) {
        return map(of(iterable), transform);
    }

    public static <T, R> List<R> map(Collection<T> collection, Function<? super T, ? extends R> transform) {
        return map(collection.stream(), transform);
    }

    public static <T, R> List<R> map(Stream<T> stream, Function<? super T, ? extends R> transform) {
        return stream.map(transform).collect(Collectors.toList());
    }

    public static <R> List<R> map(int[] source, IntFunction<? extends R> transform) {
        return IntStream.of(source).mapToObj(transform).collect(Collectors.toList());
    }

    public static <T> List<T> filter(Iterable<T> list, Predicate<? super T> predicate) {
        return of(list).filter(predicate).collect(Collectors.toList());
    }

    public static <T, R> Set<R> unique(Collection<T> collection, Function<? super T, ? extends R> transform) {
        return unique(collection.stream(), transform);
    }

    public static <T, R> Set<R> unique(Stream<T> stream, Function<? super T, ? extends R> transform) {
        return stream.map(transform).collect(Collectors.toSet());
    }

    public static <K, V> Map<K, V> uniqueIndex(Collection<V> collection, Function<? super V, ? extends K> keyFunction) {
        return uniqueIndex(collection.stream(), keyFunction);
    }

    public static <K, V> Map<K, V> uniqueIndex(Stream<V> stream, Function<? super V, ? extends K> keyFunction) {
        return stream.collect(Collectors.toMap(keyFunction, Function.identity()));
    }

    public static BigDecimal sum(Stream<BigDecimal> stream) {
        return stream.reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static IntStream reverseRange(int endExclusive, int start) {
        return IntStream.range(start, endExclusive).map(i -> endExclusive - 1 - (i - start));
    }
}
