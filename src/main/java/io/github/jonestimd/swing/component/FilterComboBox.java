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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;

import io.github.jonestimd.swing.ComponentDefaults;
import io.github.jonestimd.swing.ComponentResources;
import io.github.jonestimd.swing.validation.ValidatedTextField;
import io.github.jonestimd.swing.validation.Validator;

public class FilterComboBox<T> extends ValidatedTextField {
    /** {@link ComponentResources} key for default visible rows */
    public static final String VISIBLE_ROWS_KEY = "FilterComboBox.visibleRows";
    public static final String AUTO_SELECT_TEXT = "autoSelectText";
    public static final String AUTO_SELECT_ITEM = "autoSelectItem";
    public static final String SELECT_NEXT_KEY = "select next";
    public static final String SELECT_PREVIOUS_KEY = "select previous";
    public static final String CLEAR_SELECTION_KEY = "clear selection";

    private final FilterComboBoxModel<T> model;
    private final JList<T> popupList;
    private final JPopupMenu popupWindow;
    private boolean autoSelectText = true;
    // TODO allow new item (editable combo box)
    private boolean autoSelectItem = true;
    private boolean autoSelected = false;
    private T initialSelection;

    public FilterComboBox(FilterComboBoxModel<T> model) {
        this(model, ComponentResources.lookupInt(VISIBLE_ROWS_KEY));
    }

    public FilterComboBox(FilterComboBoxModel<T> model, int visibleRows) {
        this(model, visibleRows, Validator.empty());
    }

