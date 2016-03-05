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
package io.github.jonestimd.swing.table;

import java.awt.Component;
import java.awt.Insets;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import io.github.jonestimd.collection.MapBuilder;
import io.github.jonestimd.swing.table.model.BeanListTableModel;

/**
 * <p>Calculates a column's width based on the value returned by {@link JTable#getColumnClass(int)}.  Intended for use
 * with a table model that overrides {@link AbstractTableModel#getColumnClass(int)} because that method  always returns
 * {@code Object.class}.  The column cell renderer is used with the value specified below to determine the width.</p>
 * <table>
 *   <tr><th align="left">Column Class</th><th align="left">Renderer Value</th><th align="left">Fixed Width</th></tr>
 *   <tr><td>{@code Boolean}</td><td>Column header</td><td>{@code true}</td></tr>
 *   <tr><td>{@code Number}</td><td>Configuration prototype</td><td>{@code true}</td></tr>
 *   <tr><td>{@code Enum}</td><td>Longest {@code toString()} value</td><td>{@code true}</td></tr>
 *   <tr><td>other</td><td>Column header</td><td>{@code false}</td></tr>
 * </table>
 * @see BeanListTableModel
 */
public class ValueClassColumnWidthCalculator implements ColumnWidthCalculator {
    private final WidthCalculator headerWidthCalculator = new WidthCalculator() {
        protected Object getPrototypeValue(TableColumn column) {
            return column.getHeaderValue();
        }
    };

    private final Map<Class<?>, WidthCalculator> calculatorMap = new MapBuilder<Class<?>, WidthCalculator>()
            .put(Boolean.class, headerWidthCalculator)
            .put(Number.class, new NumberWidthCalculator())
            .put(Enum.class, new EnumWidthCalculator()).get();
    private final JTable table;
    private final ColumnConfiguration configuration;

    public ValueClassColumnWidthCalculator(JTable table, ColumnConfiguration configuration) {
        this.table = table;
        this.configuration = configuration;
    }

    @Override
    public boolean isFixedWidth(TableColumn column) {
        return getWidthCalculator(column) != null;
    }

    @Override
    public int preferredWidth(TableColumn column) {
        WidthCalculator calculator = getWidthCalculator(column);
        return calculator != null ? calculator.calculateWidth(column) : headerWidthCalculator.calculateWidth(column);
    }

    private WidthCalculator getWidthCalculator(TableColumn column) {
        Class<?> columnClass = table.getColumnClass(column.getModelIndex());
        for (Entry<Class<?>, WidthCalculator> entry : calculatorMap.entrySet()) {
            if (entry.getKey().isAssignableFrom(columnClass)) return entry.getValue();
        }
        return null;
    }

    private abstract class WidthCalculator {
        public int calculateWidth(TableColumn column) {
            TableCellRenderer renderer = table.getTableHeader().getDefaultRenderer();
            Component rendererComponent = renderer.getTableCellRendererComponent(table, getPrototypeValue(column), false, false, -1, 0);
            Insets insets = ((JComponent)renderer).getInsets();
            return rendererComponent.getPreferredSize().width + insets.left + insets.right + 2;
        }

        protected abstract Object getPrototypeValue(TableColumn column);
    }

    private class NumberWidthCalculator extends WidthCalculator {
        protected Object getPrototypeValue(TableColumn column) {
            return configuration.getPrototypeValue(column);
        }
    }

    private class EnumWidthCalculator extends WidthCalculator {
        protected Object getPrototypeValue(TableColumn column) {
            Class<?> enumClass = table.getModel().getColumnClass(column.getModelIndex());
            Enum<?>[] constants = (Enum<?>[]) enumClass.getEnumConstants();
            String longestValue = "";
            for (Enum<?> value : constants) {
                if (longestValue.length() < value.toString().length()) longestValue = value.toString();
            }
            return longestValue;
        }
    }
}
