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

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;

/**
 * Extends {@link ValidationBorder} to display the validation error as a tooltip.
 */
public class ValidationTooltipBorder extends ValidationBorder {
    private JComponent component;
    private boolean mouseOverIndicator;

    public ValidationTooltipBorder(JComponent component) {
        this.component = component;
        component.addMouseMotionListener(new MouseHandler());
    }

    public boolean isMouseOverIndicator() {
        return ! isValid() && mouseOverIndicator;
    }

    private void updateMouseOverIndicator(Point p) {
        mouseOverIndicator = p.x > component.getWidth()-component.getInsets().right;
    }

    private class MouseHandler implements MouseMotionListener {
        public void mouseMoved(MouseEvent e) {
            updateMouseOverIndicator(e.getPoint());
        }

        public void mouseDragged(MouseEvent e) {
            updateMouseOverIndicator(e.getPoint());
        }
    }
}