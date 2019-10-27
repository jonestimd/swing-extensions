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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.border.LineBorder;

import io.github.jonestimd.swing.ButtonBarFactory;
import io.github.jonestimd.swing.ComponentResources;
import io.github.jonestimd.swing.ComponentTreeUtils;
import io.github.jonestimd.swing.action.ActionUtils;

/**
 * A table cell editor for selecting a color.  Displays a JColorChooser in a popup window.
 */
public class ColorTableCellEditor extends PopupTableCellEditor {
    public static final String OK_NAME = ComponentResources.lookupString("action.ok.name");
    public static final String CANCEL_NAME = ComponentResources.lookupString("action.cancel.name");
    private final JPanel popupPanel = new JPanel(new BorderLayout());
    private JColorChooser colorChooser;
    private JTabbedPane tabPane;
    private final Action okAction = new AbstractAction(OK_NAME) {
        @Override
        public void actionPerformed(ActionEvent e) {
            fireEditingStopped();
        }
    };
    private final Action cancelAction = new AbstractAction(CANCEL_NAME) {
        @Override
        public void actionPerformed(ActionEvent e) {
            fireEditingCanceled();
        }
    };

    public ColorTableCellEditor() {
        popupPanel.setBorder(new LineBorder(Color.BLACK));
        popupPanel.add(new ButtonBarFactory().alignRight().border(10, 10).add(okAction, cancelAction).get(), BorderLayout.SOUTH);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        colorChooser = new JColorChooser();
        tabPane = ComponentTreeUtils.findComponent(colorChooser, JTabbedPane.class);
        if (value instanceof Color) colorChooser.setColor((Color) value);
        JComponent editorComponent = (JComponent) super.getTableCellEditorComponent(table, value, isSelected, row, column);
        editorComponent.setOpaque(true);
        if (value instanceof Color) editorComponent.setBackground((Color) value);
        return editorComponent;
    }

    @Override
    public Object getCellEditorValue() {
        return colorChooser.getColor();
    }

    @Override
    protected Point getPopupLocation() {
        Point point = super.getPopupLocation();
        point.y += getTableCellSize().height;
        return point;
    }

    @Override
    protected Window createWindow(Window owner) {
        popupPanel.add(colorChooser, BorderLayout.CENTER);
        JWindow popupWindow = new JWindow(owner);
        popupWindow.getContentPane().add(popupPanel);
        ActionUtils.install(popupWindow.getRootPane(), okAction, "commit", "ENTER");
        ActionUtils.install(popupWindow.getRootPane(), cancelAction, "close", "ESCAPE");
        colorChooser.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isAltDown()) {
                    KeyStroke ks = KeyStroke.getKeyStrokeForEvent(e);
                    for (int i = 0; i < tabPane.getTabCount(); i++) {
                        if (ks.equals(KeyStroke.getKeyStroke(tabPane.getMnemonicAt(i), KeyEvent.ALT_DOWN_MASK))) tabPane.setSelectedIndex(i);
                    }
                }
            }
        });
        return popupWindow;
    }

    @Override
    protected void showPopup() {
        super.showPopup();
        colorChooser.requestFocus();
    }

    @Override
    protected void hidePopup() {
        super.hidePopup();
        popupPanel.remove(colorChooser);
    }
}
