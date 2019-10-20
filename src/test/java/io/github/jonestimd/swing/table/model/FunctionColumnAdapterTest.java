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

import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FunctionColumnAdapterTest {
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("test-resources");
    @Mock
    private Function<FunctionColumnAdapterTest, String> getter;
    @Mock
    private BiConsumer<FunctionColumnAdapterTest, String> setter;

    private FunctionColumnAdapter<FunctionColumnAdapterTest, String> adapter;

    @Before
    public void createAdapter() throws Exception {
        adapter = new FunctionColumnAdapter<>(BUNDLE, "functionColumnAdapterTest.", "column1", String.class, getter, setter);
    }

    @Test
    public void getValueCallsGetter() throws Exception {
        final String value = "value";
        when(getter.apply(this)).thenReturn(value);

        assertThat(adapter.getValue(this)).isSameAs(value);
    }

    @Test
    public void setValueCallsSetter() throws Exception {
        final String value = "value";

        adapter.setValue(this, value);

        verify(setter).accept(this, value);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void nullSetterThrowsUnsupportedOperationException() throws Exception {
        adapter = new FunctionColumnAdapter<>(BUNDLE, "functionColumnAdapterTest.", "column1", String.class, getter, null);

        adapter.setValue(this, "x");
    }
}