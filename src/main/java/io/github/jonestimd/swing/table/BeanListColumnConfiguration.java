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

import javax.swing.table.TableColumn;

import io.github.jonestimd.swing.table.model.BeanListTableModel;
import io.github.jonestimd.swing.table.model.ColumnAdapter;

/**
 * Provides column configuration for a table that uses {@link BeanListTableModel}.  The configuration values are
 * saved/loaded to System properties.  The property names are derived from the class name of the table model and
 * the value returned by {@link ColumnAdapter#getColumnId()}.  Column prototype values are retrieved using
 * {@link ColumnAdapter#getResource(String, String)} with {@link #PROTOTYPE_SUFFIX} as the resource name and the
 * column header value as the default.
 * @see DecoratedTable
 */
public class BeanListColumnConfiguration implements ColumnConfiguration {
    public static final String PROTOTYPE_SUFFIX = ".prototype";
    private enum Setting {
        WIDTH, INDEX;

        public final String keySuffix = "." + name().toLowerCase();
    }
    private final DecoratedTable<?, ?> table;

    public BeanListColumnConfiguration(DecoratedTable<?, ?> table) {
        this.table = table;
    }

    private String settingsPrefix() {
        return table.getModel().getClass().getSimpleName() + ".";
    }

    @Override
    public Integer getWidth(TableColumn column) {
        return Integer.getInteger(getSettingKey(column, Setting.WIDTH));
    }

    @Override
    public void setWidth(TableColumn column, int width) {
        System.setProperty(getSettingKey(column, Setting.WIDTH), Integer.toString(width));
    }

    @Override
    public int getIndex(TableColumn column) {
        return Integer.getInteger(getSettingKey(column, Setting.INDEX), -1);
    }

    @Override
    public void setIndex(TableColumn column, int index) {
        System.setProperty(getSettingKey(column, Setting.INDEX), Integer.toString(index));
    }

    private String getSettingKey(TableColumn column, Setting setting) {
        return settingsPrefix() + ((ColumnAdapter<?,?>) column.getIdentifier()).getColumnId() + setting.keySuffix;
    }

    @Override
    public Object getPrototypeValue(TableColumn column) {
        return ((ColumnAdapter<?,?>) column.getIdentifier()).getResource(PROTOTYPE_SUFFIX, column.getHeaderValue().toString());
    }
}
