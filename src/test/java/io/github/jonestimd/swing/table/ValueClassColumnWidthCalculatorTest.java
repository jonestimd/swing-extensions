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

import java.awt.Component;
import java.awt.Insets;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.table.TableColumnModel;

import com.google.common.collect.ImmutableList;
import io.github.jonestimd.swing.table.model.BeanListTableModel;
import io.github.jonestimd.swing.table.model.ColumnAdapter;
import io.github.jonestimd.swing.table.model.FunctionColumnAdapter;
import org.junit.Test;

import static org.fest.assertions.Assertions.*;
import static org.mockito.Mockito.*;

public class ValueClassColumnWidthCalculatorTest {
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("test-resources");
    public static final String PREFIX = "valueClassColumnWidthCalculator.";
    public static final List<ColumnAdapter<TestBean, ?>> ADAPTERS = ImmutableList.of(
            new FunctionColumnAdapter<>(BUNDLE, PREFIX, "first", String.class, TestBean::getFirst, null),
            new FunctionColumnAdapter<>(BUNDLE, PREFIX, "second", Long.class, TestBean::getSecond, null),
            new FunctionColumnAdapter<>(BUNDLE, PREFIX, "third", TestEnum.class, TestBean::getThird, null),
            new FunctionColumnAdapter<>(BUNDLE, PREFIX, "forth", Boolean.class, TestBean::getFourth, null)
    );
    private final BeanListTableModel<TestBean> model = new BeanListTableModel<>(ADAPTERS);
    private final DecoratedTable<?, ?> table = new DecoratedTable<>(model);
    private final ColumnConfiguration configuration = mock(ColumnConfiguration.class);
    private final ValueClassColumnWidthCalculator calculator = new ValueClassColumnWidthCalculator(table, configuration);
    private final TableColumnModel columnModel = table.getColumnModel();
    private final Random random = new Random();

    @Test
    public void isFixedWidth() throws Exception {
        assertThat(calculator.isFixedWidth(columnModel.getColumn(0))).isFalse();
        assertThat(calculator.isFixedWidth(columnModel.getColumn(1))).isTrue();
        assertThat(calculator.isFixedWidth(columnModel.getColumn(2))).isTrue();
        assertThat(calculator.isFixedWidth(columnModel.getColumn(3))).isTrue();
    }

    @Test
    public void preferredWidth() throws Exception {
        final String numberPrototype = "000000.00";
        when(configuration.getPrototypeValue(columnModel.getColumn(1))).thenReturn(numberPrototype);

        assertThat(calculator.preferredWidth(columnModel.getColumn(0))).isEqualTo(getWidth(BUNDLE.getString(PREFIX + "first")));
        assertThat(calculator.preferredWidth(columnModel.getColumn(1))).isEqualTo(getWidth(numberPrototype));
        assertThat(calculator.preferredWidth(columnModel.getColumn(2))).isEqualTo(getWidth(TestEnum.ExtraLong.toString()));
        assertThat(calculator.preferredWidth(columnModel.getColumn(3))).isEqualTo(getWidth(BUNDLE.getString(PREFIX + "forth")));
    }

    private int getWidth(String prototype) {
        Component renderer = getHeaderRenderer(prototype);
        Insets insets = ((JComponent) renderer).getInsets();
        return renderer.getPreferredSize().width + insets.left + insets.right + 2;
    }

    private Component getHeaderRenderer(Object value) {
        return table.getTableHeader().getDefaultRenderer().getTableCellRendererComponent(table, value, false, false, -1, 0);
    }

    private enum TestEnum { Short, Long, ExtraLong }

    private static class TestBean {
        private final String first;
        private final Long second;
        private final TestEnum third;
        private final Boolean fourth;

        public TestBean(String first, Long second, TestEnum third, Boolean fourth) {
            this.first = first;
            this.second = second;
            this.third = third;
            this.fourth = fourth;
        }

        public String getFirst() {
            return first;
        }

        public Long getSecond() {
            return second;
        }

        public TestEnum getThird() {
            return third;
        }

        public Boolean getFourth() {
            return fourth;
        }
    }
}