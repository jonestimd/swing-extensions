// The MIT License (MIT)
//
// Copyright (c) 2019 Timothy D. Jones
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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyBoundsAdapter;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractCellEditor;
import javax.swing.FocusManager;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 * Abstract base class for a table cell editor that displays a popup window.
 */
public abstract class PopupTableCellEditor extends AbstractCellEditor implements TableCellEditor {
    private Window popupWindow;
    /**
     * Added to the table to track position for the popup window.
     */
    private final JLabel editorComponent = new JLabel();
    private final HierarchyBoundsListener movementListener = new HierarchyBoundsAdapter() {
        public void ancestorMoved(HierarchyEvent e) {
            popupWindow.setLocation(getPopupLocation());
        }
    };
    private final ComponentListener componentListener = new ComponentAdapter() {
        public void componentMoved(ComponentEvent e) {
            popupWindow.setLocation(getPopupLocation());
        }
    };
    private final PropertyChangeListener focusedWindowListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getOldValue() == popupWindow) {
                fireEditingCanceled();
            }
        }
    };

    /**
     * Create a {@code PopupTableCellEditor}.
     */
    protected PopupTableCellEditor() {
        editorComponent.addHierarchyListener(this::hierarchyChanged);
    }

    /**
     * Overridden to hide the popup window.
     */
    @Override
    protected void fireEditingStopped() {
        hidePopup();
        super.fireEditingStopped();
    }

    /**
     * Overridden to hide the popup window.
     */
    @Override
    protected void fireEditingCanceled() {
        hidePopup();
        super.fireEditingCanceled();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        return editorComponent;
    }

    /**
     * Default implementation returns the top left corner of the table cell.
     */
    protected Point getPopupLocation() {
        return editorComponent.getLocationOnScreen();
    }

    /**
     * Hide the popup window if it is showing.
     */
    protected void hidePopup() {
        if (popupWindow != null) {
            FocusManager.getCurrentManager().removePropertyChangeListener("focusedWindow", focusedWindowListener);
            editorComponent.removeHierarchyBoundsListener(movementListener);
            editorComponent.removeComponentListener(componentListener);
            popupWindow.dispose();
            popupWindow = null;
        }
    }

    /**
     * Get the size of the table cell.  May be useful for sizing/placing the popup window.
     */
    protected Dimension getTableCellSize() {
        return  editorComponent.getSize();
    }

    /**
     * Show the popup window.
     */
    protected void showPopup() {
        FocusManager.getCurrentManager().addPropertyChangeListener("focusedWindow", focusedWindowListener);
        popupWindow.pack();
        popupWindow.setLocation(getPopupLocation());
        popupWindow.setVisible(true);
        popupWindow.toFront();
        editorComponent.addHierarchyBoundsListener(movementListener);
        editorComponent.addComponentListener(componentListener);
    }

    /**
     * Create the popup window.
     * @param owner the window containing the table
     */
    protected abstract Window createWindow(Window owner);

    /**
     * Creates and displays the popup window when the {@code editorComponent} is added to the table.
     */
    private void hierarchyChanged(HierarchyEvent event) {
        if ((event.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && editorComponent.isShowing()) {
            popupWindow = createWindow((Window) editorComponent.getTopLevelAncestor());
            showPopup();
        }
    }
}