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

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import io.github.jonestimd.collection.MapBuilder;
import io.github.jonestimd.swing.LabelBuilder;

import static java.awt.GridBagConstraints.*;

public class GridBagBuilder {
    public static final int RELATED_GAP = 2;
    public static final int UNRELATED_GAP = 10;
    private static final GridBagFormula EMPTY_CONSTRAINT = new GridBagFormula() {
        public GridBagConstraints setConstraints(GridBagConstraints gbc) {
            return gbc;
        }

        public GridBagFormula getLabelConstraints() {
            return this;
        }
    };
    private static final Map<Class<?>, GridBagFormula> DEFAULT_CONSTRAINTS = new MapBuilder<Class<?>, GridBagFormula>()
        .put(JLabel.class, FormElement.TOP_LABEL)
        .put(JComboBox.class, FormElement.TEXT_FIELD)
        .put(JTextField.class, FormElement.TEXT_FIELD)
        .put(JTextArea.class, FormElement.TEXT_AREA)
        .put(JTable.class, FormElement.TABLE)
        .put(JList.class, FormElement.LIST)
        .put(JPanel.class, FormElement.BUTTON_GROUP)
        .put(Box.class, FormElement.BUTTON_GROUP)
        .get();

    private final Map<Class<?>, GridBagFormula> fieldConstrains;
    private ResourceBundle bundle;
    private String resourcePrefix;
    private GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 1d, 0d, WEST, HORIZONTAL, new Insets(RELATED_GAP, 0, 0, RELATED_GAP), 0, 0);
    private Container container;
    private final int columns;

    /**
     * Create a new builder. The constraints are initialized as follows
     * <ul>
     * <li><code>(gridx, gridy) =</code> (0, 0)
     * <li><code>fill = HORIZONTAL</code>
     * <li><code>weightx, weighty =</code> 1.0
     * <li><code>anchor = WEST</code>
     * </ul>
     */
    public GridBagBuilder(Container container, ResourceBundle bundle, String resourcePrefix) {
        this(container, bundle, resourcePrefix, 2, DEFAULT_CONSTRAINTS);
    }

    public GridBagBuilder(Container container, ResourceBundle bundle, String resourcePrefix,
            int columns, Map<Class<?>, GridBagFormula> fieldConstraints) {
        this.fieldConstrains = fieldConstraints;
        this.bundle = bundle;
        this.resourcePrefix = resourcePrefix;
        this.columns = columns;
        this.container = container;
        container.setLayout(new GridBagLayout());
    }

    public GridBagBuilder relatedGap() {
        return insets(RELATED_GAP, 0, 0, RELATED_GAP);
    }

    public GridBagBuilder unrelatedVerticalGap() {
        return insets(UNRELATED_GAP, 0, 0, RELATED_GAP);
    }

    public GridBagBuilder insets(int top, int left, int bottom, int right) {
        gbc.insets.set(top, left, bottom, right);
        return this;
    }

    private GridBagFormula getConstraints(Class<?> fieldClass) {
        GridBagFormula constraints = fieldConstrains.get(fieldClass);
        while (constraints == null && fieldClass != null) {
            fieldClass = fieldClass.getSuperclass();
            constraints = fieldConstrains.get(fieldClass);
        }
        return constraints == null ? EMPTY_CONSTRAINT : constraints;
    }

    public <T extends JComponent> T append(String labelKey, T field) {
        GridBagFormula constraints = getConstraints(field.getClass());
        relatedGap();
        container.add(new LabelBuilder().mnemonicAndName(bundle.getString(resourcePrefix + labelKey)).forComponent(field).get(),
                constraints.getLabelConstraints().setConstraints(gbc));
        nextCell();
        return append(field, constraints);
    }

    public <T extends JComponent> T append(T field) {
        return append(field, getConstraints(field.getClass()));
    }

    private <T extends JComponent> T append(T field, GridBagFormula formula) {
        if (field instanceof JTextArea || field instanceof JTable || field instanceof JList) {
            container.add(new JScrollPane(field), formula.setConstraints(gbc));
        }
        else {
            container.add(field, formula.setConstraints(gbc));
        }
        nextCell();
        return field;
    }

    public void append(JRadioButton ... buttons) {
        Box box = Box.createHorizontalBox();
        for (JRadioButton button : buttons) {
            box.add(button);
            box.add(Box.createHorizontalStrut(RELATED_GAP));
        }
        append(box, FormElement.BUTTON_GROUP);
    }

    private void nextCell() {
        gbc.gridx += gbc.gridwidth;
        if (gbc.gridx >= columns) {
            gbc.gridx = 0;
            gbc.gridy++;
        }
    }
}