// The MIT License (MIT)
//
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

import java.util.Random;
import java.util.function.Predicate;

import org.junit.Test;

import static org.fest.assertions.Assertions.*;

public class JavaPredicatesTest {
    private Random random = new Random();

    @Test
    public void onResultAppliesPredicateToFunctionResult() throws Exception {
        Predicate<String> predicate = JavaPredicates.onResult(Long::parseLong, n -> n == 0);

        assertThat(predicate.test("0")).isTrue();
        assertThat(predicate.test("00")).isTrue();
        assertThat(predicate.test("-1")).isFalse();
        assertThat(predicate.test("1")).isFalse();
    }

    @Test
    public void notNegatesPredicate() throws Exception {
        Predicate<String> notEmpty = JavaPredicates.not(String::isEmpty);

        assertThat(notEmpty.test("")).isFalse();
        assertThat(notEmpty.test("x")).isTrue();
        assertThat(notEmpty.test(" ")).isTrue();
    }

    @Test
    public void alwaysTrue() throws Exception {
        Predicate<Long> predicate = JavaPredicates.alwaysTrue();

        assertThat(predicate.test(random.nextLong())).isTrue();
    }

    @Test
    public void alwaysFalse() throws Exception {
        Predicate<Long> predicate = JavaPredicates.alwaysFalse();

        assertThat(predicate.test(random.nextLong())).isFalse();
    }
}