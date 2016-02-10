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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.border.LineBorder;

import io.github.jonestimd.swing.ComponentTreeUtils;

public class ValidatingTextCellEditor extends DefaultCellEditor {
    private final ValidatedTextField textComponent;
    private final ComponentListener resizeListener;
    private String textValue;
    private JViewport viewport;

    public ValidatingTextCellEditor(final JTable table, Validator<String> validator) {
        super(new ValidatedTextField(validator));
        textComponent = (ValidatedTextField) getComponent();
        textComponent.setBorder(new LineBorder(Color.BLACK));
        resizeListener = new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                if (table.isEditing()) {
                    table.scrollRectToVisible(table.getCellRect(table.getEditingRow(), table.getEditingColumn(), true));
                }
            }
        };
        updateViewport(table);
        table.addHierarchyListener(new HierarchyListener() {
            public void hierarchyChanged(HierarchyEvent e) {
                updateViewport(table);
            }
        });
        // TODO when is this listener needed?
        table.addPropertyChangeListener("tableCellEditor", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                if (isCancelEvent(event)) {
                    cancelCellEditing();
                }
            }
        });
    }

    private void updateViewport(JTable table) {
        JViewport ancestor = ComponentTreeUtils.findAncestor(table, JViewport.class);
        if (viewport != ancestor) {
            if (viewport != null) {
                viewport.removeComponentListener(resizeListener);
            }
            viewport = ancestor;
            if (viewport != null) {
                viewport.addComponentListener(resizeListener);
            }
        }
    }

    private boolean isCancelEvent(PropertyChangeEvent event) {
        return event.getOldValue() == this && event.getNewValue() == null && textValue == null;
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        super.getTableCellEditorComponent(table, value, isSelected, row, column);
        textValue = null;
        return textComponent;
    }

    public boolean stopCellEditing() {
        if (textComponent.getValidationMessages() == null && super.stopCellEditing()) {
            textValue = textComponent.getText();
            return true;
        }
        return false;
    }
}