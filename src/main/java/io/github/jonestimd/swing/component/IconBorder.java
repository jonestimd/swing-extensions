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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.border.Border;

/**
 * A border that displays an icon to the left or right of the component.
 */
public class IconBorder implements Border {
    /** Available sides for displaying the icon. */
    public enum Side { LEFT, RIGHT };
    protected final Icon icon;
    protected final Side side;

    public IconBorder(Side side, Icon icon) {
        this.side = side;
        this.icon = icon;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        icon.paintIcon(c, g, side == Side.LEFT ? x : width - icon.getIconWidth(), y + (height - icon.getIconHeight())/2);
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return side == Side.LEFT ? new Insets(0, icon.getIconWidth()+2, 0, 0) : new Insets(0, 0, 0, icon.getIconWidth()+2);
    }

    @Override
    public boolean isBorderOpaque() {
        return true;
    }
}
