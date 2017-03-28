// Copyright (c) 2016 Timothy D. Jones
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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeListener;
import java.text.Format;
import java.util.Collection;
import java.util.Collections;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JList;

import io.github.jonestimd.swing.validation.ValidatedComponent;
import io.github.jonestimd.swing.validation.ValidatedTextField;
import io.github.jonestimd.swing.validation.ValidationBorder;
import io.github.jonestimd.swing.validation.Validator;

/**
 * Extends {@link JComboBox} to display a list of beans using a {@link Format} to render the list items.  Also handles
 * keyboard selection when the combo box is not editable.  The list of items may include a {@code null} when a
 * selection is not required.  When the combo box is editable the {@link Format} should also support parsing the
 * input text to create a new bean.
 * @param <T> list item class
 * @see BeanListComboBoxEditor
 */
public class BeanListComboBox<T> extends JComboBox<T> implements ValidatedComponent {
    private String requiredMessage;
    private String validationMessages;

    /**
     * Create a non-editable combo box.
     * @param format display format for the items
     */
    public BeanListComboBox(Format format) {
        this(format, Collections.emptyList());
    }

    /**
     * Create a non-editable combo box for a required value.
     * @param format display format for the items
     * @param requiredMessage the message to display when no value is selected
     */
    public BeanListComboBox(Format format, String requiredMessage) {
        this(format, Collections.emptyList());
        this.requiredMessage = requiredMessage;
        addItemListener(event -> validateValue());
        validateValue();
    }

    /**
     * Create a non-editable combo box.
     * @param format display format for the items
     * @param items  the list of items
     */
    public BeanListComboBox(Format format, Collection<? extends T> items) {
        this(format, new BeanListModel<>(items));
        setKeySelectionManager(new PrefixKeySelectionManager(new FormatPrefixSelector<>(format)));
    }

    /**
     * Create an editable combo box.
     * @param format    display format for the items
     * @param validator validator for new items (applied to the editor value)
     * @param items     the list of items
     */
    public BeanListComboBox(Format format, Validator<String> validator, Collection<? extends T> items) {
        this(format, validator, new BeanListModel<>(items));
    }

    /**
     * Create an editable combo box.
     * @param format    display format for the items
     * @param validator validator for new items (applied to the editor value)
     * @param model     the model containing the list of items
     */
    public BeanListComboBox(Format format, Validator<String> validator, BeanListModel<T> model) {
        this(format, validator, model, new FormatPrefixSelector<>(format));
    }

    /**
     * Create an editable combo box.
     * @param format         display format for the items
     * @param validator      validator for new items (applied to the editor value)
     * @param items          the list of items
     * @param prefixSelector selector for the best matching item for the editor content
     */
    public BeanListComboBox(Format format, Validator<String> validator, Collection<? extends T> items, PrefixSelector<T> prefixSelector) {
        this(format, validator, new BeanListModel<>(items), prefixSelector);
    }

    /**
     * Create an editable combo box.
     * @param format         display format for the items
     * @param validator      validator for new items (applied to the editor value)
     * @param model          the model containing the list of items
     * @param prefixSelector selector for the best matching item for the editor content
     */
    public BeanListComboBox(Format format, Validator<String> validator, BeanListModel<T> model, PrefixSelector<T> prefixSelector) {
        this(format, model);
        setEditor(new BeanListComboBoxEditor<>(this, format, validator, prefixSelector));
        getEditorComponent().addValidationListener(event -> firePropertyChange(VALIDATION_MESSAGES, event.getOldValue(), event.getNewValue()));
        setEditable(true);
    }

    @SuppressWarnings("unchecked")
    private BeanListComboBox(Format format, BeanListModel<T> model) {
        super(model);
        setRenderer(new Renderer(format));
    }

    @Override
    @SuppressWarnings("unchecked")
    public BeanListModel<T> getModel() {
        return (BeanListModel<T>) super.getModel();
    }

    /**
     * @throws IllegalArgumentException if {@code aModel} is not an instance of {@link BeanListModel}
     */
    @Override
    public void setModel(ComboBoxModel<T> aModel) {
        if (!(aModel instanceof BeanListModel)) {
            throw new IllegalArgumentException("not a BeanListModel");
        }
        super.setModel(aModel);
    }

    @SuppressWarnings("unchecked")
    public T getSelectedItem() {
        return (T) super.getSelectedItem();
    }

    /**
     * Overridden to fire item state changed for null selection/deselection.
     */
    @Override
    protected void selectedItemChanged() {
        if (selectedItemReminder == null) {
            fireItemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED,
                    selectedItemReminder, ItemEvent.DESELECTED));
        }
        super.selectedItemChanged();
        if (selectedItemReminder == null) {
            fireItemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED,
                    selectedItemReminder, ItemEvent.SELECTED));
        }
    }

    /**
     * Overridden to handle selection of null.
     */
    @Override
    public int getSelectedIndex() {
        if (getSelectedItem() == null) {
            return indexOf(null);
        }
        return super.getSelectedIndex();
    }

    private int indexOf(Object item) {
        for (int i = 0; i < dataModel.getSize(); i++) {
            if (item == dataModel.getElementAt(i)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void validateValue() {
        if (requiredMessage != null) {
            String oldValue = validationMessages;
            validationMessages = getSelectedItem() == null ? requiredMessage : null;
            setToolTipText(validationMessages);
            firePropertyChange(VALIDATION_MESSAGES, oldValue, validationMessages);
        }
    }

    @Override
    public String getValidationMessages() {
        if (isEditable() && getEditor().getEditorComponent() instanceof ValidatedComponent) {
            return ((ValidatedComponent) getEditor().getEditorComponent()).getValidationMessages();
        }
        return validationMessages;
    }

    @Override
    public void addValidationListener(PropertyChangeListener listener) {
        addPropertyChangeListener(VALIDATION_MESSAGES, listener);
    }

    @Override
    public void removeValidationListener(PropertyChangeListener listener) {
        removePropertyChangeListener(VALIDATION_MESSAGES, listener);
    }

    protected String getEditorText() {
        return getEditorComponent().getText();
    }

    protected ValidatedTextField getEditorComponent() {
        return (ValidatedTextField) getEditor().getEditorComponent();
    }

    private class PrefixKeySelectionManager implements KeySelectionManager {
        private static final long MAX_DELAY = 500L;
        private final PrefixSelector<T> prefixSelector;
        private long lastTime = 0L;
        private String prefix = "";

        public PrefixKeySelectionManager(PrefixSelector<T> prefixSelector) {
            this.prefixSelector = prefixSelector;
        }

        public int selectionForKey(char aKey, ComboBoxModel aModel) {
            if (System.currentTimeMillis() - lastTime > MAX_DELAY) {
                prefix = "";
            }
            lastTime = System.currentTimeMillis();
            prefix += Character.toUpperCase(aKey);
            Object match = prefixSelector.selectMatch(dataModel, prefix);
            if (match != null) {
                return indexOf(match);
            }
            return -1;
        }
    }

    private class Renderer extends FormatComboBoxRenderer {
        private boolean showMarker = false;

        public Renderer(Format format) {
            super(format);
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            showMarker = index < 0;
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            if (showMarker && validationMessages != null) {
                ValidationBorder.paintInvalidMarker(this, g, 0, 0, getWidth(), getHeight());
            }
        }
    }
}