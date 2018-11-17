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
package io.github.jonestimd.swing.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyBoundsAdapter;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;

import javax.swing.AbstractCellEditor;
import javax.swing.FocusManager;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellEditor;
import javax.swing.text.Highlighter.HighlightPainter;

import io.github.jonestimd.swing.ComponentResources;
import io.github.jonestimd.swing.component.ListField;
import io.github.jonestimd.swing.component.ListField.ItemValidator;
import io.github.jonestimd.util.Streams;

/**
 * A table cell editor that displays a popup with a {@link ListField} for editing a list of
 * values.  {@link MultiSelectTableCellRenderer} can be used to render the list.
 * @param <T> the type of the list items
 */
public class PopupListTableCellEditor<T> extends AbstractCellEditor implements TableCellEditor {
    private final Border popupBorder = new LineBorder(Color.BLACK, 1);
    protected Window popupWindow;
    protected final ListField textArea;
    private final Function<T, String> format;
    private final Function<String, T> parser;
    /**
     * Added to the table to track position for the popup window.
     */
    private final JLabel editorComponent = new JLabel();
    private List<T> editorValue;
    private final HierarchyBoundsListener movementListener = new HierarchyBoundsAdapter() {
        public void ancestorMoved(HierarchyEvent e) {
            popupWindow.setLocation(getPopupLocation());
        }
    };
    private final ComponentListener componentListener = new ComponentAdapter() {
        public void componentMoved(ComponentEvent e) {
            popupWindow.setLocation(getPopupLocation());
        }
    };
    private final PropertyChangeListener focusedWindowListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getOldValue() == popupWindow) {
                fireEditingCanceled();
            }
        }
    };

    /**
     * Create a {@code PopupListTableCellEditor}.
     * @param format a function for formatting the list items
     * @param parser a function for parsing a list item from a string
     * @param validator a validator for items in the popup editor
     * @param errorPainter a highlighter for indicating errors in the popup editor
     * @param rows the number of rows to display in the popup editor
     * @param bundle the resource bundle to use to configure the {@link ListField}
     */
    public PopupListTableCellEditor(Function<T, String> format, Function<String, T> parser, ItemValidator validator,
            HighlightPainter errorPainter, int rows, ResourceBundle bundle) {
        textArea = new ListField(validator, errorPainter, bundle, this::cancelCellEditing, this::commitEdit);
        textArea.setRows(rows);
        this.format = format;
        this.parser = parser;
        editorComponent.addHierarchyListener(this::hierarchyChanged);
    }

    private void commitEdit(List<String> items) {
        editorValue = Streams.map(items, parser);
        fireEditingStopped();
    }

    /**
     * Overridden to prevent closing the editor if it contains invalid items.
     * @return false if the editor contains invalid items
     */
    @Override
    public boolean stopCellEditing() {
        return textArea.isValidList() && super.stopCellEditing();
    }

    /**
     * Overridden to hide the popup window.
     */
    @Override
    protected void fireEditingStopped() {
        hidePopup();
        super.fireEditingStopped();
    }

    /**
     * Overridden to hide the popup window.
     */
    @Override
    protected void fireEditingCanceled() {
        hidePopup();
        super.fireEditingCanceled();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (value instanceof List) {
            editorValue = (List<T>) value;
            textArea.setText(Streams.map(editorValue, format));
        }
        else {
            editorValue = Collections.emptyList();
            textArea.setText("");
        }
        return editorComponent;
    }

    @Override
    public Object getCellEditorValue() {
        return editorValue;
    }

    private Point getPopupLocation() {
        return editorComponent.getLocationOnScreen();
    }

    /**
     * Hide the popup window if it is showing.
     */
    protected void hidePopup() {
        if (popupWindow != null) {
            FocusManager.getCurrentManager().removePropertyChangeListener("focusedWindow", focusedWindowListener);
            editorComponent.removeHierarchyBoundsListener(movementListener);
            editorComponent.removeComponentListener(componentListener);
            popupWindow.dispose();
            popupWindow = null;
        }
    }

    /**
     * Show the popup window.
     */
    protected void showPopup() {
        Dimension size = editorComponent.getSize();
        size.height = size.height*5;
        textArea.setPreferredSize(size);
        FocusManager.getCurrentManager().addPropertyChangeListener("focusedWindow", focusedWindowListener);
        popupWindow.pack();
        popupWindow.setLocation(getPopupLocation());
        popupWindow.setVisible(true);
        popupWindow.toFront();
        textArea.requestFocus();
        editorComponent.addHierarchyBoundsListener(movementListener);
        editorComponent.addComponentListener(componentListener);
    }

    /**
     * Creates and displays the popup window when the {@code editorComponent} is added to the table.
     */
    private void hierarchyChanged(HierarchyEvent event) {
        if ((event.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && editorComponent.isShowing()) {
            popupWindow = new Window((Window) editorComponent.getTopLevelAncestor());
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setBorder(popupBorder);
            popupWindow.add(scrollPane);
            showPopup();
        }
    }

    /**
     * Create a builder for creating a new {@code PopupListTableCellEditor}.
     * @param format a function for formatting the list items
     * @param parser a function for parsing a list item from a string
     * @param <T> the type of the list items
     */
    public static <T> Builder<T> builder(Function<T, String> format, Function<String, T> parser) {
        return new Builder<>(format, parser);
    }

    public static class Builder<T> {
        private final Function<T, String> format;
        private final Function<String, T> parser;
        private ItemValidator validator = ListField.DEFAULT_VALIDATOR;
        private HighlightPainter errorPainter = ListField.DEFAULT_ERROR_HIGHLIGHTER;
        private int rows = ListField.DEFAULT_ROWS;
        private ResourceBundle bundle = ComponentResources.BUNDLE;

        public Builder(Function<T, String> format, Function<String, T> parser) {
            this.format = format;
            this.parser = parser;
        }

        public Builder<T> validator(ItemValidator isValidItem) {
            this.validator = isValidItem;
            return this;
        }

        public Builder<T> errorPainter(HighlightPainter errorPainter) {
            this.errorPainter = errorPainter;
            return this;
        }

        public Builder<T> rows(int rows) {
            this.rows = rows;
            return this;
        }

        public Builder<T> bundle(ResourceBundle bundle) {
            this.bundle = bundle;
            return this;
        }

        public PopupListTableCellEditor<T> build() {
            return new PopupListTableCellEditor<>(format, parser, validator, errorPainter, rows, bundle);
        }
    }
}