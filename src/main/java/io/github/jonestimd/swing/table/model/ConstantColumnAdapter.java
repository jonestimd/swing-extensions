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

import java.util.ResourceBundle;

/**
 * Bean adapter for a table column that is always editable or always read-only.
 */
public abstract class ConstantColumnAdapter<Bean, Value> extends AbstractColumnAdapter<Bean, Value> {
    private final Class<? super Value> valueType;
    private final boolean editable;

    protected ConstantColumnAdapter(ResourceBundle bundle, String tableResourcePrefix, String columnId,
            Class<? super Value> valueType, boolean editable) {
        super(bundle, tableResourcePrefix, columnId);
        this.valueType = valueType;
        this.editable = editable;
    }

    @Override
    public Class<? super Value> getType() {
        return valueType;
    }

    @Override
    public boolean isEditable(Bean row) {
        return editable;
    }
}