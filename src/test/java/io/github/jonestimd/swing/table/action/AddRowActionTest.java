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

import java.awt.Rectangle;
import java.util.ResourceBundle;
import java.util.function.Supplier;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumnModel;

import io.github.jonestimd.swing.table.model.BufferedBeanListTableModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AddRowActionTest {
    private static final String NEW_BEAN = "new bean";
    private final ResourceBundle bundle = ResourceBundle.getBundle("test-resources");
    @Mock
    private BufferedBeanListTableModel<String> model;
    @Mock
    private JTable table;
    @Mock
    private ListSelectionModel selectionModel;
    @Mock
    private TableColumnModel columnModel;
    @Mock
    private ListSelectionModel columnSelectionModel;
    private Supplier<String> beanFactory = () -> NEW_BEAN;
    private AddRowAction<String> action;
    private final int modelIndex = 1;
    private final int viewIndex = 2;
    private final Rectangle cellRect = new Rectangle();

    @Before
    public void setupMocks() throws Exception {
        when(model.indexOf(NEW_BEAN)).thenReturn(modelIndex);
        when(table.convertRowIndexToView(1)).thenReturn(viewIndex);
        when(table.getSelectionModel()).thenReturn(selectionModel);
        when(table.getColumnModel()).thenReturn(columnModel);
        when(table.getCellRect(viewIndex, 0, true)).thenReturn(cellRect);
        when(table.getColumnCount()).thenReturn(2);
        action = new AddRowAction<>(bundle, "addRowActionTest", model, table, beanFactory);
    }

    @Test
    public void actionPerformedEditsFirstEditableCell() throws Exception {
        when(table.isCellEditable(eq(viewIndex), anyInt())).thenReturn(false, true);
        when(columnModel.getSelectionModel()).thenReturn(columnSelectionModel);

        action.actionPerformed(null);

        verify(model).queueAdd(NEW_BEAN);
        verify(selectionModel).setSelectionInterval(viewIndex, viewIndex);
        verify(columnSelectionModel).setSelectionInterval(0, 0);
        verify(table).scrollRectToVisible(same(cellRect));
        verify(table).grabFocus();
        verify(table).editCellAt(viewIndex, 1);
    }

    @Test
    public void actionPerformedNoEditableCells() throws Exception {
        when(table.isCellEditable(eq(viewIndex), anyInt())).thenReturn(false, false);
        when(columnModel.getSelectionModel()).thenReturn(columnSelectionModel);

        action.actionPerformed(null);

        verify(model).queueAdd(NEW_BEAN);
        verify(selectionModel).setSelectionInterval(viewIndex, viewIndex);
        verify(columnSelectionModel).setSelectionInterval(0, 0);
        verify(table).scrollRectToVisible(same(cellRect));
        verify(table).grabFocus();
        verify(table).editCellAt(viewIndex, -1);
    }
}