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
import java.awt.Cursor;
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
import java.beans.PropertyChangeListener;
import java.util.Optional;
import java.util.function.BiFunction;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

import io.github.jonestimd.swing.ComponentDefaults;
import io.github.jonestimd.swing.ComponentResources;
import io.github.jonestimd.swing.validation.ValidatedComponent;
import io.github.jonestimd.swing.validation.ValidationSupport;
import io.github.jonestimd.swing.validation.ValidationTooltipBorder;
import io.github.jonestimd.swing.validation.Validator;

/**
 * A combo box that filters the list items based on the input text.
 * Keyboard selection:
 * <ul>
 *     <li><strong>up</strong> - select previous item in the list</li>
 *     <li><strong>down</strong> - select next item in the list</li>
 *     <li><strong>ctrl-enter</strong> - if a parser exists and no item matches the filter text then create a new item
 *          (the new item is not added to the unfiltered list)
 *     </li>
 *     <li><strong>ctrl-backspace</strong> - clears selection and text when cursor is at end of text</li>
 * </ul>
 * @param <T> list item class
 */
public class FilterComboBox<T> extends JTextField implements ValidatedComponent {
    /** {@link ComponentResources} key for default visible rows */
    public static final String VISIBLE_ROWS_KEY = "FilterComboBox.visibleRows";
    public static final String AUTO_SELECT_TEXT = "autoSelectText";
    public static final String AUTO_SELECT_ITEM = "autoSelectItem";
    public static final String SET_TEXT_ON_FOCUS_LOST = "setTextOnFocusLost";
    public static final String SELECT_NEXT_KEY = "select next";
    public static final String SELECT_PREVIOUS_KEY = "select previous";
    public static final String ADD_ITEM_KEY = "add item";
    public static final String CLEAR_SELECTION_KEY = "clear selection";

    private final FilterComboBoxModel<T> model;
    private final BiFunction<FilterComboBox<T>, String, T> parser;
    private final JList<T> popupList;
    private final JPopupMenu popupWindow;
    private boolean autoSelectText = true;
    private boolean autoSelectItem = true;
    private boolean autoSelected = false;
    private boolean setTextOnFocusLost = true;
    private T initialSelection;
    private final ValidationSupport<T> validationSupport;
    private final ValidationTooltipBorder validationBorder;

    /**
     * Create a {@code FilterComboBox} with the default visible rows and no validation.
     */
    public FilterComboBox(FilterComboBoxModel<T> model) {
        this(model, Validator.empty(), null);
    }

    /**
     * Create a {@code FilterComboBox} with the default visible rows and no validation.
     */
    public FilterComboBox(FilterComboBoxModel<T> model, BiFunction<FilterComboBox<T>, String, T> parser) {
        this(model, Validator.empty(), parser);
    }

    /**
     * Create a {@code FilterComboBox} with no validation.
     */
    public FilterComboBox(FilterComboBoxModel<T> model, Validator<T> validator, BiFunction<FilterComboBox<T>, String, T> parser) {
        this(model, ComponentResources.lookupInt(VISIBLE_ROWS_KEY), validator, parser);
    }

    /**
     * @param model the model for the list
     * @param visibleRows the number of visible rows for the popup list
     * @param validator selection validator
     * @param parser a function for creating a new item from the input text
     */
    public FilterComboBox(FilterComboBoxModel<T> model, int visibleRows, Validator<T> validator, BiFunction<FilterComboBox<T>, String, T> parser) {
        this.model = model;
        this.parser = parser;
        this.validationSupport = new ValidationSupport<>(this, validator);
        this.validationBorder = new ValidationTooltipBorder(this);
        validateValue();
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
        model.addPropertyChangeListener(FilterComboBoxModel.SELECTED_ITEM, (event) -> {
            selectListItem();
            validateValue();
        });
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
                    if (setTextOnFocusLost) setText(model.getSelectedItem() == null ? "" : model.getSelectedItemText());
                }
            }
        });
        getActionMap().put(SELECT_NEXT_KEY, new SelectNextAction());
        getActionMap().put(SELECT_PREVIOUS_KEY, new SelectPreviousAction());
        getActionMap().put(ADD_ITEM_KEY, new NewItemAction());
        getActionMap().put(CLEAR_SELECTION_KEY, new ClearSelectionAction());
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("pressed DOWN"), SELECT_NEXT_KEY);
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("pressed UP"), SELECT_PREVIOUS_KEY);
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ctrl pressed ENTER"), ADD_ITEM_KEY);
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ctrl pressed BACK_SPACE"), CLEAR_SELECTION_KEY);
    }

    /**
     * Overridden to wrap the {@link ValidationTooltipBorder} with the input border.
     * @param border the border to add around the {@code ValidationBorder} or null
     *        to use the {@code ValidationBorder} alone
     */
    @Override
    public void setBorder(Border border) {
        if (border == null) {
            super.setBorder(validationBorder);
        }
        else {
            super.setBorder(new CompoundBorder(border, validationBorder));
        }
    }

    @Override
    public void validateValue() {
        validationBorder.setValid(validationSupport.validateValue(getSelectedItem()) == null);
    }

    @Override
    public String getValidationMessages() {
        return validationSupport.getMessages();
    }

    @Override
    public void addValidationListener(PropertyChangeListener listener) {
        validationSupport.addValidationListener(listener);
    }

    @Override
    public void removeValidationListener(PropertyChangeListener listener) {
        validationSupport.removeValidationListener(listener);
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

    /**
     * @return true if the text will be set to the selected item when focus is lost
     */
    public boolean isSetTextOnFocusLost() {
        return setTextOnFocusLost;
    }

    public void setSetTextOnFocusLost(boolean setTextOnFocusLost) {
        boolean oldValue = this.setTextOnFocusLost;
        this.setTextOnFocusLost = setTextOnFocusLost;
        firePropertyChange(SET_TEXT_ON_FOCUS_LOST, oldValue, setTextOnFocusLost);
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

    @Override
    public Cursor getCursor() {
        return validationBorder.isMouseOverIndicator() ? Cursor.getDefaultCursor() : super.getCursor();
    }

    @Override
    public String getToolTipText() {
        return validationBorder.isMouseOverIndicator() ? validationSupport.getMessages() : super.getToolTipText();
    }

    /**
     * Select the item that exactly matches the input text.  If a parser exists and no item matches tnen
     * a new item is created and selected.
     */
    protected void selectMatch() {
        Optional<T> match = model.findElement(getText());
        if (match.isPresent()) model.setSelectedItem(match.get());
        else {
            T item = parser.apply(this, getText());
            if (item != null) model.setSelectedItem(item);
        }
        setText(model.getSelectedItemText());
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

    protected class NewItemAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (popupWindow.isVisible()) hidePopup();
            if (parser != null && !getText().isEmpty()) selectMatch();
            else setText(model.getSelectedItemText());
        }
    }

    protected class ClearSelectionAction extends AbstractAction {
        private final Action defaultAction;

        public ClearSelectionAction() {
            this.defaultAction = getActionMap().get(getInputMap(WHEN_FOCUSED).get(KeyStroke.getKeyStroke("ctrl pressed BACK_SPACE")));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (getCaret().getDot() == getDocument().getLength()) {
                setText("");
                model.setSelectedItem(null);
            }
            else if (defaultAction != null) defaultAction.actionPerformed(e);
        }
    }
}
