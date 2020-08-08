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

import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JTable;

/**
 * Base class for {@link TableCellDecorator}s that display a hover effect.
 */
public abstract class HoverTableCellDecorator implements TableCellDecorator {
    /**
     * Calls {@link #onHover(JTable, Point, int, int, CompositeTableCellRenderer)} if the mouse cursor is inside the table cell.
     * @return the {@code value} input parameter
     */
    @Override
    public Object configure(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column, CompositeTableCellRenderer renderer) {
        Point position = table.getMousePosition();
        if (position != null) {
            Rectangle rect = table.getCellRect(row, column, false);
            if (rect.contains(position)) {
                position.translate(-rect.x, -rect.y);
                onHover(table, position, row, column, renderer);
            }
        }
        return value;
    }

    /**
     * Configures the {@code renderer} to display the hover effect.
     * @param table the table
     * @param position the cursor position within the table cell
     * @param row the row index of the table cell
     * @param column the column index of the table cell
     * @param renderer the cell renderer
     */
    protected abstract void onHover(JTable table, Point position, int row, int column, CompositeTableCellRenderer renderer);
}
