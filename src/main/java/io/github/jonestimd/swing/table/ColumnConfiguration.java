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

/**
 * Provides table column configuration.
 */
public interface ColumnConfiguration {
    /**
     * Get the saved width for the table column.
     * @param column the table column
     * @return the saved width or {@code null} if no width has been saved
     */
    Integer getWidth(TableColumn column);

    /**
     * Get the saved index for the table column.
     * @param column the table column
     * @return the saved index or {@code -1} if no index has been saved
     */
    int getIndex(TableColumn column);

    /**
     * Set the saved width for a table column setting.
     * @param column the table column
     * @param width the width to save
     */
    void setWidth(TableColumn column, int width);

    /**
     * Set the saved index for a table column setting.
     * @param column the table column
     * @param index the index to save
     */
    void setIndex(TableColumn column, int index);

    /**
     * Get the prototype value to use for calculating the column width.
     */
    default Object getPrototypeValue(TableColumn column) {
        return column.getHeaderValue().toString();
    }
}
