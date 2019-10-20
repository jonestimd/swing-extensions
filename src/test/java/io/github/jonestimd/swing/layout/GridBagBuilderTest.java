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
package io.github.jonestimd.swing.layout;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static java.awt.GridBagConstraints.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GridBagBuilderTest {
    private final ResourceBundle bundle = ResourceBundle.getBundle("test-resources");
    private final JPanel panel = new JPanel();
    private final GridBagConstraints textFieldLabelConstraints = new GridBagConstraints(0, 0, 1, 1, 0, 0, EAST, NONE, new Insets(2, 5, 0, 2), 0, 0);
    private final GridBagConstraints textAreaLabelConstraints = new GridBagConstraints(0, 0, 2, 1, 0, 0, WEST, NONE, new Insets(2, 0, 0, 2), 0, 0);
    private final GridBagConstraints textFieldConstraints = new GridBagConstraints(1, 0, 1, 1, 1, 0, WEST, HORIZONTAL, new Insets(2, 0, 0, 2), 0, 0);
    private final GridBagConstraints tableScrollPaneConstraints = new GridBagConstraints(0, 1, 2, 1, 1, 1, WEST, BOTH, new Insets(2, 0, 0, 2), 0, 0);
    @Mock
    private Map<Class<?>, GridBagFormula> constraintsMap;

    @Test
    public void setsContainerLayout() throws Exception {
        new GridBagBuilder(panel, bundle, "");

        assertThat(panel.getLayout()).isInstanceOf(GridBagLayout.class);
    }

    @Test
    public void setConstraints() throws Exception {
        new GridBagBuilder(panel, bundle, "", 2, constraintsMap).setConstraints(JPanel.class, FormElement.LIST);

        verify(constraintsMap).put(JPanel.class, FormElement.LIST);
    }

    @Test
    public void appendJTextFieldWithLabel() throws Exception {
        GridBagBuilder builder = new GridBagBuilder(panel, bundle, "gridBagBuilder.");

        JTextField field = builder.append("field1", new JTextField());

        assertThat(panel.getComponentCount()).isEqualTo(2);
        JLabel label = (JLabel) panel.getComponent(0);
        assertThat(builder.getLastLabel()).isSameAs(label);
        assertThat(label.getText()).isEqualTo("Field 1");
        assertThat(label.getDisplayedMnemonic()).isEqualTo('1');
        assertThat(label.getLabelFor()).isSameAs(field);
        assertThat(panel.getComponent(1)).isSameAs(field);
        verifyConstraints(getConstraints(label), textFieldLabelConstraints);
        verifyConstraints(getConstraints(field), textFieldConstraints);
    }

    @Test
    public void unrelatedVerticalGap() throws Exception {
        GridBagBuilder builder = new GridBagBuilder(panel, bundle, "gridBagBuilder.");
        builder.append("field1", new JTextField());

        JButton button = builder.unrelatedVerticalGap().append(new JButton("button"));

        assertThat(panel.getComponentCount()).isEqualTo(3);
        assertThat(panel.getComponent(2)).isSameAs(button);
        verifyConstraints(getConstraints(button), new GridBagConstraints(0, 1, 1, 1, 1, 0, WEST, HORIZONTAL, new Insets(10, 0, 0, 2), 0, 0));
    }

    @Test
    public void appendJTextFormattedFieldWithLabel() throws Exception {
        GridBagBuilder builder = new GridBagBuilder(panel, bundle, "gridBagBuilder.");

        JFormattedTextField field = builder.append("field1", new JFormattedTextField());

        assertThat(panel.getComponentCount()).isEqualTo(2);
        JLabel label = (JLabel) panel.getComponent(0);
        assertThat(builder.getLastLabel()).isSameAs(label);
        assertThat(label.getText()).isEqualTo("Field 1");
        assertThat(label.getDisplayedMnemonic()).isEqualTo('1');
        assertThat(label.getLabelFor()).isSameAs(field);
        assertThat(panel.getComponent(1)).isSameAs(field);
        verifyConstraints(getConstraints(label), textFieldLabelConstraints);
        verifyConstraints(getConstraints(field), textFieldConstraints);
    }

    @Test
    public void appendJTextAreaWithLabel() throws Exception {
        GridBagBuilder builder = new GridBagBuilder(panel, bundle, "gridBagBuilder.");

        JTextArea field = builder.append("field1", new JTextArea());

        assertThat(panel.getComponentCount()).isEqualTo(2);
        JLabel label = (JLabel) panel.getComponent(0);
        assertThat(builder.getLastLabel()).isSameAs(label);
        assertThat(label.getText()).isEqualTo("Field 1");
        assertThat(label.getDisplayedMnemonic()).isEqualTo('1');
        assertThat(label.getLabelFor()).isSameAs(field);
        assertThat(panel.getComponent(1)).isInstanceOf(JScrollPane.class);
        assertThat(((JScrollPane) panel.getComponent(1)).getViewport().getView()).isSameAs(field);
        verifyConstraints(getConstraints(label), textAreaLabelConstraints);
        verifyConstraints(getConstraints(1), new GridBagConstraints(0, 1, 2, 1, 1, 0.3, WEST, BOTH, new Insets(2, 0, 0, 2), 0, 0));
    }

    @Test
    public void appendJTableWithLabel() throws Exception {
        GridBagBuilder builder = new GridBagBuilder(panel, bundle, "gridBagBuilder.");

        JTable field = builder.append("field1", new JTable());

        assertThat(panel.getComponentCount()).isEqualTo(2);
        JLabel label = (JLabel) panel.getComponent(0);
        assertThat(builder.getLastLabel()).isSameAs(label);
        assertThat(label.getText()).isEqualTo("Field 1");
        assertThat(label.getDisplayedMnemonic()).isEqualTo('1');
        assertThat(label.getLabelFor()).isSameAs(field);
        assertThat(panel.getComponent(1)).isInstanceOf(JScrollPane.class);
        assertThat(((JScrollPane) panel.getComponent(1)).getViewport().getView()).isSameAs(field);
        verifyConstraints(getConstraints(label), textAreaLabelConstraints);
        verifyConstraints(getConstraints(1), tableScrollPaneConstraints);
    }

    @Test
    public void appendJListWithLabel() throws Exception {
        GridBagBuilder builder = new GridBagBuilder(panel, bundle, "gridBagBuilder.");

        JList<String> field = builder.append("field1", new JList<>());

        assertThat(panel.getComponentCount()).isEqualTo(2);
        JLabel label = (JLabel) panel.getComponent(0);
        assertThat(builder.getLastLabel()).isSameAs(label);
        assertThat(label.getText()).isEqualTo("Field 1");
        assertThat(label.getDisplayedMnemonic()).isEqualTo('1');
        assertThat(label.getLabelFor()).isSameAs(field);
        assertThat(panel.getComponent(1)).isInstanceOf(JScrollPane.class);
        assertThat(((JScrollPane) panel.getComponent(1)).getViewport().getView()).isSameAs(field);
        verifyConstraints(getConstraints(label), textAreaLabelConstraints);
        verifyConstraints(getConstraints(1), tableScrollPaneConstraints);
    }

    @Test
    public void appendRadioButtons() throws Exception {
        GridBagBuilder builder = new GridBagBuilder(panel, bundle, "gridBagBuilder.");
        JRadioButton button1 = new JRadioButton("Button 1");
        JRadioButton button2 = new JRadioButton("Button 2");

        builder.append(button1, button2);

        assertThat(panel.getComponentCount()).isEqualTo(1);
        assertThat(button1.getParent()).isSameAs(panel.getComponent(0));
        assertThat(button2.getParent()).isSameAs(panel.getComponent(0));
        verifyConstraints(getConstraints(0), new GridBagConstraints(0, 0, 2, 1, 1, 0, WEST, HORIZONTAL, new Insets(2, 0, 0, 2), 0, 0));
    }

    @Test
    public void appendJButton() throws Exception {
        GridBagBuilder builder = new GridBagBuilder(panel, bundle, "gridBagBuilder.");

        JButton button = builder.append(new JButton());

        assertThat(panel.getComponentCount()).isEqualTo(1);
        assertThat(panel.getComponent(0)).isSameAs(button);
        verifyConstraints(getConstraints(button), new GridBagConstraints(0, 0, 1, 1, 1, 0, WEST, HORIZONTAL, new Insets(2, 0, 0, 2), 0, 0));
    }

    private GridBagConstraints getConstraints(Component component) {
        return getLayout().getConstraints(component);
    }

    private GridBagConstraints getConstraints(int index) {
        return getLayout().getConstraints(panel.getComponent(index));
    }

    private GridBagLayout getLayout() {
        return (GridBagLayout) panel.getLayout();
    }

    private void verifyConstraints(GridBagConstraints actual, GridBagConstraints expected) {
        assertThat(actual.anchor).isEqualTo(expected.anchor);
        assertThat(actual.gridx).isEqualTo(expected.gridx);
        assertThat(actual.gridy).isEqualTo(expected.gridy);
        assertThat(actual.gridwidth).isEqualTo(expected.gridwidth);
        assertThat(actual.gridheight).isEqualTo(expected.gridheight);
        assertThat(actual.fill).isEqualTo(expected.fill);
        assertThat(actual.weightx).isEqualTo(expected.weightx);
        assertThat(actual.weighty).isEqualTo(expected.weighty);
        assertThat(actual.insets).isEqualTo(expected.insets);
    }
}