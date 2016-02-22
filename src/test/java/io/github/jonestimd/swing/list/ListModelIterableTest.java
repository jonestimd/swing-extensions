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
package io.github.jonestimd.swing.list;

import java.util.List;

import javax.swing.DefaultListModel;

import io.github.jonestimd.util.Streams;
import org.junit.Test;

import static org.fest.assertions.Assertions.*;

public class ListModelIterableTest {
    @Test
    public void readModelItems() throws Exception {
        DefaultListModel<String> model = new DefaultListModel<>();
        model.addElement("a");
        model.addElement("b");

        List<Object> list = Streams.toList(new ListModelIterable<>(model));

        assertThat(list).containsExactly("a", "b");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void removeThrowsUnsupportedOperationException() throws Exception {
        DefaultListModel<String> model = new DefaultListModel<>();
        model.addElement("a");

        new ListModelIterable<>(model).iterator().remove();
    }
}