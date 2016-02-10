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
package io.github.jonestimd.swing.table;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

public class SectionTableUI extends BasicTableUI {
    public static ComponentUI createUI(JComponent c) {
        return new SectionTableUI();
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        super.paint(g, c);
        Rectangle clip = g.getClipBounds();
        Rectangle bounds = table.getBounds();
        // account for the fact that the graphics has already been translated
        // into the table's bounds
        bounds.x = bounds.y = 0;

        JTableHeader header = table.getTableHeader();
        if (bounds.intersects(clip) && (header == null || header.getDraggedColumn() == null)) {
            boolean ltr = table.getComponentOrientation().isLeftToRight();

            Point upperLeft = clip.getLocation();
            if (!ltr) {
                upperLeft.x++;
            }

            Point lowerRight = new Point(clip.x + clip.width - (ltr ? 1 : 0),
                    clip.y + clip.height);

            int rowMin = Math.max(table.rowAtPoint(upperLeft), 0);
            int rowMax = table.rowAtPoint(lowerRight);
            if (rowMax == -1) {
                rowMax = table.getRowCount()-1;
            }
            for (int row = rowMin; row <= rowMax; row++) {
                if (((SectionTable<?, ?>) table).isSectionRow(row)) {
                    Rectangle cellRect = table.getCellRect(row, 0, false);
                    cellRect.add(table.getCellRect(row, table.getColumnCount()-1, false));
                    paintCell(g, cellRect, row);
                }
            }
        }
    }

    private void paintCell(Graphics g, Rectangle cellRect, int row) {
        TableCellRenderer renderer = table.getCellRenderer(row, 0);
        Component component = table.prepareRenderer(renderer, row, 0);
        forceFillBackground(g, cellRect, component);
        rendererPane.paintComponent(g, component, table, cellRect.x, cellRect.y,
                cellRect.width, cellRect.height, true);
    }

    private void forceFillBackground(Graphics g, Rectangle cellRect, Component component) {
        // make sure isOpaque() returns correct value for instance of DefaultTableCellRenderer.UIResource
        if (component.getParent() != rendererPane) {
            rendererPane.add(component);
        }
        if (! component.isOpaque()) {
            g.setColor(component.getBackground());
            g.fillRect(cellRect.x, cellRect.y, cellRect.width, cellRect.height);
        }
    }
}
