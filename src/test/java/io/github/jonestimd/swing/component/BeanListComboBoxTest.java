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
package io.github.jonestimd.swing.component;

import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Objects;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JList;
import javax.swing.text.JTextComponent;

import com.google.common.collect.Lists;
import io.github.jonestimd.swing.validation.RequiredValidator;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BeanListComboBoxTest {
    @Test(expected = IllegalArgumentException.class)
    public void setModelRequiresBeanListComboBoxModel() throws Exception {
        BeanListComboBox<TestBean> comboBox = new BeanListComboBox<>(new BeanFormat());

        comboBox.setModel(new DefaultComboBoxModel<>());
    }

    @Test
    public void firesItemStateChangeForNullSelection() throws Exception {
        ItemListener listener = mock(ItemListener.class);
        BeanListComboBox<TestBean> comboBox = new BeanListComboBox<>(new BeanFormat(), Lists.newArrayList(null, new TestBean("aaa")));
        comboBox.addItemListener(listener);

        comboBox.setSelectedIndex(1);
        comboBox.setSelectedIndex(0);

        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).itemStateChanged(itemEvent(null, ItemEvent.DESELECTED));
        inOrder.verify(listener).itemStateChanged(itemEvent(comboBox.getModel().getElementAt(1), ItemEvent.SELECTED));
        inOrder.verify(listener).itemStateChanged(itemEvent(comboBox.getModel().getElementAt(1), ItemEvent.DESELECTED));
        inOrder.verify(listener).itemStateChanged(itemEvent(null, ItemEvent.SELECTED));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void keySelectionNoMatch() throws Exception {
        ArrayList<TestBean> beans = Lists.newArrayList(null, new TestBean("abc"), new TestBean("aei"), new TestBean("eio"));
        BeanListComboBox<TestBean> comboBox = new BeanListComboBox<>(new BeanFormat(), beans);

        comboBox.selectWithKeyChar('x');

        assertThat(comboBox.getSelectedIndex()).isEqualTo(0);
    }

    @Test
    public void keySelectionByInitialChar() throws Exception {
        ArrayList<TestBean> beans = Lists.newArrayList(null, new TestBean("abc"), new TestBean("aei"), new TestBean("eio"));
        BeanListComboBox<TestBean> comboBox = new BeanListComboBox<>(new BeanFormat(), beans);

        comboBox.selectWithKeyChar('a');

        assertThat(comboBox.getSelectedIndex()).isEqualTo(1);
    }

    @Test
    public void keySelectionByPrefix() throws Exception {
        ArrayList<TestBean> beans = Lists.newArrayList(null, new TestBean("abc"), new TestBean("aei"), new TestBean("eio"));
        BeanListComboBox<TestBean> comboBox = new BeanListComboBox<>(new BeanFormat(), beans);

        comboBox.selectWithKeyChar('a');
        comboBox.selectWithKeyChar('e');

        assertThat(comboBox.getSelectedIndex()).isEqualTo(2);
    }

    private ItemEvent itemEvent(Object item, int stateChange) {
        return argThat(new BaseMatcher<ItemEvent>() {
            @Override
            public boolean matches(Object o) {
                ItemEvent event = (ItemEvent) o;
                return Objects.equals(event.getItem(), item) && event.getStateChange() == stateChange;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("ItemEvent:item=").appendValue(item);
            }
        });
    }

    @Test
    public void noValidation() throws Exception {
        PropertyChangeListener listener = mock(PropertyChangeListener.class);
        BeanListComboBox<TestBean> comboBox = new BeanListComboBox<>(new BeanFormat(), Lists.newArrayList(null, new TestBean("aaa")));
        comboBox.addPropertyChangeListener(BeanListComboBox.VALIDATION_MESSAGES, listener);

        comboBox.validateValue();

        verifyZeroInteractions(listener);
    }

    @Test
    public void editableNotValidated() throws Exception {
        PropertyChangeListener listener = mock(PropertyChangeListener.class);
        BeanListComboBox<TestBean> comboBox = new BeanListComboBox<>(new BeanFormat(), Lists.newArrayList(null, new TestBean("aaa")));
        comboBox.setEditable(true);
        comboBox.addPropertyChangeListener(BeanListComboBox.VALIDATION_MESSAGES, listener);

        comboBox.validateValue();

        assertThat(comboBox.getValidationMessages()).isNull();
        verifyZeroInteractions(listener);
    }

    @Test
    public void validatesValueWhenSelectionChanges() throws Exception {
        PropertyChangeListener listener = mock(PropertyChangeListener.class);
        BeanListComboBox<TestBean> comboBox = new BeanListComboBox<>(new BeanFormat(), "required");
        comboBox.addValidationListener(listener);
        comboBox.getModel().setElements(Lists.newArrayList(new TestBean("aaa")), false);
        assertThat(comboBox.getValidationMessages()).isEqualTo("required");

        comboBox.setSelectedIndex(0);

        assertThat(comboBox.getValidationMessages()).isNull();
        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).propertyChange(propertyEvent(BeanListComboBox.VALIDATION_MESSAGES, "required", null));
        inOrder.verify(listener).propertyChange(propertyEvent(BeanListComboBox.VALIDATION_MESSAGES, null, null));
    }

    @Test
    public void rendererDoesNotShowValidationErrorForSelectedValue() throws Exception {
        Graphics2D g = mock(Graphics2D.class);
        BeanListComboBox<TestBean> comboBox = new BeanListComboBox<>(new BeanFormat(), "required");
        comboBox.getModel().setElements(Lists.newArrayList(new TestBean("aaa"), new TestBean("bbb")), false);
        Component component = comboBox.getRenderer().getListCellRendererComponent(new JList<TestBean>(), null, 0, false, false);

        component.paint(g);

        verifyZeroInteractions(g);
    }

    @Test
    public void rendererShowsValidationErrorForNoSelectedValue() throws Exception {
        FontMetrics fm = mock(FontMetrics.class);
        Graphics g = mock(Graphics.class);
        Graphics2D g2d = mock(Graphics2D.class);
        when(g.getFontMetrics(any())).thenReturn(fm);
        when(g.create(anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(g2d);
        BeanListComboBox<TestBean> comboBox = new BeanListComboBox<>(new BeanFormat(), "required");
        comboBox.getModel().setElements(Lists.newArrayList(new TestBean("aaa"), new TestBean("bbb")), false);
        Component component = comboBox.getRenderer().getListCellRendererComponent(new JList<TestBean>(), null, -1, false, false);

        component.paint(g);

        verify(g).create(anyInt(), anyInt(), anyInt(), anyInt());
        verify(g2d).dispose();
    }

    @Test
    public void removeValidationListener() throws Exception {
        PropertyChangeListener listener = mock(PropertyChangeListener.class);
        BeanListComboBox<TestBean> comboBox = new BeanListComboBox<>(new BeanFormat(), "required");
        comboBox.addValidationListener(listener);
        comboBox.removeValidationListener(listener);
        comboBox.getModel().setElements(Lists.newArrayList(new TestBean("aaa")), false);

        comboBox.setSelectedIndex(0);

        verifyZeroInteractions(listener);
    }

    @Test
    public void validatedEditableComboBox() throws Exception {
        RequiredValidator validator = new RequiredValidator("required");
        BeanListComboBox<TestBean> comboBox = new BeanListComboBox<>(new BeanFormat(), validator, Lists.newArrayList(new TestBean("aaa")));
        PropertyChangeListener listener = mock(PropertyChangeListener.class);
        comboBox.addValidationListener(listener);
        assertThat(comboBox.getValidationMessages()).isEqualTo("required");

        ((JTextComponent) comboBox.getEditor().getEditorComponent()).setText("x");

        assertThat(comboBox.getValidationMessages()).isNull();
        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).propertyChange(propertyEvent(BeanListComboBox.VALIDATION_MESSAGES, "required", null));
        inOrder.verify(listener).propertyChange(propertyEvent(BeanListComboBox.VALIDATION_MESSAGES, null, null));
    }

    @Test
    public void settingEnabledSetsEditorEditable() throws Exception {
        RequiredValidator validator = new RequiredValidator("required");
        BeanListComboBox<TestBean> comboBox = new BeanListComboBox<>(new BeanFormat(), validator, Lists.newArrayList(new TestBean("aaa")));

        comboBox.setEnabled(false);
        assertThat(comboBox.getEditorComponent().isEditable()).isFalse();

        comboBox.setEnabled(true);
        assertThat(comboBox.getEditorComponent().isEditable()).isTrue();
    }

    private PropertyChangeEvent propertyEvent(String property, Object oldValue, Object newValue) {
        return argThat(new BaseMatcher<PropertyChangeEvent>() {
            @Override
            public boolean matches(Object o) {
                PropertyChangeEvent event = (PropertyChangeEvent) o;
                return property.equals(event.getPropertyName()) &&
                        Objects.equals(event.getOldValue(), oldValue) && Objects.equals(event.getNewValue(), newValue);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("PropertyChangeEvent:name=").appendValue(property)
                        .appendText(",oldValue=").appendValue(oldValue)
                        .appendText(",newValue=").appendValue(newValue);
            }
        });
    }

    @Test
    public void getSelectedItemParsesTextInput() throws Exception {
        RequiredValidator validator = new RequiredValidator("required");
        BeanListComboBox<TestBean> comboBox = new BeanListComboBox<>(new BeanFormat(), validator, Lists.newArrayList(new TestBean("aaa")));

        ((JTextComponent) comboBox.getEditor().getEditorComponent()).setText("bbb");

        assertThat(comboBox.getSelectedItem()).isNull();
        assertThat(((TestBean) comboBox.getEditor().getItem()).name).isEqualTo("bbb");
    }

    private static class TestBean {
        public final String name;

        public TestBean(String name) {
            this.name = name;
        }
    }

    private static class BeanFormat extends Format {
        @Override
        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            if (obj != null) {
                toAppendTo.append(((TestBean) obj).name);
            }
            return toAppendTo;
        }

        @Override
        public Object parseObject(String source, ParsePosition pos) {
            pos.setIndex(source.length());
            return new TestBean(source);
        }
    }
}