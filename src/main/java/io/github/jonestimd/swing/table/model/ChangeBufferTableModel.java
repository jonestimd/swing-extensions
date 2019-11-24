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
package io.github.jonestimd.swing.table.model;

import java.util.List;
import java.util.stream.Stream;

import io.github.jonestimd.swing.ChangeBuffer;

/**
 * A table model that stores changes until committed or reverted.
 */
public interface ChangeBufferTableModel<T> extends BeanTableModel<T>, ColumnIdentifier, ChangeBuffer {
    /**
     * Append an unsaved bean.
     */
    void queueAdd(T bean);

    /**
     * Insert an unsaved bean.
     */
    void queueAdd(int index, T bean);

    /**
     * @return true if the delete was queued, false if the row was an unsaved addition and was deleted immediately.
     */
    boolean queueDelete(T bean);

    /**
     * @return updated, added and deleted rows.
     */
    Stream<T> getChangedRows();

    List<T> getPendingAdds();

    List<T> getPendingDeletes();

    /**
     * @return added and changed rows.
     */
    Stream<T> getPendingUpdates();

    boolean isChangedAt(int rowIndex, int columnIndex);

    void undoChangedAt(int rowIndex, int columnIndex);

    boolean isPendingDelete(int rowIndex);

    void undoDelete(int rowIndex);
}