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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;

import io.github.jonestimd.swing.border.OblongBorder;
import io.github.jonestimd.swing.component.FilterField;
import io.github.jonestimd.swing.component.IconBorder;
import io.github.jonestimd.swing.component.IconBorder.Side;

public class ComponentFactory {
    public static final ResourceBundle DEFAULT_BUNDLE = ResourceBundle.getBundle("io.github.jonestimd.swing.ComponentResources");

    protected final ResourceBundle bundle;

    public ComponentFactory() {
        this(DEFAULT_BUNDLE);
    }

    public ComponentFactory(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    public static JRadioButton[] newRadioButtonGroup(ResourceBundle bundle, String ... mnemonicAndNameKeys) {
        ButtonGroup group = new ButtonGroup();
        JRadioButton[] buttons = new JRadioButton[mnemonicAndNameKeys.length];
        for (int i = 0; i < mnemonicAndNameKeys.length; i++) {
            String mnemonicAndName = bundle.getString(mnemonicAndNameKeys[i]);
            buttons[i] = new JRadioButton(mnemonicAndName.substring(1));
            if (mnemonicAndName.charAt(0) != ' ') {
                buttons[i].setMnemonic(mnemonicAndName.charAt(0));
            }
            group.add(buttons[i]);
        }
        return buttons;
    }

    public static JTextArea newValidationStatusArea(int rows, ResourceBundle bundle) {
        JTextArea statusArea = new JTextArea();
        statusArea.setEditable(false);
        statusArea.setRows(rows);
        statusArea.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), new EmptyBorder(2, 2, 2, 2)));
        statusArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, statusArea.getHeight()));
        statusArea.setBackground(ColorFactory.createColor(bundle.getString(SwingResource.VALIDATION_MESSAGE_BACKGROUND.key())));
        return statusArea;
    }

    public static JSeparator newMenuBarSeparator() {
        JSeparator separator = new JSeparator(JSeparator.VERTICAL);
        separator.setMaximumSize(new Dimension(separator.getPreferredSize().width, Integer.MAX_VALUE));
        return separator;
    }

    public static JToolBar newMenuToolBar() {
        JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL);
        toolbar.setFloatable(false);
        toolbar.setRollover(true);
        toolbar.setBorderPainted(false);
        return toolbar;
    }

    /**
     * Creates a toggle button for a toolbar. Sets the button's tooltip text using the button's text and accelerator.
     * @param action the button action (should have text and an icon)
     * @return the new button
     * @see Action#ACCELERATOR_KEY
     */
    public static JToggleButton newToolbarToggleButton(Action action) {
        return initToolbarButton(action, new JToggleButton(action));
    }

    /**
     * Creates a button for a toolbar. Sets the button's tooltip text using the action's text and accelerator key.
     * @param action the button action (should have text and an icon)
     * @return a new non-focusable button with {@code null} text
     * @see Action#ACCELERATOR_KEY
     */
    public static JButton newToolbarButton(Action action) {
        return initToolbarButton(action, new JButton(action));
    }

    private static <T extends AbstractButton> T initToolbarButton(Action action, T button) {
        StringBuilder tooltip = new StringBuilder(button.getText());
        KeyStroke accelerator = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);
        if (accelerator != null) {
            tooltip.append(" (");
            if (accelerator.getModifiers() > 0) {
                tooltip.append(KeyEvent.getKeyModifiersText(accelerator.getModifiers()));
                tooltip.append('-');
            }
            if (accelerator.getKeyCode() != 0) {
                tooltip.append(KeyEvent.getKeyText(accelerator.getKeyCode()));
            }
            else {
                tooltip.append(accelerator.getKeyChar());
            }
            tooltip.append(')');
        }
        button.setToolTipText(tooltip.toString());
        button.setText(null);
        button.setFocusable(false);
        return button;
    }

    public static JMenu newMenu(ResourceBundle bundle, String mnemonicAndNameKey) {
        String mnemonicAndName = bundle.getString(mnemonicAndNameKey);
        JMenu menu = new JMenu(mnemonicAndName.substring(1));
        if (mnemonicAndName.charAt(0) != ' ') menu.setMnemonic(mnemonicAndName.charAt(0));
        menu.putClientProperty(ClientProperty.MNEMONIC_AND_NAME_KEY, mnemonicAndNameKey);
        return menu;
    }

    /**
     * Create a {@link JTextField} with an {@link OblongBorder} and an {@link IconBorder} using the filter icon.
     */
    public JTextField newFilterField() {
        return initializeFilterField(new JTextField());
    }

    /**
     * Create a {@link FilterField} with an {@link OblongBorder} and an {@link IconBorder} using the filter icon.
     */
    public <T> FilterField<T> newFilterField(Function<String, Predicate<T>> predicateFactory) {
        return initializeFilterField(new FilterField<>(predicateFactory, (Color) bundle.getObject("filter.invalid.background")));
    }

    private <T extends JTextComponent> T initializeFilterField(T field) {
        ImageIcon filterIcon = (ImageIcon) bundle.getObject("filter.iconImage");
        field.setBorder(new CompoundBorder(new OblongBorder(2, Color.GRAY),
                new CompoundBorder(new EmptyBorder(0, 4, 0, 0), new IconBorder(Side.LEFT, filterIcon))));
        return field;
    }

    public static JComponent newTableSummaryPanel(String mnemonicAndName, JComponent summary) {
        JLabel label = new LabelBuilder().mnemonicAndName(mnemonicAndName).bold().get();
        JComponent summaryPanel = new ButtonBarFactory().alignRight().add(label, summary).get();
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, UIManager.getInt("ScrollBar.width")));
        return summaryPanel;
    }
}