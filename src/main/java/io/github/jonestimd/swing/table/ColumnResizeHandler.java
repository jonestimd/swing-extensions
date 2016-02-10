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
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import io.github.jonestimd.collection.MapBuilder;
import io.github.jonestimd.swing.ClientProperty;
import io.github.jonestimd.swing.SettingsPersister;
import io.github.jonestimd.swing.table.model.ColumnAdapter;

// TODO allow user to change auto resize mode
// TODO save/restore column widths for maximized & normal
// TODO save columns when TransactionTable model changes
public class ColumnResizeHandler implements SettingsPersister {
    private static final String WIDTH_SUFFIX = ".width";
    private static final String INDEX_SUFFIX = ".index";
    private static final String PROTOTYPE_SUFFIX = ".prototype";
    private static final int DEFAULT_MIN_WIDTH = new TableColumn().getMinWidth();
    private static final int DEFAULT_MAX_WIDTH = new TableColumn().getMaxWidth();

    private final WidthCalculator headerWidthCalculator = new WidthCalculator() {
        protected Object getPrototypeValue(TableColumn column) {
            return column.getHeaderValue();
        }
    };

    private final Map<Class<?>, WidthCalculator> calculatorMap = new MapBuilder<Class<?>, WidthCalculator>()
            .put(Boolean.class, headerWidthCalculator)
            .put(Number.class, new NumberWidthCalculator())
            .put(Enum.class, new EnumWidthCalculator()).get();

    private JTable table;
    private TableColumn resizingColumn;
    private TableColumn lastColumn;
    private String settingsPrefix;
    private final PropertyChangeListener modelChangeHandler = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            settingsPrefix = table.getModel().getClass().getSimpleName() + ".";
            table.setPreferredScrollableViewportSize(
                    new Dimension(initializePreferredWidths(table.getColumnModel()), table.getPreferredScrollableViewportSize().height));
            initializeColumnOrder(table.getColumnModel());
        }
    };

    public ColumnResizeHandler(JTable table) {
        this.table = table;
        table.putClientProperty(ClientProperty.SETTINGS_PERSISTER, this);
        table.getTableHeader().addMouseListener(new HeaderMouseListener());
        table.addPropertyChangeListener("model", modelChangeHandler);
        modelChangeHandler.propertyChange(null);
    }

    private int initializePreferredWidths(TableColumnModel columnModel) {
        int tableWidth = 0;
        for (int i=0; i < table.getColumnCount(); i++) {
            setPreferredWidth(columnModel.getColumn(i));
            tableWidth += columnModel.getColumn(i).getPreferredWidth();
        }
        return tableWidth;
    }

    private void initializeColumnOrder(TableColumnModel columnModel) {
        Enumeration<TableColumn> columns = columnModel.getColumns();
        while (columns.hasMoreElements()) {
            TableColumn column = columns.nextElement();
            int oldIndex = columnModel.getColumnIndex(column.getIdentifier());
            int index = Integer.getInteger(getPropertyName(column, INDEX_SUFFIX), -1);
            if (index >= 0 && index < columnModel.getColumnCount() && index != oldIndex) {
                columnModel.moveColumn(oldIndex, index);
            }
        }
    }

    private void setPreferredWidth(TableColumn column) {
        int width = Integer.getInteger(getPropertyName(column, WIDTH_SUFFIX), -1);
        boolean restoreWidth = width >= column.getMinWidth();
        WidthCalculator widthCalculator = getWidthCalculator(column);
        if (widthCalculator != null) {
            setFixedWidth(column, restoreWidth ? width : widthCalculator.calculateWidth(column));
        }
        else if (restoreWidth) {
            column.setPreferredWidth(width);
        }
        else {
            column.setPreferredWidth(Math.max(column.getPreferredWidth(), headerWidthCalculator.calculateWidth(column)));
        }
    }

    private WidthCalculator getWidthCalculator(TableColumn column) {
        Class<?> columnClass = table.getColumnClass(column.getModelIndex());
        for (Entry<Class<?>, WidthCalculator> entry : calculatorMap.entrySet()) {
            if (entry.getKey().isAssignableFrom(columnClass)) return entry.getValue();
        }
        return null;
    }

    private void setFixedWidth(TableColumn column, int width) {
        column.setMinWidth(width);
        column.setPreferredWidth(width);
        column.setMaxWidth(width);
    }

    private void unlockWidth(TableColumn column) {
        column.setMinWidth(DEFAULT_MIN_WIDTH);
        column.setMaxWidth(DEFAULT_MAX_WIDTH);
    }

    private boolean isFixedWidth(TableColumn column) {
        return column.getMinWidth() == column.getMaxWidth();
    }

    public void saveSettings() {
        TableColumnModel columnModel = table.getColumnModel();
        for (int i = 0; i < table.getColumnCount(); i++) {
            TableColumn column = columnModel.getColumn(i);
            System.setProperty(getPropertyName(column, WIDTH_SUFFIX), Integer.toString(column.getWidth()));
            System.setProperty(getPropertyName(column, INDEX_SUFFIX), Integer.toString(i));
        }
    }

    private String getPropertyName(TableColumn column, String suffix) {
        return settingsPrefix + ((ColumnAdapter<?,?>) column.getIdentifier()).getColumnId() + suffix;
    }

    private boolean areTrailingColumnsFixed(TableColumn column) {
        TableColumnModel columnModel = table.getColumnModel();
        int index = columnModel.getColumnIndex(column.getIdentifier())+1;
        boolean fixedWidths = index < columnModel.getColumnCount();
        while (fixedWidths && index < columnModel.getColumnCount()) {
            column = columnModel.getColumn(index++);
            fixedWidths = isFixedWidth(column);
        }
        return fixedWidths;
    }

    private class HeaderMouseListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            resizingColumn = table.getTableHeader().getResizingColumn();
            if (resizingColumn != null) {
                if (areTrailingColumnsFixed(resizingColumn)) {
                    lastColumn = table.getColumnModel().getColumn(table.getColumnCount()-1);
                    unlockWidth(lastColumn);
                }
                if (isFixedWidth(resizingColumn)) {
                    unlockWidth(resizingColumn);
                }
                else {
                    resizingColumn = null;
                }
            }
        }

        public void mouseReleased(MouseEvent e) {
            if (resizingColumn != null) {
                setFixedWidth(resizingColumn, resizingColumn.getWidth());
            }
            if (lastColumn != null) {
                setFixedWidth(lastColumn, lastColumn.getWidth());
            }
        }
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
            return ((ColumnAdapter<?,?>) column.getIdentifier()).getResource(PROTOTYPE_SUFFIX, column.getHeaderValue().toString());
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