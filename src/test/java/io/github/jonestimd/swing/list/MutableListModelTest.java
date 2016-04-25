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

import java.util.Arrays;
import java.util.Iterator;

import org.junit.Test;

import static org.fest.assertions.Assertions.*;

public class MutableListModelTest {
    @Test
    public void addElementAppendsToTheList() throws Exception {
        MutableListModel<String> model = new MutableListModel<>();

        model.addElement("a");
        model.addElement("b");

        assertThat(model.getSize()).isEqualTo(2);
        assertThat(model.getElementAt(0)).isEqualTo("a");
        assertThat(model.getElementAt(1)).isEqualTo("b");
    }

    @Test
    public void removeElement() throws Exception {
        MutableListModel<String> model = new MutableListModel<>();
        model.setElements(Arrays.asList("a", "b"));

        model.removeElement(0);

        assertThat(model.getSize()).isEqualTo(1);
        assertThat(model.getElementAt(0)).isEqualTo("b");
    }

    @Test
    public void setElementsReplacesTheListItems() throws Exception {
        MutableListModel<String> model = new MutableListModel<>();

        model.setElements(Arrays.asList("a", "b"));

        assertThat(model.getSize()).isEqualTo(2);
        assertThat(model.getElementAt(0)).isEqualTo("a");
        assertThat(model.getElementAt(1)).isEqualTo("b");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void iteratorIsReadOnly() throws Exception {
        MutableListModel<String> model = new MutableListModel<>();
        model.addElement("one");
        model.addElement("two");

        Iterator<String> iterator = model.iterator();
        iterator.remove();
    }
}