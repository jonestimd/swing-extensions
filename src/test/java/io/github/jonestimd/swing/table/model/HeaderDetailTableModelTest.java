// The MIT License (MIT)
//
// Copyright (c) 2017 Timothy D. Jones
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.function.Function;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static io.github.jonestimd.mockito.Matchers.matches;
import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HeaderDetailTableModelTest {
    public static final String COLUMN_ID = "columnId";
    @Mock
    private ColumnAdapter<Object, String> columnAdapter;
    @Mock
    private ColumnAdapter<Object, String> detailColumnAdapter;
    @Mock
    private DetailAdapter<Object> detailAdapter;
    @Mock
    private TableModelListener tableModelListener;

    private HeaderDetailTableModel<Object> model;

    @Before
    public void createModel() {
        when(columnAdapter.getColumnId()).thenReturn(COLUMN_ID);
        model = new HeaderDetailTableModel<>(detailAdapter, Function.identity(), singletonList(columnAdapter), singletonList(singletonList(detailColumnAdapter)));
        model.addTableModelListener(tableModelListener);
        when(detailAdapter.getDetailCount(any())).thenReturn(1);
    }

    @Test
    public void updateBeansAddsMissingRows() throws Exception {
        model.setBeans(singletonList(BigDecimal.ONE));
        reset(tableModelListener);

        model.updateBeans(singletonList(BigDecimal.TEN), Object::equals);

        assertThat(model.getBeanCount()).isEqualTo(2);
        assertThat(model.getBean(0)).isSameAs(BigDecimal.ONE);
        assertThat(model.getBean(1)).isSameAs(BigDecimal.TEN);
        verify(tableModelListener).tableChanged(matches(new TableModelEvent(model, 2, 3, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT)));
        verifyNoMoreInteractions(tableModelListener);
    }

    @Test
    public void updateBeansReplacesRows() throws Exception {
        model.setBeans(singletonList("one"));
        reset(tableModelListener);

        model.updateBeans(Arrays.asList("ONE", "two"), (s1, s2) -> s1.toString().equalsIgnoreCase(s2.toString()));

        assertThat(model.getBeanCount()).isEqualTo(2);
        assertThat(model.getBean(0)).isEqualTo("ONE");
        assertThat(model.getBean(1)).isEqualTo("two");
        verify(tableModelListener).tableChanged(matches(new TableModelEvent(model, 0, 1, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE)));
        verify(tableModelListener).tableChanged(matches(new TableModelEvent(model, 2, 3, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT)));
        verifyNoMoreInteractions(tableModelListener);
    }
}