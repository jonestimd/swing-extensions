package io.github.jonestimd.lang;

import java.util.Arrays;

import org.junit.Test;

import static org.fest.assertions.Assertions.*;

public class ComparablesTest {
    @Test
    public void minReturnsMinValue() throws Exception {
        assertThat(Comparables.min("a", "A")).isEqualTo("A");
        assertThat(Comparables.min(3, 1, -9, 1)).isEqualTo(-9);
    }

    @Test
    public void nullFirst_nullAndNotNull() throws Exception {
        assertThat(Comparables.nullFirst(null, 0L)).isLessThan(0);
    }

    @Test
    public void nullFirst_notNullAndNull() throws Exception {
        assertThat(Comparables.nullFirst(0L, null)).isGreaterThan(0);
    }

    @Test
    public void nullFirst_nullAndNull() throws Exception {
        assertThat(Comparables.nullFirst(Long.class.cast(null), null)).isEqualTo(0);
    }

    @Test
    public void nullFirst_notNullAndNotNull() throws Exception {
        assertThat(Comparables.nullFirst(1L, 2L)).isEqualTo(Long.valueOf(1L).compareTo(2L));
    }

    @Test
    public void nullLast_nullAndNotNull() throws Exception {
        assertThat(Comparables.nullLast(null, 1L)).isGreaterThan(0);
    }

    @Test
    public void nullLast_notNullAndNull() throws Exception {
        assertThat(Comparables.nullLast(1L, null)).isLessThan(0);
    }

    @Test
    public void nullLast_nullAndNull() throws Exception {
        assertThat(Comparables.nullLast(Long.class.cast(null), null)).isEqualTo(0);
    }

    @Test
    public void nullLast_notNullAndNotNull() throws Exception {
        assertThat(Comparables.nullLast(1L, 2L)).isEqualTo(Long.valueOf(1L).compareTo(2L));
    }

    @Test
    public void compareListsIgnoringCase() throws Exception {
        assertThat(Comparables.compareIgnoreCase(Arrays.asList("A"), Arrays.asList("a"))).isEqualTo(0);
        assertThat(Comparables.compareIgnoreCase(Arrays.asList("A"), Arrays.asList("b"))).isLessThan(0);
        assertThat(Comparables.compareIgnoreCase(Arrays.asList("b"), Arrays.asList("A"))).isGreaterThan(0);
        assertThat(Comparables.compareIgnoreCase(Arrays.asList("A"), Arrays.asList("A", "?"))).isLessThan(0);
        assertThat(Comparables.compareIgnoreCase(Arrays.asList("A", "?"), Arrays.asList("A"))).isGreaterThan(0);
        assertThat(Comparables.compareIgnoreCase(Arrays.asList("A", "A"), Arrays.asList("A", "b"))).isLessThan(0);
        assertThat(Comparables.compareIgnoreCase(Arrays.asList("A", "b"), Arrays.asList("A", "A"))).isGreaterThan(0);
        assertThat(Comparables.compareIgnoreCase(Arrays.asList("A", "a"), Arrays.asList("a", "A"))).isEqualTo(0);
    }
}
