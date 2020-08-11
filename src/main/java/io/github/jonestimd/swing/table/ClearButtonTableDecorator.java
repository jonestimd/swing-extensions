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

import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.border.CompoundBorder;

import io.github.jonestimd.swing.component.AbstractButtonBorder.Side;
import io.github.jonestimd.swing.component.ClearButtonBorder;
import io.github.jonestimd.swing.table.model.BeanTableModel;
import io.github.jonestimd.swing.table.model.ValidatedTableModel;

import static java.awt.event.MouseEvent.*;

public class ClearButtonTableDecorator implements TableHoverDecorator, TableMouseDecorator {
    protected final ClearButtonBorder border;

    public ClearButtonTableDecorator() {
        this(Side.RIGHT);
    }

    public ClearButtonTableDecorator(Side side) {
        border = new ClearButtonBorder(side);
    }

    @Override
    public <B, M extends BeanTableModel<B>> void prepareRenderer(DecoratedTable<B, M> table, JComponent renderer, int modelRow, int modelColumn) {
        Point mousePosition = getCellMousePosition(table, modelRow, modelColumn, renderer);
        if (mousePosition != null && hoverEffect(table, modelRow, modelColumn)) {
            if (renderer.getBorder() == null) renderer.setBorder(border);
            else {
                renderer.setBorder(new CompoundBorder(border, renderer.getBorder()));
            }
        }
    }

    private Point getCellMousePosition(DecoratedTable<?, ?> table, int modelRow, int modelColumn, JComponent renderer) {
        Point mousePosition = table.getMousePosition();
        if (mousePosition != null) {
            Rectangle cellRect = table.getCellRect(table.convertRowIndexToView(modelRow), table.convertColumnIndexToView(modelColumn), false);
            if (cellRect.contains(mousePosition)) {
                mousePosition.translate(-cellRect.x, -cellRect.y);
                Insets insets = border.getBorderInsets(renderer, cellRect.width, cellRect.height);
                boolean highlight = insets.left > 0 && mousePosition.x < insets.left
                        || insets.right > 0 && mousePosition.x > cellRect.width - insets.right;
                border.setHighlightButton(highlight);
                return mousePosition;
            }
        }
        return null;
    }

    @Override
    public <B, M extends BeanTableModel<B>> boolean hoverEffect(DecoratedTable<B, M> table, int modelRow, int modelColumn) {
        M model = table.getModel();
        if (model.isCellEditable(modelRow, modelColumn) && model.getValueAt(modelRow, modelColumn) != null
                && model.getColumnClass(modelColumn) != Boolean.class) {
            if (model instanceof ValidatedTableModel) {
                ValidatedTableModel validatedModel = (ValidatedTableModel) model;
                return validatedModel.validateAt(modelRow, modelColumn, null) == null;
            }
        }
        return false;
    }

    @Override
    public <B, M extends BeanTableModel<B>> boolean onClick(MouseEvent event, DecoratedTable<B, M> table, int modelRow, int modelColumn) {
        if (event.getClickCount() == 1 && event.getButton() == BUTTON1 && hoverEffect(table, modelRow, modelColumn)) {
            if (isMouseOverButton(table, modelRow, modelColumn)) {
                table.getModel().setValueAt(null, modelRow, modelColumn);
                return true;
            }
        }
        return false;
    }

    @Override
    public <B, M extends BeanTableModel<B>> boolean startEdit(MouseEvent event, DecoratedTable<B, M> table, int modelRow, int modelColumn) {
        return !(event.getButton() == BUTTON1 && event.getID() == MOUSE_PRESSED
                && hoverEffect(table, modelRow, modelColumn) && isMouseOverButton(table, modelRow, modelColumn));
    }

    private boolean isMouseOverButton(DecoratedTable<?, ?> table, int modelRow, int modelColumn) {
        border.setHighlightButton(false);
        getCellMousePosition(table, modelRow, modelColumn, (JComponent) table.getCellRenderer(modelRow, modelColumn));
        return border.isHighlightButton();
    }
}
