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

/**
 * No-op adapter for filler columns in a {@link MixedRowTableModel}.
 */
public class EmptyColumnAdapter<T, V> implements ColumnAdapter<T, V> {
    private final String identifier;
    private final Class<V> type;

    /**
     * @param identifier the column identifier
     * @param type the value type
     */
    public EmptyColumnAdapter(String identifier, Class<V> type) {
        this.identifier = identifier;
        this.type = type;
    }

    @Override
    public String getColumnId() {
        return identifier;
    }

    @Override
    public String getResource(String resourceId, String defaultValue) {
        return defaultValue;
    }

    @Override
    public String getName() {
        return " ";
    }

    @Override
    public Class<V> getType() {
        return type;
    }

    @Override
    public V getValue(T row) {
        return null;
    }

    @Override
    public boolean isEditable(T row) {
        return false;
    }

    @Override
    public void setValue(T row, V value) {
    }
}