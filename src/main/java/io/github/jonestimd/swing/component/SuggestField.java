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
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.Format;

import javax.swing.text.JTextComponent;

import io.github.jonestimd.swing.validation.Validator;

/**
 * An editable combo box that keeps the popup menu visible and updates the menu items when the input text changes.
 * Handles the the following keyboard input:
 * <ul>
 *     <li>any key except <strong>escape</strong> and <strong>enter</strong> will show the popup menu</li>
 *     <li><strong>ctrl-space</strong> clears the selection and moves the cursor to the end of the input text</li>
 * </ul>
 */
public class SuggestField<T> extends BeanListComboBox<T> {
    public SuggestField(Format format, Validator<String> validator, SuggestModel<T> model) {
        this(format, format, validator, model);
    }

    public SuggestField(Format format, Format itemFormat, Validator<String> validator, SuggestModel<T> model) {
        super(format, itemFormat, validator, model);
        getEditorComponent().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent event) {
                SuggestField.this.processEditorKeyEvent(event);
            }
        });
        getEditorComponent().addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                setPopupVisible(true);
            }

            @Override
            public void focusLost(FocusEvent e) {
                moveToEnd();
            }
        });
        addItemListener(event -> {
            if (event.getStateChange() == ItemEvent.SELECTED && event.getItem() != null) getModel().updateSuggestions(getEditorText());
        });
    }

    private void moveToEnd() {
        JTextComponent editorComponent = getEditorComponent();
        int length = editorComponent.getText().length();
        editorComponent.setSelectionStart(length);
        editorComponent.setSelectionEnd(length);
    }

    protected void processEditorKeyEvent(KeyEvent event) {
        int position = getEditorComponent().getCaretPosition();
        int selectionStart = getEditorComponent().getSelectionStart();
        int selectionEnd = getEditorComponent().getSelectionEnd();
        setSelectedItem(getModel().updateSuggestions(getEditorText()));
        if (event.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {
            int length = getEditorText().length();
            getEditorComponent().setCaretPosition(Math.min(position, length));
            getEditorComponent().setSelectionStart(Math.min(selectionStart, length));
            getEditorComponent().setSelectionEnd(Math.min(selectionEnd, length));
        }

        int keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.VK_SPACE && event.getModifiersEx() == KeyEvent.CTRL_DOWN_MASK) moveToEnd();
        else if (keyCode != KeyEvent.VK_ENTER && keyCode != KeyEvent.VK_ESCAPE && !isPopupVisible()) setPopupVisible(true);
    }

    @Override
    public SuggestModel<T> getModel() {
        return (SuggestModel<T>) super.getModel();
    }
}