    public FilterComboBox(FilterComboBoxModel<T> model, int visibleRows, Validator<String> validator) {
        super(validator);
        this.model = model;
        popupList = new JList<>(model);
        popupList.setCellRenderer(new FunctionListCellRenderer<>(model.getFormat()));
        popupList.setVisibleRowCount(visibleRows);
        popupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        popupList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) model.setSelectedItem(popupList.getSelectedValue());
        });
        popupList.setBackground(ComponentDefaults.getColor("ComboBox.background"));
        popupList.setSelectionForeground( ComponentDefaults.getColor( "ComboBox.selectionForeground" ) );
        popupList.setSelectionBackground( ComponentDefaults.getColor( "ComboBox.selectionBackground" ) );
        popupWindow = new JPopupMenu();
        popupWindow.setLayout(new BoxLayout(popupWindow, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(popupList);
        scrollPane.setBorder(null);
        popupWindow.add(scrollPane);
        popupWindow.setFocusable(false);
        popupWindow.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        model.addPropertyChangeListener(FilterComboBoxModel.SELECTED_ITEM, (event) -> selectListItem());
        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                showPopup();
                selectListItem();
                if (getText().equals(model.getSelectedItemText()) && autoSelectText) selectAll();
                autoSelected = false;
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (popupWindow.isVisible()) {
                    hidePopup();
                    setText(model.getSelectedItem() == null ? "" : model.getSelectedItemText());
                }
            }
        });
        getActionMap().put(SELECT_NEXT_KEY, new SelectNextAction());
        getActionMap().put(SELECT_PREVIOUS_KEY, new SelectPreviousAction());
        getActionMap().put(CLEAR_SELECTION_KEY, new ClearSelectionAction());
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("pressed DOWN"), SELECT_NEXT_KEY);
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("pressed UP"), SELECT_PREVIOUS_KEY);
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ctrl pressed BACK_SPACE"), CLEAR_SELECTION_KEY);
    }

    @Override
    public void setText(String t) {
        super.setText(t);
        filterList();
    }

    public T getSelectedItem() {
        return model.getSelectedItem();
    }

    public FilterComboBoxModel<T> getModel() {
        return model;
    }

    public JList<T> getPopupList() {
        return popupList;
    }

    /**
     * Get the behavior for text selection on receiving focus.
     * @return  true if the input text is selected when focus is gained and the text matches the list selection.
     */
    public boolean isAutoSelectText() {
        return autoSelectText;
    }

    /**
     * Set the behavior for text selection on receiving focus.
     * @param autoSelectText true to select the input text when focus is gained and the text matches the list selection.
     */
    public void setAutoSelectText(boolean autoSelectText) {
        boolean oldValue = this.autoSelectText;
        this.autoSelectText = autoSelectText;
        firePropertyChange(AUTO_SELECT_TEXT, oldValue, autoSelectText);
    }

    /**
     * @return true if a single match is automatically selected
     */
    public boolean isAutoSelectItem() {
        return autoSelectItem;
    }

    public void setAutoSelectItem(boolean autoSelectItem) {
        boolean oldValue = this.autoSelectItem;
        this.autoSelectItem = autoSelectItem;
        firePropertyChange(AUTO_SELECT_ITEM, oldValue, autoSelectItem);
    }

    @Override
    protected void processKeyEvent(KeyEvent event) {
        if (event.getID() == KeyEvent.KEY_PRESSED && event.getModifiersEx() == 0) {
            if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                if (popupWindow.isVisible()) {
                    hidePopup();
                    model.setSelectedItem(initialSelection);
                    autoSelected = false;
                    setText(model.getSelectedItemText());
                }
                else showPopup();
            }
            else if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                if (popupWindow.isVisible()) {
                    hidePopup();
                    if (model.getSelectedItem() != null) setText(model.getSelectedItemText());
                }
            }
            else if (!popupWindow.isVisible()) showPopup();
        }
        super.processKeyEvent(event);
        if (event.getID() == KeyEvent.KEY_TYPED) filterList();
    }

    protected void filterList() {
        model.setFilter(getText());
        if (model.getSelectedItemIndex() < 0) {
            model.setSelectedItem(null);
            autoSelected = false;
        }
        if (autoSelectItem && !getText().isEmpty()) {
            if (model.getSize() == 1) {
                if (model.getSelectedItem() == null) {
                    model.setSelectedItem(model.getElementAt(0));
                    autoSelected = true;
                }
            }
            else if (autoSelected) {
                model.setSelectedItem(null);
                autoSelected = false;
            }
        }
        selectListItem();
    }

    private void selectListItem() {
        if (model.getSelectedItem() == null) popupList.clearSelection();
        else popupList.setSelectedIndex(model.getSelectedItemIndex());
    }

    protected void showPopup() {
        if (isShowing()) {
            popupWindow.setPreferredSize(new Dimension(getWidth(), popupWindow.getPreferredSize().height));
            popupWindow.pack();
            Point location = getPopupLocation();
            popupWindow.show(this, location.x, location.y);
            initialSelection = model.getSelectedItem();
        }
    }

    protected void hidePopup() {
        popupWindow.setVisible(false);
    }

    protected Point getPopupLocation() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Rectangle screenBounds;
        // Calculate the desktop dimensions relative to the combo box.
        GraphicsConfiguration gc = getGraphicsConfiguration();
        if (gc != null) {
            Insets screenInsets = toolkit.getScreenInsets(gc);
            screenBounds = gc.getBounds();
            screenBounds.width -= screenInsets.right;
            screenBounds.height -= screenInsets.bottom;
            screenBounds.x += screenInsets.left;
            screenBounds.y += screenInsets.top;
        }
        else {
            screenBounds = new Rectangle(new Point(), toolkit.getScreenSize());
        }
        Dimension popupSize = popupWindow.getSize();
        return getPopupLocation(screenBounds, popupSize);
    }

    protected Point getPopupLocation(Rectangle screenBounds, Dimension popupSize) {
        Point location = new Point();
        if (getLocationOnScreen().y + getHeight() + popupSize.height > screenBounds.height) location.y -= popupSize.height;
        else location.y += getHeight();
        return location;
    }

    private void setSelectedIndex(int selectedIndex) {
        popupList.setSelectedIndex(selectedIndex);
        popupList.scrollRectToVisible(popupList.getCellBounds(selectedIndex, selectedIndex));
        autoSelected = false;
    }

    protected class SelectNextAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            int size = popupList.getModel().getSize();
            if (size > 0) setSelectedIndex((popupList.getSelectedIndex() + 1) % size);
        }
    }

    protected class SelectPreviousAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            int size = popupList.getModel().getSize();
            if (size > 0) {
                int selectedIndex = popupList.getSelectedIndex();
                setSelectedIndex(selectedIndex <= 0 ? size - 1 : selectedIndex - 1);
            }
        }
    }

    protected class ClearSelectionAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            setText("");
            model.setSelectedItem(null);
        }
    }
}
