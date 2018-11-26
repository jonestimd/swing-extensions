// The MIT License (MIT)
//
// Copyright (c) 2018 Timothy D. Jones
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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.HierarchyBoundsAdapter;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.FocusManager;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.Highlighter.HighlightPainter;

import com.google.common.base.Joiner;
import io.github.jonestimd.swing.ComponentResources;
import io.github.jonestimd.swing.component.ListField.ItemValidator;
import io.github.jonestimd.swing.validation.ValidatedComponent;
import io.github.jonestimd.swing.validation.ValidationBorder;
import io.github.jonestimd.swing.validation.ValidationSupport;
import io.github.jonestimd.swing.validation.ValidationTooltipBorder;

/**
 * A component that displays a list of string values using {@link MultiSelectItem} and allows editing
 * the items in the list.  When the component receives focus, a popup editor is displayed with the
 * list items.  The items can be modified by editing the text in the editor.  Each line in the
 * editor corresponds to a list item.  The items are validated using the {@code validator} and
 * invalid items are highlighted using the {@code errorPainter}.  Changes made in the editor are committed
 * by typing {@code ctrl ENTER}.  Changes in the editor are cancelled by typing {@code ctrl ESCAPE} or when
 * the editor loses focus.
 * <p/>
 * If the delete buttons are enabled on the {@link MultiSelectItem}s then items can also be
 * removed from the list using the delete buttons.
 */
public class PopupListField extends JPanel implements ValidatedComponent {
    public static final String ITEMS_PROPERTY = "items";
    public static final String LINE_SEPARATOR = "\n";
    protected static final int MIN_HEIGHT = new MultiSelectItem("x", true, false).getMinHeight() + 2;
    protected static final int HGAP = 2;
    protected static final int VGAP = 2;

