// The MIT License (MIT)
//
// Copyright (c) 2019 Timothy D. Jones
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
import com.google.common.collect.Lists;
import io.github.jonestimd.swing.table.PopupListTableCellEditor;

import static io.github.jonestimd.swing.ComponentResources.*;

/**
 * Extends {@link JTextArea} to support editing a list of string values.  When the text contains
 * a valid list of items and the commit key stroke is received, the commit callback is called with the list
 * of items.  If the cancel key stroke is received then the cancel callback is called.
 * <p/>
 * The following resources are used to configure the behavior of this component and can be overridden by a custom resource bundle.
 * <ul>
 *     <li><strong>listField.commitKey</strong> - string definition of the commit key stroke</li>
 *     <li><strong>listField.cancelKey</strong> - string definition of the cancel key stroke</li>
 * </ul>
 * <p/>
 * Used by {@link PopupListTableCellEditor}.
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
    private static final KeyStroke COMMIT_KEY = KeyStroke.getKeyStroke(lookupString("listField.commitKey"));
    private static final KeyStroke CANCEL_KEY = KeyStroke.getKeyStroke(lookupString("listField.cancelKey"));
    private final ItemValidator validator;
    private final HighlightPainter errorPainter;
    private final Runnable cancelCallback;
    private final Consumer<List<String>> commitCallback;
    private boolean isValid = true;

    /**
     * Create a list field that requires non-blank list items and highlights invalid items with a {@link Color#PINK} background.
     */
    public ListField(Runnable cancelCallback, Consumer<List<String>> commitCallback) {
        this(DEFAULT_VALIDATOR, DEFAULT_ERROR_HIGHLIGHTER, cancelCallback, commitCallback);
    }

    public ListField(ItemValidator validator, HighlightPainter errorPainter, Runnable cancelCallback, Consumer<List<String>> commitCallback) {
        this.validator = validator;
        this.errorPainter = errorPainter;
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

    /**
     * Set the component text using a list of items.
     */
    public void setText(List<String> value) {
        setText(Joiner.on(LINE_SEPARATOR).join(value));
    }

    /**
     * @return true if all of the items are valid
     */
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

    /**
     * Parse the component text and return the list of items.
     */
    public List<String> parseItems() {
        final String text = getText();
        if (text.isEmpty()) return new ArrayList<>();
        final List<String> items = Lists.newArrayList(text.split(LINE_SEPARATOR, -1));
        if (text.endsWith(LINE_SEPARATOR)) items.remove(items.size()-1);
        return items;
    }

    @Override
    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
        if (ks == CANCEL_KEY) cancelCallback.run();
        else if (isValid && ks == COMMIT_KEY) commitCallback.accept(parseItems());
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

    /**
     * Interface for validating items in the list.
     */
    public interface ItemValidator {
        /**
         * @param items the items in the list
         * @param index the index of the current item
         * @return true if the item is valid
         */
        boolean isValid(List<String> items, int index);
    }
}
