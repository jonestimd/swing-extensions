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

import java.util.List;

import io.github.jonestimd.swing.table.SectionTable;

/**
 * Table model interface for a {@link SectionTable}
 */
public interface SectionTableModel<T> extends BeanTableModel<T>, ColumnIdentifier {
    /**
     * @return true of the row is a group header.
     */
    boolean isSectionRow(int rowIndex);

    /**
     * @return the group number of the specified row.
     */
    int getGroupNumber(int rowIndex);

    /**
     * @return the index of the group header row for the specified row.
     */
    int getSectionRow(int rowIndex);

    /**
     * @return the display text for the header row of the group containing the specified row.
     */
    String getSectionName(int rowIndex);

    /**
     * @return the beans in the specified group.
     */
    List<T> getGroup(int groupNumber);
}
