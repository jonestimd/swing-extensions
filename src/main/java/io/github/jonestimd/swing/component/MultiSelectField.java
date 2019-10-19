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
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;

import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import io.github.jonestimd.swing.ComponentResources;
import io.github.jonestimd.util.Streams;

import static java.awt.KeyboardFocusManager.*;

/**
 * A text component that displays a list of string values using {@link MultiSelectItem} and allows adding and
 * removing items in the list.  Items can be removed from the list using the {@code backspace} and {@code delete} keys
 * or using the delete button on the {@link MultiSelectItem}s.  Items can be added to the list by typing text after the
 * existing items and pressing the {@code enter} key.
 */
public class MultiSelectField extends JTextPane {
    public static final String ITEMS_PROPERTY = "items";
    public static final BiPredicate<MultiSelectField, String> DEFAULT_IS_VALID_ITEM = (field, text) -> !text.trim().isEmpty();
    private static final Color INVALID_ITEM_BACKGROUND = ComponentResources.lookupColor("multiSelectField.invalidItem.background");
    protected static final float ITEM_ALIGNMENT = 0.75f;

    private final List<MultiSelectItem> items = new ArrayList<>();
    private final boolean showItemDelete;
    private final boolean opaqueItems;
    private final BiPredicate<MultiSelectField, String> isValidItem;
    private final MutableAttributeSet invalidItemStyle = new SimpleAttributeSet();

    /**
     * Create a new {@code MultiSelectField}.
     * @param showItemDelete true to show delete buttons on the list items
     * @param opaqueItems true to fill the list items with their background color
     */
    public MultiSelectField(boolean showItemDelete, boolean opaqueItems) {
        this(showItemDelete, opaqueItems, DEFAULT_IS_VALID_ITEM);
    }

    /**
     * Create a new {@code MultiSelectField}.
     * @param showItemDelete true to show delete buttons on the list items
     * @param opaqueItems true to fill the list items with their background color
     * @param isValidItem predicate to use to validate items before adding them to the list
     */
    public MultiSelectField(boolean showItemDelete, boolean opaqueItems, BiPredicate<MultiSelectField, String> isValidItem) {
        this.showItemDelete = showItemDelete;
        this.opaqueItems = opaqueItems;
        this.isValidItem = isValidItem;
        addCaretListener(event -> {
            int start = getSelectionStart();
            int end = getSelectionEnd();
            for (int i = 0; i < items.size(); i++) items.get(i).setSelected(i >= start && i < end);
        });
        StyleConstants.setBackground(invalidItemStyle, INVALID_ITEM_BACKGROUND);
        getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {}

            @Override
            public void removeUpdate(DocumentEvent e) {
                int offset = e.getOffset();
                int length = e.getLength();
                if (offset < items.size()) removeItems(offset, length);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {}
        });
    }

    public MutableAttributeSet getInvalidItemStyle() {
        return invalidItemStyle;
    }

    /**
     * Replace the list of values.
     */
    public void setItems(Collection<String> items) {
        this.items.forEach(this::removeItem);
        items.forEach(this::addItem);
    }

    /**
     * Add an item to the list of values.
     */
    public void addItem(String text) {
        addItem(newItem(text));
    }

    protected void addItem(MultiSelectItem item) {
        setSelectionStart(items.size());
        items.add(item);
        insertComponent(item);
        setSelectionStart(items.size());
    }

    /**
     * Create a new {@link MultiSelectItem} to add to the field.
     */
    protected MultiSelectItem newItem(String text) {
        MultiSelectItem item = new MultiSelectItem(text, showItemDelete, opaqueItems);
        item.setAlignmentY(ITEM_ALIGNMENT);
        item.addDeleteListener(this::removeItem);
        return item;
    }

    /**
     * Remove an item from the list of values.
     */
    protected void removeItem(MultiSelectItem item) {
        int index = items.indexOf(item);
        if (index >= 0) {
            try {
                getDocument().remove(index, 1);
                items.remove(item);
            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected void removeItems(int start, int count) {
        int lastItem = Math.min(items.size(), start + count);
        items.subList(start, lastItem).clear();
        firePropertyChange(ITEMS_PROPERTY, null, Collections.unmodifiableList(items));
    }

    /**
     * Get the list of values.
     * @return an immutable copy of the list of values.
     */
    public List<String> getItems() {
        return Streams.map(items, MultiSelectItem::getText);
    }

    /**
     * Overridden to handle changes to the list of values.
     */
    @Override
    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
        if (pressed) {
            int selectionStart = getSelectionStart();
            if (ks.getKeyCode() == KeyEvent.VK_ENTER) {
                try {
                    String text = getText().substring(items.size());
                    if (isValidItem.test(this, text)) {
                        getDocument().remove(items.size(), text.length());
                        addItem(text);
                    }
                    return true;
                } catch (BadLocationException e1) {
                    throw new RuntimeException(e1);
                }
            }
            else if (ks.getKeyCode() != KeyEvent.VK_DELETE && ks.getKeyCode() != KeyEvent.VK_BACK_SPACE
                    && selectionStart < items.size() && Character.isDefined(e.getKeyChar())) {
                setSelectionStart(getDocument().getLength());
            }
        }
        if (super.processKeyBinding(ks, e, condition, pressed)) {
            String text = getText().substring(items.size());
            if (!text.isEmpty()) {
                AttributeSet attrs = isValidItem.test(this, text) ? SimpleAttributeSet.EMPTY : invalidItemStyle;
                getStyledDocument().setCharacterAttributes(items.size(), text.length(), attrs, true);
            }
            return true;
        }
        return false;
    }

    public static class Builder {
        protected final MultiSelectField field;

        public Builder(boolean showDelete, boolean opaqueItems) {
            this.field = new MultiSelectField(showDelete, opaqueItems);
        }

        public Builder(boolean showDelete, boolean opaqueItems, BiPredicate<MultiSelectField, String> isValidItem) {
            this.field = new MultiSelectField(showDelete, opaqueItems, isValidItem);
        }

        public Builder setItems(Collection<String> items) {
            field.setItems(items);
            return this;
        }

        public Builder disableTab() {
            field.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "disable-insert-tab");
            field.setFocusTraversalKeys(FORWARD_TRAVERSAL_KEYS, Collections.singleton(KeyStroke.getKeyStroke("pressed TAB")));
            field.setFocusTraversalKeys(BACKWARD_TRAVERSAL_KEYS, Collections.singleton(KeyStroke.getKeyStroke("shift pressed TAB")));
            return this;
        }

        public MultiSelectField get() {
            return field;
        }
    }
}