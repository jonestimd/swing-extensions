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
package io.github.jonestimd.lang;

import java.util.List;

public class Comparables {
    private Comparables() {
    }

    @SafeVarargs
    public static <T extends Comparable<? super T>> T min(T c1, T... others) {
        T min = c1;
        for (T other : others) {
            min = min.compareTo(other) > 0 ? other : min;
        }
        return min;
    }

    public static <T extends Comparable<? super T>> int nullFirst(T c1, T c2) {
        if (c1 == null) {
            return c2 == null ? 0 : -1;
        }
        return c2 == null ? 1 : c1.compareTo(c2);
    }

    public static <T extends Comparable<? super T>> int nullLast(T c1, T c2) {
        if (c1 == null) {
            return c2 == null ? 0 : 1;
        }
        return c2 == null ? -1 : c1.compareTo(c2);
    }

    /**
     * Compare each element in {@code list1} to the corresponding element in {@code list2} until a difference is found.
     * If one list contains the leading elements of the other then the comparison result is the difference in the list sizes.
     * @return the result of tne first non-zero comparison of the list elements or the comparison of the list sizes
     */
    public static int compareIgnoreCase(List<String> list1, List<String> list2) {
        for (int i = 0; i < Math.min(list1.size(), list2.size()); i++) {
            int diff = list1.get(i).compareToIgnoreCase(list2.get(i));
            if (diff != 0) {
                return diff;
            }
        }
        return list1.size() - list2.size();
    }
}
