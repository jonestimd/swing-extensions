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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

import io.github.jonestimd.swing.table.model.BeanListTableModel;
import io.github.jonestimd.swing.table.model.ValidatedBeanListTableModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static java.awt.event.MouseEvent.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ClearButtonTableDecoratorTest {
    @Mock
    private BeanListTableModel<Object> model;
    @Mock
    private ValidatedBeanListTableModel<Object> validatedModel;
    @Mock
    private DecoratedTable<Object, BeanListTableModel<Object>> table;
    private DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();

    @Test
    public void prepareRenderer_mouseNotOverTable() throws Exception {
        when(table.getMousePosition()).thenReturn(null);
        ClearButtonTableDecorator decorator = new ClearButtonTableDecorator();
        DefaultTableCellRenderer renderer = mock(DefaultTableCellRenderer.class);

        decorator.prepareRenderer(table, renderer, 0, 0);

        verifyNoInteractions(renderer);
    }

    @Test
    public void prepareRenderer_mouseNotOverCell() throws Exception {
        setupMouse(0, 0, 1, 1);
        ClearButtonTableDecorator decorator = new ClearButtonTableDecorator();
        DefaultTableCellRenderer renderer = mock(DefaultTableCellRenderer.class);

        decorator.prepareRenderer(table, renderer, 0, 0);

        verifyNoInteractions(renderer);
    }

    @Test
    public void prepareRenderer_mouseOverCell_noHoverEffect() throws Exception {
        when(table.getModel()).thenReturn(validatedModel);
        setupMouse(2, 2, 5, 5);
        when(validatedModel.isCellEditable(0, 0)).thenReturn(false);
        ClearButtonTableDecorator decorator = new ClearButtonTableDecorator();
        renderer.setBorder(null);

        decorator.prepareRenderer(table, renderer, 0, 0);

        assertThat(renderer.getBorder()).isNull();
    }

    @Test
    public void prepareRenderer_mouseOverCell_hoverEffect_noBorder() throws Exception {
        when(table.getModel()).thenReturn(validatedModel);
        setupMouse(2, 2, 50, 16);
        when(validatedModel.isCellEditable(0, 0)).thenReturn(true);
        when(validatedModel.getValueAt(0, 0)).thenReturn("x");
        when(validatedModel.validateAt(0, 0, null)).thenReturn(null);
        ClearButtonTableDecorator decorator = new ClearButtonTableDecorator();
        renderer.setBorder(null);

        decorator.prepareRenderer(table, renderer, 0, 0);

        assertThat(renderer.getBorder()).isSameAs(decorator.border);
        assertThat(decorator.border.isHighlightButton()).isFalse();
    }

    @Test
    public void prepareRenderer_mouseOverCell_hoverEffect_withBorder() throws Exception {
        when(table.getModel()).thenReturn(validatedModel);
        setupMouse(2, 2, 50, 16);
        when(validatedModel.isCellEditable(0, 0)).thenReturn(true);
        when(validatedModel.getValueAt(0, 0)).thenReturn("x");
        when(validatedModel.validateAt(0, 0, null)).thenReturn(null);
        ClearButtonTableDecorator decorator = new ClearButtonTableDecorator();
        Border border = new EmptyBorder(0, 1, 0, 1);
        renderer.setBorder(border);

        decorator.prepareRenderer(table, renderer, 0, 0);

        assertThat(((CompoundBorder)renderer.getBorder()).getInsideBorder()).isSameAs(border);
        assertThat(((CompoundBorder)renderer.getBorder()).getOutsideBorder()).isSameAs(decorator.border);
    }

    @Test
    public void prepareRenderer_mouseOverCell_hoverEffect_highlighted() throws Exception {
        when(table.getModel()).thenReturn(validatedModel);
        setupMouse(50, 2, 50, 16);
        when(validatedModel.isCellEditable(0, 0)).thenReturn(true);
        when(validatedModel.getValueAt(0, 0)).thenReturn("x");
        when(validatedModel.validateAt(0, 0, null)).thenReturn(null);
        ClearButtonTableDecorator decorator = new ClearButtonTableDecorator();
        renderer.setBorder(null);

        decorator.prepareRenderer(table, renderer, 0, 0);

        assertThat(renderer.getBorder()).isSameAs(decorator.border);
        assertThat(decorator.border.isHighlightButton()).isTrue();
    }

    private <M extends BeanListTableModel<?>> void setupCell(M model, int column, boolean editable, Object value, Class<?> valueClass) {
        when(model.isCellEditable(0, column)).thenReturn(editable);
        when(model.getValueAt(0, column)).thenReturn(value);
        when(model.getColumnClass(column)).thenAnswer((invocation) -> valueClass);
    }

    @Test
    public void hoverEffect_unvalidatedModel() throws Exception {
        when(table.getModel()).thenReturn(model);
        setupCell(model, 0, false, "x", String.class);
        setupCell(model, 1, true, null, String.class);
        setupCell(model, 2, true, "x", String.class);
        setupCell(model, 3, true, false, Boolean.class);
        ClearButtonTableDecorator decorator = new ClearButtonTableDecorator();

        assertThat(decorator.hoverEffect(table, 0, 0)).isFalse();
        assertThat(decorator.hoverEffect(table, 0, 1)).isFalse();
        assertThat(decorator.hoverEffect(table, 0, 2)).isFalse();
        assertThat(decorator.hoverEffect(table, 0, 3)).isFalse();
    }

    private void setupValidatedCell(int column, boolean editable, String value, String validationMessage) {
        setupCell(validatedModel, column, editable, value, String.class);
        when(validatedModel.validateAt(0, column, null)).thenReturn(validationMessage);
    }

    @Test
    public void hoverEffect_validatedModel() throws Exception {
        when(table.getModel()).thenReturn(validatedModel);
        setupValidatedCell(0, true, "x", "invalid");
        setupValidatedCell(1, true, "x", null);
        ClearButtonTableDecorator decorator = new ClearButtonTableDecorator();

        assertThat(decorator.hoverEffect(table, 0, 0)).isFalse();
        assertThat(decorator.hoverEffect(table, 0, 1)).isTrue();
    }

    @Test
    public void onClick_ignoresButton2() throws Exception {
        ClearButtonTableDecorator decorator = new ClearButtonTableDecorator();
        MouseEvent event = mockEvent(MOUSE_CLICKED, 1, MouseEvent.BUTTON2);

        assertThat(decorator.onClick(event, table, 0, 0)).isFalse();

        verify(validatedModel, never()).setValueAt(null, 0, 0);
    }

    private MouseEvent mockEvent(int id, int clickCount, int button) {
        MouseEvent event = mock(MouseEvent.class);
        when(event.getID()).thenReturn(id);
        when(event.getClickCount()).thenReturn(clickCount);
        when(event.getButton()).thenReturn(button);
        return event;
    }

    @Test
    public void onClick_ignoresDoubleClick() throws Exception {
        ClearButtonTableDecorator decorator = new ClearButtonTableDecorator();
        MouseEvent event = mockEvent(MOUSE_CLICKED, 2, MouseEvent.BUTTON1);

        assertThat(decorator.onClick(event, table, 0, 0)).isFalse();

        verify(validatedModel, never()).setValueAt(null, 0, 0);
    }

    private void setupMouse(int x, int y, int cellWidth, int cellHeight) {
        when(table.getMousePosition()).thenReturn(new Point(x, y));
        when(table.getCellRect(0, 0, false)).thenReturn(new Rectangle(1, 1, cellWidth, cellHeight));
    }

    @Test
    public void onClick_ignoresClickOffButton() throws Exception {
        DefaultTableCellRenderer renderer = mock(DefaultTableCellRenderer.class);
        when(table.getModel()).thenReturn(validatedModel);
        when(table.getCellRenderer(0, 0)).thenReturn(renderer);
        setupMouse(2, 2, 50, 16);
        setupValidatedCell(0, true, "x", null);
        ClearButtonTableDecorator decorator = new ClearButtonTableDecorator();

        assertThat(decorator.onClick(mockEvent(MOUSE_CLICKED, 1, MouseEvent.BUTTON1), table, 0, 0)).isFalse();

        verify(validatedModel, never()).setValueAt(null, 0, 0);
    }

    @Test
    public void onClick_clearsCell() throws Exception {
        DefaultTableCellRenderer renderer = mock(DefaultTableCellRenderer.class);
        when(table.getModel()).thenReturn(validatedModel);
        setupMouse(50, 2, 50, 16);
        when(table.getCellRenderer(0, 0)).thenReturn(renderer);
        setupValidatedCell(0, true, "x", null);
        ClearButtonTableDecorator decorator = new ClearButtonTableDecorator();

        assertThat(decorator.onClick(mockEvent(MOUSE_CLICKED, 1, MouseEvent.BUTTON1), table, 0, 0)).isTrue();

        verify(validatedModel).setValueAt(null, 0, 0);
    }

    @Test
    public void startEdit_ignoresButton2() throws Exception {
        ClearButtonTableDecorator decorator = new ClearButtonTableDecorator();

        assertThat(decorator.startEdit(mockEvent(MOUSE_CLICKED, 1, MouseEvent.BUTTON2), table, 0, 0)).isTrue();
    }

    @Test
    public void startEdit_ignoresDoubleClick() throws Exception {
        ClearButtonTableDecorator decorator = new ClearButtonTableDecorator();

        assertThat(decorator.startEdit(mockEvent(MOUSE_CLICKED, 2, MouseEvent.BUTTON1), table, 0, 0)).isTrue();
    }

    @Test
    public void startEdit_ignoresClickWithNoButton() throws Exception {
        DefaultTableCellRenderer renderer = mock(DefaultTableCellRenderer.class);
        when(table.getModel()).thenReturn(validatedModel);
        setupMouse(2, 2, 50, 16);
        when(table.getCellRenderer(0, 0)).thenReturn(renderer);
        ClearButtonTableDecorator decorator = new ClearButtonTableDecorator();

        assertThat(decorator.startEdit(mockEvent(MOUSE_PRESSED, 1, MouseEvent.BUTTON1), table, 0, 0)).isTrue();
    }

    @Test
    public void startEdit_ignoresClickOffButton() throws Exception {
        DefaultTableCellRenderer renderer = mock(DefaultTableCellRenderer.class);
        when(table.getModel()).thenReturn(validatedModel);
        setupMouse(2, 2, 50, 16);
        when(table.getCellRenderer(0, 0)).thenReturn(renderer);
        setupValidatedCell(0, true, "x", null);
        ClearButtonTableDecorator decorator = new ClearButtonTableDecorator();

        assertThat(decorator.startEdit(mockEvent(MOUSE_PRESSED, 1, MouseEvent.BUTTON1), table, 0, 0)).isTrue();
    }

    @Test
    public void startEdit_preventsEditForClickOnButton() throws Exception {
        DefaultTableCellRenderer renderer = mock(DefaultTableCellRenderer.class);
        when(table.getModel()).thenReturn(validatedModel);
        setupMouse(50, 2, 50, 16);
        when(table.getCellRenderer(0, 0)).thenReturn(renderer);
        setupValidatedCell(0, true, "x", null);
        ClearButtonTableDecorator decorator = new ClearButtonTableDecorator();

        assertThat(decorator.startEdit(mockEvent(MOUSE_PRESSED, 1, MouseEvent.BUTTON1), table, 0, 0)).isFalse();
    }
}