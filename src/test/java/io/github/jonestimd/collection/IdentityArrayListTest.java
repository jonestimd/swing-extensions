package io.github.jonestimd.collection;

import java.lang.reflect.Field;
import java.util.ArrayList;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class IdentityArrayListTest {
    @Test
    public void indexOfUsesObjectIdentity() throws Exception {
        String s1 = "string";
        String s2 = new String(s1);
        IdentityArrayList<String> list = new IdentityArrayList<>(ImmutableList.of(s1, s2));

        assertThat(list.indexOf(s1)).isEqualTo(0);
        assertThat(list.indexOf(s2)).isEqualTo(1);
    }

    @Test
    public void indexOfReturnsNegativeNumberForNotFound() throws Exception {
        IdentityArrayList<String> list = new IdentityArrayList<>(ImmutableList.of("a", "b"));

        assertThat(list.indexOf("c")).isLessThan(0);
        assertThat(list.lastIndexOf("c")).isLessThan(0);
    }

    @Test
    public void lastIndexOfUsesObjectIdentity() throws Exception {
        String s1 = "string";
        String s2 = new String(s1);
        IdentityArrayList<String> list = new IdentityArrayList<>();
        list.add(s1);
        list.add(s2);

        assertThat(list.lastIndexOf(s1)).isEqualTo(0);
        assertThat(list.lastIndexOf(s2)).isEqualTo(1);
    }

    @Test
    public void setsInitialCapacity() throws Exception {
        IdentityArrayList<String> list = new IdentityArrayList<>(9);

        Field field = ArrayList.class.getDeclaredField("elementData");
        field.setAccessible(true);
        Object[] elementData = (Object[]) field.get(list);
        assertThat(elementData.length).isEqualTo(9);
    }
}