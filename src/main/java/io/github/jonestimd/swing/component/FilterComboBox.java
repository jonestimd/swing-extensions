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
package io.github.jonestimd.swing.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyBoundsAdapter;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.KeyEvent;

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import io.github.jonestimd.swing.DocumentChangeHandler;

public class FilterComboBox<T> extends JTextField {
    private final FilterComboBoxModel<T> model;
    private final JList<T> popupList;
    private final Window popupWindow;
    private boolean autoSelectText = true;
    private boolean autoSelectItem = true;
    private HierarchyBoundsListener movementListener = new HierarchyBoundsAdapter() {
        public void ancestorMoved(HierarchyEvent e) {
            popupWindow.setLocation(getPopupLocation());
            popupWindow.toFront();
        }
    };
    private ComponentListener componentListener = new ComponentAdapter() {
        public void componentMoved(ComponentEvent e) {
            popupWindow.setLocation(getPopupLocation());
            popupWindow.toFront();
        }
    };

    public FilterComboBox(FilterComboBoxModel<T> model) {
        this(model, 5);
    }

    public FilterComboBox(FilterComboBoxModel<T> model, int visibleRows) {
        this.model = model;
        popupList = new JList<>(model);
        popupList.setVisibleRowCount(visibleRows);
        popupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        popupList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) model.setSelectedItem(popupList.getSelectedValue());
        });
        popupWindow = new Window(null);
        popupWindow.setLayout(new BorderLayout());
        popupWindow.add(new JScrollPane(popupList), BorderLayout.CENTER);
        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                showPopup();
                selectListItem();
                if (autoSelectText) selectAll();
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (popupWindow.isVisible()) {
                    hidePopup();
                    setText(model.getSelectedItem() == null ? "" : model.formatItem(model.getSelectedItem()));
                }
            }
        });
        getDocument().addDocumentListener(new DocumentChangeHandler(this::filterList));
    }

    public boolean isAutoSelectText() {
        return autoSelectText;
    }

    public void setAutoSelectText(boolean autoSelectText) {
        boolean oldValue = this.autoSelectText;
        this.autoSelectText = autoSelectText;
        firePropertyChange("autoSelectText", oldValue, autoSelectText);
    }

    /**
     * Auto select an item when it is the only match.
     */
    public boolean isAutoSelectItem() {
        return autoSelectItem;
    }

    public void setAutoSelectItem(boolean autoSelectItem) {
        boolean oldValue = this.autoSelectItem;
        this.autoSelectItem = autoSelectItem;
        firePropertyChange("autoSelectItem", oldValue, autoSelectItem);
    }

    @Override
    protected void processKeyEvent(KeyEvent event) {
        super.processKeyEvent(event);
        if (event.getID() == KeyEvent.KEY_PRESSED && event.getModifiersEx() == 0) {
            if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                if (popupWindow.isVisible()) {
                    hidePopup();
                    if (model.getSelectedItem() != null && !model.formatItem(model.getSelectedItem()).equals(getText())) {
                        setText("");
                        model.setSelectedItem(null);
                        popupList.clearSelection();
                    }
                }
                else showPopup();
            }
            else if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                if (popupWindow.isVisible() && popupList.getSelectedIndex() >= 0) {
                    hidePopup();
                    setText(model.getSelectedItem() == null ? "" : model.formatItem(model.getSelectedItem()));
                }
            }
            else {
                if (!popupWindow.isVisible()) showPopup();
                int selectedIndex = popupList.getSelectedIndex();
                int size = popupList.getModel().getSize();
                if (size > 0) {
                    if (event.getKeyCode() == KeyEvent.VK_DOWN) {
                        if (++selectedIndex >= size) selectedIndex = 0;
                        popupList.setSelectedIndex(selectedIndex);
                    }
                    else if (event.getKeyCode() == KeyEvent.VK_UP) {
                        if (--selectedIndex < 0) selectedIndex = size - 1;
                        popupList.setSelectedIndex(selectedIndex);
                    }
                }
            }
        }
    }

    protected void filterList() {
        model.applyFilter(getText());
        if (autoSelectItem && model.getSize() == 1) model.setSelectedItem(model.getElementAt(0));
        selectListItem();
    }

    private void selectListItem() {
        if (model.getSelectedItem() == null) popupList.clearSelection();
        else popupList.setSelectedIndex(model.indexOf(model.getSelectedItem()));
    }

    protected void showPopup() {
        popupWindow.setPreferredSize(new Dimension(getWidth(), popupWindow.getPreferredSize().height));
        popupWindow.pack();
        popupWindow.setLocation(getPopupLocation());
        popupWindow.setVisible(true);
        popupWindow.toFront();
        addHierarchyBoundsListener(movementListener);
        addComponentListener(componentListener);
    }

    protected void hidePopup() {
        removeHierarchyBoundsListener(movementListener);
        removeComponentListener(componentListener);
        popupWindow.dispose();
    }

    protected Point getPopupLocation() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
        screenSize.width -= insets.left + insets.right;
        screenSize.height -= insets.top + insets.bottom;
        Dimension popupSize = popupWindow.getSize();
        return getPopupLocation(screenSize, popupSize);
    }

    protected Point getPopupLocation(Dimension screenSize, Dimension popupSize) {
        Point location = getLocationOnScreen();
        if (location.y + getHeight() + popupSize.height > screenSize.height) location.y -= popupSize.height;
        else location.y += getHeight();
        return location;
    }
}
