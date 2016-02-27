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

import java.text.Format;
import java.text.NumberFormat;
import java.util.Random;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FunctionPropertyAdapterTest {
    public static final String NAME = "name";
    public static final String LABEL = "label";
    @Mock
    private Supplier<Integer> getter;
    @Mock
    private Supplier<Format> formatFactor;

    private FunctionPropertyAdapter<Integer> adapter;

    @Before
    public void createAdapter() {
        adapter = new FunctionPropertyAdapter<>(NAME, LABEL, getter, formatFactor);
    }

    @Test
    public void getName() throws Exception {
        assertThat(adapter.getName()).isEqualTo(NAME);
    }

    @Test
    public void getLabel() throws Exception {
        assertThat(adapter.getLabel()).isEqualTo(LABEL);
    }

    @Test
    public void getValueCallsGetter() throws Exception {
        int value = new Random().nextInt();
        when(getter.get()).thenReturn(value);

        assertThat(adapter.getValue()).isEqualTo(value);

        verify(getter).get();
    }

    @Test
    public void getFormatCallsFactory() throws Exception {
        Format format = NumberFormat.getInstance();
        when(formatFactor.get()).thenReturn(format);

        assertThat(adapter.getFormat()).isSameAs(format);

        verify(formatFactor).get();
    }
}