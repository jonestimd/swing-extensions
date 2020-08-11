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

import java.util.Collections;
import java.util.List;

import javax.swing.JTable;

import io.github.jonestimd.swing.table.model.BufferedBeanListTableModel;
import io.github.jonestimd.swing.table.model.ValidatedBeanListTableModel;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class TableDecoratorTest {
    @Test
    public void defaultDecorators_ChangeBufferTableModel() throws Exception {
        BufferedBeanListTableModel<Object> model = new BufferedBeanListTableModel<>();
        JTable table = new JTable(model);

        List<TableDecorator> decorators = TableDecorator.defaultDecorators(table);

        assertThat(decorators).hasSize(1);
        assertThat(decorators.get(0)).isInstanceOf(UnsavedChangeDecorator.class);
    }

    @Test
    public void defaultDecorators_ValidatedTableModel() throws Exception {
        ValidatedBeanListTableModel<Object> model = new ValidatedBeanListTableModel<>(Collections.emptyList());
        JTable table = new JTable(model);

        List<TableDecorator> decorators = TableDecorator.defaultDecorators(table);

        assertThat(decorators).hasSize(3);
        assertThat(decorators.get(0)).isInstanceOf(UnsavedChangeDecorator.class);
        assertThat(decorators.get(1)).isInstanceOf(ValidationDecorator.class);
        assertThat(decorators.get(2)).isInstanceOf(ClearButtonTableDecorator.class);
    }
}