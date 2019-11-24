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

import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.plaf.basic.BasicComboBoxUI;

import io.github.jonestimd.swing.SwingEdtRule;
import io.github.jonestimd.swing.validation.Validator;
import io.github.jonestimd.text.StringFormat;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SuggestFieldTest {
    @Rule
    public final SwingEdtRule swingEdtRule = new SwingEdtRule();

    private BasicComboBoxUI comboBoxUI = mock(BasicComboBoxUI.class);
    @SuppressWarnings("unchecked")
    private SuggestModel<String> model = mock(SuggestModel.class);

    private KeyListener getKeyListener(SuggestField<?> field) {
        KeyListener[] listeners = field.getEditorComponent().getKeyListeners();
        return listeners[listeners.length-1];
    }

    private KeyEvent newKeyEvent(SuggestField<?> field, int keyCode, char keyChar) {
        return newKeyEvent(field, 0, keyCode, keyChar);
    }

    private FocusListener getFocusListener(SuggestField<?> field) {
        FocusListener[] listeners = field.getEditorComponent().getFocusListeners();
        return listeners[listeners.length-1];
    }

    private KeyEvent newKeyEvent(SuggestField<?> field, int modifiers, int keyCode, char keyChar) {
        return new KeyEvent(field.getEditorComponent(), KeyEvent.KEY_RELEASED, System.currentTimeMillis(), modifiers, keyCode, keyChar);
    }

    @Test
    public void createFieldWithoutValidation() throws Exception {
        SuggestField<String> field = new SuggestField<>(new StringFormat(), Validator.empty(), model);

        assertThat((Object) field.getModel()).isSameAs(model);
    }

    @Test
    public void setsPopupVisibleOnKeyRelease() throws Exception {
        SuggestField<String> field = new SuggestField<>(new StringFormat(), Validator.empty(), model);
        field.setUI(comboBoxUI);
        when(comboBoxUI.isPopupVisible(field)).thenReturn(false);

        getKeyListener(field).keyReleased(newKeyEvent(field, KeyEvent.VK_X, 'x'));

        verify(comboBoxUI).setPopupVisible(field, true);
    }

    @Test
    public void doesNotSetPopupVisibleWhenAlreadyShowing() throws Exception {
        SuggestField<String> field = new SuggestField<>(new StringFormat(), Validator.empty(), model);
        field.setUI(comboBoxUI);
        when(comboBoxUI.isPopupVisible(field)).thenReturn(true);

        getKeyListener(field).keyReleased(newKeyEvent(field, KeyEvent.VK_X, 'x'));

        verify(comboBoxUI, never()).setPopupVisible(field, true);
    }

    @Test
    public void doesNotSetPopupVisibleOnEscape() throws Exception {
        SuggestField<String> field = new SuggestField<>(new StringFormat(), Validator.empty(), model);
        field.setUI(comboBoxUI);

        getKeyListener(field).keyReleased(newKeyEvent(field, KeyEvent.VK_ESCAPE, (char)0));

        verify(comboBoxUI, never()).setPopupVisible(field, true);
    }

    @Test
    public void doesNotSetPopupVisibleOnEnter() throws Exception {
        SuggestField<String> field = new SuggestField<>(new StringFormat(), Validator.empty(), model);
        field.setUI(comboBoxUI);

        getKeyListener(field).keyReleased(newKeyEvent(field, KeyEvent.VK_ENTER, (char)0));

        verify(comboBoxUI, never()).setPopupVisible(field, true);
    }

    @Test
    public void updatesSuggestionsOnKeyRelease() throws Exception {
        SuggestField<String> field = new SuggestField<>(new StringFormat(), Validator.empty(), model);
        field.getEditorComponent().setText("abc");
        field.setUI(comboBoxUI);

        getKeyListener(field).keyReleased(newKeyEvent(field, KeyEvent.VK_X, 'x'));

        verify(model).updateSuggestions("abc");
    }

    @Test
    public void updatesSuggestionsOnSpaceKeyRelease() throws Exception {
        SuggestField<String> field = new SuggestField<>(new StringFormat(), Validator.empty(), model);
        field.getEditorComponent().setText("abc");
        field.setUI(comboBoxUI);

        getKeyListener(field).keyReleased(newKeyEvent(field, KeyEvent.VK_SPACE, ' '));

        verify(model).updateSuggestions("abc");
    }

    @Test
    public void movesCaretToEntOnCtrlSpace() throws Exception {
        SuggestField<String> field = new SuggestField<>(new StringFormat(), Validator.empty(), model);
        field.getEditorComponent().setText("abc");
        field.getEditorComponent().setSelectionStart(1);
        field.getEditorComponent().setSelectionEnd(2);
        field.setUI(comboBoxUI);

        getKeyListener(field).keyReleased(newKeyEvent(field, KeyEvent.CTRL_DOWN_MASK, KeyEvent.VK_SPACE, ' '));

        verify(model).updateSuggestions("abc");
        assertThat(field.getEditorComponent().getSelectionStart()).isEqualTo(3);
        assertThat(field.getEditorComponent().getSelectionEnd()).isEqualTo(3);
    }

    @Test
    public void showsPopupOnFocusGained() throws Exception {
        SuggestField<String> field = new SuggestField<>(new StringFormat(), Validator.empty(), model);
        field.setUI(comboBoxUI);

        getFocusListener(field).focusGained(null);

        verify(comboBoxUI).setPopupVisible(field, true);
    }

    @Test
    public void movesCaretToEntOnFocusLost() throws Exception {
        SuggestField<String> field = new SuggestField<>(new StringFormat(), Validator.empty(), model);
        field.getEditorComponent().setText("abc");
        field.getEditorComponent().setSelectionStart(1);
        field.getEditorComponent().setSelectionEnd(2);
        field.setUI(comboBoxUI);

        getFocusListener(field).focusLost(null);

        assertThat(field.getEditorComponent().getSelectionStart()).isEqualTo(3);
        assertThat(field.getEditorComponent().getSelectionEnd()).isEqualTo(3);
    }

    @Test
    public void setSelectedItemUpdatesSuggestionsUsingEditorText() throws Exception {
        SuggestField<String> field = new SuggestField<>(new StringFormat(), Validator.empty(), model);
        field.getEditorComponent().setText("abc");
        when(model.getSelectedItem()).thenReturn("abcd");

        field.setSelectedItem("abcd");

        verify(model).updateSuggestions("abc");
    }
}