// The MIT License (MIT)
//
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
package io.github.jonestimd.swing;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.Box.Filler;
import javax.swing.JButton;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class ButtonBarFactoryTest {
    @Test
    public void setsSizesOfButtons() throws Exception {
        JButton button1 = newButton(10, 20);
        JButton button2 = newButton(20, 5);
        JButton button3 = newButton(15, 15);

        Box buttonBar = new ButtonBarFactory().add(button1, button2, button3).get();

        Dimension expectedSize = new Dimension(20, 20);
        Arrays.asList(buttonBar.getComponents()).stream().filter(AbstractButton.class::isInstance).forEach(button -> {
            assertThat(button.getMinimumSize()).isEqualTo(expectedSize);
            assertThat(button.getMaximumSize()).isEqualTo(expectedSize);
            assertThat(button.getPreferredSize()).isEqualTo(expectedSize);
        });
    }

    @Test
    public void createVerticalButtonBarSetsSizesOfButtons() throws Exception {
        JButton button1 = newButton(10, 20);
        JButton button2 = newButton(20, 5);
        JButton button3 = newButton(15, 15);

        Box buttonBar = ButtonBarFactory.createVerticalButtonBar(button1, button2, button3);

        Dimension expectedSize = new Dimension(20, 20);
        Arrays.asList(buttonBar.getComponents()).stream().filter(AbstractButton.class::isInstance).forEach(button -> {
            assertThat(button.getMinimumSize()).isEqualTo(expectedSize);
            assertThat(button.getMaximumSize()).isEqualTo(expectedSize);
            assertThat(button.getPreferredSize()).isEqualTo(expectedSize);
        });
        for (int i = 1; i < buttonBar.getComponentCount(); i+=2) {
            assertThat(buttonBar.getComponent(i)).isInstanceOf(Filler.class);
            assertThat(buttonBar.getComponent(i).getMinimumSize()).isEqualTo(new Dimension(0, 5));
            assertThat(buttonBar.getComponent(i).getPreferredSize()).isEqualTo(new Dimension(0, 5));
            assertThat(buttonBar.getComponent(i).getMaximumSize()).isEqualTo(new Dimension(Short.MAX_VALUE, 5));
        }
    }

    @Test
    public void alignRightAddsHorizontalGlue() throws Exception {
        Box buttonBar = new ButtonBarFactory().add(new JButton()).alignRight().get();

        assertThat(buttonBar.getComponent(0)).isInstanceOf(Filler.class);
        assertThat(buttonBar.getComponent(0).getMinimumSize()).isEqualTo(new Dimension(0, 0));
        assertThat(buttonBar.getComponent(0).getPreferredSize()).isEqualTo(new Dimension(0, 0));
        assertThat(buttonBar.getComponent(0).getMaximumSize()).isEqualTo(new Dimension(Short.MAX_VALUE, 0));
    }

    @Test
    public void borderUsesTopForMissingValues() throws Exception {
        Box box = new ButtonBarFactory().border(5).get();

        assertThat(box.getBorder().getBorderInsets(box)).isEqualTo(new Insets(5, 5, 5, 5));
    }

    @Test
    public void borderUsesTopForBottomAndLeftForRight() throws Exception {
        Box box = new ButtonBarFactory().border(1, 2).get();

        assertThat(box.getBorder().getBorderInsets(box)).isEqualTo(new Insets(1, 2, 1, 2));
    }

    @Test
    public void borderUsesLeftForRight() throws Exception {
        Box box = new ButtonBarFactory().border(1, 2, 3).get();

        assertThat(box.getBorder().getBorderInsets(box)).isEqualTo(new Insets(1, 2, 3, 2));
    }

    @Test
    public void borderUsesAllInputValues() throws Exception {
        Box box = new ButtonBarFactory().border(1, 2, 3, 4).get();

        assertThat(box.getBorder().getBorderInsets(box)).isEqualTo(new Insets(1, 2, 3, 4));
    }

    @Test
    public void addActionCreatesButtonForTheAction() throws Exception {
        TestAction action = new TestAction();

        Box buttonBar = new ButtonBarFactory().add(action).get();

        assertThat(buttonBar.getComponent(0)).isInstanceOf(JButton.class);
        assertThat(((JButton) buttonBar.getComponent(0)).getAction()).isSameAs(action);
    }

    private JButton newButton(int width, int height) {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(width, height));
        return button;
    }

    private static class TestAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
        }
    }
}