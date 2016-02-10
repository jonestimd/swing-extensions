package io.github.jonestimd.swing.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.junit.Test;

import static org.fest.assertions.Assertions.*;
import static org.mockito.Mockito.*;

public class BasicFilterParserTest {
    private static final String OPERATORS = "()!&|";
    private Function<String, Predicate<String>> predicateFactory = search -> value -> value.contains(search);
    private List<String> terms = new ArrayList<>();
    private BasicFilterParser<String> parser = new BasicFilterParser<>(predicateFactory, terms::add);
    private FilterSource source = mock(FilterSource.class);

    private void trainSource(String text) {
        when(source.getText()).thenReturn(text);
        when(source.isOperator(anyInt())).thenAnswer(invocation -> {
            char c = text.charAt((Integer) invocation.getArguments()[0]);
            return OPERATORS.indexOf(c) >= 0;
        });
    }

    @Test(expected = IllegalStateException.class)
    public void empty() throws Exception {
        trainSource("");

        parser.parse(source);
    }

    @Test
    public void singleTerm() throws Exception {
        trainSource("search");

        Predicate<String> predicate = parser.parse(source);

        assertThat(predicate.test("search")).isTrue();
        assertThat(terms).containsExactly("search");
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
        trainSource("search |");

        parser.parse(source);
    }

    @Test
    public void andTwoTerms() throws Exception {
        trainSource("search1 & search2");

        Predicate<String> predicate = parser.parse(source);

        assertThat(predicate.test("search1,search2")).isTrue();
        assertThat(predicate.test("search1,")).isFalse();
        assertThat(predicate.test(",search2")).isFalse();
        assertThat(terms).containsExactly("search1", "search2");
    }

    @Test
    public void andThreeTerms() throws Exception {
        trainSource("search1 & search2 & search3");

        Predicate<String> predicate = parser.parse(source);

        assertThat(predicate.test("search1,search2,search3")).isTrue();
        assertThat(predicate.test("search1,search2")).isFalse();
        assertThat(predicate.test("search1,search3")).isFalse();
        assertThat(predicate.test("search2,search3")).isFalse();
        assertThat(terms).containsExactly("search1", "search2", "search3");
    }

    @Test
    public void orTwoTerms() throws Exception {
        trainSource("search1 | search2");

        Predicate<String> predicate = parser.parse(source);

        assertThat(predicate.test("search1")).isTrue();
        assertThat(predicate.test("search2")).isTrue();
        assertThat(predicate.test("search3")).isFalse();
        assertThat(terms).containsExactly("search1", "search2");
    }

    @Test
    public void orThreeTerms() throws Exception {
        trainSource("search1 | search2 | search3");

        Predicate<String> predicate = parser.parse(source);

        assertThat(predicate.test("search1")).isTrue();
        assertThat(predicate.test("search2")).isTrue();
        assertThat(predicate.test("search3")).isTrue();
        assertThat(terms).containsExactly("search1", "search2", "search3");
    }

    @Test
    public void andThenOr() throws Exception {
        trainSource("search1 & search2 | search3");

        Predicate<String> predicate = parser.parse(source);

        assertThat(predicate.test("search1,search2")).isTrue();
        assertThat(predicate.test("search1")).isFalse();
        assertThat(predicate.test("search2")).isFalse();
        assertThat(predicate.test("search3")).isTrue();
        assertThat(terms).containsExactly("search1", "search2", "search3");
    }

    @Test
    public void orThenAnd() throws Exception {
        trainSource("search1 | search2 & search3");

        Predicate<String> predicate = parser.parse(source);

        assertThat(predicate.test("search1")).isTrue();
        assertThat(predicate.test("search2,search3")).isTrue();
        assertThat(predicate.test("search2")).isFalse();
        assertThat(predicate.test("search3")).isFalse();
        assertThat(terms).containsExactly("search1", "search2", "search3");
    }

    @Test
    public void andThenOrThenAnd() throws Exception {
        trainSource("search1 & search2 | search3 & search4");

        Predicate<String> predicate = parser.parse(source);

        assertThat(predicate.test("search1,search2")).isTrue();
        assertThat(predicate.test("search3,search4")).isTrue();
        assertThat(predicate.test("search2,search3")).isFalse();
        assertThat(predicate.test("search1")).isFalse();
        assertThat(predicate.test("search2")).isFalse();
        assertThat(predicate.test("search3")).isFalse();
        assertThat(predicate.test("search4")).isFalse();
        assertThat(terms).containsExactly("search1", "search2", "search3", "search4");
    }

    @Test
    public void orThenAndThenOr() throws Exception {
        trainSource("search1 | search2 & search3 | search4");

        Predicate<String> predicate = parser.parse(source);

        assertThat(predicate.test("search1")).isTrue();
        assertThat(predicate.test("search2,search3")).isTrue();
        assertThat(predicate.test("search4")).isTrue();
        assertThat(predicate.test("search2")).isFalse();
        assertThat(predicate.test("search3")).isFalse();
        assertThat(terms).containsExactly("search1", "search2", "search3", "search4");
    }

    @Test
    public void notInvertsPredicate() throws Exception {
        trainSource("! search");

        Predicate<String> predicate = parser.parse(source);

        assertThat(predicate.test("search")).isFalse();
        assertThat(predicate.test("other")).isTrue();
        assertThat(terms).containsExactly("search");
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
        assertThat(terms).containsExactly("search1", "search2", "search3");
    }

    @Test
    public void notAppliedBeforeOr() throws Exception {
        trainSource("! search1 | search2");

        Predicate<String> predicate = parser.parse(source);

        assertThat(predicate.test("search1 search3")).isFalse();
        assertThat(predicate.test("search1 search2")).isTrue();
        assertThat(terms).containsExactly("search1", "search2");
    }

    @Test
    public void orderOfOperations() throws Exception {
        trainSource("search1 & ! search2 | search3");

        Predicate<String> predicate = parser.parse(source);

        assertThat(predicate.test("search1 search2")).isFalse();
        assertThat(predicate.test("search1")).isTrue();
        assertThat(predicate.test("search3")).isTrue();
        assertThat(terms).containsExactly("search1", "search2", "search3");
    }

    @Test
    public void overrideAndPrecedence() throws Exception {
        trainSource("(search1 | search2) & search3");

        Predicate<String> predicate = parser.parse(source);

        assertThat(predicate.test("search1 search3")).isTrue();
        assertThat(predicate.test("search2 search3")).isTrue();
        assertThat(predicate.test("search1")).isFalse();
        assertThat(predicate.test("search2")).isFalse();
        assertThat(predicate.test("search3")).isFalse();
        assertThat(terms).containsExactly("search1", "search2", "search3");
    }

    @Test
    public void notGroup() throws Exception {
        trainSource("search1 & ! ( search2 | search3 )");

        Predicate<String> predicate = parser.parse(source);

        assertThat(predicate.test("search1 search2")).isFalse();
        assertThat(predicate.test("search1 search3")).isFalse();
        assertThat(predicate.test("search1")).isTrue();
        assertThat(predicate.test("search2")).isFalse();
        assertThat(predicate.test("search3")).isFalse();
        assertThat(terms).containsExactly("search1", "search2", "search3");
    }
}