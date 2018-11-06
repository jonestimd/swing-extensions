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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import javax.swing.JLabel;

import io.github.jonestimd.swing.ComponentResources;

/**
 * This component draws an outlined text string.  The component can include a delete button inside the outline.
 * The {@link #addDeleteListener(Consumer)} method can be used to handle clicks on the delete button.
 * <p/>
 * If this component is set to opaque then the outline will be filled with the background color.
 * <p/>
 * This component is used by the {@link MultiSelectField} to draw selected items.
 * <p/>
 * The following resources are used to configure the apearance of this component and can be overridden by a custom resource bundle.
 * <ul>
 *     <li><strong>multiSelectItem.background</strong> - the background color used to fill the outline</li>
 *     <li><strong>multiSelectItem.outline.color</strong> - the color of the outline</li>
 *     <li><strong>multiSelectItem.outline.strokeWidth</strong> - the stroke width of the outline</li>
 *     <li><strong>multiSelectItem.outline.flatness</strong> - length of the sides to be drawn as a straight line (0 for semicircle sides)</li>
 *     <li><strong>multiSelectItem.button.color</strong> - color of the delete button</li>
 *     <li><strong>multiSelectItem.button.hoverColor</strong> - highlight color for the delete button when the mouse cursor is over it</li>
 *     <li><strong>multiSelectItem.button.size</strong> - radius of the delete button</li>
 * </ul>
 */
public class MultiSelectItem extends JLabel {
    public static final int GAP = 2;
    protected static final double CROSS_SIZE = 0.4;

    protected final int strokeWidth;
    protected final Color outlineColor;
    protected final int outlineFlatness;
    protected final Color buttonColor;
    protected final Color buttonHoverColor;
    protected final int buttonSize;
    protected final int buttonSizeSquared;
    protected final ButtonGeometry buttonGeometry;
    private final boolean showDelete;
    private boolean isOverButton = false;
    private final List<Consumer<MultiSelectItem>> deleteListeners = new ArrayList<>();

    /**
     * Create a new {@code MultiSelectItem}.
     * @param text the text to display
     * @param showDelete true to show the delete button
     * @param opaque true to fill the outline with the background color
     */
    public MultiSelectItem(String text, boolean showDelete, boolean opaque) {
        this(text, showDelete, opaque, ComponentResources.BUNDLE);
    }

