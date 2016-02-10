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

import javax.swing.table.TableModel;

import io.github.jonestimd.swing.table.MixedRowTable;

/**
 * This interface defines the data model API for a {@link MixedRowTable}.  The rows are arranged in groups.
 * Each group contains one or more sub-rows in any combination of sub-row types.  All sub-row types must
 * have the same number of columns.
 */
public interface MixedRowTableModel extends TableModel, ColumnIdentifier {
    /**
     * @return thw number of row types defined by this model.
     */
    int getRowTypeCount();

    /**
     * @param rowIndex the index of the row.
     * @return the index of the group containing the row.
     */
    int getGroupNumber(int rowIndex);

    /**
     * @param groupNumber the index of the group.
     * @return the index of the first row of the group.
     */
    int getLeadRowForGroup(int groupNumber);

    /**
     * @param groupNumber the group index.
     * @return the number of rows in the group.
     */
    int getRowCount(int groupNumber);

    /**
     * @param rowIndex the index of the row.
     * @return the index of the row within its group.
     */
    int getSubRowIndex(int rowIndex);

    /**
     * @param rowIndex index of the row.
     * @return the type index of the row.
     */
    int getRowTypeIndex(int rowIndex);

    /**
     * @param typeIndex the row type index.
     * @param columnIndex the column index.
     * @return the {@link Class} of the column value.
     */
    Class<?> getColumnClass(int typeIndex, int columnIndex);

    /**
     * @param typeIndex the row type index.
     * @param columnIndex the column index.
     * @return the header text for the column.
     */
    String getColumnName(int typeIndex, int columnIndex);

    /**
     * @param typeIndex the row type index.
     * @param columnIndex the column index.
     * @return the {@link javax.swing.table.TableColumn} identifier of the column.
     */
    Object getColumnIdentifier(int typeIndex, int columnIndex);

    /**
     * @param rowIndex the index of the row.
     * @param columnIndex the index of the column.
     * @return the {@link Class} of the cell value.
     */
    Class<?> getCellClass(int rowIndex, int columnIndex);
}