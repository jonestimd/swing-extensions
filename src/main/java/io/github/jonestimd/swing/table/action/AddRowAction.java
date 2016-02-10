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
package io.github.jonestimd.swing.table.action;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import java.util.function.Supplier;

import javax.swing.JTable;

import io.github.jonestimd.swing.action.MnemonicAction;
import io.github.jonestimd.swing.table.model.BufferedBeanListTableModel;

public class AddRowAction<T> extends MnemonicAction {
    private final BufferedBeanListTableModel<T> tableModel;
    private final JTable table;
    private final Supplier<T> newBeanSupplier;

    public AddRowAction(ResourceBundle bundle, String keyPrefix, BufferedBeanListTableModel<T> tableModel, JTable table, Supplier<T> newBeanSupplier) {
        super(bundle, keyPrefix);
        this.tableModel = tableModel;
        this.table = table;
        this.newBeanSupplier = newBeanSupplier;
    }

    public void actionPerformed(ActionEvent e) {
        T bean = newBeanSupplier.get();
        tableModel.queueAdd(bean);
        int row = table.convertRowIndexToView(tableModel.indexOf(bean));
        table.getSelectionModel().setSelectionInterval(row, row);
        table.getColumnModel().getSelectionModel().setSelectionInterval(0, 0);
        table.scrollRectToVisible(table.getCellRect(row, 0, true));
        table.grabFocus();
        table.editCellAt(row, getFirstEditableColumn(row));
    }

    private int getFirstEditableColumn(int row) {
        for (int column = 0; column < table.getColumnCount(); column++) {
            if (table.isCellEditable(row, column)) {
                return column;
            }
        }
        return  -1;
    }
}