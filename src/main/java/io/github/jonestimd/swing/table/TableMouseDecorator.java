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

import java.awt.event.MouseEvent;

import io.github.jonestimd.swing.table.model.BeanTableModel;

public interface TableMouseDecorator extends TableDecorator {
    /**
     * Called when the decorated table receives a mouse click event on a cell.
     * @param event the mouse event
     * @param table the decorated table
     * @param modelRow the model row index of the cell
     * @param modelColumn the model column index of the cell
     * @param <B> the type of beans in the table model
     * @param <M> the class of the table model
     * @return true if the click was handled
     */
    <B, M extends BeanTableModel<B>> boolean onClick(MouseEvent event, DecoratedTable<B, M> table, int modelRow, int modelColumn);

    /**
     * Called before the decorated table starts editing a cell based on a mouse event.
     * @param event the mouse event that triggered the cell edit
     * @param table the decorated table
     * @param modelRow the model row index of the cell
     * @param modelColumn the model column index of the cell
     * @param <B> the type of beans in the table model
     * @param <M> the class of the table model
     * @return true if the cell edit should proceed or false to prevent the edit
     */
    <B, M extends BeanTableModel<B>> boolean startEdit(MouseEvent event, DecoratedTable<B, M> table, int modelRow, int modelColumn);
}
