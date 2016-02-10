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
package io.github.jonestimd.swing.table.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import io.github.jonestimd.util.Streams;

/**
 * Adapter class for mapping bean properties to table columns.
 * @param <T> the class representing a row in the table
 */
public class BeanTableAdapter<T> {
    private final Logger logger = Logger.getLogger(BeanTableAdapter.class.getName());

    private final AbstractTableModel tableModel;
    private final List<ColumnAdapter<? super T, ?>> columnAdapters = new ArrayList<>();
    private final List<TableDataProvider<T>> dataProviders = new LinkedList<>();

    public BeanTableAdapter(AbstractTableModel tableModel, List<? extends ColumnAdapter<T, ?>> columnAdapters) {
        this(tableModel, columnAdapters, Collections.<TableDataProvider<T>>emptyList());
    }

    public BeanTableAdapter(AbstractTableModel tableModel, List<? extends ColumnAdapter<? super T, ?>> columnAdapters, Iterable<? extends TableDataProvider<T>> dataProviders) {
        this.tableModel = tableModel;
        Streams.of(dataProviders).forEach(this.dataProviders::add);
        if (!this.dataProviders.isEmpty()) {
            DataProviderListener listener = new DataProviderListener();
            for (TableDataProvider<T> dataProvider : this.dataProviders) {
                dataProvider.addStateChangeListener(listener);
            }
        }
        setColumnAdapters(columnAdapters);
    }

    public void setColumnAdapters(Collection<? extends ColumnAdapter<? super T, ?>> columnAdapters) {
        this.columnAdapters.clear();
        this.columnAdapters.addAll(columnAdapters);
        for (TableDataProvider<T> dataProvider : dataProviders) {
            this.columnAdapters.addAll(dataProvider.getColumnAdapters());
        }
        tableModel.fireTableStructureChanged();
    }

    public ColumnAdapter<? super T, ?> getColumnAdapter(int index) {
        return columnAdapters.get(index);
    }

    public int getColumnCount() {
        return columnAdapters.size();
    }

    public String getColumnName(int columnIndex) {
        return columnAdapters.get(columnIndex).getName();
    }

    public Class<?> getColumnClass(int columnIndex) {
        return columnAdapters.get(columnIndex).getType();
    }

    public boolean isCellEditable(T bean, int columnIndex) {
        return columnAdapters.get(columnIndex).isEditable(bean);
    }

    public Object getValue(T row, int columnIndex) {
        try {
            return columnAdapters.get(columnIndex).getValue(row);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Failed to get column value: {0}", ex.getMessage());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public void setValue(Object value, T row, int rowIndex, int columnIndex) {
        ColumnAdapter<T, Object> columnAdapter = (ColumnAdapter<T, Object>) columnAdapters.get(columnIndex);
        Object oldValue = columnAdapter.getValue(row);
        columnAdapter.setValue(row, value);
        notifyDataProviders(row, rowIndex, columnAdapter.getColumnId(), oldValue);
    }

    public void setBeans(Collection<T> beans) {
        for (TableDataProvider<T> provider : dataProviders) {
            provider.setBeans(beans);
        }
    }

    public void addBean(T bean) {
        for (TableDataProvider<T> provider : dataProviders) {
            provider.addBean(bean);
        }
    }

    public void updateBean(T bean, String columnId, Object oldValue) {
        for (TableDataProvider<T> provider : dataProviders) {
            provider.updateBean(bean, columnId, oldValue);
        }
    }

    public void removeBean(T bean) {
        for (TableDataProvider<T> provider : dataProviders) {
            provider.removeBean(bean);
        }
    }

    public void notifyDataProviders(T row, int rowIndex, String columnId, Object oldValue) {
        for (TableDataProvider<T> dataProvider : dataProviders) {
            if (dataProvider.updateBean(row, columnId, oldValue)) {
                fireTableColumnsChanged(dataProvider, rowIndex, rowIndex);
            }
        }
    }

    private void fireTableColumnsChanged(TableDataProvider<T> provider, int firstRow, int lastRow) {
        for (ColumnAdapter<T, ?> columnAdapter : provider.getColumnAdapters()) {
            int column = columnAdapters.indexOf(columnAdapter);
            tableModel.fireTableChanged(new TableModelEvent(tableModel, firstRow, lastRow, column, TableModelEvent.UPDATE));
        }
    }

    private class DataProviderListener implements PropertyChangeListener {
        @Override
        @SuppressWarnings("unchecked")
        public void propertyChange(PropertyChangeEvent evt) {
            if (tableModel.getRowCount() > 0) {
                fireTableColumnsChanged((TableDataProvider<T>) evt.getSource(), 0, tableModel.getRowCount() - 1);
            }
        }
    }
}
