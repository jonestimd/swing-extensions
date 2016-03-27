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
package io.github.jonestimd.swing.border;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.border.Border;

/**
 * A line border drawn with semi-circles for the left and right sides of the component.
 */
public class OblongBorder implements Border {
    private final int lineWidth;
    private final Color lineColor;

    public OblongBorder(int lineWidth, Color lineColor) {
        this.lineWidth = lineWidth;
        this.lineColor = lineColor;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D) g.create(x, y, width, height);
        g2d.setStroke(new BasicStroke(lineWidth));
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(c.getBackground());
        g2d.clearRect(x, y, height/2, height);
        g2d.clearRect(x+width-height/2, y, height/2, height);
        g2d.fillArc(x + lineWidth / 2, y + lineWidth / 2, height - lineWidth, height - lineWidth, 90, 180);
        g2d.fillArc(x + width - height - lineWidth / 2, y + lineWidth / 2, height - lineWidth, height - lineWidth, -90, 180);

        g2d.setColor(lineColor);
        g2d.drawRoundRect(x+lineWidth/2, y+lineWidth/2, width-lineWidth, height-lineWidth, height, height);
        g2d.dispose();
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(lineWidth+1, lineWidth, lineWidth, lineWidth);
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }
}
