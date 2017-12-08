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
package io.github.jonestimd.swing.table.model;

import java.awt.Cursor;
import java.awt.event.MouseEvent;

import javax.swing.JTable;

import io.github.jonestimd.beans.ReadWriteAccessor;

/**
 * Interface for mapping a bean property to a table column.
 */
public interface ColumnAdapter<Bean, Value> extends ReadWriteAccessor<Bean, Value> {
    /**
     * @return the table column ID
     */
    String getColumnId();

    /**
     * Get a resource value for the table column.
     */
    String getResource(String name, String defaultValue);

    /**
     * Get the name to be displayed in the table column header.
     */
    String getName();

    /**
     * Get the property value type.
     */
    Class<? super Value> getType();

    /**
     * @return true if the property is editable for the specified bean.
     */
    boolean isEditable(Bean row);

    /**
     * Get the mouse cursor to display for a cell.  Return {@code null} to use the default cursor.
     * The default implementation returns {@code null}.
     * @param event the mouse event
     * @param table the table that received the event
     * @param row the row containing the cell
     */
    default Cursor getCursor(MouseEvent event, JTable table, Bean row) {
        return null;
    }

    /**
     * Handle a mouse click on a cell.
     * @param event the click event
     * @param table the table that received the event
     * @param row the row containing the cell
     */
    default void handleClick(MouseEvent event, JTable table, Bean row) {
    }
}
