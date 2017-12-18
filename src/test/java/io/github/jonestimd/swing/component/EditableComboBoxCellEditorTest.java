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
import java.util.function.Supplier;

import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.github.jonestimd.AsyncTest;
import io.github.jonestimd.swing.validation.Validator;
import io.github.jonestimd.util.Streams;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EditableComboBoxCellEditorTest extends AbstractComboBoxCellEditorTest {
    @Mock
    private Validator<String> validator;
    @Mock
    private Supplier<TestBean> saveService;
    private TestBean savedBean;

    private List<TestBean> comboBoxValues = ImmutableList.of(new TestBean("abc"), new TestBean("adf"), new TestBean("xyz"));

    @Test
    public void tableCellEditorComponentLoadsValues() throws Exception {
        final TestBean selectedItem = new TestBean("cdf");
        TestEditor editor = new TestEditor();

        SwingUtilities.invokeAndWait(() -> {
            editor.getTableCellEditorComponent(new JTable(), selectedItem, false, 0, 0);
        });

        AsyncTest.timeout(10000L, () -> editor.getComboBoxModel().getSize() == 5);
        SwingUtilities.invokeAndWait(() -> {
            checkModel(editor.getComboBoxModel(), selectedItem);
            assertThat(editor.getComboBoxModel().getSelectedItem()).isSameAs(selectedItem);
        });
    }

    @SuppressWarnings("unchecked")
    private void checkModel(LazyLoadComboBoxModel<TestBean> model, TestBean... otherBeans) {
        List<TestBean> items = Streams.toList(model);
        assertThat(items).hasSize(comboBoxValues.size() + 1 + otherBeans.length);
        assertThat(items.get(0)).isNull();
        assertThat(items.containsAll(comboBoxValues)).isTrue();
        for (TestBean bean : otherBeans) {
            assertThat(items).contains(bean);
        }
    }

    @Test
    public void stopCellEditingReturnsTrueForEmptyOptionalValue() throws Exception {
        when(validator.validate(anyString())).thenReturn(null);
        TestEditor editor = new TestEditor();
        SwingUtilities.invokeAndWait(() -> {
            editor.getTableCellEditorComponent(new JTable(), null, false, 0, 0);

            assertThat(editor.stopCellEditing()).isTrue();

            assertThat(editor.getCellEditorValue()).isNull();
        });
    }

    @Test
    public void stopCellEditingReturnsTrueForExistingValue() throws Exception {
        when(validator.validate(anyString())).thenReturn(null);
        TestEditor editor = new TestEditor();
        SwingUtilities.invokeAndWait(() -> {
            editor.getTableCellEditorComponent(new JTable(), comboBoxValues.get(1), false, 0, 0);

            assertThat(editor.stopCellEditing()).isTrue();

            assertThat(editor.getCellEditorValue()).isSameAs(comboBoxValues.get(1));
        });
    }

    @Test
    public void stopCellEditingReturnsTrueForNewlySavedValue() throws Exception {
        final TestBean savedItem = new TestBean("saved");
        when(validator.validate(anyString())).thenReturn(null);
        when(saveService.get()).thenReturn(savedItem);
        TestEditor editor = new TestEditor();
        SwingUtilities.invokeAndWait(() -> {
            BeanListComboBox component = (BeanListComboBox) editor.getTableCellEditorComponent(new JTable(), null, false, 0, 0);
            ((JTextField)component.getEditor().getEditorComponent()).setText("selected");

            assertThat(editor.stopCellEditing()).isTrue();

            assertThat(editor.getCellEditorValue()).isEqualTo(savedItem);
            assertThat(savedBean.name).isEqualTo("selected");
        });
    }

    @Test
    public void stopCellEditingReturnsFalseForUnsavedValue() throws Exception {
        final String name = "new item";
        when(validator.validate(anyString())).thenReturn(null);
        TestEditor editor = new TestEditor();
        SwingUtilities.invokeAndWait(() -> {
            BeanListComboBox component = (BeanListComboBox) editor.getTableCellEditorComponent(new JTable(), null, false, 0, 0);
            ((JTextField)component.getEditor().getEditorComponent()).setText(name);

            assertThat(editor.stopCellEditing()).isFalse();

            assertThat(editor.getCellEditorValue()).isNull();
            assertThat(savedBean.name).isEqualTo(name);
        });
    }

    @Test
    public void stopCellEditingReturnsFalseForValidationError() throws Exception {
        when(validator.validate(anyString())).thenReturn("error");
        TestEditor editor = new TestEditor();
        SwingUtilities.invokeAndWait(() -> {
            editor.getTableCellEditorComponent(new JTable(), null, false, 0, 0);

            assertThat(editor.stopCellEditing()).isFalse();

            verifyZeroInteractions(saveService);
        });
    }

    private class TestEditor extends EditableComboBoxCellEditor<TestBean> {
        protected TestEditor() {
            super(new TestFormat(), validator, "loading");
        }

        @Override
        protected TestBean saveItem(TestBean item) {
            savedBean = item;
            return saveService.get();
        }

        @Override
        protected List<TestBean> getComboBoxValues() {
            return Lists.newArrayList(comboBoxValues);
        }
    }
}