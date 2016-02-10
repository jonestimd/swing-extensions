package io.github.jonestimd.util;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.Test;

import static org.fest.assertions.Assertions.*;

public class StreamsTest {
    @Test
    public void sum() throws Exception {
        BigDecimal sum = Streams.sum(Arrays.asList(BigDecimal.ONE, BigDecimal.ONE).stream());

        assertThat(sum.compareTo(BigDecimal.valueOf(2))).isEqualTo(0);
    }

    @Test
    public void reverseRange() throws Exception {
        int[] values = Streams.reverseRange(5, 0).toArray();

        assertThat(values).isEqualTo(new int[] { 4, 3, 2, 1, 0 });
    }
}