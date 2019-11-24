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
package io.github.jonestimd.swing.table;

import java.math.BigDecimal;
import java.util.Date;
import java.util.function.Supplier;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.github.jonestimd.swing.table.model.BeanListTableModel;
import io.github.jonestimd.swing.table.model.BufferedBeanListTableModel;
import io.github.jonestimd.swing.table.model.ColumnAdapter;
import io.github.jonestimd.swing.table.model.HeaderDetailTableModel;
import io.github.jonestimd.swing.table.model.ValidatedBeanListTableModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TableInitializerTest {
    @Mock
    private ColumnAdapter<Object, Date> dateColumnAdapter;
    @Mock
    private ColumnAdapter<Object, BigDecimal> numberColumnAdapter;

    @Before
    public void createModel() throws Exception {
        doReturn(Date.class).when(dateColumnAdapter).getType();
        when(dateColumnAdapter.getName()).thenReturn("Date");
        when(dateColumnAdapter.getResource(".renderer", null)).thenReturn("date renderer");
        when(dateColumnAdapter.getResource(".editor", null)).thenReturn("date editor");
        doReturn(BigDecimal.class).when(numberColumnAdapter).getType();
        when(numberColumnAdapter.getName()).thenReturn("Number");
        when(numberColumnAdapter.getResource(".renderer", null)).thenReturn("number renderer");
    }

    @Test
    public void setsDefaultTableCellRenderers() throws Exception {
        BeanListTableModel<Object> tableModel = new BeanListTableModel<>(ImmutableList.of(dateColumnAdapter, numberColumnAdapter));
        TableCellRenderer dateRenderer = mock(TableCellRenderer.class);
        ImmutableMap<Class<?>, TableCellRenderer> cellRenderers = ImmutableMap.of(Date.class, dateRenderer);
        TableInitializer initializer = new TableInitializer(cellRenderers, emptyMap(), null, null);
        DecoratedTable<Object, BeanListTableModel<Object>> table = new DecoratedTable<>(tableModel);

        initializer.initialize(table);

        assertThat(table.getDefaultRenderer(Date.class)).isSameAs(dateRenderer);
    }

    @Test
    public void setsTableColumnCellRenderers() throws Exception {
        BeanListTableModel<Object> tableModel = new BeanListTableModel<>(ImmutableList.of(dateColumnAdapter, numberColumnAdapter));
        TableCellRenderer dateRenderer = mock(TableCellRenderer.class);
        ImmutableMap<String, TableCellRenderer> cellRenderers = ImmutableMap.of("date renderer", dateRenderer);
        TableInitializer initializer = new TableInitializer(emptyMap(), emptyMap(), cellRenderers, emptyMap());
        DecoratedTable<Object, BeanListTableModel<Object>> table = new DecoratedTable<>(tableModel);

        initializer.initialize(table);

        assertThat(table.getColumn(dateColumnAdapter).getCellRenderer()).isSameAs(dateRenderer);
    }

    @Test
    public void keepsExistingColumnCellRenderers() throws Exception {
        BeanListTableModel<Object> tableModel = new BeanListTableModel<>(ImmutableList.of(dateColumnAdapter, numberColumnAdapter));
        ImmutableMap<String, TableCellRenderer> cellRenderers = ImmutableMap.of("date renderer", mock(TableCellRenderer.class));
        TableInitializer initializer = new TableInitializer(emptyMap(), emptyMap(), cellRenderers, emptyMap());
        DecoratedTable<Object, BeanListTableModel<Object>> table = new DecoratedTable<>(tableModel);
        TableCellRenderer existingRenderer = mock(TableCellRenderer.class);
        table.getColumn(dateColumnAdapter).setCellRenderer(existingRenderer);

        initializer.initialize(table);

        assertThat(table.getColumn(dateColumnAdapter).getCellRenderer()).isSameAs(existingRenderer);
    }

    @Test
    public void setsTableColumnCellRenderersOnMixedColumn() throws Exception {
        HeaderDetailTableModel<Object> tableModel = new HeaderDetailTableModel<>(null, null, ImmutableList.of(numberColumnAdapter),
                ImmutableList.of(ImmutableList.of(dateColumnAdapter)));
        TableCellRenderer dateRenderer = mock(TableCellRenderer.class);
        ImmutableMap<String, TableCellRenderer> cellRenderers = ImmutableMap.of("date renderer", dateRenderer);
        TableInitializer initializer = new TableInitializer(emptyMap(), emptyMap(), cellRenderers, emptyMap());
        MixedRowTable<Object, HeaderDetailTableModel<Object>> table = new MixedRowTable<>(tableModel);

        initializer.initialize(table);

        MixedRowTableColumn column = (MixedRowTableColumn) table.getColumn(numberColumnAdapter);
        assertThat(column.getSubColumn(0).getCellRenderer()).isSameAs(dateRenderer);
    }

    @Test
    public void setsDefaultTableCellEditors() throws Exception {
        BeanListTableModel<Object> tableModel = new BeanListTableModel<>(ImmutableList.of(dateColumnAdapter, numberColumnAdapter));
        TableCellEditor dateEditor = mock(TableCellEditor.class);
        ImmutableMap<Class<?>, Supplier<TableCellEditor>> cellEditors = ImmutableMap.of(Date.class, () -> dateEditor);
        TableInitializer initializer = new TableInitializer(emptyMap(), cellEditors, emptyMap(), emptyMap());
        DecoratedTable<Object, BeanListTableModel<Object>> table = new DecoratedTable<>(tableModel);

        initializer.initialize(table);

        assertThat(table.getDefaultEditor(Date.class)).isSameAs(dateEditor);
    }

    @Test
    public void setsTableColumnCellEditors() throws Exception {
        BeanListTableModel<Object> tableModel = new BeanListTableModel<>(ImmutableList.of(dateColumnAdapter, numberColumnAdapter));
        TableCellEditor dateEditor = mock(TableCellEditor.class);
        ImmutableMap<String, Supplier<TableCellEditor>> cellEditors = ImmutableMap.of("date editor", () -> dateEditor);
        TableInitializer initializer = new TableInitializer(emptyMap(), emptyMap(), emptyMap(), cellEditors);
        DecoratedTable<Object, BeanListTableModel<Object>> table = new DecoratedTable<>(tableModel);

        initializer.initialize(table);

        assertThat(table.getColumn(dateColumnAdapter).getCellEditor()).isSameAs(dateEditor);
    }

    @Test
    public void keepsExistingColumnCellEditors() throws Exception {
        BeanListTableModel<Object> tableModel = new BeanListTableModel<>(ImmutableList.of(dateColumnAdapter, numberColumnAdapter));
        ImmutableMap<String, Supplier<TableCellEditor>> cellEditors = ImmutableMap.of("date editor", () -> mock(TableCellEditor.class));
        TableInitializer initializer = new TableInitializer(emptyMap(), emptyMap(), emptyMap(), cellEditors);
        DecoratedTable<Object, BeanListTableModel<Object>> table = new DecoratedTable<>(tableModel);
        TableCellEditor existingEditor = mock(TableCellEditor.class);
        table.getColumn(dateColumnAdapter).setCellEditor(existingEditor);

        initializer.initialize(table);

        assertThat(table.getColumn(dateColumnAdapter).getCellEditor()).isSameAs(existingEditor);
    }

    @Test
    public void setsTableColumnCellEditorsOnMixedColumn() throws Exception {
        HeaderDetailTableModel<Object> tableModel = new HeaderDetailTableModel<>(null, null, ImmutableList.of(numberColumnAdapter),
                ImmutableList.of(ImmutableList.of(dateColumnAdapter)));
        TableCellEditor dateEditor = mock(TableCellEditor.class);
        ImmutableMap<String, Supplier<TableCellEditor>> cellEditors = ImmutableMap.of("date editor", () -> dateEditor);
        TableInitializer initializer = new TableInitializer(emptyMap(), emptyMap(), emptyMap(), cellEditors);
        MixedRowTable<Object, HeaderDetailTableModel<Object>> table = new MixedRowTable<>(tableModel);

        initializer.initialize(table);

        MixedRowTableColumn column = (MixedRowTableColumn) table.getColumn(numberColumnAdapter);
        assertThat(column.getSubColumn(0).getCellEditor()).isSameAs(dateEditor);
    }

    @Test
    public void addsUnsavedChangeDecorator() throws Exception {
        BufferedBeanListTableModel<Object> tableModel = new BufferedBeanListTableModel<>(dateColumnAdapter, numberColumnAdapter);
        TableInitializer initializer = new TableInitializer(emptyMap(), emptyMap(), null, null);
        DecoratedTable<Object, BeanListTableModel<Object>> table = new DecoratedTable<>(tableModel);

        initializer.initialize(table);

        assertThat(table.getDecorators().get(0)).isInstanceOf(UnsavedChangeDecorator.class);
    }

    @Test
    public void addsValidationDecorator() throws Exception {
        BufferedBeanListTableModel<Object> tableModel = new ValidatedBeanListTableModel<>(ImmutableList.of(dateColumnAdapter, numberColumnAdapter));
        TableInitializer initializer = new TableInitializer(emptyMap(), emptyMap(), null, null);
        DecoratedTable<Object, BeanListTableModel<Object>> table = new DecoratedTable<>(tableModel);

        initializer.initialize(table);

        assertThat(table.getDecorators().get(0)).isInstanceOf(UnsavedChangeDecorator.class);
        assertThat(table.getDecorators().get(1)).isInstanceOf(ValidationDecorator.class);
    }
}