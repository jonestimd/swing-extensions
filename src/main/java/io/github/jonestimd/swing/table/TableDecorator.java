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

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTable;

import io.github.jonestimd.swing.table.model.BeanTableModel;
import io.github.jonestimd.swing.table.model.ChangeBufferTableModel;
import io.github.jonestimd.swing.table.model.ValidatedTableModel;

/**
 * An interface for preparing table cell renderers.
 * @see DecoratedTable
 */
public interface TableDecorator {
    /**
     * @param table the table
     * @param renderer the table cell renderer
     * @param row the table model row index of the cell
     * @param column the table model column index of the cell
     * @param <B> the type of beans displayed by the table
     * @param <M> the class of the table model
     */
    <B, M extends BeanTableModel<B>> void prepareRenderer(DecoratedTable<B, M> table, JComponent renderer, int row, int column);

    /**
     * Get a set of decorators for a table based on it's model.
     * <ul>
     *     <li>includes {@link UnsavedChangeDecorator} if the table's model is a {@link ChangeBufferTableModel}</li>
     *     <li>includes {@link ValidationDecorator} if the table's model is a {@link ValidatedTableModel}</li>
     *     <li>includes {@link ClearButtonTableDecorator} if the table's model is a {@link ValidatedTableModel}</li>
     * </ul>
     * @return a list of table decorators
     */
    static List<TableDecorator> defaultDecorators(JTable table) {
        List<TableDecorator> decorators = new ArrayList<>();
        if (table.getModel() instanceof ChangeBufferTableModel) {
            decorators.add(new UnsavedChangeDecorator());
        }
        if (table.getModel() instanceof ValidatedTableModel) {
            decorators.add(new ValidationDecorator());
            decorators.add(new ClearButtonTableDecorator());
        }
        return decorators;
    }
}