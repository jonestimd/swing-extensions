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
        super(format, validator, model);
        getEditorComponent().addKeyListener(new KeyAdapter() {
            @Override
                public void keyReleased(KeyEvent event) {
                    updateSuggestions(getEditorText());
                    int keyCode = event.getKeyCode();
                    if (keyCode == KeyEvent.VK_SPACE && event.getModifiersEx() == KeyEvent.CTRL_DOWN_MASK) moveToEnd();
                    else if (keyCode != KeyEvent.VK_ENTER && keyCode != KeyEvent.VK_ESCAPE && ! isPopupVisible()) setPopupVisible(true);
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

    /**
     * Called whenever the input text changes.  Must be implemented to update the model based on the editor text.
     * @param editorText the current text from the editor
     */
    protected void updateSuggestions(String editorText) {
        setSelectedItem(getModel().updateSuggestions(editorText));
    }

    @Override
    public SuggestModel<T> getModel() {
        return (SuggestModel<T>) super.getModel();
    }
}
