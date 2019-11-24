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

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.junit.Test;

import static io.github.jonestimd.swing.ButtonBarLayout.*;
import static org.assertj.core.api.Assertions.*;

public class ButtonBarLayoutTest {
    private List<JButton> buttons = Arrays.asList(new JButton("medium name"), new JButton("name"), new JButton("very long name"));

    @Test(expected = IllegalArgumentException.class)
    public void requiresHorizontalOrVertical() throws Exception {
        new ButtonBarLayout(SwingConstants.VERTICAL + 2, SwingConstants.LEADING);
    }

    @Test(expected = IllegalArgumentException.class)
    public void requiresLeadingOrTrailing() throws Exception {
        new ButtonBarLayout(SwingConstants.VERTICAL, SwingConstants.LEADING + 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void requiresPositiveGap() throws Exception {
        new ButtonBarLayout(SwingConstants.VERTICAL, -2, SwingConstants.LEADING);
    }

    @Test
    public void maximumLayoutSize_returnsMaxDimension() throws Exception {
        ButtonBarLayout layout = new ButtonBarLayout();

        assertThat(layout.maximumLayoutSize(null)).isEqualTo(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }

    @Test
    public void getLayoutAlignmentX_returnsCenter() throws Exception {
        ButtonBarLayout layout = new ButtonBarLayout();

        assertThat(layout.getLayoutAlignmentX(null)).isEqualTo(0.5f);
    }

    @Test
    public void getLayoutAlignmentY_returnsCenter() throws Exception {
        ButtonBarLayout layout = new ButtonBarLayout();

        assertThat(layout.getLayoutAlignmentY(null)).isEqualTo(0.5f);
    }

    @Test
    public void preferredLayoutSize_horizontalMultipliesMaxWidth() throws Exception {
        JPanel target = new JPanel();
        target.setBorder(new EmptyBorder(1, 2, 3, 4));
        buttons.forEach(target::add);
        ButtonBarLayout layout = new ButtonBarLayout();

        Dimension size = layout.preferredLayoutSize(target);

        Insets insets = target.getInsets();
        assertThat(size.width).isEqualTo(insets.left + insets.right + getExpectedWidth());
        assertThat(size.height).isEqualTo(insets.top + insets.bottom + buttons.get(2).getPreferredSize().height);
    }

    @Test
    public void preferredLayoutSize_ignoresInvisibleComponents() throws Exception {
        JPanel target = new JPanel();
        target.setBorder(new EmptyBorder(1, 2, 3, 4));
        buttons.forEach(target::add);
        buttons.get(2).setVisible(false);
        ButtonBarLayout layout = new ButtonBarLayout();

        Dimension size = layout.preferredLayoutSize(target);

        Insets insets = target.getInsets();
        assertThat(size.width).isEqualTo(insets.left + insets.right + getExpectedWidth());
        assertThat(size.height).isEqualTo(insets.top + insets.bottom + buttons.get(2).getPreferredSize().height);
    }

    @Test
    public void preferredLayoutSize_verticalMultipliesMaxHeight() throws Exception {
        JPanel target = new JPanel();
        target.setBorder(new EmptyBorder(1, 2, 3, 4));
        buttons.forEach(target::add);
        ButtonBarLayout layout = ButtonBarLayout.vertical();

        Dimension size = layout.preferredLayoutSize(target);

        Insets insets = target.getInsets();
        assertThat(size.width).isEqualTo(insets.left + insets.right + buttons.get(2).getPreferredSize().width);
        assertThat(size.height).isEqualTo(insets.top + insets.bottom + getExpectedHeight());
    }

    @Test
    public void minimumLayoutSize_horizontalMultipliesMaxWidth() throws Exception {
        JPanel target = new JPanel();
        target.setBorder(new EmptyBorder(1, 2, 3, 4));
        buttons.forEach(target::add);
        ButtonBarLayout layout = new ButtonBarLayout();

        Dimension size = layout.minimumLayoutSize(target);

        Insets insets = target.getInsets();
        assertThat(size.width).isEqualTo(insets.left + insets.right + getExpectedWidth());
        assertThat(size.height).isEqualTo(insets.top + insets.bottom + buttons.get(2).getPreferredSize().height);
    }

    @Test
    public void minimumLayoutSize_verticalMultipliesMaxHeight() throws Exception {
        JPanel target = new JPanel();
        target.setBorder(new EmptyBorder(1, 2, 3, 4));
        buttons.forEach(target::add);
        ButtonBarLayout layout = ButtonBarLayout.vertical();

        Dimension size = layout.minimumLayoutSize(target);

        Insets insets = target.getInsets();
        assertThat(size.width).isEqualTo(insets.left + insets.right + buttons.get(2).getPreferredSize().width);
        assertThat(size.height).isEqualTo(insets.top + insets.bottom + getExpectedHeight());
    }

    @Test
    public void layoutContainer_sizesAndPlacesButtons() throws Exception {
        JPanel target = new JPanel();
        target.setBorder(new EmptyBorder(1, 2, 3, 4));
        buttons.forEach(target::add);
        ButtonBarLayout layout = new ButtonBarLayout();

        layout.layoutContainer(target);

        Insets insets = target.getInsets();
        Dimension buttonSize = buttons.get(2).getSize();
        assertThat(buttons.get(0).getSize()).isEqualTo(buttonSize);
        assertThat(buttons.get(1).getSize()).isEqualTo(buttonSize);
        assertThat(buttons.get(0).getLocation()).isEqualTo(new Point(insets.left, insets.top));
        assertThat(buttons.get(1).getLocation()).isEqualTo(new Point(insets.left + buttonSize.width + DEFAULT_GAP, insets.top));
        assertThat(buttons.get(2).getLocation()).isEqualTo(new Point(insets.left + buttonSize.width*2 + DEFAULT_GAP*2, insets.top));
    }

    @Test
    public void layoutContainer_ignoresInvisibleButtons() throws Exception {
        JPanel target = new JPanel();
        target.setBorder(new EmptyBorder(1, 2, 3, 4));
        buttons.forEach(target::add);
        buttons.get(1).setVisible(false);
        ButtonBarLayout layout = new ButtonBarLayout();

        layout.layoutContainer(target);

        Insets insets = target.getInsets();
        Dimension buttonSize = buttons.get(2).getSize();
        assertThat(buttons.get(0).getSize()).isEqualTo(buttonSize);
        assertThat(buttons.get(1).getSize()).isEqualTo(new Dimension());
        assertThat(buttons.get(0).getLocation()).isEqualTo(new Point(insets.left, insets.top));
        assertThat(buttons.get(1).getLocation()).isEqualTo(new Point());
        assertThat(buttons.get(2).getLocation()).isEqualTo(new Point(insets.left + buttonSize.width + DEFAULT_GAP, insets.top));
    }

    @Test
    public void layoutContainer_rightAlignsButtons() throws Exception {
        int width = 500;
        JPanel target = new JPanel();
        target.setBorder(new EmptyBorder(1, 2, 3, 4));
        target.setSize(width, target.getPreferredSize().height);
        buttons.forEach(target::add);
        ButtonBarLayout layout = ButtonBarLayout.rightAligned();

        layout.layoutContainer(target);

        Insets insets = target.getInsets();
        Dimension buttonSize = buttons.get(2).getSize();
        assertThat(buttons.get(0).getSize()).isEqualTo(buttonSize);
        assertThat(buttons.get(1).getSize()).isEqualTo(buttonSize);
        int startX = width - insets.right - getExpectedWidth();
        assertThat(buttons.get(0).getLocation()).isEqualTo(new Point(startX, insets.top));
        assertThat(buttons.get(1).getLocation()).isEqualTo(new Point(startX + buttonSize.width + DEFAULT_GAP, insets.top));
        assertThat(buttons.get(2).getLocation()).isEqualTo(new Point(startX + buttonSize.width*2 + DEFAULT_GAP*2, insets.top));

    }

    @Test
    public void layoutContainer_bottomAlignsButtons() throws Exception {
        int height = 300;
        JPanel target = new JPanel();
        target.setBorder(new EmptyBorder(1, 2, 3, 4));
        target.setSize(target.getPreferredSize().width, height);
        buttons.forEach(target::add);
        ButtonBarLayout layout = new ButtonBarLayout(SwingConstants.VERTICAL, SwingConstants.TRAILING);

        layout.layoutContainer(target);

        Insets insets = target.getInsets();
        Dimension buttonSize = buttons.get(2).getSize();
        assertThat(buttons.get(0).getSize()).isEqualTo(buttonSize);
        assertThat(buttons.get(1).getSize()).isEqualTo(buttonSize);
        int startY = height - insets.bottom - getExpectedHeight();
        assertThat(buttons.get(0).getLocation()).isEqualTo(new Point(insets.left, startY));
        assertThat(buttons.get(1).getLocation()).isEqualTo(new Point(insets.left, startY + buttonSize.height + DEFAULT_GAP));
        assertThat(buttons.get(2).getLocation()).isEqualTo(new Point(insets.left, startY + buttonSize.height*2 + DEFAULT_GAP*2));
    }

    protected int getExpectedWidth() {
        int count = 0;
        int width = 0;
        for (JButton button : buttons) {
            if (button.isVisible()) {
                count++;
                width = Math.max(width, button.getPreferredSize().width);
            }
        }
        return count*width + (count - 1)*DEFAULT_GAP;
    }

    protected int getExpectedHeight() {
        int count = 0;
        int height = 0;
        for (JButton button : buttons) {
            if (button.isVisible()) {
                count++;
                height = Math.max(height, button.getPreferredSize().height);
            }
        }
        return count*height + (count - 1)*DEFAULT_GAP;
    }
}