// The MIT License (MIT)
//
// Copyright (c) 2021 Timothy D. Jones
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
import java.util.ResourceBundle;
import java.util.function.Predicate;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultButtonModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

import io.github.jonestimd.swing.border.OblongBorder;
import io.github.jonestimd.swing.component.BeanListComboBox;
import io.github.jonestimd.swing.component.FilterField;
import io.github.jonestimd.swing.component.IconBorder;
import io.github.jonestimd.util.JavaPredicates;
import org.junit.Test;

import static io.github.jonestimd.swing.ComponentFactory.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ComponentFactoryTest {
    private final ResourceBundle bundle = ResourceBundle.getBundle("test-resources");

    @Test
    public void newRadioButtonGroupSetsMnemonicAndName() throws Exception {
        JRadioButton[] group = ComponentFactory.newRadioButtonGroup(bundle,
                "radio1.mnemonicAndName", "radio2.mnemonicAndName", "radio3.mnemonicAndName");

        assertThat(group.length).isEqualTo(3);
        assertThat(group[0].getMnemonic()).isEqualTo('1');
        assertThat(group[0].getText()).isEqualTo("Choice 1");
        assertThat(group[1].getMnemonic()).isEqualTo('2');
        assertThat(group[1].getText()).isEqualTo("Choice 2");
        assertThat(group[2].getMnemonic()).isEqualTo(0);
        assertThat(group[2].getText()).isEqualTo("Choice 3");
        ButtonGroup buttonGroup = ((DefaultButtonModel) group[0].getModel()).getGroup();
        assertThat(buttonGroup.getButtonCount()).isEqualTo(3);
        assertThat(((DefaultButtonModel) group[1].getModel()).getGroup()).isSameAs(buttonGroup);
        assertThat(((DefaultButtonModel) group[2].getModel()).getGroup()).isSameAs(buttonGroup);
    }

    @Test
    public void newCheckBoxSetsMnemonicAndName() throws Exception {
        JCheckBox checkBox = ComponentFactory.newCheckBox(bundle, "radio1.mnemonicAndName");

        assertThat(checkBox.getText()).isEqualTo("Choice 1");
        assertThat(checkBox.getMnemonic()).isEqualTo('1');
    }

    @Test
    public void createValidationStatusAreaWithDefaultColor() throws Exception {
        JTextArea statusArea = ComponentFactory.newValidationStatusArea(2, bundle);

        assertThat(statusArea.isEditable()).isFalse();
        assertThat(statusArea.getRows()).isEqualTo(2);
        assertThat(statusArea.getMaximumSize().width).isEqualTo(Integer.MAX_VALUE);
        assertThat(statusArea.getBackground()).isEqualTo(new Color(255, 255, 200));
    }

    @Test
    public void createValidationStatusAreaWithCustomColor() throws Exception {
        ResourceBundle bundle = ResourceBundle.getBundle("test-resources2");

        JTextArea statusArea = ComponentFactory.newValidationStatusArea(2, bundle);

        assertThat(statusArea.isEditable()).isFalse();
        assertThat(statusArea.getRows()).isEqualTo(2);
        assertThat(statusArea.getMaximumSize().width).isEqualTo(Integer.MAX_VALUE);
        assertThat(statusArea.getBackground()).isEqualTo(new Color(254, 254, 199));
    }

    @Test
    public void newFormSeparator() throws Exception {
        JComponent separator = ComponentFactory.newFormSeparator("the title");

        assertThat(separator.getComponentCount()).isEqualTo(5);
        assertThat(separator.getComponent(0)).isInstanceOf(JSeparator.class);
        assertThat(separator.getComponent(0).getAlignmentY()).isEqualTo(0f);
        assertThat(separator.getComponent(1)).isInstanceOf(Box.Filler.class);
        assertThat(separator.getComponent(2)).isInstanceOf(JLabel.class);
        assertThat(((JLabel) separator.getComponent(2)).getText()).isEqualTo("the title");
        assertThat(separator.getComponent(2).getFont().isBold()).isTrue();
        assertThat(separator.getComponent(3)).isInstanceOf(Box.Filler.class);
        assertThat(separator.getComponent(4)).isInstanceOf(JSeparator.class);
        assertThat(separator.getComponent(4).getAlignmentY()).isEqualTo(0f);
    }

    @Test
    public void newMenuSeparatorCreatesVerticalSeparator() throws Exception {
        JSeparator separator = ComponentFactory.newMenuBarSeparator();

        assertThat(separator.getOrientation()).isEqualTo(JSeparator.VERTICAL);
        assertThat(separator.getMaximumSize().height).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    public void newMenuToolBarCreatesHorizontalToolBar() throws Exception {
        JToolBar toolBar = ComponentFactory.newMenuToolBar();

        assertThat(toolBar.getOrientation()).isEqualTo(JToolBar.HORIZONTAL);
        assertThat(toolBar.isFloatable()).isFalse();
        assertThat(toolBar.isRollover()).isTrue();
        assertThat(toolBar.isBorderPainted()).isFalse();
    }

    @Test
    public void newMenuToolBarSetsLayoutForSynthUI() throws Exception {
        LookAndFeel lookAndFeel = UIManager.getLookAndFeel();
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.synth.SynthLookAndFeel");

            JToolBar toolBar = ComponentFactory.newMenuToolBar();

            assertThat(toolBar.getLayout()).isInstanceOf(BoxLayout.class);
        } finally {
            UIManager.setLookAndFeel(lookAndFeel);
        }
    }

    @Test
    public void newToolbarToggleButton() throws Exception {
        Action action = mock(Action.class);
        when(action.getValue(Action.NAME)).thenReturn("action");

        JToggleButton button = ComponentFactory.newToolbarToggleButton(action);

        assertThat(button.getAction()).isSameAs(action);
        assertThat(button.getToolTipText()).isEqualTo("action");
        assertThat(button.getText()).isNull();
        assertThat(button.isFocusable()).isFalse();
    }

    @Test
    public void newToolbarToggleButtonShowsAcceleratorInTooltipForTypedKey() throws Exception {
        Action action = mock(Action.class);
        when(action.getValue(Action.NAME)).thenReturn("action");
        when(action.getValue(Action.ACCELERATOR_KEY)).thenReturn(KeyStroke.getKeyStroke("typed B"));

        JToggleButton button = ComponentFactory.newToolbarToggleButton(action);

        assertThat(button.getToolTipText()).isEqualTo("action (B)");
    }

    @Test
    public void newToolbarToggleButtonWithModifierShowsAcceleratorInTooltip1() throws Exception {
        Action action = mock(Action.class);
        when(action.getValue(Action.NAME)).thenReturn("action");
        when(action.getValue(Action.ACCELERATOR_KEY)).thenReturn(KeyStroke.getKeyStroke("ctrl B"));

        JToggleButton button = ComponentFactory.newToolbarToggleButton(action);

        assertThat(button.getToolTipText()).isEqualTo("action (Ctrl" + ACCELERATOR_DELIMITER + "B)");
    }

    @Test
    public void newToolbarButton() throws Exception {
        Action action = mock(Action.class);
        when(action.getValue(Action.NAME)).thenReturn("action");

        JButton button = ComponentFactory.newToolbarButton(action);

        assertThat(button.getAction()).isSameAs(action);
        assertThat(button.getToolTipText()).isEqualTo("action");
        assertThat(button.getText()).isNull();
        assertThat(button.isFocusable()).isFalse();
    }

    @Test
    public void newToolbarButtonShowsAcceleratorInTooltipForTypedKey() throws Exception {
        Action action = mock(Action.class);
        when(action.getValue(Action.NAME)).thenReturn("action");
        KeyStroke keyStroke = KeyStroke.getKeyStroke("typed B");
        when(action.getValue(Action.ACCELERATOR_KEY)).thenReturn(keyStroke);

        JButton button = ComponentFactory.newToolbarButton(action);

        assertThat(button.getToolTipText()).isEqualTo("action (B)");
        assertThat(button.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(keyStroke)).isEqualTo(TOOLBAR_BUTTON_ACTION_KEY);
        assertThat(button.getActionMap().get(TOOLBAR_BUTTON_ACTION_KEY)).isSameAs(action);
    }

    @Test
    public void newToolbarButtonWithModifierShowsAcceleratorInTooltip1() throws Exception {
        Action action = mock(Action.class);
        when(action.getValue(Action.NAME)).thenReturn("action");
        KeyStroke keyStroke = KeyStroke.getKeyStroke("ctrl B");
        when(action.getValue(Action.ACCELERATOR_KEY)).thenReturn(keyStroke);

        JButton button = ComponentFactory.newToolbarButton(action);

        assertThat(button.getToolTipText()).isEqualTo("action (Ctrl" + ACCELERATOR_DELIMITER + "B)");
        assertThat(button.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(keyStroke)).isEqualTo(TOOLBAR_BUTTON_ACTION_KEY);
        assertThat(button.getActionMap().get(TOOLBAR_BUTTON_ACTION_KEY)).isSameAs(action);
    }

    @Test
    public void newMenuWithMnemonic() throws Exception {
        JMenu menu = ComponentFactory.newMenu(bundle, "menu1.mnemonicAndKey");

        assertThat(menu.getText()).isEqualTo("Menu");
        assertThat(menu.getMnemonic()).isEqualTo('M');
    }

    @Test
    public void newMenuWithoutMnemonic() throws Exception {
        JMenu menu = ComponentFactory.newMenu(bundle, "menu2.mnemonicAndKey");

        assertThat(menu.getText()).isEqualTo("Menu");
        assertThat(menu.getMnemonic()).isEqualTo(0);
    }

    @Test
    public void newFilterFieldSetsBorders() throws Exception {
        JTextField field = new ComponentFactory().newFilterField();

        assertThat(field.getBorder()).isInstanceOf(CompoundBorder.class);
        assertThat(getOutsideBorder(field.getBorder())).isInstanceOf(OblongBorder.class);
        assertThat(getInsideBorder(field.getBorder())).isInstanceOf(IconBorder.class);
    }

    private Border getOutsideBorder(Border border) {
        return ((CompoundBorder) border).getOutsideBorder();
    }

    private Border getInsideBorder(Border border) {
        return ((CompoundBorder) border).getInsideBorder();
    }

    @Test
    public void newFilterFieldSetsErrorBackgroundForParseError() throws Exception {
        FilterField<String> field = new ComponentFactory().newFilterField(ComponentFactoryTest::parseFilter, 0, 0);
        Color background = field.getBackground();

        field.setText("invalid");

        assertThat(field.getBackground()).isEqualTo(ComponentResources.BUNDLE.getObject("filter.invalid.background"));
        field.setText("");
        assertThat(field.getBackground()).isEqualTo(background);
    }

    private static Predicate<String> parseFilter(String text) {
        if ("invalid".equals(text)) throw new IllegalArgumentException("Invalid search");
        return JavaPredicates.alwaysFalse();
    }


    @Test
    public void newTableSummaryPanel() throws Exception {
        JTextField field = new JTextField();
        JComponent summary = ComponentFactory.newTableSummaryPanel("_Summary", field);

        assertThat(summary.getComponent(1)).isInstanceOf(JLabel.class);
        assertThat(((JLabel) summary.getComponent(1)).getText()).isEqualTo("Summary");
        assertThat(((JLabel) summary.getComponent(1)).getDisplayedMnemonic()).isEqualTo(0);
        assertThat(summary.getComponent(3)).isSameAs(field);
    }

    private enum TestEnum {ONE, TWO, THREE}

    @Test
    public void newComboBox_createsValidatedComboBox() throws Exception {
        final String message = "value is required";

        BeanListComboBox<TestEnum> comboBox = newComboBox(TestEnum.class, message);

        assertThat(comboBox.getValidationMessages()).isEqualTo(message);
        assertThat(comboBox.getModel().indexOf(TestEnum.ONE)).isEqualTo(0);
        assertThat(comboBox.getModel().indexOf(TestEnum.TWO)).isEqualTo(1);
        assertThat(comboBox.getModel().indexOf(TestEnum.THREE)).isEqualTo(2);
    }
}