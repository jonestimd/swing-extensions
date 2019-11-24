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

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;

import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import io.github.jonestimd.swing.ClientProperty;
import io.github.jonestimd.swing.SettingsPersister;

// TODO allow user to change auto resize mode
// TODO save/restore column widths for maximized & normal
/**
 * Provides improved column resizing and handles save/restore of column sizes and ordering.
 * <h2>Column Sizing</h2>
 * <p>if {@link ColumnConfiguration#getWidth(TableColumn)} returns a {@code non-null} value then the column
 * is initialized to that width.  Otherwise, the width is initialized to the value returned by
 * {@link ColumnWidthCalculator#preferredWidth(TableColumn)}.</p>
 * <p>If {@link ColumnWidthCalculator#isFixedWidth(TableColumn)} returns {@code true} then the column's min, max and
 * preferred widths are all initialized to the same value so that the column's size will not change when other columns
 * are resized by the user.  This class provides special handling for resizing a column with only fixed width columns
 * to the right.  In that case, the right most column will adjust to compensate for the changing column's new size.
 * This class does not prevent the user from explicitly changing the size of a column, including fixed width columns.</p>
 * <p>If {@link ColumnWidthCalculator#isFixedWidth(TableColumn)} returns {@code false} for a column then only the preferred
 * width is initialized using the saved width or the calculated preferred width.</p>
 * <h2>Saved Settings</h2>
 * <p>This class delegates to {@link ColumnConfiguration} for saving/loading the column widths and ordering.</p>
 * @see ColumnConfiguration
 * @see ColumnWidthCalculator
 */
public class ColumnResizeHandler implements SettingsPersister {
    private static final int DEFAULT_MIN_WIDTH = new TableColumn().getMinWidth();
    private static final int DEFAULT_MAX_WIDTH = new TableColumn().getMaxWidth();

    private final JTable table;
    private final ColumnConfiguration configuration;
    private final ColumnWidthCalculator widthCalculator;
    private TableColumn resizingColumn;
    private TableColumn lastColumn;
    private final PropertyChangeListener modelChangeHandler = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            table.setPreferredScrollableViewportSize(
                    new Dimension(initializePreferredWidths(table.getColumnModel()), table.getPreferredScrollableViewportSize().height));
            initializeColumnOrder(table.getColumnModel());
        }
    };

    public ColumnResizeHandler(JTable table, ColumnConfiguration configuration, ColumnWidthCalculator widthCalculator) {
        this.table = table;
        this.configuration = configuration;
        this.widthCalculator = widthCalculator;
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
        Enumeration<TableColumn> enumeration = columnModel.getColumns();
        List<TableColumn> columns = new ArrayList<>(columnModel.getColumnCount());
        while(enumeration.hasMoreElements()) {
            columns.add(enumeration.nextElement());
        }
        for (TableColumn column : columns) {
            int index = indexOf(column);
            int configIndex = configuration.getIndex(column);
            if (configIndex >= 0 && configIndex < columnModel.getColumnCount() && configIndex != index) {
                columnModel.moveColumn(index, configIndex);
            }
        }
    }

    private int indexOf(TableColumn column) {
        Enumeration<TableColumn> enumeration = table.getColumnModel().getColumns();
        for (int i = 0; enumeration.hasMoreElements(); i++) {
            if (enumeration.nextElement() == column) return i;
        }
        throw new NoSuchElementException();
    }

    private void setPreferredWidth(TableColumn column) {
        Integer width = configuration.getWidth(column);
        if (width == null) {
            width = widthCalculator.preferredWidth(column);
        }
        if (widthCalculator.isFixedWidth(column)) {
            TableInitializer.setFixedWidth(column, width);
        }
        else {
            column.setPreferredWidth(width);
        }
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
            configuration.setWidth(column, column.getWidth());
            configuration.setIndex(column, i);
        }
    }

    private boolean areTrailingColumnsFixed(TableColumn column) {
        TableColumnModel columnModel = table.getColumnModel();
        int index = indexOf(column)+1;
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
                TableInitializer.setFixedWidth(resizingColumn, resizingColumn.getWidth());
            }
            if (lastColumn != null) {
                TableInitializer.setFixedWidth(lastColumn, lastColumn.getWidth());
            }
        }
    }
}