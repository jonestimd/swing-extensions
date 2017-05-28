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
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.JTable;

import io.github.jonestimd.swing.action.BackgroundAction;
import io.github.jonestimd.swing.dialog.Dialogs;
import io.github.jonestimd.swing.table.DecoratedTable;
import io.github.jonestimd.swing.table.model.BeanListTableModel;
import io.github.jonestimd.swing.table.model.BufferedBeanListTableModel;

public abstract class ReloadTableAction<T> extends BackgroundAction<List<T>> {
    private final JTable table;
    private final BufferedBeanListTableModel<T> tableModel;

    public ReloadTableAction(JComponent owner, ResourceBundle bundle, String resourcePrefix,
            DecoratedTable<T, ? extends BeanListTableModel<T>> table, BufferedBeanListTableModel<T> tableModel) {
        super(owner, bundle, resourcePrefix);
        this.table = table;
        this.tableModel = tableModel;
    }

    public boolean confirmAction(ActionEvent event) {
        return ! tableModel.isChanged() || Dialogs.confirmDiscardChanges(table.getTopLevelAncestor());
    }

    public void updateUI(List<T> rows) {
        int selectedRow = Math.max(0, table.getSelectedRow());
        int selectedColumn = Math.max(0, table.getSelectedColumn());
        tableModel.setBeans(rows);
        int rowCount = table.getRowCount();
        if (rowCount > 0) {
            selectedRow = Math.min(selectedRow, rowCount -1);
            table.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
            table.getColumnModel().getSelectionModel().setSelectionInterval(selectedColumn, selectedColumn);
        }
    }
}