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

import java.awt.Component;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;

import io.github.jonestimd.util.Streams;

public class ButtonBarFactory {
    public static final int BUTTON_GAP = 5;

    public static Box setSize(Box buttonBar) {
        Dimension maxSize = getMaxSize(buttonBar.getComponents());
        buttonBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, maxSize.height));
        for (Component component : buttonBar.getComponents()) {
            if (component instanceof AbstractButton) {
                component.setMinimumSize(maxSize);
                component.setMaximumSize(maxSize);
                component.setPreferredSize(maxSize);
            }
        }
        return buttonBar;
    }

    private static Dimension getMaxSize(Component... components) {
        return getMaxSize(Arrays.asList(components));
    }

    private static Dimension getMaxSize(List<? extends Component> components) {
        Dimension max = new Dimension();
        for (Component component : components) {
            if (component instanceof AbstractButton) {
                max.height = Math.max(max.height, component.getPreferredSize().height);
                max.width = Math.max(max.width, component.getPreferredSize().width);
            }
        }
        return max;
    }

    public static JComponent createVerticalButtonBar(Component... buttons) {
        return createVerticalButtonBar(Arrays.asList(buttons));
    }

    public static JComponent createVerticalButtonBar(List<? extends Component> components) {
        Dimension maxSize = getMaxSize(components);
        Box box = Box.createVerticalBox();
        box.setBorder(new EmptyBorder(0, 5, 0, 5));
        for (Component component : components) {
            component.setMinimumSize(maxSize);
            component.setMaximumSize(maxSize);
            component.setPreferredSize(maxSize);
            box.add(component);
            box.add(Box.createVerticalStrut(5));
        }
        box.remove(box.getComponentCount() - 1);
        return box;
    }

    private Box buttonBar;

    public ButtonBarFactory() {
        buttonBar = Box.createHorizontalBox();
    }

    public ButtonBarFactory alignRight() {
        buttonBar.add(Box.createHorizontalGlue(), 0);
        return this;
    }

    public ButtonBarFactory border(int top, int... borders) {
        int left = borders.length > 0 ? borders[0] : top;
        int bottom = borders.length > 1 ? borders[1] : top;
        int right = borders.length > 2 ? borders[2] : left;
        buttonBar.setBorder(BorderFactory.createEmptyBorder(top, left, bottom, right));
        return this;
    }

    public ButtonBarFactory add(Action... actions) {
        return addActions(Arrays.asList(actions));
    }

    public ButtonBarFactory addActions(Iterable<? extends Action> actions) {
        return addButtons(Streams.map(actions, JButton::new));
    }

    public ButtonBarFactory add(Component... buttons) {
        return addButtons(Arrays.asList(buttons));
    }

    public ButtonBarFactory addButtons(Iterable<? extends Component> buttons) {
        for (Component component : buttons) {
            buttonBar.add(component);
            buttonBar.add(Box.createHorizontalStrut(BUTTON_GAP));
        }
        buttonBar.remove(buttonBar.getComponentCount() - 1);
        return this;
    }

    public Box get() {
        return setSize(buttonBar);
    }
}