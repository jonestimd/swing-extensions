package io.github.jonestimd.collection;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.junit.Test;

import static java.util.Collections.*;
import static org.fest.assertions.Assertions.*;

public class HashListTest {
    @Test
    public void constructFromList() throws Exception {
        List<String> items = Arrays.asList("one", "two", "three");

        HashList<String, String> list = new HashList<>(items, Function.identity());

        assertThat(list).containsExactly(items.toArray());
        assertThat(list.indexOf("one")).isEqualTo(0);
        assertThat(list.indexOf("two")).isEqualTo(1);
        assertThat(list.indexOf("three")).isEqualTo(2);
    }

    @Test(expected = IllegalStateException.class)
    public void exceptionForDuplicateValues() throws Exception {
        new HashList<>(singletonList("one"), Function.identity()).add("one");
    }

    @Test
    public void replaceValue() throws Exception {
        List<String> list = new HashList<>(Function.identity());
        list.addAll(Arrays.asList("a", "b", "c"));

        list.set(1, "two");

        assertThat(list).containsExactly("a", "two", "c");
        assertThat(list.indexOf("b")).isEqualTo(-1);
        assertThat(list.indexOf("two")).isEqualTo(1);
        assertThat(list.lastIndexOf("two")).isEqualTo(1);
    }

    @Test
    public void remove() throws Exception {
        List<String> list = new HashList<>(Arrays.asList("a", "b", "c"), Function.identity());

        assertThat(list.remove("b")).isTrue();

        assertThat(list).containsExactly("a", "c");
        assertThat(list.indexOf("a")).isEqualTo(0);
        assertThat(list.indexOf("c")).isEqualTo(1);
    }

    @Test
    public void removeByIndex() throws Exception {
        List<String> list = new HashList<>(Arrays.asList("a", "b", "c"), Function.identity());

        assertThat(list.remove(1)).isEqualTo("b");

        assertThat(list).containsExactly("a", "c");
        assertThat(list.indexOf("a")).isEqualTo(0);
        assertThat(list.indexOf("c")).isEqualTo(1);
    }

    @Test
    public void clear() throws Exception {
        List<String> list = new HashList<>(Arrays.asList("a", "b", "c"), Function.identity());

        list.clear();

        assertThat(list).isEmpty();
        assertThat(list.indexOf("a")).isEqualTo(-1);
        assertThat(list.indexOf("b")).isEqualTo(-1);
        assertThat(list.indexOf("c")).isEqualTo(-1);
    }

    @Test
    public void indexOfAfterSettingId() throws Exception {
        final TestBean bean1 = new TestBean().setId(1L);
        final TestBean bean2 = new TestBean();
        List<TestBean> list = new HashList<>(Arrays.asList(bean1, bean2), TestBean::getId);
        assertThat(list.indexOf(bean2)).isEqualTo(1);

        bean2.setId(2L);

        assertThat(list.indexOf(bean2)).isEqualTo(1);
    }

    private class TestBean {
        private Long id;

        public Long getId() {
            return id;
        }

        public TestBean setId(Long id) {
            this.id = id;
            return this;
        }
    }
}