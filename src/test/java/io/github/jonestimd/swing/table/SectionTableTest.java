// The MIT License (MIT)
//
// Copyright (c) 2020 Timothy D. Jones
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

import java.awt.Color;
import java.awt.Component;
import java.util.Arrays;
import java.util.Collections;

import javax.swing.table.TableCellRenderer;

import io.github.jonestimd.swing.ComponentResources;
import io.github.jonestimd.swing.table.model.BeanListMultimapTableModel;
import io.github.jonestimd.swing.table.model.ColumnAdapter;
import io.github.jonestimd.swing.table.model.TestColumnAdapter;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SectionTableTest {
    private static final Color sectionRowBackground = ComponentResources.lookupColor(SectionTable.SECTION_ROW_BACKGROUND_KEY);
    private static final ColumnAdapter<TestBean, String> BEAN_NAME_ADAPTER = new TestColumnAdapter<>("Name", String.class, TestBean::getName);
    private static final ColumnAdapter<TestBean, String> BEAN_VALUE_ADAPTER = new TestColumnAdapter<>("Value", String.class, TestBean::getValue, TestBean::setValue);
    private final BeanListMultimapTableModel<TestGroup, TestBean> model = new BeanListMultimapTableModel<>(
            Arrays.asList(BEAN_NAME_ADAPTER, BEAN_VALUE_ADAPTER),
            Collections.emptyList(), TestBean::getGroup, TestGroup::getGroupName);
    private final SectionTable<TestBean, BeanListMultimapTableModel<TestGroup, TestBean>> table = new SectionTable<>(model);

    private TestGroup group1 = new TestGroup("group1");

    @Test
    public void setSectionRowBackground() throws Exception {
        assertThat(table.getSectionRowBackground()).isEqualTo(sectionRowBackground);

        table.setSectionRowBackground(Color.cyan);

        assertThat(table.getSectionRowBackground()).isEqualTo(Color.cyan);
    }

    @Test
    public void isSectionRow() throws Exception {
        model.setBeans(Arrays.asList(
                new TestBean(group1, "bean1a", "x"),
                new TestBean(group1, "bean1b", "x")));

        assertThat(table.isSectionRow(0)).isTrue();
        assertThat(table.isSectionRow(1)).isFalse();
        assertThat(table.isSectionRow(2)).isFalse();
    }

    @Test
    public void isCellEditable() throws Exception {
        model.setBeans(Arrays.asList(
                new TestBean(group1, "bean1a", "x"),
                new TestBean(group1, "bean1b", "x")));

        assertThat(table.isCellEditable(0, 0)).isFalse();
        assertThat(table.isCellEditable(0, 1)).isFalse();
        assertThat(table.isCellEditable(1, 0)).isFalse();
        assertThat(table.isCellEditable(1, 1)).isTrue();
        assertThat(table.isCellEditable(2, 0)).isFalse();
        assertThat(table.isCellEditable(2, 1)).isTrue();
    }

    @Test
    public void getValueAt() throws Exception {
        model.setBeans(Arrays.asList(
                new TestBean(group1, "bean1a", "x"),
                new TestBean(group1, "bean1b", "y")));

        assertThat(table.getValueAt(0, 0)).isEqualTo(group1.getGroupName());
        assertThat(table.getValueAt(0, 1)).isNull();
        assertThat(table.getValueAt(1, 0)).isEqualTo("bean1a");
        assertThat(table.getValueAt(1, 1)).isEqualTo("x");
        assertThat(table.getValueAt(2, 0)).isEqualTo("bean1b");
        assertThat(table.getValueAt(2, 1)).isEqualTo("y");
    }

    @Test
    public void getCellRenderer() throws Exception {
        model.setBeans(Arrays.asList(
                new TestBean(group1, "bean1a", "x"),
                new TestBean(group1, "bean1b", "x")));
        assertThat(table.getCellRenderer(0, 0).getClass().getSimpleName()).isEqualTo("DefaultSectionRowRenderer");
        assertThat(table.getCellRenderer(1, 0)).isEqualTo(table.getDefaultRenderer(String.class));

        final TableCellRenderer renderer = mock(TableCellRenderer.class);
        table.setSectionRowRenderer(renderer);

        assertThat(table.getCellRenderer(0, 0)).isSameAs(renderer);
    }

    @Test
    public void defaultSectionRowRenderer() throws Exception {
        model.setBeans(Arrays.asList(
                new TestBean(group1, "bean1a", "x"),
                new TestBean(group1, "bean1b", "x")));
        final TableCellRenderer renderer = table.getCellRenderer(0, 0);

        Component component = renderer.getTableCellRendererComponent(table, "", false, false, 0, 0);

        assertThat(component.getBackground()).isEqualTo(sectionRowBackground);
        assertThat(component.getFont().isBold()).isTrue();
    }

    private static class TestGroup {
        private final String groupName;

        private TestGroup(String groupName) {
            this.groupName = groupName;
        }

        public String getGroupName() {
            return groupName;
        }
    }

    private static class TestBean {
        private TestGroup group;
        private final String name;
        private String value;

        public TestBean(TestGroup group, String name, String value) {
            this.group = group;
            this.name = name;
            this.value = value;
        }

        public TestGroup getGroup() {
            return group;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String beanValue) {
            this.value = beanValue;
        }
    }
}