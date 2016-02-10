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
import java.awt.event.ActionEvent;
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

public abstract class AbstractButtonBorder<C extends JComponent, P extends JComponent> implements Border {
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
        component.getActionMap().put(SHOW_POPUP, new ActionAdapter(this::showPopup));
        component.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.ALT_DOWN_MASK), SHOW_POPUP);
    }

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

    public Insets getBorderInsets(Component c) {
        return side == Side.LEFT ? new Insets(0, iconSize+2, 0, 0) : new Insets(0, 0, 0, iconSize+2);
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        paintBorder(c, g, x, y, width, height, iconSize);
    }

    protected abstract void paintBorder(Component c, Graphics g, int x, int y, int width, int height, int inset);

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

    protected abstract Point getPopupLocation(Dimension screenSize, Dimension popupSize, C borderComponent);

    protected boolean isOverButton(Point point) {
        if (side == Side.RIGHT) {
            return point.x > component.getWidth() - iconSize;
        }
        return point.x < iconSize;
    }

    public void hidePopup() {
        if (popupWindow != null) {
            FocusManager.getCurrentManager().removePropertyChangeListener("focusedWindow", focusedWindowListener);
            component.removeHierarchyBoundsListener(movementListener);
            component.removeComponentListener(componentListener);
            popupWindow.dispose();
            component.requestFocus();
        }
    }

    private void showPopup(ActionEvent event) {
        FocusManager.getCurrentManager().addPropertyChangeListener("focusedWindow", focusedWindowListener);
        popupWindow.pack();
        popupWindow.setVisible(true);
        popupWindow.setLocation(getPopupLocation());
        popupWindow.toFront();
        popupPanel.requestFocus();
        component.addHierarchyBoundsListener(movementListener);
        component.addComponentListener(componentListener);
    }

    protected abstract void initializePopup(C borderComponent, P popupComponent);

    private class MouseHandler extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            if (isOverButton(e.getPoint()) && ! popupWindow.isShowing() && ! ignoreClick) {
                showPopup(null);
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