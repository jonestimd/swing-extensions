package io.github.jonestimd.text;

import java.util.Random;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class StringFormatTest {
    private StringFormat format = new StringFormat();
    private Random random = new Random();

    @Test
    public void formatReturnsInputString() throws Exception {
        String input = Long.toString(random.nextLong());

        assertThat(format.format(input)).isEqualTo(input);
    }

    @Test
    public void parseReturnsInputString() throws Exception {
        String input = Long.toString(random.nextLong());

        assertThat(format.parseObject(input)).isSameAs(input);
    }
}