    private final List<String> items = new ArrayList<>();
    private final boolean showItemDelete;
    private final boolean opaqueItems;
    private final ResourceBundle bundle;
    private final ListField textArea;
    private final JLabel focusCursor;
    private final Border popupBorder;
    private final String requiredMessage;
    private ValidationTooltipBorder validationBorder;
    private final ValidationSupport<List<String>> validationSupport;
    private Window popupWindow;
    private boolean ignoreFocusGained = false;
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
                hidePopup();
            }
        }
    };

    /**
     * Create a new {@code MultiSelectField}.
     * @param showItemDelete true to show delete buttons on the list items
     * @param opaqueItems true to fill the list items with their background color
     * @param validator predicate to use to validate items before adding them to the list
     * @param errorPainter painter for highlighting invalid items
     * @param popupBorder the border for the popup window
     * @param bundle the {@code ResourceBundle} to use for configuration
     */
    public PopupListField(boolean showItemDelete, boolean opaqueItems, int popupRows, ItemValidator validator,
            HighlightPainter errorPainter, Border popupBorder, ResourceBundle bundle, String requiredMessage) {
        super(new FlowLayout(FlowLayout.LEADING, HGAP, VGAP));
        this.showItemDelete = showItemDelete;
        this.opaqueItems = opaqueItems;
        this.focusCursor = new JLabel(ComponentResources.getString(bundle, "popupListField.focusCursor"));
        this.popupBorder = popupBorder;
        this.bundle = bundle;
        this.requiredMessage = requiredMessage;
        this.validationSupport = new ValidationSupport<>(this, value -> value.isEmpty() ? requiredMessage : null);
        textArea = new ListField(validator, errorPainter, bundle, this::hidePopup, this::commitEdit);
        textArea.setRows(popupRows);
        textArea.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                ignoreFocusGained = true;
                hidePopup();
            }
        });
        setOpaque(true);
        setBackground(textArea.getBackground());
        setBorder(UIManager.getBorder("TextField.border"));
        setFocusable(true);
        enableEvents(FocusEvent.FOCUS_EVENT_MASK + MouseEvent.MOUSE_EVENT_MASK);
        addHierarchyListener(this::hierarchyChanged);
        if (requiredMessage != null) {
            addPropertyChangeListener(ITEMS_PROPERTY, event -> validateValue());
            validateValue();
        }
    }

    /**
     * Overridden to wrap the {@link ValidationBorder} with the input border.
     * @param border the border to add around the {@code ValidationBorder} or null
     *        to use the {@code ValidationBorder} alone
     */
    @Override
    public void setBorder(Border border) {
        if (requiredMessage == null) super.setBorder(border);
        else {
            if (validationBorder == null) validationBorder = new ValidationTooltipBorder(this);
            if (border == null) super.setBorder(validationBorder);
            else super.setBorder(new CompoundBorder(border, validationBorder));
        }
    }

    @Override
    public void validateValue() {
        if (requiredMessage != null) validationBorder.setValid(validationSupport.validateValue(getItems()) == null);
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

    protected void setItems(String... items) {
        setItems(Arrays.asList(items));
    }

    /**
     * Replace the list of values.
     */
    public void setItems(Collection<String> items) {
        for (int i = getComponentCount() - 1; i >= 0; i--) {
            if (getComponent(i) instanceof MultiSelectItem) remove(i);
        }
        this.items.clear();
        this.items.addAll(items);
        for (int i = 0; i < this.items.size(); i++) {
            add(newItem(this.items.get(i)), null, i);
        }
        firePropertyChange(ITEMS_PROPERTY, null, Collections.unmodifiableList(this.items));
    }

    /**
     * Add an item to the list of values.
     */
    public void addItem(String text) {
        items.add(text);
        add(newItem(text));
        firePropertyChange(ITEMS_PROPERTY, null, Collections.unmodifiableList(items));
    }

    /**
     * Create a new {@link MultiSelectItem} to add to the field.
     */
    protected MultiSelectItem newItem(String text) {
        MultiSelectItem item = new MultiSelectItem(text, showItemDelete, opaqueItems, bundle);
        item.addDeleteListener(this::onDeleteItem);
        return item;
    }

    /**
     * Handles clicking on an item's delete button.
     * @param item the item to be deleted
     */
    protected void onDeleteItem(MultiSelectItem item) {
        removeItem(item.getText());
        revalidate();
        repaint();
    }

    /**
     * Remove an item from the list of values.
     */
    public void removeItem(String text) {
        int index = items.indexOf(text);
        if (index >= 0) {
            remove(index);
            items.remove(text);
            firePropertyChange(ITEMS_PROPERTY, null, Collections.unmodifiableList(items));
        }
    }

    /**
     * Get the list of values.
     * @return an immutable copy of the list of values.
     */
    public List<String> getItems() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public Dimension getMinimumSize() {
        Dimension size = super.getMinimumSize();
        if (items.isEmpty()) size.height = MIN_HEIGHT;
        else {
            getLayout().layoutContainer(this);
            int maxY = 0;
            for (Component component : getComponents()) {
                maxY = Math.max(maxY, component.getY() + component.getHeight());
            }
            Insets insets = getInsets();
            size.height = maxY + VGAP + insets.bottom;
        }
        return size;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        if (items.isEmpty()) size.height = MIN_HEIGHT;
        else {
            getLayout().layoutContainer(this);
            int maxY = 0;
            for (Component component : getComponents()) {
                maxY = Math.max(maxY, component.getY() + component.getHeight());
            }
            Insets insets = getInsets();
            size.height = maxY + VGAP + insets.bottom;
        }
        return size;
    }

    private void hierarchyChanged(HierarchyEvent event) {
        if ((event.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
            popupWindow = new Window((Window) getTopLevelAncestor());
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setBorder(popupBorder);
            popupWindow.add(scrollPane);
        }
    }

    private Point getPopupLocation() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
        screenSize.width -= insets.left + insets.right;
        screenSize.height -= insets.top + insets.bottom;
        Dimension popupSize = popupWindow.getSize();
        popupSize.width = getWidth();
        popupWindow.setSize(popupSize);
        return getPopupLocation(screenSize, popupSize);
    }

    protected Point getPopupLocation(Dimension screenSize, Dimension popupSize) {
        Point location = getLocationOnScreen();
        if (location.y + popupSize.height > screenSize.height) {
            location.y -= popupSize.height - getHeight();
        }
        return location;
    }

    /**
     * Hide the popup window if it is showing.
     */
    protected void hidePopup() {
        if (popupWindow != null) {
            FocusManager.getCurrentManager().removePropertyChangeListener("focusedWindow", focusedWindowListener);
            removeHierarchyBoundsListener(movementListener);
            removeComponentListener(componentListener);
            popupWindow.dispose();
            requestFocus();
        }
    }

    /**
     * Show the popup window.
     */
    protected void showPopup() {
        textArea.setText(Joiner.on(LINE_SEPARATOR).join(items));
        FocusManager.getCurrentManager().addPropertyChangeListener("focusedWindow", focusedWindowListener);
        popupWindow.pack();
        popupWindow.setLocation(getPopupLocation());
        popupWindow.setVisible(true);
        popupWindow.toFront();
        textArea.requestFocus();
        addHierarchyBoundsListener(movementListener);
        addComponentListener(componentListener);
    }

    @Override
    protected void processFocusEvent(FocusEvent e) {
        if (e.getID() == FocusEvent.FOCUS_GAINED) {
            add(focusCursor);
            if (e.getOppositeComponent() != null && !e.isTemporary() && !ignoreFocusGained) {
                showPopup();
            }
        }
        else if (e.getID() == FocusEvent.FOCUS_LOST) {
            remove(focusCursor);
        }
        revalidate();
        repaint();
        ignoreFocusGained = false;
        super.processFocusEvent(e);
    }

    @Override
    protected void processMouseEvent(MouseEvent e) {
        if (e.getID() == MouseEvent.MOUSE_RELEASED && e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
            showPopup();
        }
        super.processMouseEvent(e);
    }

    @Override
    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
        if (pressed && ks.getKeyCode() == KeyEvent.VK_SPACE) {
            e.consume();
            showPopup();
            return true;
        }
        return super.processKeyBinding(ks, e, condition, pressed);
    }

    private void commitEdit(List<String> items) {
        setItems(items);
        hidePopup();
    }

    /**
     * Create a builder for creating a new {@code PopupListField}.
     * @param showItemDelete true to show delete buttons on the list items
     * @param opaqueItems true to fill the list items with their background color
     */
    public static Builder builder(boolean showItemDelete, boolean opaqueItems) {
        return new Builder(showItemDelete, opaqueItems);
    }

    public static class Builder {
        private final boolean showItemDelete;
        private final boolean opaqueItems;
        private int popupRows = ListField.DEFAULT_ROWS;
        private Border popupBorder = new LineBorder(Color.BLACK, 1);
        private ItemValidator validator = ListField.DEFAULT_VALIDATOR;
        private HighlightPainter errorHighlighter = ListField.DEFAULT_ERROR_HIGHLIGHTER;
        private ResourceBundle bundle = ComponentResources.BUNDLE;
        private String requiredMessage = null;

        public Builder(boolean showItemDelete, boolean opaqueItems) {
            this.showItemDelete = showItemDelete;
            this.opaqueItems = opaqueItems;
        }

        public Builder popupRows(int popupRows) {
            this.popupRows = popupRows;
            return this;
        }

        public Builder validator(ItemValidator validator) {
            this.validator = validator;
            return this;
        }

        public Builder errorHighlighter(HighlightPainter errorHighlighter) {
            this.errorHighlighter = errorHighlighter;
            return this;
        }

        public Builder popupBorder(Border popupBorder) {
            this.popupBorder = popupBorder;
            return this;
        }

        public Builder bundle(ResourceBundle bundle) {
            this.bundle = bundle;
            return this;
        }

        public Builder requiredMessage(String requiredMessage) {
            this.requiredMessage = requiredMessage;
            return  this;
        }

        public PopupListField build() {
            return new PopupListField(showItemDelete, opaqueItems, popupRows, validator, errorHighlighter, popupBorder, bundle, requiredMessage);
        }
    }
}