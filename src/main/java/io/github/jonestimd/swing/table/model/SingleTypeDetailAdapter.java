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

/**
 * {@link DetailAdapter} for a {@link HeaderDetailTableModel} with only one detail type.
 * @param <H> the class representing a group bean in the table model
 */
public abstract class SingleTypeDetailAdapter<H> implements DetailAdapter<H> {
    private static final int DETAIL_TYPE_INDEX = 0;

    @Override
    public int getDetailTypeIndex(H bean, int detailIndex) {
        return DETAIL_TYPE_INDEX;
    }

    @Override
    public int getDetailCount(H bean) {
        return getDetails(bean, DETAIL_TYPE_INDEX).size();
    }

    @Override
    public Object getDetail(H bean, int detailIndex) {
        return getDetails(bean, DETAIL_TYPE_INDEX).get(detailIndex);
    }

    @Override
    public Object removeDetail(H bean, int index) {
        return getDetails(bean, DETAIL_TYPE_INDEX).remove(index);
    }

    @Override
    public void removeDetail(H bean, Object detail) {
        getDetails(bean, DETAIL_TYPE_INDEX).remove(detail);
    }

    @Override
    public int detailIndex(H header, Object detail) {
        return getDetails(header, DETAIL_TYPE_INDEX).indexOf(detail);
    }
}
