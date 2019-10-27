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
import java.awt.Window;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import javax.swing.JTable;
import javax.swing.JWindow;
import javax.swing.border.LineBorder;
import javax.swing.text.Highlighter.HighlightPainter;

import io.github.jonestimd.swing.component.ListField;
import io.github.jonestimd.swing.component.ListField.ItemValidator;
import io.github.jonestimd.util.Streams;

/**
 * A table cell editor that displays a popup with a {@link ListField} for editing a list of
 * values.  {@link MultiSelectTableCellRenderer} can be used to render the list.
 * @param <T> the type of the list items
 */
public class PopupListTableCellEditor<T> extends PopupTableCellEditor {
    protected final ListField textArea;
    private final Function<T, String> format;
    private final Function<String, T> parser;
    private List<T> editorValue;

    /**
     * Create a {@code PopupListTableCellEditor}.
     * @param format a function for formatting the list items
     * @param parser a function for parsing a list item from a string
     * @param validator a validator for items in the popup editor
     * @param errorPainter a highlighter for indicating errors in the popup editor
     * @param rows the number of rows to display in the popup editor
     */
    public PopupListTableCellEditor(Function<T, String> format, Function<String, T> parser, ItemValidator validator,
            HighlightPainter errorPainter, int rows) {
        textArea = new ListField(validator, errorPainter, this::cancelCellEditing, this::commitEdit);
        textArea.setRows(rows);
        textArea.setBorder(new LineBorder(Color.BLACK, 1));
        this.format = format;
        this.parser = parser;
    }

    private void commitEdit(List<String> items) {
        editorValue = Streams.map(items, parser);
        fireEditingStopped();
    }

    @Override
    protected Window createWindow(Window owner) {
        JWindow window = new JWindow(owner);
        window.add(textArea);
        return window;
    }

    /**
     * Overridden to prevent closing the editor if it contains invalid items.
     * @return false if the editor contains invalid items
     */
    @Override
    public boolean stopCellEditing() {
        return textArea.isValidList() && super.stopCellEditing();
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
        return super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }

    @Override
    public Object getCellEditorValue() {
        return editorValue;
    }

    @Override
    protected void showPopup() {
        Dimension size = getTableCellSize();
        size.height = size.height*5;
        textArea.setPreferredSize(size);
        super.showPopup();
        textArea.requestFocus();
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

        public PopupListTableCellEditor<T> build() {
            return new PopupListTableCellEditor<>(format, parser, validator, errorPainter, rows);
        }
    }
}