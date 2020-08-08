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
package io.github.jonestimd.swing.table;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.mockito.InOrder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CompositeTableCellRendererTest {
    @Test
    public void callsDecorators() throws Exception {
        Object[][] data = {
            {"abc", 123, 234.56},
            {"def", 456, 56.789},
        };
        String[] columns = {"First", "Second", "Third"};
        TableCellDecorator decorator1 = mockDecorator("value1");
        TableCellDecorator decorator2 = mockDecorator("value2");
        CompositeTableCellRenderer renderer = new CompositeTableCellRenderer(Lists.newArrayList(decorator1, decorator2));
        JTable table = new JTable(new DefaultTableModel(data, columns));
        boolean isSelected = false;
        boolean hasFocus = true;
        int row = 1;
        int column = 2;

        renderer.getTableCellRendererComponent(table, "value", isSelected, hasFocus, row, column);

        InOrder ordered = inOrder(decorator1, decorator2);
        ordered.verify(decorator1).configure(table, "value", isSelected, hasFocus, row, column, renderer);
        ordered.verify(decorator2).configure(table, "value1", isSelected, hasFocus, row, column, renderer);
        assertThat(renderer.getText()).isEqualTo("value2");
    }

    protected TableCellDecorator mockDecorator(String value) {
        TableCellDecorator mock = mock(TableCellDecorator.class);
        when(mock.configure(any(JTable.class), any(), anyBoolean(), anyBoolean(), anyInt(), anyInt(), any())).thenReturn(value);
        return mock;
    }
}
