// The MIT License (MIT)
//
// Copyright (c) 2018 Timothy D. Jones
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

import java.util.List;
import java.util.function.Consumer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.*;

@RunWith(MockitoJUnitRunner.class)
public class ListFieldTest {
    @Mock
    private Runnable cancelCallback;
    @Mock
    private Consumer<List<String>> commitCallback;

    @Test
    public void parseItemsReturnsSingleItem() throws Exception {
        ListField field = new ListField(cancelCallback, commitCallback);

        field.setText("one");

        assertThat(field.parseItems()).containsExactly("one");
    }

    @Test
    public void parseItemsExcludesEndingEmptyLine() throws Exception {
        ListField field = new ListField(cancelCallback, commitCallback);

        field.setText("one"+ListField.LINE_SEPARATOR+ListField.LINE_SEPARATOR);

        assertThat(field.parseItems()).containsExactly("one", "");
    }

    @Test
    public void parseItemsReturnsEmptyListForEmptyText() throws Exception {
        ListField field = new ListField(cancelCallback, commitCallback);

        field.setText("");

        assertThat(field.parseItems()).isEmpty();
    }

    @Test
    public void parseItemsReturnsSingletonListForNewLine() throws Exception {
        ListField field = new ListField(cancelCallback, commitCallback);

        field.setText(ListField.LINE_SEPARATOR);

        assertThat(field.parseItems()).containsExactly("");
    }

    @Test
    public void parseSplitsTextOnNewLine() throws Exception {
        ListField field = new ListField(cancelCallback, commitCallback);

        field.setText(ListField.LINE_SEPARATOR+ListField.LINE_SEPARATOR);

        assertThat(field.parseItems()).containsExactly("", "");
    }
}