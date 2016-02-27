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
package io.github.jonestimd.swing.table.model;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class TestColumnAdapter<Bean, Value> implements ColumnAdapter<Bean, Value> {
    private final String name;
    private final String columnId;
    private final Class<Value> type;
    private final Function<Bean, Value> getter;
    private final BiConsumer<Bean, Value> setter;

    public TestColumnAdapter(String name, Class<Value> type, Function<Bean, Value> getter) {
        this(name, type, getter, null);
    }

    public TestColumnAdapter(String name, Class<Value> type, Function<Bean, Value> getter, BiConsumer<Bean, Value> setter) {
        this(name, name, type, getter, setter);
    }

    public TestColumnAdapter(String name, String columnId, Class<Value> type, Function<Bean, Value> getter, BiConsumer<Bean, Value> setter) {
        this.name = name;
        this.columnId = columnId;
        this.type = type;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public String getColumnId() {
        return columnId;
    }

    @Override
    public String getResource(String name, String defaultValue) {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<? super Value> getType() {
        return type;
    }

    @Override
    public boolean isEditable(Bean row) {
        return setter != null;
    }

    @Override
    public Value getValue(Bean bean) {
        return getter.apply(bean);
    }

    @Override
    public void setValue(Bean bean, Value value) {
        setter.accept(bean, value);
    }
}
