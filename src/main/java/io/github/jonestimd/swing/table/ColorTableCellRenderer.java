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
package io.github.jonestimd.swing.table;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.table.DefaultTableCellRenderer;

/**
 * Renders a color swatch in a table cell.
 */
public class ColorTableCellRenderer extends DefaultTableCellRenderer {
    public static final int DEFAULT_INSET = 2;
    private Color value;
    private Insets insets = new Insets(DEFAULT_INSET, DEFAULT_INSET, DEFAULT_INSET, DEFAULT_INSET);

    public void setInsets(Insets insets) {
        this.insets = insets;
    }

    @Override
    protected void setValue(Object value) {
        if (value instanceof Color) this.value = (Color) value;
        else this.value = null;
        super.setValue(null);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (g != null) {
            super.paintComponent(g);
            if (value != null) {
                Graphics scratchGraphics = g.create();
                try {
                    Dimension size = getSize();
                    scratchGraphics.setColor(value);
                    scratchGraphics.fillRect(insets.left, insets.top, size.width - insets.left - insets.right, size.height - insets.top - insets.bottom);
                } finally {
                    scratchGraphics.dispose();
                }
            }
        }
    }
}
