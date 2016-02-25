// The MIT License (MIT)
//
// Copyright (c) 2016 Timothy D. Jones
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
package io.github.jonestimd.swing.table;

import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

import com.google.common.collect.ImmutableList;
import io.github.jonestimd.swing.table.model.BeanListTableModel;
import io.github.jonestimd.swing.table.model.ColumnAdapter;
import io.github.jonestimd.swing.table.model.FunctionColumnAdapter;
import org.junit.Test;

import static org.fest.assertions.Assertions.*;

public class BeanListColumnConfigurationTest {
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("test-resources");
    public static final String PREFIX = "beanListColumnConfiguration.";
    public static final List<ColumnAdapter<TestBean, ?>> ADAPTERS = ImmutableList.of(
        new FunctionColumnAdapter<>(BUNDLE, PREFIX, "first", String.class, TestBean::getFirst, null),
        new FunctionColumnAdapter<>(BUNDLE, PREFIX, "second", String.class, TestBean::getSecond, null)
    );
    private final BeanListTableModel<TestBean> model = new BeanListTableModel<>(ADAPTERS);
    private final DecoratedTable<?, ?> table = new DecoratedTable<>(model);
    private final BeanListColumnConfiguration configuration = new BeanListColumnConfiguration(table);
    private final Random random = new Random();

    @Test
    public void getWidthReturnsSystemPropertyIfSet() throws Exception {
        int width = random.nextInt();
        System.setProperty("BeanListTableModel.first.width", Integer.toString(width));

        assertThat(configuration.getWidth(table.getColumnModel().getColumn(0))).isEqualTo(width);
    }

    @Test
    public void getWidthReturnsNullIfPropertyNotSet() throws Exception {
        int width = random.nextInt();
        System.getProperties().remove("BeanListTableModel.first.width");

        assertThat(configuration.getWidth(table.getColumnModel().getColumn(0))).isNull();
    }

    @Test
    public void setWidthSetsSystemProperty() throws Exception {
        int width = random.nextInt();
        System.getProperties().remove("BeanListTableModel.first.width");

        configuration.setWidth(table.getColumnModel().getColumn(0), width);

        assertThat(Integer.getInteger("BeanListTableModel.first.width")).isEqualTo(width);
    }

    @Test
    public void getIndexReturnsSystemPropertyIfSet() throws Exception {
        int index = random.nextInt();
        System.setProperty("BeanListTableModel.first.index", Integer.toString(index));

        assertThat(configuration.getIndex(table.getColumnModel().getColumn(0))).isEqualTo(index);
    }

    @Test
    public void getIndexReturnsDefaultValueIfPropertyNotSet() throws Exception {
        System.getProperties().remove("BeanListTableModel.first.index");

        assertThat(configuration.getIndex(table.getColumnModel().getColumn(0))).isEqualTo(-1);
    }

    @Test
    public void setIndexSetsSystemProperty() throws Exception {
        int index = random.nextInt();
        System.getProperties().remove("BeanListTableModel.first.index");

        configuration.setIndex(table.getColumnModel().getColumn(0), index);

        assertThat(Integer.getInteger("BeanListTableModel.first.index")).isEqualTo(index);
    }

    @Test
    public void getPrototypeReturnsResourceValue() throws Exception {
        assertThat(configuration.getPrototypeValue(table.getColumnModel().getColumn(0))).isEqualTo("first's prototype");
    }

    @Test
    public void getPrototypeReturnsHeaderIfNoResource() throws Exception {
        assertThat(configuration.getPrototypeValue(table.getColumnModel().getColumn(1))).isEqualTo("Second");
    }

    private static class TestBean {
        public final String first;
        public final String second;

        public TestBean(String first, String second) {
            this.first = first;
            this.second = second;
        }

        public String getFirst() {
            return first;
        }

        public String getSecond() {
            return second;
        }
    }
}