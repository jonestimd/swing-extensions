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
import java.awt.Rectangle;
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
import java.awt.event.KeyAdapter;
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
import java.util.function.Predicate;

import javax.swing.FocusManager;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.Position.Bias;

import com.google.common.base.Joiner;
import io.github.jonestimd.swing.ComponentResources;

/**
 * A component that displays a list of string values using {@link MultiSelectItem} and allows editing
 * the items in the list.  When the component receives focus, a popup editor is displayed with the
 * list items.  The items can be modified by editing the text in the editor.  Each line in the
 * editor corresponds to a list item.  The items are validated using the {@code isValidItem} predicate and
 * invalid items are highlighted using the {@code errorPainter}.  Changes made in the editor are committed
 * by typing {@code ctrl ENTER}.  Changes in the editor are cancelled by typing {@code ctrl ESCAPE} or when
 * the editor loses focus.
 * <p/>
 * If the delete buttons are enabled on the {@link MultiSelectItem}s then items can also be
 * removed from the list using the delete buttons.
 */
public class PopupListField extends JPanel {
    public static final String ITEMS_PROPERTY = "items";
    public static final Predicate<String> DEFAULT_IS_VALID_ITEM = (text) -> !text.trim().isEmpty();
    public static final String LINE_SEPARATOR = "\n";
    protected static final HighlightPainter DEFAULT_ERROR_HIGHLIGHTER = (g, p0, p1, bounds, c) -> {
        g.setColor(Color.PINK);
        Rectangle rect = bounds.getBounds();
        TextUI mapper = c.getUI();
        try {
            Rectangle start = mapper.modelToView(c, p0, Bias.Forward);
            g.fillRect(start.x, start.y+1, rect.width, start.height-1);
        } catch (BadLocationException e) {
            // can't render
        }
    };
    protected static final int MIN_HEIGHT = new MultiSelectItem("x", true, false).getMinimumSize().height+1;
    protected static final int HGAP = 2;
    protected static final int VGAP = 2;

    private final List<String> items = new ArrayList<>();
    private final boolean showItemDelete;
    private final boolean opaqueItems;
    private final ResourceBundle bundle;
    private final Predicate<String> isValidItem;
    private final JTextArea textArea = new JTextArea();
    private final JLabel focusCursor;
    private final Border popupBorder;
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
    private final HighlightPainter errorPainter;
    private boolean isValid = true;

    /**
     * Create a new {@code MultiSelectField}.
     * @param showItemDelete true to show delete buttons on the list items
     * @param opaqueItems true to fill the list items with their background color
     * @param isValidItem predicate to use to validate items before adding them to the list
     * @param errorPainter painter for highlighting invalid items
     * @param popupBorder the border for the popup window
     * @param bundle the {@code ResourceBundle} to use for configuration
     */
    public PopupListField(boolean showItemDelete, boolean opaqueItems, int popupRows,
            Predicate<String> isValidItem, HighlightPainter errorPainter, Border popupBorder, ResourceBundle bundle) {
        super(new FlowLayout(FlowLayout.LEADING, HGAP, VGAP));
        this.showItemDelete = showItemDelete;
        this.opaqueItems = opaqueItems;
        this.isValidItem = isValidItem;
        this.errorPainter = errorPainter;
        this.focusCursor = new JLabel(ComponentResources.getString(bundle, "popupListField.focusCursor"));
        this.popupBorder = popupBorder;
        this.bundle = bundle;
        textArea.setRows(popupRows);
        textArea.getDocument().addDocumentListener(new EditorDocumentListener());
        textArea.addKeyListener(new EditorKeyListener());
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
        enableEvents(FocusEvent.FOCUS_EVENT_MASK+MouseEvent.MOUSE_EVENT_MASK);
        addHierarchyListener(this::hierarchyChanged);
    }

    protected void setItems(String... items) {
        setItems(Arrays.asList(items));
    }

    /**
     * Replace the list of values.
     */
    public void setItems(Collection<String> items) {
        for (int i = getComponentCount()-1; i >= 0; i--) {
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

    private void validateText() {
        isValid = true;
        textArea.getHighlighter().removeAllHighlights();
        int pos = 0;
        String text = textArea.getText();
        while (pos < text.length()) {
            int end = text.indexOf(LINE_SEPARATOR, pos);
            String line = end > 0 ? text.substring(pos, end) : text.substring(pos);
            if (!isValidItem.test(line)) {
                isValid = false;
                try {
                    textArea.getHighlighter().addHighlight(pos, pos+line.length(), errorPainter);
                } catch (BadLocationException e) {
                    throw new RuntimeException(e);
                }
            }
            pos += line.length()+1;
        }
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        if (items.isEmpty()) size.height = MIN_HEIGHT;
        else {
            getLayout().layoutContainer(this);
            int maxY = 0;
            for (Component component : getComponents()) {
                maxY = Math.max(maxY, component.getY()+component.getHeight());
            }
            Insets insets = getInsets();
            size.height = maxY+VGAP+insets.bottom;
        }
        return size;
    }

    private void hierarchyChanged(HierarchyEvent event) {
        if ((event.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
            createPopupWindow();
        }
    }

    private Point getPopupLocation() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
        screenSize.width -= insets.left+insets.right;
        screenSize.height -= insets.top+insets.bottom;
        Dimension popupSize = popupWindow.getSize();
        popupSize.width = getWidth();
        popupWindow.setSize(popupSize);
        return getPopupLocation(screenSize, popupSize);
    }

    protected Point getPopupLocation(Dimension screenSize, Dimension popupSize) {
        Point location = getLocationOnScreen();
        if (location.y+popupSize.height > screenSize.height) {
            location.y -= popupSize.height-getHeight();
        }
        return location;
    }

    private void createPopupWindow() {
        popupWindow = new Window((Window) getTopLevelAncestor());
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(popupBorder);
        popupWindow.add(scrollPane);
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
            if (e.getOppositeComponent() != null && !e.isTemporary() && ! ignoreFocusGained) {
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

    private class EditorDocumentListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            validateText();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            validateText();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            validateText();
        }
    }

    private class EditorKeyListener extends KeyAdapter {
        final KeyStroke commitKey = KeyStroke.getKeyStroke(ComponentResources.getString(bundle, "popupListField.commitKey"));
        final KeyStroke cancelKey = KeyStroke.getKeyStroke(ComponentResources.getString(bundle, "popupListField.cancelKey"));

        @Override
        public void keyReleased(KeyEvent e) {
            KeyStroke keyStroke = KeyStroke.getKeyStroke(e.getExtendedKeyCode(), e.getModifiers(), true);
            if (keyStroke == cancelKey) hidePopup();
            else if (keyStroke == commitKey && isValid) {
                setItems(textArea.getText().split(LINE_SEPARATOR));
                hidePopup();
            }
        }
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
        private int popupRows = 5;
        private Border popupBorder = new LineBorder(Color.BLACK, 1);
        private Predicate<String> validator = DEFAULT_IS_VALID_ITEM;
        private HighlightPainter errorHighlighter = DEFAULT_ERROR_HIGHLIGHTER;
        private ResourceBundle bundle = ComponentResources.BUNDLE;

        public Builder(boolean showItemDelete, boolean opaqueItems) {
            this.showItemDelete = showItemDelete;
            this.opaqueItems = opaqueItems;
        }

        public Builder popupRows(int popupRows) {
            this.popupRows = popupRows;
            return this;
        }

        public Builder validator(Predicate<String> validator) {
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

        public PopupListField build() {
            return new PopupListField(showItemDelete, opaqueItems, popupRows, validator, errorHighlighter, popupBorder, bundle);
        }
    }
}