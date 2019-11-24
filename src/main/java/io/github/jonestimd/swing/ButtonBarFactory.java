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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import io.github.jonestimd.util.Streams;

/**
 * Helper class for building button bars using {@link ButtonBarLayout}.
 */
public class ButtonBarFactory {
    public static final int BUTTON_GAP = 5;

    /**
     * Create a vertical button bar.
     * @param buttons the buttons to add to the button bar
     * @return the button bar
     */
    public static JComponent createVerticalButtonBar(Component... buttons) {
        return createVerticalButtonBar(Arrays.asList(buttons));
    }

    /**
     * Create a vertical button bar.
     * @param components the components to add to the button bar
     * @return the button bar
     */
    public static JComponent createVerticalButtonBar(List<? extends Component> components) {
        return new ButtonBarFactory().vertical().addButtons(components).border(0, 5, 0, 5).get();
    }

    private List<Component> buttons = new ArrayList<>();
    private int orientation = SwingConstants.HORIZONTAL;
    private int alignment = SwingConstants.LEADING;
    private Border border;

    /**
     * Make the button bar right aligned.
     * @return this factory
     */
    public ButtonBarFactory alignRight() {
        alignment = SwingConstants.TRAILING;
        return this;
    }

    public ButtonBarFactory vertical() {
        orientation = SwingConstants.VERTICAL;
        return this;
    }

    /**
     * Set an empty border on the button bar.  If only {@code top} is specified then it is used for all edges.
     * If {@code bottom} is not specified then the value for {@code top} is used.  If {@code right} is not
     * specified then the value for {@code left} (or {@code top}) is used.
     * @param top the top border width
     * @param borders the {@code left}, {@code bottom}, and {@code right} border widths
     * @return this factory
     */
    public ButtonBarFactory border(int top, int... borders) {
        int left = borders.length > 0 ? borders[0] : top;
        int bottom = borders.length > 1 ? borders[1] : top;
        int right = borders.length > 2 ? borders[2] : left;
        border = BorderFactory.createEmptyBorder(top, left, bottom, right);
        return this;
    }

    public ButtonBarFactory add(Action... actions) {
        return addActions(Arrays.asList(actions));
    }

    public ButtonBarFactory addActions(Collection<? extends Action> actions) {
        return addButtons(Streams.map(actions, JButton::new));
    }

    public ButtonBarFactory add(Component... buttons) {
        return addButtons(Arrays.asList(buttons));
    }

    public ButtonBarFactory addButtons(Collection<? extends Component> buttons) {
        this.buttons.addAll(buttons);
        return this;
    }

    public JComponent get() {
        JPanel buttonBar = new JPanel(new ButtonBarLayout(orientation, BUTTON_GAP, alignment));
        buttons.forEach(buttonBar::add);
        if (border != null) buttonBar.setBorder(border);
        return buttonBar;
    }
}