    /**
     * Create a new {@code MultiSelectItem}.
     * @param text the text to display
     * @param showDelete true to show the delete button
     * @param opaque true to fill the outline with the background color
     * @param bundle the {@code ResourceBundle} to use for configuration
     */
    public MultiSelectItem(String text, boolean showDelete, boolean opaque, ResourceBundle bundle) {
        super(text);
        this.showDelete = showDelete;
        this.outlineColor = ComponentResources.getColor(bundle, "multiSelectItem.outline.color");
        this.strokeWidth = ComponentResources.getInt(bundle, "multiSelectItem.outline.strokeWidth");
        this.outlineFlatness = ComponentResources.getInt(bundle, "multiSelectItem.outline.flatness");

        this.buttonColor = ComponentResources.getColor(bundle, "multiSelectItem.button.color");
        this.buttonHoverColor = ComponentResources.getColor(bundle, "multiSelectItem.button.hoverColor");
        this.buttonSize = ComponentResources.getInt(bundle, "multiSelectItem.button.size");
        this.buttonSizeSquared = buttonSize*buttonSize;
        this.buttonGeometry = new ButtonGeometry(buttonSize, strokeWidth);

        setFont(getFont().deriveFont(Font.PLAIN));
        setOpaque(opaque);
        setBackground(ComponentResources.getColor(bundle, "multiSelectItem.background"));
        setCursor(Cursor.getDefaultCursor());
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (showDelete) onMouseMove(e.getPoint());
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                isOverButton = false;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (isOverButton && e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
                    fireDelete();
                }
            }
        });
    }

    /**
     * Updates the hover state of the delete button.
     * @param point the mouse cursor position
     */
    protected void onMouseMove(Point point) {
        int distance = (int) point.distanceSq(buttonGeometry.centerX, buttonGeometry.centerY);
        boolean wasOverButton = isOverButton;
        isOverButton = distance < buttonSizeSquared;
        if (wasOverButton != isOverButton) repaint();
    }

    /**
     * Notifies listeners that the delete button has been clicked.
     */
    protected void fireDelete() {
        for (Consumer<MultiSelectItem> listener : deleteListeners) {
            listener.accept(this);
        }
    }

    /**
     * Add a listener to be notified when the delete button is clicked.
     */
    public void addDeleteListener(Consumer<MultiSelectItem> listener) {
        deleteListeners.add(listener);
    }

    /**
     * Remove a delete button listener.
     */
    public void removeDeleteListener(Consumer<MultiSelectItem> listener) {
        deleteListeners.remove(listener);
    }

    /**
     * Get the flag that indicates whether to draw the delete button.
     */
    public boolean isShowDelete() {
        return showDelete;
    }

    /**
     * Overridden to add space for the outline and delete button.
     */
    @Override
    public Insets getInsets() {
        return getInsets(null);
    }

    /**
     * Overridden to add space for the outline and delete button.
     */
    @Override
    public Insets getInsets(Insets insets) {
        insets = super.getInsets(insets);
        int vertical = GAP+strokeWidth;
        insets.top += vertical;
        insets.bottom += vertical;
        insets.left += GAP*2+(showDelete ? buttonSize+GAP*2 : getIconTextGap());
        insets.right += getIconTextGap()+GAP;
        return insets;
    }

    /**
     * Overridden to draw the outline and delete button.
     */
    @Override
    protected void paintComponent(Graphics g) {
        int width = getWidth(), height = getHeight()-strokeWidth;
        Shape outline = getOutline(GAP, 0, width-GAP*2, height);
        Graphics2D g2d = (Graphics2D) g.create(0, 0, width, getHeight());
        try {
            g2d.setStroke(new BasicStroke(strokeWidth));
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (isOpaque()) {
                g2d.setColor(getBackground());
                g2d.fill(outline);
            }
            g2d.setColor(outlineColor);
            g2d.draw(outline);
            if (showDelete) {
                drawDeleteButton(g2d, isOverButton);
            }
        } finally {
            g2d.dispose();
        }
        boolean opaque = isOpaque();
        setOpaque(false);
        super.paintComponent(g);
        setOpaque(opaque);
    }

    /**
     * Get the outline to draw around the text.
     * @param x the x offset
     * @param y the y offset
     * @param width the width of the outline
     * @param height the height of the outline
     */
    protected Shape getOutline(int x, int y, int width, int height) {
        return new RoundRectangle2D.Double(x, y, width, height, height-outlineFlatness, height-outlineFlatness);
    }

    /**
     * Draw the delete button.
     * @param g2d the drawing context
     * @param hoverEffect true if the mouse is over the button
     */
    protected void drawDeleteButton(Graphics2D g2d, boolean hoverEffect) {
        g2d.setColor(hoverEffect ? buttonHoverColor : buttonColor);
        g2d.fill(new Ellipse2D.Double(GAP*2+strokeWidth, GAP+strokeWidth, buttonSize, buttonSize));
        g2d.setColor(isOpaque() ? getBackground() : getParent().getBackground());
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        g2d.drawLine(buttonGeometry.crossLeft, buttonGeometry.crossTop, buttonGeometry.crossRight, buttonGeometry.crossBottom);
        g2d.drawLine(buttonGeometry.crossRight, buttonGeometry.crossTop, buttonGeometry.crossLeft, buttonGeometry.crossBottom);
    }

    protected static class ButtonGeometry {
        public final int centerX;
        public final int centerY;
        public final int crossLeft;
        public final int crossRight;
        public final int crossTop;
        public final int crossBottom;

        protected ButtonGeometry(int buttonSize, int strokeWidth) {
            int dx = (int) (buttonSize*Math.cos(Math.PI/4)*CROSS_SIZE);
            this.centerX = GAP*2+strokeWidth+buttonSize/2;
            this.centerY = GAP+strokeWidth+buttonSize/2;
            this.crossRight = centerX+dx;
            this.crossLeft = centerX-dx-1;
            this.crossBottom = centerY+dx;
            this.crossTop = centerY-dx-1;
        }
    }
}
