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
package io.github.jonestimd.swing.validation;

import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.Assertions.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.same;

@RunWith(MockitoJUnitRunner.class)
public class ValidatingTextCellEditorTest {
    @Mock
    private Validator<String> validator;
    @Mock
    private JTable table;
    @Mock
    private TableColumnModel columnModel;
    @Mock
    private TableColumn column;
    @Mock
    private JViewport viewport;
    @Captor
    private ArgumentCaptor<ComponentListener> componentListener;
    @Captor
    private ArgumentCaptor<HierarchyListener> hierarchyListener;
    @Captor
    private ArgumentCaptor<PropertyChangeListener> changeListener;

    @Test
    public void validatesOnStopEditing() throws Exception {
        when(table.getParent()).thenReturn(viewport);
        when(validator.validate("invalid")).thenReturn("error");
        when(validator.validate("valid")).thenReturn(null);
        ValidatingTextCellEditor editor = new ValidatingTextCellEditor(table, validator);
        ValidatedTextField component = (ValidatedTextField) editor.getTableCellEditorComponent(table, null, false, 0, 0);

        component.setText("invalid");
        assertThat(editor.stopCellEditing()).isFalse();

        component.setText("valid");
        assertThat(editor.stopCellEditing()).isTrue();
    }

    @Test
    public void doesNotupdateViewportOnResizeWhenNotEditing() throws Exception {
        when(table.getParent()).thenReturn(viewport);
        final ValidatingTextCellEditor editor = new ValidatingTextCellEditor(table, validator);
        when(table.isEditing()).thenReturn(false);
        when(table.getColumnModel()).thenReturn(columnModel);
        when(columnModel.getColumn(anyInt())).thenReturn(column);
        when(column.getCellEditor()).thenReturn(editor);
        verify(viewport).addComponentListener(componentListener.capture());

        componentListener.getValue().componentResized(new ComponentEvent(viewport, -1));

        verify(table, never()).getEditingRow();
        verify(table, never()).getEditingColumn();
        verify(table, never()).getCellRect(0, 0, true);
        verify(table, never()).scrollRectToVisible(any(Rectangle.class));
    }

    @Test
    public void updatesViewportOnResizeDuringEdit() throws Exception {
        when(table.getParent()).thenReturn(viewport);
        final ValidatingTextCellEditor editor = new ValidatingTextCellEditor(table, validator);
        when(table.isEditing()).thenReturn(true);
        when(table.getEditingRow()).thenReturn(0);
        when(table.getEditingColumn()).thenReturn(0);
        final Rectangle cellRectangle = new Rectangle();
        when(table.getCellRect(0, 0, true)).thenReturn(cellRectangle);
        when(table.getColumnModel()).thenReturn(columnModel);
        when(columnModel.getColumn(anyInt())).thenReturn(column);
        when(column.getCellEditor()).thenReturn(editor);
        verify(viewport).addComponentListener(componentListener.capture());

        componentListener.getValue().componentResized(new ComponentEvent(viewport, -1));

        verify(table).scrollRectToVisible(same(cellRectangle));
    }

    @Test
    public void removesListenerWhenViewportChanges() throws Exception {
        when(table.getParent()).thenReturn(viewport).thenReturn(null);
        new ValidatingTextCellEditor(table, validator);
        verify(viewport).addComponentListener(componentListener.capture());
        verify(table).addHierarchyListener(hierarchyListener.capture());

        hierarchyListener.getValue().hierarchyChanged(new HierarchyEvent(table, -1, viewport, null));

        verify(viewport).removeComponentListener(componentListener.getValue());
    }

    @Test
    public void addsListenerWhenViewportChanges() throws Exception {
        JViewport viewport2 = mock(JViewport.class);
        when(table.getParent()).thenReturn(viewport, viewport2);
        final ValidatingTextCellEditor editor = new ValidatingTextCellEditor(table, validator);
        verify(viewport).addComponentListener(componentListener.capture());
        verify(table).addHierarchyListener(hierarchyListener.capture());

        hierarchyListener.getValue().hierarchyChanged(new HierarchyEvent(table, -1, viewport, null));

        verify(viewport).removeComponentListener(componentListener.getValue());
        verify(viewport2).addComponentListener(componentListener.getValue());
    }

    @Test
    public void cancelsEditingWhenEditorRemovedBeforeStopEditing() throws Exception {
        when(table.getParent()).thenReturn(viewport);
        CellEditorListener listener = mock(CellEditorListener.class);
        final ValidatingTextCellEditor editor = new ValidatingTextCellEditor(table, validator);
        editor.addCellEditorListener(listener);
        verify(table).addPropertyChangeListener(eq("tableCellEditor"), changeListener.capture());

        changeListener.getValue().propertyChange(new PropertyChangeEvent(table, "tableCellEditor", null, editor));
        changeListener.getValue().propertyChange(new PropertyChangeEvent(table, "tableCellEditor", editor, null));

        verify(listener).editingCanceled(any());
    }

    @Test
    public void doesNotCancelEditingWhenEditorRemovedAfterStopEditing() throws Exception {
        when(table.getParent()).thenReturn(viewport);
        CellEditorListener listener = mock(CellEditorListener.class);
        ValidatingTextCellEditor editor = new ValidatingTextCellEditor(table, validator);
        editor.addCellEditorListener(listener);
        ValidatedTextField component = (ValidatedTextField) editor.getTableCellEditorComponent(table, null, false, 0, 0);
        component.setText("not null");
        verify(table).addPropertyChangeListener(eq("tableCellEditor"), changeListener.capture());

        changeListener.getValue().propertyChange(new PropertyChangeEvent(table, "tableCellEditor", null, editor));
        editor.stopCellEditing();
        changeListener.getValue().propertyChange(new PropertyChangeEvent(table, "tableCellEditor", editor, null));

        verify(listener, never()).editingCanceled(any());
    }
}