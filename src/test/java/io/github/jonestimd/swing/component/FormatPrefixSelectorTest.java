package io.github.jonestimd.swing.component;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class FormatPrefixSelectorTest {
    @Test
    public void defaultsToAlphabeticalOrder() throws Exception {
        FormatPrefixSelector<TestBean> selector = new FormatPrefixSelector<>(new BeanFormat());
        BeanListModel<TestBean> model = new BeanListModel<>(createBeans("B", "Ab", "Aa", "C"));

        assertThat(selector.selectMatch(model, "a")).isSameAs(model.getElementAt(2));
    }

    @Test
    public void returnsNullForNoMatch() throws Exception {
        FormatPrefixSelector<TestBean> selector = new FormatPrefixSelector<>(new BeanFormat());
        BeanListModel<TestBean> model = new BeanListModel<>(createBeans("B", "Ab", "Aa", "C"));

        assertThat(selector.selectMatch(model, "d")).isNull();
    }

    @Test
    public void selectionOrderingOverridesAlphabeticalOrdering() throws Exception {
        FormatPrefixSelector<TestBean> selector = new FormatPrefixSelector<>(new BeanFormat(), new WeightComparator());
        BeanListModel<TestBean> model = new BeanListModel<>(createBeans("B", "Ab", "Aa", "C"));

        assertThat(selector.selectMatch(model, "a")).isSameAs(model.getElementAt(1));
    }

    @Test
    public void selectsExactMatch() throws Exception {
        FormatPrefixSelector<TestBean> selector = new FormatPrefixSelector<>(new BeanFormat(), new WeightComparator());
        BeanListModel<TestBean> model = new BeanListModel<>(createBeans("B", "Ab", "Aa", "C"));

        assertThat(selector.selectMatch(model, "aa")).isSameAs(model.getElementAt(2));
    }

    private List<TestBean> createBeans(String ... names) {
        List<TestBean> beans = new ArrayList<>(names.length);
        int weight = 0;
        for (String name : names) {
            beans.add(new TestBean(name, weight++));
        }
        return beans;
    }

    public static class TestBean {
        private final String name;
        private final int weight;

        public TestBean(String name, int weight) {
            this.name = name;
            this.weight = weight;
        }
    }

    public static class BeanFormat extends Format {

        @Override
        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            return toAppendTo.append(((TestBean) obj).name);
        }

        @Override
        public Object parseObject(String source, ParsePosition pos) {
            throw new UnsupportedOperationException();
        }
    }

    public static class WeightComparator implements Comparator<TestBean> {
        @Override
        public int compare(TestBean o1, TestBean o2) {
            return o1.weight - o2.weight;
        }
    }
}
