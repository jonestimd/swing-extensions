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
package io.github.jonestimd.swing.validation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.border.Border;

/**
 * A border that indicates a validation error by displaying a red X to the right of the contained component.
 */
public class ValidationBorder implements Border {
    private boolean valid = true;

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
            paintInvalidMarker(c, g, x, y, width, height);
        }
    }

    public static void paintInvalidMarker(Component c, Graphics g, int x, int y, int width, int height) {
        FontMetrics fontMetrics = g.getFontMetrics(c.getFont());
        int fontHeight = fontMetrics.getMaxAscent() + fontMetrics.getMaxDescent();
        int size = Math.min(fontHeight, height);
        Graphics2D g2d = (Graphics2D) g.create(x + width - size, y + (height - size)/2, size, size--);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        g2d.drawLine(2, 2, size-2, size-2);
        g2d.drawLine(2, size-2, size-2, 2);
        g2d.dispose();
    }
}