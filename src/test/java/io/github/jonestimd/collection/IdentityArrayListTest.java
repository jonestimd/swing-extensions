package io.github.jonestimd.collection;

import org.junit.Test;

import static org.fest.assertions.Assertions.*;

public class IdentityArrayListTest {
    @Test
    public void indexOfUsesObjectIdentity() throws Exception {
        String s1 = "string";
        String s2 = new String(s1);
        IdentityArrayList<String> list = new IdentityArrayList<>();
        list.add(s1);
        list.add(s2);

        assertThat(list.indexOf(s1)).isEqualTo(0);
        assertThat(list.indexOf(s2)).isEqualTo(1);
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
}