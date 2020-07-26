// The MIT License (MIT)
//
// Copyright (c) 2020 Timothy D. Jones
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
package io.github.jonestimd.swing.component;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.junit.Test;
import org.mockito.internal.stubbing.answers.ReturnsArgumentAt;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ContainsFilterComboBoxModelTest {
    private static final List<String> items = Arrays.asList(
        "Apple","Banana","Blueberry","cherry","Grape","Peach","Pineapple","Raspberry"
    );

    @SuppressWarnings("unchecked")
    private Function<String, String> format = mock(Function.class, new ReturnsArgumentAt(0));

    private ContainsFilterComboBoxModel<String> model = new ContainsFilterComboBoxModel<>(items, format);

    @Test
    public void applyFilterIgnoresCase() throws Exception {
        model.applyFilter("berry");

        items.forEach(item -> verify(format).apply(item));
        assertThat(model.getSize()).isEqualTo(2);
        assertThat(model.getElementAt(0)).isEqualTo("Blueberry");
        assertThat(model.getElementAt(1)).isEqualTo("Raspberry");
    }

    @Test
    public void formatItemUsesFormat() throws Exception {
        model.formatItem("Xyz");

        verify(format).apply("Xyz");
    }
}