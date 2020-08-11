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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.stream.IntStream;

import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

import io.github.jonestimd.swing.ComponentResources;
import io.github.jonestimd.swing.component.AbstractButtonBorder.Side;

public class ClearButtonBorder implements Border {
    public static final String BUTTON_COLOR_KEY = "ClearButtonBorder.buttonColor";
    public static final String HIGHLIGHT_COLOR_KEY = "ClearButtonBorder.highlightColor";
    public static final String CROSS_COLOR_KEY = "ClearButtonBorder.crossColor";
    private static final float STROKE_WIDTH = 1.5f;
    private static final float CROSS_START = 0.2f;
    private static final float CROSS_END = 1 - CROSS_START;
    private static final Color BUTTON_COLOR = ComponentResources.lookupColor(BUTTON_COLOR_KEY);
    private static final Color HIGHLIGHT_COLOR = ComponentResources.lookupColor(HIGHLIGHT_COLOR_KEY);
    private static final Color CROSS_COLOR = ComponentResources.lookupColor(CROSS_COLOR_KEY);
    private final Side side;
    private final int maxSize;
    private boolean highlightButton = false;

    /**
     * Create a border with the button on the right of the component.
     */
    public ClearButtonBorder() {
        this(Side.RIGHT);
    }

    public ClearButtonBorder(Side side) {
        this(side, Integer.MAX_VALUE);
    }

    public ClearButtonBorder(Side side, int maxSize) {
        this.side = side;
        this.maxSize = maxSize;
    }

    public boolean isHighlightButton() {
        return highlightButton;
    }

    public void setHighlightButton(boolean highlightButton) {
        this.highlightButton = highlightButton;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        int size = Math.min(height, getIconSize(c, c.getWidth(), c.getHeight()));
        int left = side == Side.LEFT ? 0 : width - size;
        Graphics2D g2d = (Graphics2D) g.create(x + left, y + (height - size)/2, size, size--);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(highlightButton ? HIGHLIGHT_COLOR : BUTTON_COLOR);
        g2d.fillOval(1, 1, size - 1, size - 1);
        g2d.setColor(CROSS_COLOR);
        g2d.setStroke(new BasicStroke(STROKE_WIDTH, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        int start = Math.round(CROSS_START*size) + 1;
        int end = Math.round(CROSS_END*size) - 1;
        g2d.drawLine(start, start, end, end);
        g2d.drawLine(end, start, start, end);
        g2d.dispose();
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return getBorderInsets(c, c.getWidth(), c.getHeight());
    }

    public Insets getBorderInsets(Component c, int width, int height) {
        int iconSize = getIconSize(c, width, height);
        if (width > height && iconSize + 2 <= c.getWidth()) iconSize += 2;
        return side == Side.LEFT ? new Insets(0, iconSize, 0, 0) : new Insets(0, 0, 0, iconSize);
    }

    protected int getIconSize(Component c, int width, int height) {
        if (c instanceof JComponent) {
            Border border = ((JComponent) c).getBorder();
            while (border instanceof CompoundBorder) {
                Border outsideBorder = ((CompoundBorder) border).getOutsideBorder();
                if (outsideBorder != this) {
                    Insets insets = outsideBorder.getBorderInsets(c);
                    width -= insets.left + insets.right;
                    height -= insets.top + insets.bottom;
                }
                border = ((CompoundBorder) border).getInsideBorder();
            }
        }
        return IntStream.of(height, width, maxSize).min().getAsInt();
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }
}
