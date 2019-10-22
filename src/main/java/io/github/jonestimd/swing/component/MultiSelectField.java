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

import javax.swing.InputVerifier;
import javax.swing.JComponent;
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
    private boolean yieldFocusOnError = true;
    private boolean keepTextOnFocusLost = false;

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
     * @param isValidItem predicate to use to validate input text before adding an item to the list
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
        setInputVerifier(new InputValidator());
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

    /**
     * Get the <code>AttributeSet</code> used to style invalid input text.
     */
    public MutableAttributeSet getInvalidItemStyle() {
        return invalidItemStyle;
    }

    /**
     * Replace the list of values.
     */
    public void setItems(Collection<String> items) {
        try {
            getDocument().remove(0, this.items.size());
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
        items.forEach(this::addItem);
    }

    /**
     * Add an item to the list of values.
     */
    public void addItem(String text) {
        MultiSelectItem item = newItem(text);
        item.setAlignmentY(ITEM_ALIGNMENT);
        item.addDeleteListener(this::removeItem);
        setSelectionStart(items.size());
        items.add(item);
        insertComponent(item);
        setSelectionStart(items.size());
        firePropertyChange(ITEMS_PROPERTY, null, getItems());
    }

    /**
     * Create a new {@link MultiSelectItem} to add to the field.
     */
    protected MultiSelectItem newItem(String text) {
        return new MultiSelectItem(text, showItemDelete, opaqueItems);
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

    /**
     * Remove a range of items.  Called when items are removed from the component's <code>Document</code>.
     */
    private void removeItems(int start, int count) {
        int lastItem = Math.min(items.size(), start + count);
        items.subList(start, lastItem).clear();
        firePropertyChange(ITEMS_PROPERTY, null, getItems());
    }

    /** Get the list of values. */
    public List<String> getItems() {
        return Streams.map(items, MultiSelectItem::getText);
    }

    /**
     *  Check if the input text is valid.
     */
    public boolean isValidItem() {
        return isValidItem.test(this, getText().substring(items.size()));
    }

    /** Add the current input text as an item. */
    protected void addItem() {
        try {
            String text = getText().substring(items.size());
            getDocument().remove(items.size(), text.length());
            addItem(text);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Overridden to handle changes to the list of values.
     */
    @Override
    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
        if (pressed) {
            int selectionStart = getSelectionStart();
            if (ks.getKeyCode() == KeyEvent.VK_ENTER) {
                if (isValidItem()) addItem();
                return true;
            }
            else if (ks.getKeyCode() != KeyEvent.VK_DELETE && ks.getKeyCode() != KeyEvent.VK_BACK_SPACE
                    && selectionStart < items.size() && Character.isDefined(e.getKeyChar())) {
                setSelectionStart(getDocument().getLength());
            }
        }
        if (super.processKeyBinding(ks, e, condition, pressed)) {
            String text = getText().substring(items.size());
            if (!text.isEmpty()) {
                AttributeSet attrs = isValidItem() ? SimpleAttributeSet.EMPTY : invalidItemStyle;
                getStyledDocument().setCharacterAttributes(items.size(), text.length(), attrs, true);
            }
            return true;
        }
        return false;
    }

    public boolean isYieldFocusOnError() {
        return yieldFocusOnError;
    }

    /**
     * Set the policy for yielding focus when the input text is invalid.
     * If <code>false</code> then focus will be retained when input text is invalid.
     * Defaults to <code>true</code>.
     */
    public void setYieldFocusOnError(boolean yieldFocusOnError) {
        this.yieldFocusOnError = yieldFocusOnError;
    }

    public boolean isKeepTextOnFocusLost() {
        return keepTextOnFocusLost;
    }

    /**
     * Set the policy for handling input text when focus is lost.
     * If <code>false</code> then valid input text will be added as an item and invalid input text will be cleared.
     * Defaults to <code>false</code>.
     */
    public void setKeepTextOnFocusLost(boolean keepTextOnFocusLost) {
        this.keepTextOnFocusLost = keepTextOnFocusLost;
    }

    protected class InputValidator extends InputVerifier {
        @Override
        public boolean verify(JComponent input) {
            return getText().length() == items.size() || isValidItem();
        }

        @Override
        public boolean shouldYieldFocus(JComponent input) {
            try {
                if (super.shouldYieldFocus(input)) {
                    if (!isKeepTextOnFocusLost() && getText().length() > items.size()) addItem();
                    return true;
                }
                if (isYieldFocusOnError() && !isKeepTextOnFocusLost()) {
                    int itemCount = items.size();
                    getDocument().remove(itemCount, getText().length() - itemCount);
                }
                return isYieldFocusOnError();
            } catch (BadLocationException ex) {
                return true;
            }
        }
    }

    /**
     * Helper class for building a {@link MultiSelectField}.
     */
    public static class Builder {
        protected final MultiSelectField field;

        /** @see MultiSelectField#MultiSelectField(boolean, boolean) */
        public Builder(boolean showDelete, boolean opaqueItems) {
            this.field = new MultiSelectField(showDelete, opaqueItems);
        }

        /** @see MultiSelectField#MultiSelectField(boolean, boolean, BiPredicate) */
        public Builder(boolean showDelete, boolean opaqueItems, BiPredicate<MultiSelectField, String> isValidItem) {
            this.field = new MultiSelectField(showDelete, opaqueItems, isValidItem);
        }

        /** Initialize the list of values in the field. */
        public Builder setItems(Collection<String> items) {
            field.setItems(items);
            return this;
        }

        /** Disable <code>tab</code> as text input and enable focus traversal using <code>tab</code> and <code>shift tab</code>. */
        public Builder disableTab() {
            field.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "disable-insert-tab");
            field.setFocusTraversalKeys(FORWARD_TRAVERSAL_KEYS, Collections.singleton(KeyStroke.getKeyStroke("pressed TAB")));
            field.setFocusTraversalKeys(BACKWARD_TRAVERSAL_KEYS, Collections.singleton(KeyStroke.getKeyStroke("shift pressed TAB")));
            return this;
        }

        public Builder setYieldFocusOnError(boolean yieldFocusOnError) {
            field.setYieldFocusOnError(yieldFocusOnError);
            return this;
        }

        public Builder setKeepTextOnFocusLost(boolean keepTextOnFocusLost) {
            field.setKeepTextOnFocusLost(keepTextOnFocusLost);
            return this;
        }

        public MultiSelectField get() {
            return field;
        }
    }
}