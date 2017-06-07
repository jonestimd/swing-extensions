// The MIT License (MIT)
//
// Copyright (c) 2017 Timothy D. Jones
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
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyBoundsAdapter;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.FocusManager;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import io.github.jonestimd.swing.action.ActionAdapter;

/**
 * Abstract class for a border that displays a popup window on mouse click events.  The target area for the mouse
 * clicks can be placed on the left or right side of the contained component and subclasses should implement
 * {@link #paintBorder(Component, Graphics, int, int, int, int, int)} to draw the "button" in the target area.
 * The default "button" area is a square having the same height as the contained component.
 * @param <C> the class of the contained component
 * @param <P> the class of the panel used for the content of the popup window
 */
public abstract class AbstractButtonBorder<C extends JComponent, P extends JComponent> implements Border {
    /** Available sides for displaying the button. */
    public enum Side { LEFT, RIGHT }
    private static final String SHOW_POPUP = "showPopup";

    private int iconSize;
    private final Side side;
    private final C component;
    private final P popupPanel;
    private Window popupWindow;
    private HierarchyBoundsListener movementListener = new HierarchyBoundsAdapter() {
        public void ancestorMoved(HierarchyEvent e) {
            popupWindow.setLocation(getPopupLocation());
        }
    };
    private ComponentListener componentListener = new ComponentAdapter() {
        public void componentMoved(ComponentEvent e) {
            popupWindow.setLocation(getPopupLocation());
        }
    };
    private PropertyChangeListener focusedWindowListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getOldValue() == popupWindow) {
                ignoreClick = true;
                hidePopup();
            }
        }
    };
    private Cursor componentCursor;
    private String componentTooltip;
    private String buttonTooltip;
    private boolean ignoreClick = false;

    /**
     * Construct a new border with the "button" on the specified side.
     * @param component the component using the border
     * @param popupPanel the panel to use for the content of the popup window
     * @param side the side on which to place the "button" (mouse click target area)
     */
    public AbstractButtonBorder(final C component, final P popupPanel, Side side) {
        this.component = component;
        this.componentCursor = component.getCursor();
        this.componentTooltip = component.getToolTipText();
        this.popupPanel = popupPanel;
        this.side = side;
        popupPanel.setBorder(new LineBorder(Color.BLACK, 2));
        iconSize = component.getPreferredSize().height - component.getInsets().top - component.getInsets().bottom;
        component.addHierarchyListener(this::hierarchyChanged);
        MouseHandler mouseHandler = new MouseHandler();
        component.addMouseListener(mouseHandler);
        component.addMouseMotionListener(mouseHandler);
        component.addPropertyChangeListener(event -> iconSize = component.getPreferredSize().height - component.getInsets().top - component.getInsets().bottom);
        component.getActionMap().put(SHOW_POPUP, new ActionAdapter(event -> showPopup()));
        component.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.ALT_DOWN_MASK), SHOW_POPUP);
    }

    /**
     * Set the tooltip to display when the mouse hovers over the "button".
     */
    public void setTooltip(String tooltip) {
        this.buttonTooltip = tooltip;
    }

    private void hierarchyChanged(HierarchyEvent event) {
        if ((event.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && component.isShowing()) {
            createPopupWindow();
        }
    }

    private void createPopupWindow() {
        initializePopup(component, popupPanel);
        popupWindow = new Window((Window) component.getTopLevelAncestor());
        popupWindow.setLayout(new BorderLayout());
        popupWindow.add(preparePopupPanel(), BorderLayout.CENTER);
    }

    /**
     * Prepare the popup panel for adding to the popup window.
     * @return the component to add to the popup window, e.g. a {@link JScrollPane} containing the popup panel.
     */
    protected JComponent preparePopupPanel() {
        return popupPanel;
    }

    /**
     * Calculate the insets for a component.  The "button" area will be a square based on the height of the component.
     * @param c the component
     * @return the calculated border insets for {@code c}
     */
    @Override
    public Insets getBorderInsets(Component c) {
        return side == Side.LEFT ? new Insets(0, iconSize+2, 0, 0) : new Insets(0, 0, 0, iconSize+2);
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        paintBorder(c, g, x, y, width, height, iconSize);
    }

    /**
     * Paint the border, including the "button".
     * @param c the component for which this border is being painted
     * @param g the paint graphics
     * @param x the x position of the painted border
     * @param y the y position of the painted border
     * @param width the width of the painted border
     * @param height the height of the painted border
     * @param inset the width of the "button"
     */
    protected abstract void paintBorder(Component c, Graphics g, int x, int y, int width, int height, int inset);

    @Override
    public boolean isBorderOpaque() {
        return true;
    }

    private Point getPopupLocation() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(component.getGraphicsConfiguration());
        screenSize.width -= insets.left + insets.right;
        screenSize.height -= insets.top + insets.bottom;
        Dimension popupSize = popupWindow.getSize();
        return getPopupLocation(screenSize, popupSize, component);
    }

    /**
     * Calculate the location to display the popup.
     * @param screenSize the size of the screen
     * @param popupSize the size of the popup window
     * @param borderComponent the contained component
     * @return the location to use to display the popup
     */
    protected abstract Point getPopupLocation(Dimension screenSize, Dimension popupSize, C borderComponent);

    /**
     * @param point the mouse position
     * @return true if {@code point} is over the "button"
     */
    protected boolean isOverButton(Point point) {
        if (side == Side.RIGHT) {
            return point.x > component.getWidth() - iconSize;
        }
        return point.x < iconSize;
    }

    /**
     * Hide the popup window if it is showing.
     */
    protected void hidePopup() {
        if (popupWindow != null) {
            FocusManager.getCurrentManager().removePropertyChangeListener("focusedWindow", focusedWindowListener);
            component.removeHierarchyBoundsListener(movementListener);
            component.removeComponentListener(componentListener);
            popupWindow.dispose();
            component.requestFocus();
        }
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
        popupPanel.requestFocus();
        component.addHierarchyBoundsListener(movementListener);
        component.addComponentListener(componentListener);
    }

    /**
     * Initialize the content of the popup window.
     * @param borderComponent the contained component
     * @param popupComponent the popup window content panel
     */
    protected abstract void initializePopup(C borderComponent, P popupComponent);

    private class MouseHandler extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            if (isOverButton(e.getPoint()) && ! popupWindow.isShowing() && ! ignoreClick) {
                showPopup();
            }
            ignoreClick = false;
        }

        public void mouseMoved(MouseEvent e) {
            boolean overButton = isOverButton(e.getPoint());
            ignoreClick &= overButton && popupWindow.isShowing();
            if (overButton) {
                component.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                component.setToolTipText(buttonTooltip);
            }
            else {
                component.setCursor(componentCursor);
                component.setToolTipText(componentTooltip);
            }
        }

        public void mouseDragged(MouseEvent e) {
            ignoreClick &= isOverButton(e.getPoint()) && popupWindow.isShowing();
        }

        public void mouseEntered(MouseEvent e) {
            ignoreClick &= isOverButton(e.getPoint()) && popupWindow.isShowing();
        }

        public void mouseExited(MouseEvent e) {
            ignoreClick = false;
        }
    }
}