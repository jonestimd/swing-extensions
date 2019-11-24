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
package io.github.jonestimd.swing.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import io.github.jonestimd.swing.table.model.BeanListTableModel;

/**
 * <p>Calculates a column's width based on the value returned by {@link JTable#getColumnClass(int)}.  Intended for use
 * with a table model that overrides {@link AbstractTableModel#getColumnClass(int)} because that method  always returns
 * {@code Object.class}. By default, the column cell renderer is used with the value specified below to determine the width.</p>
 * <table>
 *   <tr><th align="left">Column Class</th><th align="left">Renderer Value</th><th align="left">Fixed Width</th></tr>
 *   <tr><td>{@code Boolean}</td><td>Column header</td><td>{@code true}</td></tr>
 *   <tr><td>{@code Number}</td><td>Configuration prototype</td><td>{@code true}</td></tr>
 *   <tr><td>{@code Enum}</td><td>Longest {@code toString()} value</td><td>{@code true}</td></tr>
 *   <tr><td>{@code Color}</td><td>Square</td><td>{@code true}</td></tr>
 *   <tr><td>other</td><td>Column header</td><td>{@code false}</td></tr>
 * </table>
 * @see BeanListTableModel
 */
public class ValueClassColumnWidthCalculator implements ColumnWidthCalculator {
    protected final WidthCalculator headerWidthCalculator = new WidthCalculator(TableColumn::getHeaderValue);

    protected final Map<Class<?>, ToIntFunction<TableColumn>> calculatorMap = new HashMap<>();
    protected final JTable table;
    protected final ColumnConfiguration configuration;

    public ValueClassColumnWidthCalculator(JTable table, ColumnConfiguration configuration) {
        this.table = table;
        this.configuration = configuration;
        setDefaultCalculators();
    }

    protected void setDefaultCalculators() {
        setWidthCalculator(Boolean.class, headerWidthCalculator);
        setWidthCalculator(Number.class, configuration::getPrototypeValue);
        setWidthCalculator(Enum.class, new EnumWidthCalculator());
        setWidthCalculator(Color.class, column -> " ", component -> component.getPreferredSize().height);
    }

    /**
     * Set the calculator for a column value type.
     */
    public void setWidthCalculator(Class<?> valueType, ToIntFunction<TableColumn> calculator) {
        calculatorMap.put(valueType, calculator);
    }

    /**
     * Use the header width for a prototype value as the column width.
     * @param getPrototype a function that returns the prototype value for a column
     */
    public void setWidthCalculator(Class<?> valueType, Function<TableColumn, ?> getPrototype) {
        calculatorMap.put(valueType, new WidthCalculator(getPrototype));
    }

    /**
     * Calculate the column width using the header renderer for a prototype value.
     * @param getPrototype a function that returns the prototype value for a column
     * @param getWidth a function that returns the width for the column given the header renderer
     */
    public void setWidthCalculator(Class<?> valueType, Function<TableColumn, ?> getPrototype, ToIntFunction<Component> getWidth) {
        calculatorMap.put(valueType, new WidthCalculator(getPrototype, getWidth));
    }

    /**
     * The default implementation returns true if a width calculator has been set for the column.
     */
    @Override
    public boolean isFixedWidth(TableColumn column) {
        return getWidthCalculator(column) != null;
    }

    @Override
    public int preferredWidth(TableColumn column) {
        ToIntFunction<TableColumn> calculator = getWidthCalculator(column);
        return calculator != null ? calculator.applyAsInt(column) : headerWidthCalculator.applyAsInt(column);
    }

    private ToIntFunction<TableColumn> getWidthCalculator(TableColumn column) {
        Class<?> columnClass = table.getColumnClass(column.getModelIndex());
        for (Entry<Class<?>, ToIntFunction<TableColumn>> entry : calculatorMap.entrySet()) {
            if (entry.getKey().isAssignableFrom(columnClass)) return entry.getValue();
        }
        return null;
    }

    private class WidthCalculator implements ToIntFunction<TableColumn> {
        private final Function<TableColumn, ?> getPrototype;
        private final ToIntFunction<Component> getWidth;

        public WidthCalculator(Function<TableColumn, ?> getPrototype) {
            this(getPrototype, component -> component.getPreferredSize().width);
        }

        protected WidthCalculator(Function<TableColumn, ?> getPrototype, ToIntFunction<Component> getWidth) {
            this.getPrototype = getPrototype;
            this.getWidth = getWidth;
        }

        @Override
        public int applyAsInt(TableColumn column) {
            TableCellRenderer renderer = table.getTableHeader().getDefaultRenderer();
            Component rendererComponent = renderer.getTableCellRendererComponent(table, getPrototype.apply(column), false, false, -1, 0);
            Insets insets = ((JComponent) renderer).getInsets();
            return getWidth.applyAsInt(rendererComponent) + insets.left + insets.right + 2;
        }
    }

    private class EnumWidthCalculator extends WidthCalculator {
        public EnumWidthCalculator() {
            super(column -> {
                Class<?> enumClass = table.getModel().getColumnClass(column.getModelIndex());
                Enum<?>[] constants = (Enum<?>[]) enumClass.getEnumConstants();
                return Arrays.stream(constants).map(Object::toString).max(Comparator.comparing(String::length)).orElse("");
            });
        }
    }
}
