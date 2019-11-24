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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeListener;
import java.text.Format;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JList;

import io.github.jonestimd.swing.validation.ValidatedComponent;
import io.github.jonestimd.swing.validation.ValidatedTextField;
import io.github.jonestimd.swing.validation.ValidationBorder;
import io.github.jonestimd.swing.validation.Validator;
import io.github.jonestimd.text.ToStringFormat;

/**
 * Extends {@link JComboBox} to display a list of beans using a {@link Format} to render the list items.  Also handles
 * keyboard selection when the combo box is not editable.  The list of items may include a {@code null} when a
 * selection is not required.  When the combo box is editable the {@link Format} should also support parsing the
 * input text to create a new bean.
 * @param <T> list item class
 * @see BeanListComboBoxEditor
 */
public class BeanListComboBox<T> extends JComboBox<T> implements ValidatedComponent {
    private Validator<T> validator;
    private String validationMessages;

    public static class Builder<T> {
        private final Format format;
        private final BeanListComboBox<T> comboBox;

        /**
         * @param format display format for the items in the popup list
         * @param model the model containing the list of items
         */
        public Builder(Format format, LazyLoadComboBoxModel<T> model) {
            this.format = format;
            this.comboBox = new BeanListComboBox<>(format, model);
        }

        /**
         * Add a null item to the beginning of the list of options.
         */
        public Builder<T> optional() {
            if (comboBox.getModel().getElementAt(0) != null) comboBox.insertItemAt(null, 0);
            return this;
        }

        /**
         * Require that an item must be selected.
         * @param message the error message if no item is selected
         */
        public Builder<T> required(String message) {
            return validated(item -> item == null ? message : null);
        }

        /**
         * Add validation of the selected item (does not make the combo box editable).
         * @param validator the selected item validator (<strong>not</strong> the input text validator)
         */
        public Builder<T> validated(Validator<T> validator) {
            comboBox.setValidator(validator);
            comboBox.addItemListener(event -> comboBox.validateValue());
            return this;
        }

        /**
         * Make the combo box editable using the same <code>format</code> as the popup list.
         * @param validator validator for input text
         */
        public Builder<T> editable(Validator<String> validator) {
            return editable(format, validator, new FormatPrefixSelector<>(format));
        }

        /**
         * Make the combo box editable using the same <code>format</code> as the popup list.
         * @param validator validator for input text
         * @param prefixSelector selector for the best matching item for the editor content
         */
        public Builder<T> editable(Validator<String> validator, PrefixSelector<T> prefixSelector) {
            return editable(format, validator, prefixSelector);
        }

        /**
         * Make the combo box editable.
         * @param itemFormat the format for converting an item to/from a string
         * @param validator validator for input text
         */
        public Builder<T> editable(Format itemFormat, Validator<String> validator) {
            return editable(itemFormat, validator, new FormatPrefixSelector<>(itemFormat));
        }

        /**
         * Make the combo box editable.
         * @param itemFormat the format for converting an item to/from a string
         * @param validator validator for input text
         * @param prefixSelector selector for the best matching item for the editor content
         */
        public Builder<T> editable(Format itemFormat, Validator<String> validator, PrefixSelector<T> prefixSelector) {
            comboBox.setEditor(new BeanListComboBoxEditor<>(comboBox, itemFormat, validator, prefixSelector));
            comboBox.getEditorComponent().addValidationListener(event -> {
                comboBox.firePropertyChange(VALIDATION_MESSAGES, event.getOldValue(), event.getNewValue());
            });
            comboBox.setEditable(true);
            return this;
        }

        public BeanListComboBox<T> get() {
            if (!comboBox.isEditable) comboBox.setKeySelectionManager(format);
            return comboBox;
        }
    }

    /**
     * @param format display format for the items
     * @param <T> item type
     */
    public static <T> Builder<T> builder(Format format) {
        return builder(format, new BeanListComboBoxModel<>());
    }

    /**
     * Create a builder for a combo box that displays enum values.
     */
    public static <T extends Enum<T>> Builder<T> builder(Class<T> enumClass) {
        List<T> items = Arrays.asList(enumClass.getEnumConstants());
        items.sort(Comparator.comparing(Objects::toString));
        return builder(new ToStringFormat(), items);
    }

    /**
     * Create a builder using {@link ToStringFormat}.
     */
    public static <T> Builder<T> builder(Collection<T> items) {
        return builder(new ToStringFormat(), items);
    }

    /**
     * @param format display format for the items
     * @param items the combo box items
     */
    public static <T> Builder<T> builder(Format format, Collection<T> items) {
        return builder(format, new BeanListComboBoxModel<>(items));
    }

    /**
     * @param format display format for the items
     * @param model the model containing the list of items
     */
    public static <T> Builder<T> builder(Format format, LazyLoadComboBoxModel<T> model) {
        return new Builder<>(format, model);
    }

    /**
     * Create a non-editable combo box.
     * @param format display format for the items
     */
    public BeanListComboBox(Format format) {
        this(format, new BeanListComboBoxModel<>());
        setKeySelectionManager(format);
    }

    /**
     * Create an editable combo box.
     * @param format display format for the popup items
     * @param itemFormat display format for the selected item
     * @param validator validator for new items (applied to the editor value)
     * @param model the model containing the list of items
     */
    public BeanListComboBox(Format format, Format itemFormat, Validator<String> validator, LazyLoadComboBoxModel<T> model) {
        this(format, model);
        setEditor(new BeanListComboBoxEditor<>(this, itemFormat, validator, new FormatPrefixSelector<>(itemFormat)));
        getEditorComponent().addValidationListener(event -> firePropertyChange(VALIDATION_MESSAGES, event.getOldValue(), event.getNewValue()));
        setEditable(true);
    }

    public BeanListComboBox(Format format, LazyLoadComboBoxModel<T> model) {
        super(model);
        setRenderer(new Renderer(format));
    }

    @Override
    public LazyLoadComboBoxModel<T> getModel() {
        return (LazyLoadComboBoxModel<T>) super.getModel();
    }

    /**
     * @throws IllegalArgumentException if {@code aModel} is not an instance of {@link BeanListComboBoxModel}
     */
    @Override
    public void setModel(ComboBoxModel<T> aModel) {
        if (!(aModel instanceof LazyLoadComboBoxModel)) {
            throw new IllegalArgumentException("not a LazyLoadComboBoxModel");
        }
        super.setModel(aModel);
    }

    /**
     * Set the validator for the selected item.
     */
    public void setValidator(Validator<T> validator) {
        this.validator = validator.when(this::isEnabled);
        validateValue();
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

    /**
     * Overridden to enable/disable editor component.
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (isEditable()) getEditorComponent().setEditable(enabled);
        validateValue();
    }

    @Override
    public void validateValue() {
        if (validator != null) {
            String oldValue = validationMessages;
            validationMessages = validator.validate(getSelectedItem());
            firePropertyChange(ValidatedComponent.VALIDATION_MESSAGES, oldValue, validationMessages);
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

    protected void setKeySelectionManager(Format format) {
        setKeySelectionManager(new PrefixKeySelectionManager(new FormatPrefixSelector<>(format)));
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