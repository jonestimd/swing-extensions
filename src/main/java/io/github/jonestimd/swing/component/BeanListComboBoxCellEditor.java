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
import java.text.Format;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;

import io.github.jonestimd.swing.BackgroundRunner;
import io.github.jonestimd.swing.BackgroundTask;
import io.github.jonestimd.swing.ComponentTreeUtils;
import io.github.jonestimd.swing.LoggerStatusIndicator;
import io.github.jonestimd.swing.StatusIndicator;

/**
 * Provides a cell editor ({@link BeanListComboBox}) for selecting from a list of beans.
 * @param <T> list item class
 */
public abstract class BeanListComboBoxCellEditor<T extends Comparable<? super T>> extends ComboBoxCellEditor {
    private static final Logger logger = Logger.getLogger(BeanListComboBoxCellEditor.class.getName());
    private final String loadingMessage;
    private StatusIndicator fallbackStatusIndicator = new LoggerStatusIndicator(logger);
    private boolean loading;
    private List<T> items;

    /**
     * Create a combo box cell editor for an optional field.
     */
    protected BeanListComboBoxCellEditor(Format format, String loadingMessage) {
        this(new BeanListComboBox<T>(format), loadingMessage);
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

    @Override
    @SuppressWarnings("unchecked")
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        initializeList(table, (T) value);
        return super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }

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

    public void setListItems(List<T> items) {
        this.items = new ArrayList<>();
        addListItems(items);
    }

    public void addListItems(Collection<T> newItems) {
        items.addAll(newItems);
        Collections.sort(items);
        getComboBoxModel().setElements(items);
        getComboBoxModel().insertElementAt(null, 0);
    }

    private void initializeList(JComponent container, T selectedValue) {
        if (items == null) {
            items = Collections.emptyList();
            new BackgroundRunner<>(new LoadComboBoxTask(), getComboBox(),
                    ComponentTreeUtils.findAncestor(container, StatusIndicator.class, fallbackStatusIndicator)).doTask();
            loading = true;
        }
        if (loading) {
            if (selectedValue != null) {
                getComboBoxModel().addElement(selectedValue);
            }
        }
        else {
            setListItemsOnComboBox();
        }
    }

    private void setListItemsOnComboBox() {
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

    protected abstract List<T> getComboBoxValues();

    private class LoadComboBoxTask implements BackgroundTask<List<T>> {
        public String getStatusMessage() {
            return loadingMessage;
        }

        public List<T> performTask() {
            List<T> comboBoxItems = getComboBoxValues();
            Collections.sort(comboBoxItems);
            return comboBoxItems;
        }

        public void updateUI(List<T> comboBoxItems) {
            items = comboBoxItems;
            logger.fine("loaded combo box values");
            loading = false;
            setListItemsOnComboBox();
            // resize the list box
            if (getComboBox().isShowing()) {
                getComboBox().setPopupVisible(false);
                getComboBox().setPopupVisible(true);
            }
        }
    }
}