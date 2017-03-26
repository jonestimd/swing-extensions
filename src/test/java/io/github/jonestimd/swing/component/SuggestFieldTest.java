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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.plaf.basic.BasicComboBoxUI;

import io.github.jonestimd.swing.validation.Validator;
import io.github.jonestimd.text.StringFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SuggestFieldTest {
    @Mock
    private BasicComboBoxUI comboBoxUI;

    private List<String> updateArgs;

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

    @Before
    public void resetUpdateArgs() {
        updateArgs = new ArrayList<>();
    }

    @Test
    public void createFieldWithoutValidation() throws Exception {
        TestSuggestField field = new TestSuggestField(Arrays.asList("one", "two"));

        assertThat(field.getModel()).containsOnly("one", "two");
    }

    @Test
    public void setsPopupVisibleOnKeyRelease() throws Exception {
        TestSuggestField field = new TestSuggestField(Arrays.asList("one", "two"));
        field.setUI(comboBoxUI);
        when(comboBoxUI.isPopupVisible(field)).thenReturn(false);

        getKeyListener(field).keyReleased(newKeyEvent(field, KeyEvent.VK_X, 'x'));

        verify(comboBoxUI).setPopupVisible(field, true);
    }

    @Test
    public void doesNotSetPopupVisibleWhenAlreadyShowing() throws Exception {
        TestSuggestField field = new TestSuggestField(Arrays.asList("one", "two"));
        field.setUI(comboBoxUI);
        when(comboBoxUI.isPopupVisible(field)).thenReturn(true);

        getKeyListener(field).keyReleased(newKeyEvent(field, KeyEvent.VK_X, 'x'));

        verify(comboBoxUI, never()).setPopupVisible(field, true);
    }

    @Test
    public void doesNotSetPopupVisibleOnEscape() throws Exception {
        TestSuggestField field = new TestSuggestField(Arrays.asList("one", "two"));
        field.setUI(comboBoxUI);

        getKeyListener(field).keyReleased(newKeyEvent(field, KeyEvent.VK_ESCAPE, (char)0));

        verify(comboBoxUI, never()).setPopupVisible(field, true);
    }

    @Test
    public void doesNotSetPopupVisibleOnEnter() throws Exception {
        TestSuggestField field = new TestSuggestField(Arrays.asList("one", "two"));
        field.setUI(comboBoxUI);

        getKeyListener(field).keyReleased(newKeyEvent(field, KeyEvent.VK_ENTER, (char)0));

        verify(comboBoxUI, never()).setPopupVisible(field, true);
    }

    @Test
    public void updatesSuggestionsOnKeyRelease() throws Exception {
        TestSuggestField field = new TestSuggestField(Arrays.asList("one", "two"));
        field.getEditorComponent().setText("abc");
        field.setUI(comboBoxUI);

        getKeyListener(field).keyReleased(newKeyEvent(field, KeyEvent.VK_X, 'x'));

        assertThat(updateArgs).containsExactly("abc");
    }

    @Test
    public void updatesSuggestionsOnSpaceKeyRelease() throws Exception {
        TestSuggestField field = new TestSuggestField(Arrays.asList("one", "two"));
        field.getEditorComponent().setText("abc");
        field.setUI(comboBoxUI);

        getKeyListener(field).keyReleased(newKeyEvent(field, KeyEvent.VK_SPACE, ' '));

        assertThat(updateArgs).containsExactly("abc");
    }

    @Test
    public void movesCaretToEntOnCtrlSpace() throws Exception {
        TestSuggestField field = new TestSuggestField(Arrays.asList("one", "two"));
        field.getEditorComponent().setText("abc");
        field.getEditorComponent().setSelectionStart(1);
        field.getEditorComponent().setSelectionEnd(2);
        field.setUI(comboBoxUI);

        getKeyListener(field).keyReleased(newKeyEvent(field, KeyEvent.CTRL_DOWN_MASK, KeyEvent.VK_SPACE, ' '));

        assertThat(updateArgs).containsExactly("abc");
        assertThat(field.getEditorComponent().getSelectionStart()).isEqualTo(3);
        assertThat(field.getEditorComponent().getSelectionEnd()).isEqualTo(3);
    }

    @Test
    public void showsPopupOnFocusGained() throws Exception {
        TestSuggestField field = new TestSuggestField(Arrays.asList("one", "two"));
        field.setUI(comboBoxUI);

        getFocusListener(field).focusGained(null);

        verify(comboBoxUI).setPopupVisible(field, true);
    }

    @Test
    public void movesCaretToEntOnFocusLost() throws Exception {
        TestSuggestField field = new TestSuggestField(Arrays.asList("one", "two"));
        field.getEditorComponent().setText("abc");
        field.getEditorComponent().setSelectionStart(1);
        field.getEditorComponent().setSelectionEnd(2);
        field.setUI(comboBoxUI);

        getFocusListener(field).focusLost(null);

        assertThat(field.getEditorComponent().getSelectionStart()).isEqualTo(3);
        assertThat(field.getEditorComponent().getSelectionEnd()).isEqualTo(3);
    }

    private class TestSuggestField extends SuggestField<String> {
        protected TestSuggestField(List<String> suggestions) {
            super(new StringFormat(), Validator.empty(), suggestions);
        }

        @Override
        protected void updateSuggestions(String editorText) {
            updateArgs.add(editorText);
        }
    }
}