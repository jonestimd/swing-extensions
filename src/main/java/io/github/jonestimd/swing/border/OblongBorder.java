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
package io.github.jonestimd.swing.border;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;

import javax.swing.border.Border;

/**
 * A line border drawn with semi-circles for the left and right sides of the component.
 */
public class OblongBorder implements Border {
    private final int lineWidth;
    private final Color lineColor;
    private final Insets insets;

    public OblongBorder(int lineWidth, Color lineColor) {
        this(lineWidth, lineColor, 1, 0, 1, 0);
    }

    public OblongBorder(int lineWidth, Color lineColor, int paddingTop, int paddingLeft, int paddingBottom, int paddingRight) {
        this.lineWidth = lineWidth;
        this.lineColor = lineColor;
        this.insets = new Insets(lineWidth + paddingTop, lineWidth + paddingLeft, lineWidth + paddingBottom, lineWidth + paddingRight);
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D) g.create(x, y, width, height);
        g2d.setStroke(new BasicStroke(lineWidth));
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        RoundRectangle2D.Double outline = new RoundRectangle2D.Double(0, 0, width-lineWidth, height-lineWidth, height, height);
        Component parent  = c.getParent();
        if (parent!=null) {
            Area borderRegion = new Area(new Rectangle(0,0,width, height));
            borderRegion.subtract(new Area(outline));
            g2d.setClip(borderRegion);
            g2d.setColor(parent.getBackground());
            g2d.fillRect(x, y, width, height);
            g2d.setClip(null);
        }
        g2d.setColor(lineColor);
        g2d.draw(outline);
        g2d.dispose();
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return (Insets) insets.clone();
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }
}
