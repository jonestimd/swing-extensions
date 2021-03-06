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
package io.github.jonestimd.swing.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import io.github.jonestimd.swing.component.MultiSelectItem;

/**
 * A table cell renderer that displays a list of values as {@link MultiSelectItem}s.  {@link PopupListTableCellEditor}
 * can be used to edit the list of values.
 * @param <T> the type of the list items
 */
public class MultiSelectTableCellRenderer<T> extends JLabel implements TableCellRenderer {
    private static final Border SAFE_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
    private static final Border DEFAULT_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
    protected static Border noFocusBorder = DEFAULT_NO_FOCUS_BORDER;

    private final boolean opaque;
    private final Function<T, String> format;

    public MultiSelectTableCellRenderer(boolean opaque) {
        this(opaque, Objects::toString);
    }

    public MultiSelectTableCellRenderer(boolean opaque, Function<T, String> format) {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.opaque = opaque;
        this.format = format;
        setOpaque(true);
    }

    private Border getNoFocusBorder() {
        Border border = (Border) UIManager.get("Table.cellNoFocusBorder", getLocale());
        if (System.getSecurityManager() != null) {
            return border != null ? border : SAFE_NO_FOCUS_BORDER;
        }
        if (border != null && (noFocusBorder == null || noFocusBorder == DEFAULT_NO_FOCUS_BORDER)) {
            return border;
        }
        return noFocusBorder;
    }

    public void setFont(Font font) {
        super.setFont(font);
        for (int i = 0; i < getComponentCount(); i++) {
            getComponent(i).setFont(font);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        removeAll();
        setFont(table.getFont());
        if (value instanceof Collection) {
            Collection<T> items = (Collection<T>) value;
            for (T item : items) {
                MultiSelectItem comp = new MultiSelectItem(format.apply(item), false, opaque);
                comp.setFont(getFont());
                add(comp);
            }
        }

        if (isSelected) {
            super.setForeground(table.getSelectionForeground());
            super.setBackground(table.getSelectionBackground());
        } else {
            super.setForeground(Optional.ofNullable(getForeground()).orElse(table.getForeground()));
            super.setBackground(Optional.ofNullable(getBackground()).orElse(table.getBackground()));
        }

        if (hasFocus) {
            Border border = null;
            if (isSelected) {
                border = (Border) UIManager.get("Table.focusSelectedCellHighlightBorder", getLocale());
            }
            if (border == null) {
                border = (Border) UIManager.get("Table.focusCellHighlightBorder", getLocale());
            }
            setBorder(border);

            if (!isSelected && table.isCellEditable(row, column)) {
                Color col;
                col = (Color) UIManager.get("Table.focusCellForeground", getLocale());
                if (col != null) {
                    super.setForeground(col);
                }
                col = (Color) UIManager.get("Table.focusCellBackground", getLocale());
                if (col != null) {
                    super.setBackground(col);
                }
            }
        } else {
            setBorder(getNoFocusBorder());
        }
        return this;
    }
}
