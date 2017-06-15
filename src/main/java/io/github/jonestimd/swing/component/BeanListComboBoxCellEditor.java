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

import java.awt.Component;
import java.text.Format;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;

import io.github.jonestimd.swing.BackgroundTask;
import io.github.jonestimd.util.Streams;

/**
 * Provides a cell editor ({@link BeanListComboBox}) for selecting from a list of beans.  Provides the option to load
 * the list items lazily the first time a cell is edited.
 * @param <T> list item class
 */
public abstract class BeanListComboBoxCellEditor<T extends Comparable<? super T>> extends ComboBoxCellEditor {
    private enum LoadStatus {PENDING, IN_PROGRESS, DONE}
    private static final Logger logger = Logger.getLogger(BeanListComboBoxCellEditor.class.getName());
    private final String loadingMessage;
    private LoadStatus status = LoadStatus.PENDING;

    /**
     * Create a combo box cell editor for an optional field.
     */
    protected BeanListComboBoxCellEditor(Format format, String loadingMessage) {
        this(new BeanListComboBox<>(format), loadingMessage);
    }

    /**
     * Create a combo box cell editor for an optional field.
     */
    protected BeanListComboBoxCellEditor(BeanListComboBox<T> comboBox, String loadingMessage) {
        super(comboBox);
        this.loadingMessage = loadingMessage;
        getComboBox().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("pressed ENTER"), "ignore");
        getComboBoxModel().addElement(null);
    }

    /**
     * Overridden start a background thread to load the list values if they haven't been loaded yet,
     * @see #getComboBoxValues()
     */
    @Override
    @SuppressWarnings("unchecked")
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        initializeList(table, (T) value);
        return super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }

    /**
     * Overridden start a background thread to load the list values if they haven't been loaded yet,
     * @see #getComboBoxValues()
     */
    @Override
    @SuppressWarnings("unchecked")
    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded,
            boolean leaf, int row) {
        initializeList(tree, (T) value);
        return super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
    }

    @SuppressWarnings("unchecked")
    protected BeanListComboBox<T> getComboBox() {
        return (BeanListComboBox<T>) getComponent();
    }

    protected BeanListModel<T> getComboBoxModel() {
        return getComboBox().getModel();
    }

    /**
     * Set the list items.  Disables background loading of the list items.
     */
    public void setListItems(List<T> items) {
        status = LoadStatus.DONE;
        addListItems(items);
    }

    /**
     * Add items to the drop down list.
     */
    public void addListItems(Collection<T> newItems) {
        List<T> items = Streams.filter(getComboBoxModel(), Objects::nonNull);
        items.addAll(newItems);
        Collections.sort(items);
        getComboBoxModel().setElements(items, getComboBox().isEditable());
        getComboBoxModel().insertElementAt(null, 0);
    }

    private void initializeList(JComponent container, T selectedValue) {
        if (status == LoadStatus.PENDING) {
            new LoadComboBoxTask().run(container);
            status = LoadStatus.IN_PROGRESS;
        }
        if (status == LoadStatus.IN_PROGRESS) {
            if (selectedValue != null) {
                getComboBoxModel().addElement(selectedValue);
            }
        }
    }

    private void setListItemsOnComboBox(List<T> items) {
        JComboBox comboBox = getComboBox();
        String editorText = comboBox.isEditable() ? ((JTextComponent)comboBox.getEditor().getEditorComponent()).getText() : "";
        BeanListModel<T> comboBoxModel = getComboBoxModel();
        int index = 1;
        for (T item : items) {
            if (comboBoxModel.indexOf(item) < 0) {
                comboBoxModel.insertElementAt(item, index);
            }
            index++;
        }
        if (! editorText.isEmpty()) {
            ((JTextComponent) comboBox.getEditor().getEditorComponent()).setText(editorText);
        }
    }

    /**
     * Must be implemented to provide the list items.  Called from a background thread the first time a cell
     * is edited unless the items have been set using {@link #setListItems(List)}.
     * @see #getTableCellEditorComponent(JTable, Object, boolean, int, int)
     * @see #getTreeCellEditorComponent(JTree, Object, boolean, boolean, boolean, int)
     */
    protected abstract List<T> getComboBoxValues();

    private class LoadComboBoxTask extends BackgroundTask<List<T>> {
        public String getStatusMessage() {
            return loadingMessage;
        }

        public List<T> performTask() {
            List<T> comboBoxItems = getComboBoxValues();
            Collections.sort(comboBoxItems);
            return comboBoxItems;
        }

        public void updateUI(List<T> comboBoxItems) {
            logger.fine("loaded combo box values");
            status = LoadStatus.DONE;
            setListItemsOnComboBox(comboBoxItems);
            // resize the list box
            if (getComboBox().isShowing()) {
                getComboBox().setPopupVisible(false);
                getComboBox().setPopupVisible(true);
            }
        }

        @Override
        public boolean handleException(Throwable th) {
            return false;
        }
    }
}