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

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.Format;
import java.util.Collections;

import io.github.jonestimd.swing.validation.Validator;

/**
 * Provides a cell editor ({@link BeanListComboBox}) for selecting from a list of beans or creating a new item.
 * @param <T> the list item class
 */
public abstract class EditableComboBoxCellEditor<T extends Comparable<? super T>> extends BeanListComboBoxCellEditor<T> {
    private BeanListComboBoxEditor<T> editor;
    private T value;

    @SuppressWarnings("unchecked")
    protected EditableComboBoxCellEditor(Format format, Validator<String> validator, String loadingMessage) {
        this(format, validator, new FormatPrefixSelector(format), loadingMessage);
    }

    @SuppressWarnings("unchecked")
    protected EditableComboBoxCellEditor(Format format, Validator<String> validator, PrefixSelector prefixSelector, String loadingMessage) {
        super(new BeanListComboBox<T>(format, validator, Collections.emptyList(), prefixSelector), loadingMessage);
        editor = (BeanListComboBoxEditor<T>) getComboBox().getEditor();
        editor.getEditorComponent().addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                getComboBox().setPopupVisible(true);
            }
        });
    }

    public boolean stopCellEditing() {
        return !editor.isAutoSelecting() && selectMatch() && super.stopCellEditing();
    }

    /**
     * Set the selected item in the combo box.  If the item is new then pass it to {@link #saveItem(Comparable)}.
     * @return true if the editor item is valid.
     */
    private boolean selectMatch() {
        value = editor.getItem();
        if (editor.getEditorComponent().getValidationMessages() == null) {
            if (value != null && editor.isNew(value)) {
                return addNewItem(value);
            }
            getComboBoxModel().setSelectedItem(value);
            return true;
        }
        return false;
    }

    private boolean addNewItem(T item) {
        value = saveItem(item);
        if (value != null) {
            editor.setItem(item);
        }
        return value != null;
    }

    /**
     * Populate {@code item} and maybe persist it.  If {@code item} is persisted then it should also
     * be added to the combo box list.
     * @param item the new item from the combo box editor
     * @return the updated item.
     */
    protected abstract T saveItem(T item);

    @Override
    public Object getCellEditorValue() {
        return value;
    }
}