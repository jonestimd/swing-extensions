// The MIT License (MIT)
//
// Copyright (c) 2020 Timothy D. Jones
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

import java.awt.Component;
import java.awt.Point;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * A {@link TableCellRenderer} that uses a list of {@link TableCellDecorator}s to configure the renderer.
 */
public class CompositeTableCellRenderer extends DefaultTableCellRenderer implements HoverTableCellRenderer {
    private final List<TableCellDecorator> handlers;

    public CompositeTableCellRenderer(List<TableCellDecorator> handlers) {
        this.handlers = handlers;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JComponent component = (JComponent) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        for (TableCellDecorator handler : handlers) {
            value = handler.configure(table, value, isSelected, hasFocus, row, column, this);
        }
        setValue(value);
        return component;
    }

    @Override
    public boolean hoverEffect(JTable table, int row, int column, Point mousePosition) {
        return handlers.stream().anyMatch(handler -> handler instanceof HoverTableCellDecorator)
                && mousePosition != null && table.getCellRect(row, column, false).contains(mousePosition);
    }
}
