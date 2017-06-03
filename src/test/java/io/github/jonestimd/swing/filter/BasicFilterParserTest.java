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
package io.github.jonestimd.swing.filter;

import java.util.function.Function;
import java.util.function.Predicate;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BasicFilterParserTest {
    private static final String OPERATORS = "()!&\u2502";
    private Function<String, Predicate<String>> predicateFactory = search -> value -> value.contains(search);
    private BasicFilterParser<String> parser = new BasicFilterParser<>(predicateFactory);
    private FilterSource source = mock(FilterSource.class);

    private void trainSource(String text) {
        when(source.getText()).thenReturn(text);
        when(source.isOperator(anyInt())).thenAnswer(invocation -> {
            char c = text.charAt((Integer) invocation.getArguments()[0]);
            return OPERATORS.indexOf(c) >= 0;
        });
    }

    @Test
    public void empty() throws Exception {
        trainSource("");

        Predicate<String> predicate = parser.parse(source);

        assertThat(predicate).isNull();
    }

    @Test
    public void blank() throws Exception {
        trainSource(" \t");

        Predicate<String> predicate = parser.parse(source);

        assertThat(predicate).isNull();
    }

    @Test
    public void singleTerm() throws Exception {
        trainSource("search");

        Predicate<String> predicate = parser.parse(source);

        assertThat(predicate.test("search")).isTrue();
        assertThat(parser.getTerms()).containsExactly("search");
    }

    @Test(expected = IllegalStateException.class)
    public void prefixAndThrowsException() throws Exception {
        trainSource("& search");

        parser.parse(source);
    }

    @Test(expected = IllegalStateException.class)
    public void suffixAndThrowsException() throws Exception {
        trainSource("search &");

        parser.parse(source);
    }

    @Test(expected = IllegalStateException.class)
    public void suffixOrThrowsException() throws Exception {
        trainSource("search \u2502");

        parser.parse(source);
    }

    @Test
    public void andTwoTerms() throws Exception {
        trainSource("search1 & search2");

        Predicate<String> predicate = parser.parse(source);

        assertThat(predicate.test("search1,search2")).isTrue();
        assertThat(predicate.test("search1,")).isFalse();
        assertThat(predicate.test(",search2")).isFalse();
        assertThat(parser.getTerms()).containsExactly("search1", "search2");
    }

    @Test
    public void andThreeTerms() throws Exception {
        trainSource("search1 & search2 & search3");

        Predicate<String> predicate = parser.parse(source);

        assertThat(predicate.test("search1,search2,search3")).isTrue();
        assertThat(predicate.test("search1,search2")).isFalse();
        assertThat(predicate.test("search1,search3")).isFalse();
        assertThat(predicate.test("search2,search3")).isFalse();
        assertThat(parser.getTerms()).containsExactly("search1", "search2", "search3");
    }

    @Test
    public void orTwoTerms() throws Exception {
        trainSource("search1 \u2502 search2");

        Predicate<String> predicate = parser.parse(source);

        assertThat(predicate.test("search1")).isTrue();
        assertThat(predicate.test("search2")).isTrue();
        assertThat(predicate.test("search3")).isFalse();
        assertThat(parser.getTerms()).containsExactly("search1", "search2");
    }

    @Test
    public void orThreeTerms() throws Exception {
        trainSource("search1 \u2502 search2 \u2502 search3");

        Predicate<String> predicate = parser.parse(source);

        assertThat(predicate.test("search1")).isTrue();
        assertThat(predicate.test("search2")).isTrue();
        assertThat(predicate.test("search3")).isTrue();
        assertThat(parser.getTerms()).containsExactly("search1", "search2", "search3");
    }

    @Test
    public void andThenOr() throws Exception {
        trainSource("search1 & search2 \u2502 search3");

        Predicate<String> predicate = parser.parse(source);

        assertThat(predicate.test("search1,search2")).isTrue();
        assertThat(predicate.test("search1")).isFalse();
        assertThat(predicate.test("search2")).isFalse();
        assertThat(predicate.test("search3")).isTrue();
        assertThat(parser.getTerms()).containsExactly("search1", "search2", "search3");
    }

    @Test
    public void orThenAnd() throws Exception {
        trainSource("search1 \u2502 search2 & search3");

        Predicate<String> predicate = parser.parse(source);

        assertThat(predicate.test("search1")).isTrue();
        assertThat(predicate.test("search2,search3")).isTrue();
        assertThat(predicate.test("search2")).isFalse();
        assertThat(predicate.test("search3")).isFalse();
        assertThat(parser.getTerms()).containsExactly("search1", "search2", "search3");
    }

    @Test
    public void andThenOrThenAnd() throws Exception {
        trainSource("search1 & search2 \u2502 search3 & search4");

        Predicate<String> predicate = parser.parse(source);

        assertThat(predicate.test("search1,search2")).isTrue();
        assertThat(predicate.test("search3,search4")).isTrue();
        assertThat(predicate.test("search2,search3")).isFalse();
        assertThat(predicate.test("search1")).isFalse();
        assertThat(predicate.test("search2")).isFalse();
        assertThat(predicate.test("search3")).isFalse();
        assertThat(predicate.test("search4")).isFalse();
        assertThat(parser.getTerms()).containsExactly("search1", "search2", "search3", "search4");
    }

    @Test
    public void orThenAndThenOr() throws Exception {
        trainSource("search1 \u2502 search2 & search3 \u2502 search4");

        Predicate<String> predicate = parser.parse(source);

        assertThat(predicate.test("search1")).isTrue();
        assertThat(predicate.test("search2,search3")).isTrue();
        assertThat(predicate.test("search4")).isTrue();
        assertThat(predicate.test("search2")).isFalse();
        assertThat(predicate.test("search3")).isFalse();
        assertThat(parser.getTerms()).containsExactly("search1", "search2", "search3", "search4");
    }

    @Test
    public void notInvertsPredicate() throws Exception {
        trainSource("! search");

        Predicate<String> predicate = parser.parse(source);

        assertThat(predicate.test("search")).isFalse();
        assertThat(predicate.test("other")).isTrue();
        assertThat(parser.getTerms()).containsExactly("search");
    }

    @Test
    public void notAppliedBeforeAnd() throws Exception {
        trainSource("! search1 & search2 & ! search3");

        Predicate<String> predicate = parser.parse(source);

        assertThat(predicate.test("search1")).isFalse();
        assertThat(predicate.test("search3")).isFalse();
        assertThat(predicate.test("search1 search2")).isFalse();
        assertThat(predicate.test("search3 search2")).isFalse();
        assertThat(predicate.test("search2")).isTrue();
        assertThat(parser.getTerms()).containsExactly("search1", "search2", "search3");
    }

    @Test
    public void notAppliedBeforeOr() throws Exception {
        trainSource("! search1 \u2502 search2");

        Predicate<String> predicate = parser.parse(source);

        assertThat(predicate.test("search1 search3")).isFalse();
        assertThat(predicate.test("search1 search2")).isTrue();
        assertThat(parser.getTerms()).containsExactly("search1", "search2");
    }

    @Test
    public void orderOfOperations() throws Exception {
        trainSource("search1 & ! search2 \u2502 search3");

        Predicate<String> predicate = parser.parse(source);

        assertThat(predicate.test("search1 search2")).isFalse();
        assertThat(predicate.test("search1")).isTrue();
        assertThat(predicate.test("search3")).isTrue();
        assertThat(parser.getTerms()).containsExactly("search1", "search2", "search3");
    }

    @Test
    public void overrideAndPrecedence() throws Exception {
        trainSource("(search1 \u2502 search2) & search3");

        Predicate<String> predicate = parser.parse(source);

        assertThat(predicate.test("search1 search3")).isTrue();
        assertThat(predicate.test("search2 search3")).isTrue();
        assertThat(predicate.test("search1")).isFalse();
        assertThat(predicate.test("search2")).isFalse();
        assertThat(predicate.test("search3")).isFalse();
        assertThat(parser.getTerms()).containsExactly("search1", "search2", "search3");
    }

    @Test
    public void notGroup() throws Exception {
        trainSource("search1 & ! ( search2 \u2502 search3 )");

        Predicate<String> predicate = parser.parse(source);

        assertThat(predicate.test("search1 search2")).isFalse();
        assertThat(predicate.test("search1 search3")).isFalse();
        assertThat(predicate.test("search1")).isTrue();
        assertThat(predicate.test("search2")).isFalse();
        assertThat(predicate.test("search3")).isFalse();
        assertThat(parser.getTerms()).containsExactly("search1", "search2", "search3");
    }
}