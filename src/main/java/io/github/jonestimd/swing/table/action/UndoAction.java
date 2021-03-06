// The MIT License (MIT)
//
// Copyright (c) 2017 Timothy D. Jones
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
package io.github.jonestimd.swing.table.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JTable;
import javax.swing.table.TableModel;

import io.github.jonestimd.swing.table.model.ChangeBufferTableModel;

/**
 * A UI action for reverting a pending change in a table.
 * @see ChangeBufferTableModel
 */
public class UndoAction extends AbstractAction {
    @Override
    public void actionPerformed(ActionEvent e) {
        JTable table = (JTable) e.getSource();
        TableModel model = table.getModel();
        int selectedRow = table.convertRowIndexToModel(table.getSelectedRow());
        if (selectedRow >= 0 && model instanceof ChangeBufferTableModel) {
            ChangeBufferTableModel<?> changeBufferTableModel = (ChangeBufferTableModel<?>) model;
            int selectedColumn = table.convertColumnIndexToModel(table.getSelectedColumn());
            if (changeBufferTableModel.isPendingDelete(selectedRow)) {
                changeBufferTableModel.undoDelete(selectedRow);
            }
            else if (selectedColumn >= 0) {
                changeBufferTableModel.undoChangedAt(selectedRow, selectedColumn);
            }
        }
    }
}
