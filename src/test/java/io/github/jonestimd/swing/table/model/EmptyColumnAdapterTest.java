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

import org.junit.Test;

import static org.fest.assertions.Assertions.*;

public class EmptyColumnAdapterTest {
    private static final String IDENTIFIER = "dummy1";
    private final EmptyColumnAdapter<?> adapter = new EmptyColumnAdapter<>(IDENTIFIER);

    @Test
    public void getColumnIdReturnsId() throws Exception {
        assertThat(adapter.getColumnId()).isEqualTo(IDENTIFIER);
    }

    @Test
    public void getResourceReturnsNull() throws Exception {
        assertThat(adapter.getResource(null, "x")).isNull();
    }

    @Test
    public void getNameReturnsBlank() throws Exception {
        assertThat(adapter.getName()).isEqualTo(" ");
    }

    @Test
    public void getTypeReturnsString() throws Exception {
        assertThat(adapter.getType()).isEqualTo(String.class);
    }

    @Test
    public void getValueReturnsNull() throws Exception {
        assertThat(adapter.getValue(null)).isNull();
    }

    @Test
    public void isEditableReturnsFalse() throws Exception {
        assertThat(adapter.isEditable(null)).isFalse();
    }

    @Test
    public void setValueDoesNothing() throws Exception {
        adapter.setValue(null, null);
    }
}