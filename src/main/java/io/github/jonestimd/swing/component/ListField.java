// The MIT License (MIT)
//
// Copyright (c) 2018 Timothy D. Jones
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

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.Position.Bias;

import com.google.common.base.Joiner;
import io.github.jonestimd.swing.ComponentResources;
import io.github.jonestimd.swing.table.PopupListTableCellEditor;

/**
 * Extends {@link JTextArea} to support editing a list of string values.  When the text contains
 * a valid list of items and the commit key stroke is received, the commit callback is called with the list
 * of items.  If the cancel key stroke is received then the cancel callback is called.
 * <p/>
 * The following resources are used to configure the behavior of this component and can be overridden by a custom resource bundle.
 * <ul>
 *     <li><strong>popupListField.commitKey</strong> - string definition of the commit key stroke</li>
 *     <li><strong>popupListField.cancelKey</strong> - string definition of the cancel key stroke</li>
 * </ul>
 * <p/>
 * Used by {@link PopupListField} and {@link PopupListTableCellEditor}.
 */
public class ListField extends JTextArea {
    public static final String VALID_PROPERTY = "valid";
    public static final ItemValidator DEFAULT_VALIDATOR = (items, index) -> !items.get(index).trim().isEmpty();
    public static final String LINE_SEPARATOR = "\n";
    public static final int DEFAULT_ROWS = 5;
    public static final HighlightPainter DEFAULT_ERROR_HIGHLIGHTER = (g, p0, p1, bounds, c) -> {
        g.setColor(Color.PINK);
        Rectangle rect = bounds.getBounds();
        TextUI mapper = c.getUI();
        try {
            Rectangle start = mapper.modelToView(c, p0, Bias.Forward);
            g.fillRect(start.x, start.y+1, rect.width, start.height-1);
        } catch (BadLocationException e) {
            // can't render
        }
    };
    private final KeyStroke commitKey;
    private final KeyStroke cancelKey;
    private final ItemValidator validator;
    private final HighlightPainter errorPainter;
    private final Runnable cancelCallback;
    private final Consumer<List<String>> commitCallback;
    private boolean isValid = true;

    public ListField(Runnable cancelCallback, Consumer<List<String>> commitCallback) {
        this(DEFAULT_VALIDATOR, DEFAULT_ERROR_HIGHLIGHTER, ComponentResources.BUNDLE, cancelCallback, commitCallback);
    }

    public ListField(ItemValidator validator, HighlightPainter errorPainter, ResourceBundle bundle,
            Runnable cancelCallback, Consumer<List<String>> commitCallback) {
        this.validator = validator;
        this.errorPainter = errorPainter;
        commitKey = KeyStroke.getKeyStroke(ComponentResources.getString(bundle, "popupListField.commitKey"));
        cancelKey = KeyStroke.getKeyStroke(ComponentResources.getString(bundle, "popupListField.cancelKey"));
        this.cancelCallback = cancelCallback;
        this.commitCallback = commitCallback;
        setRows(DEFAULT_ROWS);
        getDocument().addDocumentListener(new EditorDocumentListener());
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        validateText();
    }

    public void setText(List<String> value) {
        setText(Joiner.on(LINE_SEPARATOR).join(value));
    }

    public boolean isValidList() {
        return isValid;
    }

    private void validateText() {
        final boolean oldValue = isValid;
        isValid = true;
        getHighlighter().removeAllHighlights();
        int pos = 0;
        final List<String> items = parseItems();
        for (int i = 0; i < items.size(); i++) {
            final int lineLength = items.get(i).length();
            if (!validator.isValid(items, i)) {
                isValid = false;
                try {
                    getHighlighter().addHighlight(pos, pos+lineLength, errorPainter);
                } catch (BadLocationException e) {
                    throw new RuntimeException(e);
                }
            }
            pos += lineLength+1;
        }
        if (oldValue != isValid) firePropertyChange(VALID_PROPERTY, oldValue, isValid);
    }

    public List<String> parseItems() {
        final String text = getText();
        final int length = text.length();
        final List<String> items = new ArrayList<>();
        for (int pos = 0; pos < length;) {
            final int end = text.indexOf(LINE_SEPARATOR, pos);
            if (end >= 0) {
                items.add(text.substring(pos, end));
                pos = end + 1;
            }
            else {
                items.add(text.substring(pos));
                pos = length;
            }
        }
        return items;
    }

    @Override
    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
        if (ks == cancelKey) cancelCallback.run();
        else if (isValid && ks == commitKey) commitCallback.accept(parseItems());
        return super.processKeyBinding(ks, e, condition, pressed);
    }

    private class EditorDocumentListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            validateText();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            validateText();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            validateText();
        }
    }

    public interface ItemValidator {
        boolean isValid(List<String> items, int index);
    }
}
