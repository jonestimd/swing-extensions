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
package io.github.jonestimd.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Point;

import javax.swing.SwingConstants;

/**
 * A layout manager for button bars.  Makes all of the buttons the same size (the preferred size of
 * the largest button).  The buttons can be arranged in a single row or column and can be aligned with
 * the leading or trailing edge of the container.
 */
public class ButtonBarLayout implements LayoutManager2 {
    protected static final int DEFAULT_GAP = 5;
    protected static final float CENTERED = 0.5f;

    protected final int buttonGap;
    protected final int orientation;
    protected final int alignment;

    /**
     * Create a layout that puts the buttons in a left aligned horizontal row with a button gap of <code>5</code>.
     */
    public ButtonBarLayout() {
        this(SwingConstants.HORIZONTAL, DEFAULT_GAP, SwingConstants.LEADING);
    }

    /**
     * Create a layout with the specified alignment and orientation and a button gap of <code>5</code>.
     * @param orientation {@link SwingConstants#HORIZONTAL} or {@link SwingConstants#VERTICAL}
     * @param alignment {@link SwingConstants#LEADING} (for top or left) or {@link SwingConstants#TRAILING}
     * (for bottom or right)
     */
    public ButtonBarLayout(int orientation, int alignment) {
        this(orientation, DEFAULT_GAP, alignment);
    }

    /**
     * @param orientation {@link SwingConstants#HORIZONTAL} or {@link SwingConstants#VERTICAL}
     * @param buttonGap the spacing between buttons
     * @param alignment {@link SwingConstants#LEADING} (for top or left) or {@link SwingConstants#TRAILING}
     * (for bottom or right)
     */
    public ButtonBarLayout(int orientation, int buttonGap, int alignment) {
        if (orientation != SwingConstants.HORIZONTAL && orientation != SwingConstants.VERTICAL) {
            throw new IllegalArgumentException("Invalid orientation");
        }
        if (buttonGap < 0) throw new IllegalArgumentException("Invalid button gap");
        if (alignment != SwingConstants.LEADING && alignment != SwingConstants.TRAILING) {
            throw new IllegalArgumentException("Invalid alignment");
        }
        this.orientation = orientation;
        this.buttonGap = buttonGap;
        this.alignment = alignment;
    }

    /**
     * Create a layout that puts the buttons in a top aligned vertical column with a button gap of <code>5</code>.
     */
    public static ButtonBarLayout vertical() {
        return new ButtonBarLayout(SwingConstants.VERTICAL, SwingConstants.LEADING);
    }

    /**
     * Create a layout that puts the buttons in a right aligned horizontal row with a button gap of <code>5</code>.
     */
    public static ButtonBarLayout rightAligned() {
        return new ButtonBarLayout(SwingConstants.HORIZONTAL, SwingConstants.TRAILING);
    }

    @Override
    public void addLayoutComponent(Component comp, Object constraints) {
    }

    @Override
    public Dimension maximumLayoutSize(Container target) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public float getLayoutAlignmentX(Container target) {
        return CENTERED;
    }

    @Override
    public float getLayoutAlignmentY(Container target) {
        return CENTERED;
    }

    @Override
    public void invalidateLayout(Container target) {
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
    }

    @Override
    public void removeLayoutComponent(Component comp) {
    }

    @Override
    public Dimension preferredLayoutSize(Container target) {
        synchronized (target.getTreeLock()) {
            return new LayoutInfo(target).getTargetSize();
        }
    }

    @Override
    public Dimension minimumLayoutSize(Container target) {
        return preferredLayoutSize(target);
    }

    @Override
    public void layoutContainer(Container target) {
        synchronized (target.getTreeLock()) {
            LayoutInfo info = new LayoutInfo(target);
            Point loc = info.getStart();
            for (Component component : target.getComponents()) {
                if (component.isVisible()) {
                    component.setSize(info.buttonSize);
                    component.setLocation(loc);
                    info.next(loc);
                }
            }
        }
    }

    private class LayoutInfo {
        public final Dimension buttonSize = new Dimension();
        public final int count;
        private final Insets insets;
        private final int targetWidth;
        private final int targetHeight;

        public LayoutInfo(Container target) {
            this.insets = target.getInsets();
            this.targetWidth = target.getWidth();
            this.targetHeight = target.getHeight();
            int count = 0;
            for (Component component : target.getComponents()) {
                if (component.isVisible()) {
                    count++;
                    buttonSize.height = Math.max(buttonSize.height, component.getPreferredSize().height);
                    buttonSize.width = Math.max(buttonSize.width, component.getPreferredSize().width);
                }
            }
            this.count = count;
        }

        public Dimension getTargetSize() {
            Dimension size = new Dimension(buttonSize);
            if (orientation == SwingConstants.HORIZONTAL) size.width = size.width*count + buttonGap*(count - 1);
            else size.height = size.height*count + buttonGap*(count - 1);
            size.width += insets.left + insets.right;
            size.height += insets.top + insets.bottom;
            return size;
        }

        public Point getStart() {
            if (alignment == SwingConstants.TRAILING) {
                if (orientation == SwingConstants.HORIZONTAL) {
                    return new Point(targetWidth - buttonSize.width*count - buttonGap*(count - 1) - insets.right, insets.top);
                }
                else {
                    return new Point(insets.left, targetHeight - buttonSize.height*count - buttonGap*(count - 1) - insets.bottom);
                }
            }
            return new Point(insets.left, insets.top);
        }

        public void next(Point loc) {
            if (orientation == SwingConstants.HORIZONTAL) loc.x += buttonSize.width + buttonGap;
            else loc.y += buttonSize.height + buttonGap;
        }
    }
}
