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

import java.util.List;

import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SwingUtilities;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.github.jonestimd.AsyncTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class BeanListComboBoxCellEditorTest extends ComboBoxCellEditorTest {
    private List<TestBean> comboBoxValues = ImmutableList.of(new TestBean("abc"), new TestBean("adf"), new TestBean("xyz"));

    @Test
    public void tableCellEditorComponentLoadsValues() throws Exception {
        TestEditor editor = new TestEditor();

        SwingUtilities.invokeAndWait(() -> {
            editor.getTableCellEditorComponent(new JTable(), null, false, 0, 0);
        });

        AsyncTest.timeout(10000L, () -> editor.getComboBoxModel().getSize() == 4);
        SwingUtilities.invokeAndWait(() -> {
            checkModel(editor.getComboBoxModel());
        });
    }

    @Test
    public void tableCellEditorComponentLoadsValuesAndSelectsValue() throws Exception {
        final TestBean selectedItem = new TestBean("adf");
        TestEditor editor = new TestEditor();

        SwingUtilities.invokeAndWait(() -> {
            editor.getTableCellEditorComponent(new JTable(), selectedItem, false, 0, 0);
        });

        AsyncTest.timeout(10000L, () -> editor.getComboBoxModel().getSize() == 4);
        SwingUtilities.invokeAndWait(() -> {
            checkModel(editor.getComboBoxModel());
            assertThat(editor.getComboBox().getSelectedItem()).isSameAs(selectedItem);
        });
    }

    @Test
    public void tableCellEditorComponentDoesNotReplaceValues() throws Exception {
        TestEditor editor = new TestEditor();
        editor.setListItems(Lists.newArrayList());

        SwingUtilities.invokeAndWait(() -> {
            editor.getTableCellEditorComponent(new JTable(), null, false, 0, 0);
        });

        SwingUtilities.invokeAndWait(() -> {
            assertThat(editor.getComboBoxModel().getSize()).isEqualTo(1);
            assertThat(editor.getComboBoxModel().getElementAt(0)).isNull();
        });
    }

    @Test
    public void treeCellEditorComponentLoadsValues() throws Exception {
        TestEditor editor = new TestEditor();

        SwingUtilities.invokeAndWait(() -> {
            editor.getTreeCellEditorComponent(new JTree(), null, false, false, false, 0);
        });

        AsyncTest.timeout(10000L, () -> editor.getComboBoxModel().getSize() == 4);
        SwingUtilities.invokeAndWait(() -> {
            checkModel(editor.getComboBoxModel());
        });
    }

    private void checkModel(LazyLoadComboBoxModel<TestBean> model) {
        assertThat(model.getSize()).isEqualTo(4);
        assertThat(model.getElementAt(0)).isNull();
        assertThat(model.getElementAt(1)).isEqualTo(comboBoxValues.get(0));
        assertThat(model.getElementAt(2)).isEqualTo(comboBoxValues.get(1));
        assertThat(model.getElementAt(3)).isEqualTo(comboBoxValues.get(2));
    }

    private class TestEditor extends BeanListComboBoxCellEditor<TestBean> {
        public TestEditor() {
            super(new TestFormat(), "loading values");
        }

        @Override
        protected List<TestBean> getComboBoxValues() {
            return Lists.newArrayList(comboBoxValues);
        }
    }
}