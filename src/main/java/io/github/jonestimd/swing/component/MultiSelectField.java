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

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;

import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;

import io.github.jonestimd.swing.ComponentResources;

/**
 * A text component that displays a list of string values using {@link MultiSelectItem} and allows adding and
 * removing items in the list.  Items can be removed from the list using the {@code backspace} and {@code delete} keys
 * or using the delete button on the {@link MultiSelectItem}s.  Items can be added to the list by typing text after the
 * existing items and pressing the {@code enter} key.
 */
public class MultiSelectField extends JTextPane {
    public static final String ITEMS_PROPERTY = "items";
    public static final Predicate<String> DEFAULT_IS_VALID_ITEM = (text) -> !text.trim().isEmpty();
    protected static final float ITEM_ALIGNMENT = 0.75f;

    private final List<String> items = new ArrayList<>();
    private final boolean showItemDelete;
    private final boolean opaqueItems;
    private final ResourceBundle bundle;
    private final Predicate<String> isValidItem;

    /**
     * Create a new {@code MultiSelectField}.
     * @param showItemDelete true to show delete buttons on the list items
     * @param opaqueItems true to fill the list items with their background color
     */
    public MultiSelectField(boolean showItemDelete, boolean opaqueItems) {
        this(showItemDelete, opaqueItems, DEFAULT_IS_VALID_ITEM, ComponentResources.BUNDLE);
    }

    /**
     * Create a new {@code MultiSelectField}.
     * @param showItemDelete true to show delete buttons on the list items
     * @param opaqueItems true to fill the list items with their background color
     * @param isValidItem predicate to use to validate items before adding them to the list
     * @param bundle the {@code ResourceBundle} to use for configuration
     */
    public MultiSelectField(boolean showItemDelete, boolean opaqueItems, Predicate<String> isValidItem, ResourceBundle bundle) {
        this.showItemDelete = showItemDelete;
        this.opaqueItems = opaqueItems;
        this.isValidItem = isValidItem;
        this.bundle = bundle;
    }

    /**
     * Create a new {@code MultiSelectField}.
     * @param items initial list of values
     * @param showItemDelete true to show delete buttons on the list items
     * @param opaqueItems true to fill the list items with their background color
     * @param isValidItem predicate to use to validate items before adding them to the list
     */
    public MultiSelectField(List<String> items, boolean showItemDelete, boolean opaqueItems, Predicate<String> isValidItem) {
        this(showItemDelete, opaqueItems, isValidItem, ComponentResources.BUNDLE);
        items.forEach(this::addItem);
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
        setSelectionStart(items.size());
        items.add(text);
        insertComponent(newItem(text));
        setSelectionStart(items.size());
    }

    /**
     * Create a new {@link MultiSelectItem} to add to the field.
     */
    protected MultiSelectItem newItem(String text) {
        MultiSelectItem item = new MultiSelectItem(text, showItemDelete, opaqueItems, bundle);
        item.setAlignmentY(ITEM_ALIGNMENT);
        item.addDeleteListener(this::onDeleteItem);
        return item;
    }

    /**
     * Handles clicking on an item's delete button.
     * @param item the item to be deleted
     */
    protected void onDeleteItem(MultiSelectItem item) {
        removeItem(item.getText());
    }

    /**
     * Remove an item from the list of values.
     */
    public void removeItem(String text) {
        int index = items.indexOf(text);
        if (index >= 0) {
            try {
                getDocument().remove(index, 1);
                items.remove(text);
            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Get the list of values.
     * @return an immutable copy of the list of values.
     */
    public List<String> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Overridden to handle changes to the list of values.
     */
    @Override
    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
        if (pressed) {
            if (ks.getKeyCode() == KeyEvent.VK_DELETE) {
                if (getSelectionStart() < items.size()) {
                    if (getSelectionEnd() != getSelectionStart()) return false;
                    items.remove(getSelectionStart());
                    firePropertyChange(ITEMS_PROPERTY, null, Collections.unmodifiableList(items));
                }
            }
            else if (ks.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                if (getSelectionStart() > 0 && getSelectionStart() <= items.size()) {
                    if (getSelectionEnd() != getSelectionStart()) return false;
                    items.remove(getSelectionStart()-1);
                    firePropertyChange(ITEMS_PROPERTY, null, Collections.unmodifiableList(items));
                }
            }
            else if (ks.getKeyCode() == KeyEvent.VK_ENTER) {
                try {
                    String text = getText().substring(items.size());
                    if (isValidItem.test(text)) {
                        getDocument().remove(items.size(), text.length());
                        addItem(text);
                    }
                    return true;
                } catch (BadLocationException e1) {
                    throw new RuntimeException(e1);
                }
            }
            else if (getSelectionStart() < items.size() && Character.isDefined(e.getKeyChar())) {
                setSelectionStart(getDocument().getLength());
            }
        }
        return super.processKeyBinding(ks, e, condition, pressed);
    }
}