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
import java.util.Collection;
import java.util.function.BiPredicate;

import javax.swing.JTable;
import javax.swing.table.TableModel;

/**
 * Interface for a {@link TableModel} that uses a list of beans for the row data.
 * @param <T> the class representing a row in the table
 */
public interface BeanTableModel<T> extends TableModel {
    /**
     * Get the number of beans in the model.  Need not be the same as the number of the rows in the table.
     */
    int getBeanCount();

    /**
     * Get the bean at the specified index.
     * @param index the index of the bean.
     */
    T getBean(int index);

    /**
     * Get a column cell value for a bean.
     */
    Object getValue(T bean, int columnIndex);

    /**
     * Replace all rows of the table model.
     * @param beans the new rows
     */
    void setBeans(Collection<T> beans);

    /**
     * Update existing rows and add missing rows to the table model.
     * @param beans beans to update/append
     * @param isEqual used to determine if a row is already in the model
     */
    void updateBeans(Collection<T> beans, BiPredicate<T, T> isEqual);

    /**
     * Get the mouse cursor to display for a cell.
     * @param rowIndex the row index of the cell
     * @param columnIndex the column index of the cell
     * @return the mouse cursor to use for the cell or {@code null} to use the default cursor.
     */
    Cursor getCursor(MouseEvent event, JTable table, int rowIndex, int columnIndex);

    /**
     * Handle a mouse click on a cell.
     * @param event the click event
     * @param rowIndex the row containing the cell
     * @param columnIndex the column index of the cell
     */
    void handleClick(MouseEvent event, JTable table, int rowIndex, int columnIndex);
}
