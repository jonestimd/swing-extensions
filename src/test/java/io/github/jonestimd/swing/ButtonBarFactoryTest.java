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

import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SwingConstants;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class ButtonBarFactoryTest {
    @Test
    public void defaultsToHorizontalLeading() throws Exception {
        JButton button1 = new JButton("one");
        JButton button2 = new JButton("two");
        JButton button3 = new JButton("three");

        JComponent buttonBar = new ButtonBarFactory().add(button1, button2, button3).get();

        ButtonBarLayout layout = (ButtonBarLayout) buttonBar.getLayout();
        assertThat(layout.orientation).isEqualTo(SwingConstants.HORIZONTAL);
        assertThat(layout.alignment).isEqualTo(SwingConstants.LEADING);
    }

    @Test
    public void createVerticalButtonBarSetsOrientation() throws Exception {
        JButton button1 = new JButton("one");
        JButton button2 = new JButton("two");
        JButton button3 = new JButton("three");

        JComponent buttonBar = ButtonBarFactory.createVerticalButtonBar(button1, button2, button3);

        ButtonBarLayout layout = (ButtonBarLayout) buttonBar.getLayout();
        assertThat(layout.orientation).isEqualTo(SwingConstants.VERTICAL);
    }

    @Test
    public void alignRightSetsAlignment() throws Exception {
        JComponent buttonBar = new ButtonBarFactory().add(new JButton()).alignRight().get();

        ButtonBarLayout layout = (ButtonBarLayout) buttonBar.getLayout();
        assertThat(layout.alignment).isEqualTo(SwingConstants.TRAILING);
    }

    @Test
    public void borderUsesTopForMissingValues() throws Exception {
        JComponent buttonBar = new ButtonBarFactory().border(5).get();

        assertThat(buttonBar.getBorder().getBorderInsets(buttonBar)).isEqualTo(new Insets(5, 5, 5, 5));
    }

    @Test
    public void borderUsesTopForBottomAndLeftForRight() throws Exception {
        JComponent buttonBar = new ButtonBarFactory().border(1, 2).get();

        assertThat(buttonBar.getBorder().getBorderInsets(buttonBar)).isEqualTo(new Insets(1, 2, 1, 2));
    }

    @Test
    public void borderUsesLeftForRight() throws Exception {
        JComponent buttonBar = new ButtonBarFactory().border(1, 2, 3).get();

        assertThat(buttonBar.getBorder().getBorderInsets(buttonBar)).isEqualTo(new Insets(1, 2, 3, 2));
    }

    @Test
    public void borderUsesAllInputValues() throws Exception {
        JComponent buttonBar = new ButtonBarFactory().border(1, 2, 3, 4).get();

        assertThat(buttonBar.getBorder().getBorderInsets(buttonBar)).isEqualTo(new Insets(1, 2, 3, 4));
    }

    @Test
    public void addActionCreatesButtonForTheAction() throws Exception {
        TestAction action = new TestAction();

        JComponent buttonBar = new ButtonBarFactory().add(action).get();

        assertThat(buttonBar.getComponent(0)).isInstanceOf(JButton.class);
        assertThat(((JButton) buttonBar.getComponent(0)).getAction()).isSameAs(action);
    }

    private static class TestAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
        }
    }
}