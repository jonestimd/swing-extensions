// The MIT License (MIT)
//
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

import javax.swing.JTable;
import javax.swing.table.TableModel;

import io.github.jonestimd.swing.table.model.ChangeBufferTableModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UndoActionTest {
    @Mock
    private JTable table;
    private UndoAction action = new UndoAction();

    @Test
    public void doesNotingIfNotChangeBufferedTableModel() throws Exception {
        final TableModel tableModel = mock(TableModel.class);
        when(table.getModel()).thenReturn(tableModel);
        when(table.convertRowIndexToModel(anyInt())).thenReturn(0);

        action.actionPerformed(new ActionEvent(table, -1, null));

        verifyNoInteractions(tableModel);
    }

    @Test
    public void doesNotingIfNoSelection() throws Exception {
        final ChangeBufferTableModel tableModel = mock(ChangeBufferTableModel.class);
        when(table.getModel()).thenReturn(tableModel);
        when(table.convertRowIndexToModel(anyInt())).thenReturn(-1);

        action.actionPerformed(new ActionEvent(table, -1, null));

        verifyNoInteractions(tableModel);
    }

    @Test
    public void undoesPendingDelete() throws Exception {
        final int row = 1;
        final ChangeBufferTableModel tableModel = mock(ChangeBufferTableModel.class);
        when(table.getModel()).thenReturn(tableModel);
        when(table.convertRowIndexToModel(anyInt())).thenReturn(row);
        when(tableModel.isPendingDelete(row)).thenReturn(true);

        action.actionPerformed(new ActionEvent(table, -1, null));

        verify(tableModel).undoDelete(row);
    }

    @Test
    public void doesNothingIfColumnOutOfRange() throws Exception {
        final int row = 1;
        final int column = -1;
        final ChangeBufferTableModel tableModel = mock(ChangeBufferTableModel.class);
        when(table.getModel()).thenReturn(tableModel);
        when(table.convertRowIndexToModel(anyInt())).thenReturn(row);
        when(table.convertColumnIndexToModel(anyInt())).thenReturn(column);
        when(tableModel.isPendingDelete(row)).thenReturn(false);

        action.actionPerformed(new ActionEvent(table, -1, null));

        verify(tableModel, never()).undoChangedAt(row, column);
    }

    @Test
    public void undoesPendingChange() throws Exception {
        final int row = 1;
        final int column = 2;
        final ChangeBufferTableModel tableModel = mock(ChangeBufferTableModel.class);
        when(table.getModel()).thenReturn(tableModel);
        when(table.convertRowIndexToModel(anyInt())).thenReturn(row);
        when(table.convertColumnIndexToModel(anyInt())).thenReturn(column);
        when(tableModel.isPendingDelete(row)).thenReturn(false);

        action.actionPerformed(new ActionEvent(table, -1, null));

        verify(tableModel).undoChangedAt(row, column);
    }
}