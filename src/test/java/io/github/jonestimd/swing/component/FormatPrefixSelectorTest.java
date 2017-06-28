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
        BeanListComboBoxModel<TestBean> model = new BeanListComboBoxModel<>(createBeans("B", "Ab", "Aa", "C"));

        assertThat(selector.selectMatch(model, "a")).isSameAs(model.getElementAt(2));
    }

    @Test
    public void returnsNullForNoMatch() throws Exception {
        FormatPrefixSelector<TestBean> selector = new FormatPrefixSelector<>(new BeanFormat());
        BeanListComboBoxModel<TestBean> model = new BeanListComboBoxModel<>(createBeans("B", "Ab", "Aa", "C"));

        assertThat(selector.selectMatch(model, "d")).isNull();
    }

    @Test
    public void selectionOrderingOverridesAlphabeticalOrdering() throws Exception {
        FormatPrefixSelector<TestBean> selector = new FormatPrefixSelector<>(new BeanFormat(), new WeightComparator());
        BeanListComboBoxModel<TestBean> model = new BeanListComboBoxModel<>(createBeans("B", "Ab", "Aa", "C"));

        assertThat(selector.selectMatch(model, "a")).isSameAs(model.getElementAt(1));
    }

    @Test
    public void selectsExactMatch() throws Exception {
        FormatPrefixSelector<TestBean> selector = new FormatPrefixSelector<>(new BeanFormat(), new WeightComparator());
        BeanListComboBoxModel<TestBean> model = new BeanListComboBoxModel<>(createBeans("B", "Ab", "Aa", "C"));

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
