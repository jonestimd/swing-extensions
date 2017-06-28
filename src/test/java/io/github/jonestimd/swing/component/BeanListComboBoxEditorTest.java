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

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;

import io.github.jonestimd.swing.validation.Validator;
import io.github.jonestimd.text.StringFormat;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class BeanListComboBoxEditorTest {
    private BeanListComboBoxModel<String> model = new BeanListComboBoxModel<>(Arrays.asList("Apple", "Banana", "Cherry"));
    private JComboBox<String> comboBox = new JComboBox<>(model);
    private BeanListComboBoxEditor<String> editor = new BeanListComboBoxEditor<>(comboBox, new StringFormat(), Validator.empty());

    @Before
    public void setUp() throws Exception {
        comboBox.setEditor(editor);
        comboBox.setEditable(true);
    }

    @Test
    public void updatesTextToMatchSelectedItemOnFocusLost() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            editor.getEditorComponent().setText("apple");
            FocusEvent event = new FocusEvent(new JButton(), FocusEvent.FOCUS_LOST);
            for (FocusListener listener : editor.getEditorComponent().getFocusListeners()) {
                listener.focusLost(event);
            }
        });

        SwingUtilities.invokeAndWait(() -> {}); // wait for auto-complete to get processed
        assertThat(editor.getEditorComponent().getText()).isEqualTo("Apple");
        assertThat(comboBox.getSelectedItem()).isEqualTo("Apple");
    }

    @Test
    public void doesNotUpdateTextOnFocusLostIfNoSelectedItem() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            editor.getEditorComponent().setText("x");
            FocusEvent event = new FocusEvent(new JButton(), FocusEvent.FOCUS_LOST);
            for (FocusListener listener : editor.getEditorComponent().getFocusListeners()) {
                listener.focusLost(event);
            }
        });

        assertThat(editor.getEditorComponent().getText()).isEqualTo("x");
        assertThat(comboBox.getSelectedItem()).isNull();
    }

    @Test
    public void autoSelectDoesNothingWhenTextIsSelected() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            editor.getEditorComponent().setText("app");
            editor.getEditorComponent().setSelectionStart(0);
            editor.getEditorComponent().setSelectionEnd(2);
        });
        SwingUtilities.invokeAndWait(() -> {}); // wait for auto-complete to get processed

        assertThat(comboBox.getSelectedItem()).isNull();
        assertThat(editor.getItem()).isEqualTo("app");
    }

    @Test
    public void autoSelectDoesNothingWhenTextIsCleared() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            editor.getEditorComponent().setText("a");
            editor.getEditorComponent().setText("");
        });
        SwingUtilities.invokeAndWait(() -> {}); // wait for auto-complete to get processed

        assertThat(comboBox.getSelectedItem()).isNull();
        assertThat(editor.getItem()).isNull();
        assertThat(editor.getEditorComponent().getText()).isEmpty();
    }

    @Test
    public void autoSelectUpdatesSelectedItemAndText() throws Exception {
        String value = model.getElementAt(0);
        String text = value.toLowerCase().substring(0, 2);

        SwingUtilities.invokeAndWait(() -> editor.getEditorComponent().setText(text));
        SwingUtilities.invokeAndWait(() -> {}); // wait for auto-complete to get processed

        assertThat(comboBox.getSelectedItem()).isEqualTo(value);
        assertThat(editor.getEditorComponent().getText()).isEqualTo(value.toLowerCase());
        assertThat(editor.getEditorComponent().getCaretPosition()).isEqualTo(value.length());
        assertThat(editor.getEditorComponent().getSelectionStart()).isEqualTo(text.length());
        assertThat(editor.getEditorComponent().getSelectionEnd()).isEqualTo(value.length());
    }

    @Test
    public void autoSelectRetainsCaretPosition() throws Exception {
        String value = model.getElementAt(0);
        String text = value.toLowerCase();
        int position = 2;
        SwingUtilities.invokeAndWait(() -> {
            editor.getEditorComponent().setText(text.substring(0, position) + text.substring(position+1));
            editor.getEditorComponent().setCaretPosition(position);
        });

        SwingUtilities.invokeAndWait(() -> {
            try {
                editor.getEditorComponent().getDocument().insertString(position, text.substring(position, position + 1), null);
            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }
        });
        SwingUtilities.invokeAndWait(() -> {}); // wait for auto-complete to get processed

        assertThat(comboBox.getSelectedItem()).isEqualTo(value);
        assertThat(editor.getEditorComponent().getText()).isEqualTo(value.toLowerCase());
        assertThat(editor.getEditorComponent().getCaretPosition()).isEqualTo(value.length());
        assertThat(editor.getEditorComponent().getSelectionStart()).isEqualTo(position+1);
        assertThat(editor.getEditorComponent().getSelectionEnd()).isEqualTo(value.length());
    }
}
