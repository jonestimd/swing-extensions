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
package io.github.jonestimd.swing.validation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

import io.github.jonestimd.swing.ComponentTreeUtils;

/**
 * A border that indicates a validation error by displaying a red X to the right of the contained component.
 */
public class ValidationBorder implements Border {
    private static final float STROKE_WIDTH = 2.2f;
    private final Color background;
    private boolean valid = true;

    public ValidationBorder() {
        this(null);
    }

    public ValidationBorder(Color background) {
        this.background = background;
    }

    public Insets getBorderInsets(Component c) {
        int size = valid ? 0 : c.getHeight();
        if (! valid) {
            Graphics g = c.getGraphics();
            if (g != null) {
                FontMetrics fontMetrics = g.getFontMetrics(c.getFont());
                size = fontMetrics.getMaxAscent() + fontMetrics.getMaxDescent();
                g.dispose();
            }
        }
        return new Insets(0, 0, 0, size);
    }

    public boolean isBorderOpaque() {
        return false;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        if (! valid) {
            int size = getSize(c, g, height);
            if (background != null) {
                Graphics2D g2d = (Graphics2D) g.create(x + width - size, y, size, height);
                g2d.setColor(background);
                g2d.fillRect(0, 0, size, height);
                g2d.dispose();
            }
            paintInvalidMarker(g, x, y, width, height, size);
        }
    }

    public static void paintInvalidMarker(Component c, Graphics g, int x, int y, int width, int height) {
        int size = getSize(c, g, height);
        paintInvalidMarker(g, x, y, width, height, size);
    }

    public static void paintInvalidMarker(Graphics g, int x, int y, int width, int height, int size) {
        Graphics2D g2d = (Graphics2D) g.create(x + width - size, y + (height - size)/2, size, size--);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(STROKE_WIDTH, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        g2d.drawLine(2, 2, size-2, size-2);
        g2d.drawLine(2, size-2, size-2, 2);
        g2d.dispose();
    }

    public static int getSize(Component c, Graphics g, int height) {
        FontMetrics fontMetrics = g.getFontMetrics(c.getFont());
        int fontHeight = fontMetrics.getMaxAscent() + fontMetrics.getMaxDescent();
        return Math.min(fontHeight, height);
    }

    /**
     * Add a {@link ValidationBorder} to the {@link JViewport} containing <code>component</code>.
     */
    public static  <C extends JComponent & ValidatedComponent> void addToViewport(C component) {
        JScrollPane scrollPane = ComponentTreeUtils.findAncestor(component, JScrollPane.class);
        ValidationBorder border = new ValidationBorder(component.getBackground());
        component.addValidationListener(event -> border.setValid(event.getNewValue() == null));
        border.setValid(component.getValidationMessages() == null);
        scrollPane.setViewportBorder(new CompoundBorder(border, scrollPane.getViewportBorder()));
    }
}