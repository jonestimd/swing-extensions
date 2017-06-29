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

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.Format;
import java.text.ParseException;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxEditor;

import io.github.jonestimd.swing.validation.ValidatedTextField;
import io.github.jonestimd.swing.validation.Validator;

/**
 * Provides the text field ({@link ValidatedTextField}) for an editable {@link BeanListComboBox}.
 * Uses {@link Format#parseObject(String)} to create an item from the input text.
 * @param <T> {@link BeanListComboBox} list item class
 */
public class BeanListComboBoxEditor<T> extends BasicComboBoxEditor {
    private ComboBoxModel<T> model;
    private Format format;
    private PrefixSelector<T> prefixSelector;
    private boolean autoSelecting = false;
    private DocumentListener documentHandler = new DocumentListener() {
        public void changedUpdate(DocumentEvent e) {
            documentChange();
        }

        public void insertUpdate(DocumentEvent e) {
            documentChange();
        }

        public void removeUpdate(DocumentEvent e) {
        }
    };

    /**
     * Create a combo box editor with the default {@link PrefixSelector} (first match alphabetically).
     */
    public BeanListComboBoxEditor(JComboBox<T> comboBox, Format format, Validator<String> validator) {
        this(comboBox, format, validator, new FormatPrefixSelector<>(format));
    }

    public BeanListComboBoxEditor(JComboBox<T> comboBox, Format format, Validator<String> validator, PrefixSelector<T> prefixSelector) {
        this.model = comboBox.getModel();
        this.format = format;
        this.prefixSelector = prefixSelector;
        editor = new BorderlessTextField("", 9, validator);
        editor.getDocument().addDocumentListener(documentHandler);
        editor.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (e.getOppositeComponent() != comboBox && getItem() != null) {
                    editor.setText(itemToString(getItem()));
                }
            }
        });
    }

    public boolean isAutoSelecting() {
        return autoSelecting;
    }

    @Override
    public ValidatedTextField getEditorComponent() {
        return (ValidatedTextField) super.getEditorComponent();
    }

    @Override
    public void setItem(Object anObject) {
        super.setItem(anObject == null ? null : format.format(anObject));
    }

    @Override
    public T getItem() {
        return getItem((String) super.getItem());
    }

    public boolean isNew(Object item) {
        return item != null && indexOf(format.format(item)) < 0;
    }

    @SuppressWarnings("unchecked")
    protected T getItem(String displayText) {
        if (displayText != null && displayText.length() > 0) {
            int index = indexOf(displayText);
            return (index >= 0 ? model.getElementAt(index) : parseInput(displayText));
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    protected T parseInput(String displayText) {
        try {
            return (T) format.parseObject(displayText);
        } catch (ParseException ex) {
            return null;
        }
    }

    private int indexOf(String displayText) {
        for (int i = 0; i < model.getSize(); i++) {
            if (displayText.equalsIgnoreCase(format.format(model.getElementAt(i)))) {
                return i;
            }
        }
        return -1;
    }

    protected String itemToString(Object item) {
        return item == null ? null : format.format(item);
    }

    @SuppressWarnings("unchecked")
    protected String getFirstMatch(String displayText) {
        Object item = prefixSelector.selectMatch(model, displayText);
        if (item != null) {
            autoSelecting = true;
            model.setSelectedItem(item);
            autoSelecting = false;
            return format.format(item);
        }
        return null;
    }

    private void documentChange() {
        String text = editor.getText();
        ((ValidatedTextField) editor).validateValue();
        String selected = itemToString(model.getSelectedItem());
        if (text.length() > 0 && (selected == null || !selected.equalsIgnoreCase(text))) {
            SwingUtilities.invokeLater(this::autoComplete);
        }
    }

    private void autoComplete() {
        if (editor.getSelectedText() == null) {
            autoCompleteFirstMatch(editor.getText());
        }
    }

    private void autoCompleteFirstMatch(String text) {
        if (text.length() > 0) {
            int position = editor.getCaretPosition();
            String match = getFirstMatch(text);
            if (match != null) {
                editor.getDocument().removeDocumentListener(documentHandler);
                editor.setText(text + match.substring(text.length()));
                editor.setCaretPosition(text.length());
                editor.setSelectionStart(Math.min(position, text.length()));
                editor.setSelectionEnd(match.length());
                editor.getDocument().addDocumentListener(documentHandler);
            }
        }
    }

    private class BorderlessTextField extends ValidatedTextField {
        public BorderlessTextField(String value, int columns, Validator<String> validator) {
            super(validator);
            setText(value == null ? "" : value);
            setColumns(columns);
        }

        // workaround for 4530952
        @Override
        public void setText(String s) {
            if (!getText().equals(s)) {
                super.setText(s);
            }
        }

        @Override
        public void validateValue() {
            super.validateValue();
            if (getToolTipText() == null) {
                if (getValidationMessages() == null) {
                    ToolTipManager.sharedInstance().unregisterComponent(this);
                }
                else {
                    ToolTipManager.sharedInstance().registerComponent(this);
                }
            }
        }
    }
}