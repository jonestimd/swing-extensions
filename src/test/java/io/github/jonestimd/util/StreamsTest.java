package io.github.jonestimd.util;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class StreamsTest {
    @Test
    public void toListReturnsList() throws Exception {
        Set<String> source = Sets.newHashSet("a", "b", "c");

        List<String> result = Streams.toList(source);

        assertThat(result).containsOnlyElementsOf(source);
    }

    @Test
    public void mapIterable() throws Exception {
        Iterable<String> source = Sets.newHashSet("1", "2", "3");

        List<Integer> result = Streams.map(source, Integer::parseInt);

        assertThat(result).containsOnly(1, 2, 3);
    }

    @Test
    public void mapCollection() throws Exception {
        Set<String> source = Sets.newHashSet("1", "2", "3");

        List<Integer> result = Streams.map(source, Integer::parseInt);

        assertThat(result).containsOnly(1, 2, 3);
    }

    @Test
    public void mapInts() throws Exception {
        int[] source = {1, 2, 3};

        List<String> result = Streams.map(source, Integer::toString);

        assertThat(result).containsOnly("1", "2", "3");
    }

    @Test
    public void filterCollection() throws Exception {
        List<Integer> source = ImmutableList.of(1, 2, 3, 4, 5, 6);

        List<Integer> result = Streams.filter(source, n -> n % 2 == 0);

        assertThat(result).containsExactly(2, 4, 6);
    }

    @Test
    public void uniqueCollectionReturnsSet() throws Exception {
        List<Integer> source = ImmutableList.of(1, 1, 3, 4, 5, 3);

        Set<String> result = Streams.unique(source, String::valueOf);

        assertThat(result).containsOnly("1", "3", "4", "5");
    }

    @Test
    public void uniqueIndexReturnsMap() throws Exception {
        List<Integer> source = ImmutableList.of(1, 2, 3, 4, 5, 6);

        Map<String, Integer> result = Streams.uniqueIndex(source, String::valueOf);

        assertThat(result.size()).isEqualTo(source.size());
        for (Integer item : source) {
            assertThat(result.get(item.toString())).isEqualTo(item);
        }
    }

    @Test(expected = IllegalStateException.class)
    public void uniqueIndexFailsForNonUniqueKey() throws Exception {
        List<Integer> source = ImmutableList.of(1, 2, 3, 4, 5, 6, 6);

        Streams.uniqueIndex(source, String::valueOf);
    }

    @Test
    public void sum() throws Exception {
        BigDecimal sum = Streams.sum(Stream.of(BigDecimal.ONE, BigDecimal.ONE));

        assertThat(sum.compareTo(BigDecimal.valueOf(2))).isEqualTo(0);
    }

    @Test
    public void reverseRange() throws Exception {
        int[] values = Streams.reverseRange(5, 0).toArray();

        assertThat(values).isEqualTo(new int[] { 4, 3, 2, 1, 0 });
    }
}