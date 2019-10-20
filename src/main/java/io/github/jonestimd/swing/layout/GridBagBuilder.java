// The MIT License (MIT)
//
// Copyright (c) 2017 Timothy D. Jones
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JCheckBox;
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

import com.google.common.collect.ImmutableSet;
import io.github.jonestimd.collection.MapBuilder;
import io.github.jonestimd.swing.LabelBuilder;

import static java.awt.GridBagConstraints.*;

/**
 * A form builder that simplifies usage of {@link GridBagLayout}.  The initial constraints are as follows
 * <ul>
 * <li><code>(gridx, gridy) =</code> (0, 0)
 * <li><code>fill = HORIZONTAL</code>
 * <li><code>weightx, weighty =</code> 1.0
 * <li><code>anchor = WEST</code>
 * </ul>
 * The constraints are updated using a {@link GridBagFormula} when each component is added.
 * <p>By default, the following components are automatically wrapped in a {@link JScrollPane}.
 * Automatic wrapping of components in <code>JScrollPane</code> can be enabled/disabled using
 * {@link #useScrollPane(Class)} and {@link #noUseScrollPane(Class)}
 * <ul>
 *     <li>{@link JTable}</li>
 *     <li>{@link JList}</li>
 *     <li>{@link JTextArea}</li>
 * </ul>
 */
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
        .put(JCheckBox.class, FormElement.CHECK_BOX)
        .put(JTable.class, FormElement.TABLE)
        .put(JList.class, FormElement.LIST)
        .put(JPanel.class, FormElement.BUTTON_GROUP)
        .put(Box.class, FormElement.BUTTON_GROUP)
        .get();
    private static final Set<Class<?>> USE_SCROLL_PANE = new ImmutableSet.Builder<Class<?>>()
            .add(JTable.class)
            .add(JTextArea.class)
            .add(JList.class).build();

    private final Map<Class<?>, GridBagFormula> fieldConstrains;
    private final Set<Class<?>> useScrollPane;
    private ResourceBundle bundle;
    private String resourcePrefix;
    private GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 1d, 0d, WEST, HORIZONTAL, new Insets(RELATED_GAP, 0, 0, RELATED_GAP), 0, 0);
    private Container container;
    private final int columns;
    private JLabel lastLabel;
    private int verticalGap = RELATED_GAP;

    /**
     * Create a new builder using 2 columns and the default {@link GridBagFormula}s.
     * @param container the form container
     * @param bundle provides label text
     * @param resourcePrefix prefix for resource bundle keys
     * @see FormElement
     */
    public GridBagBuilder(Container container, ResourceBundle bundle, String resourcePrefix) {
        this(container, bundle, resourcePrefix, 2);
    }

    /**
     * Create a new builder using the default {@link GridBagFormula}s.
     * @param container the form container
     * @param bundle provides label text
     * @param resourcePrefix prefix for resource bundle keys
     * @param columns the number of grid columns
     */
    public GridBagBuilder(Container container, ResourceBundle bundle, String resourcePrefix, int columns) {
        this(container, bundle, resourcePrefix, columns, new HashMap<>(DEFAULT_CONSTRAINTS));
    }

    /**
     * Create a new builder using specific {@link GridBagFormula}s.
     * @param container the form container
     * @param bundle provides label text
     * @param resourcePrefix prefix for resource bundle keys
     * @param columns the number of grid columns
     * @param fieldConstraints map of component class to {@link GridBagFormula}
     */
    public GridBagBuilder(Container container, ResourceBundle bundle, String resourcePrefix,
            int columns, Map<Class<?>, GridBagFormula> fieldConstraints) {
        this.fieldConstrains = fieldConstraints;
        this.useScrollPane = new HashSet<>(USE_SCROLL_PANE);
        this.bundle = bundle;
        this.resourcePrefix = resourcePrefix;
        this.columns = columns;
        this.container = container;
        container.setLayout(new GridBagLayout());
    }

    /**
     * Automatically wrap instances of the class in a {@link JScrollPane}.
     */
    public GridBagBuilder useScrollPane(Class<? extends JComponent> componentClass) {
        useScrollPane.add(componentClass);
        return this;
    }

    /**
     * Disable automatic wrapping of instances of the class in a {@link JScrollPane}.
     */
    public GridBagBuilder noUseScrollPane(Class<? extends JComponent> componentClass) {
        useScrollPane.remove(componentClass);
        return this;
    }

    /**
     * @return the most recently created field label
     */
    public JLabel getLastLabel() {
        return lastLabel;
    }

    public GridBagBuilder setConstraints(Class<?> componentClass, GridBagFormula constraints) {
        fieldConstrains.put(componentClass, constraints);
        return this;
    }

    /**
     * Use the horizontal and vertical {@link #RELATED_GAP} for the next component.
     * @return this builder
     */
    public GridBagBuilder relatedGap() {
        return insets(verticalGap, 0, 0, RELATED_GAP);
    }

    /**
     * Use the vertical {@link #UNRELATED_GAP} and horizontal {@link #RELATED_GAP} for the next component.
     * @return this builder
     */
    public GridBagBuilder unrelatedVerticalGap() {
        verticalGap = UNRELATED_GAP;
        return relatedGap();
    }

    /**
     * Set the insets for the next component.
     * @return this builder
     */
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

    /**
     * Append a labeled component to the form.
     * @param labelKey the resource key for the label
     * @param field the component
     * @param <T> the class of the component
     * @return the component
     */
    public <T extends JComponent> T append(String labelKey, T field) {
        return append(labelKey, field, getConstraints(field.getClass()));
    }

    /**
     * Append a labeled component to the form.
     * @param labelKey the resource key for the label
     * @param field the component
     * @param <T> the class of the component
     * @param formula the constraints calculator for the component
     * @return the component
     */
    public <T extends JComponent> T append(String labelKey, T field, GridBagFormula formula) {
        relatedGap();
        lastLabel = new LabelBuilder().mnemonicAndName(bundle.getString(resourcePrefix+labelKey)).forComponent(field).get();
        container.add(lastLabel, formula.getLabelConstraints().setConstraints(gbc));
        relatedGap();
        nextCell();
        return append(field, formula);
    }

    /**
     * Append an unlabeled component to the form.
     * @param field the component
     * @param <T> the class of the component
     * @return the component
     */
    public <T extends JComponent> T append(T field) {
        return append(field, getConstraints(field.getClass()));
    }

    /**
     * Append an unlabeled component to the form.
     * @param field the component
     * @param formula the constraints calculator for the component
     * @param <T> the class of the component
     * @return the component
     */
    public  <T extends JComponent> T append(T field, GridBagFormula formula) {
        if (useScrollPane.stream().anyMatch(clazz -> clazz.isInstance(field))) {
            container.add(new JScrollPane(field), formula.setConstraints(gbc));
        }
        else {
            container.add(field, formula.setConstraints(gbc));
        }
        nextCell();
        return field;
    }

    /**
     * Append a group of radio buttons in a horizontal box.
     * @param buttons the buttons to append
     */
    public void append(JRadioButton ... buttons) {
        Box box = Box.createHorizontalBox();
        for (JRadioButton button : buttons) {
            box.add(button);
            box.add(Box.createHorizontalStrut(RELATED_GAP));
        }
        append(box, FormElement.BUTTON_GROUP);
    }

    /**
     * Move to the next cell.  If there are columns left in the row then moves to the next column.
     * Otherwise, moves to the first column of the next row.
     */
    public void nextCell() {
        verticalGap = RELATED_GAP;
        gbc.gridx += gbc.gridwidth;
        if (gbc.gridx >= columns) {
            gbc.gridx = 0;
            gbc.gridy++;
        }
    }
}