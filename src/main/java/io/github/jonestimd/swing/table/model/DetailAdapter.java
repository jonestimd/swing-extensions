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

/**
 * Interface for retrieving detail rows for a group bean in a {@link HeaderDetailTableModel}.
 * @param <T> the class representing a group bean in the table model
 */
public interface DetailAdapter<T> {
    /**
     * @return the number of details for the {@code group}.
     */
    int getDetailCount(T group);

    /**
     * @return the details of the specified type for the {@code group}.
     */
    List<?> getDetails(T group, int subRowTypeIndex);

    /**
     * @return the detail at the specified index.
     */
    Object getDetail(T group, int detailIndex);

    /**
     * @return the index of the detail within the {@code group}.
     */
    int detailIndex(T group, Object detail);

    /**
     * @return the type of the detail at the specified index.
     */
    int getDetailTypeIndex(T group, int detailIndex);

    /**
     * Appendd an empty detail to the group.
     * @return the new detail count.
     */
    int appendDetail(T group);

    /**
     * Remove a detail from a group.
     * @return the deleted detail.
     */
    Object removeDetail(T group, int index);

    /**
     * Remove a detail from a group.
     */
    void removeDetail(T group, Object detail);
}
