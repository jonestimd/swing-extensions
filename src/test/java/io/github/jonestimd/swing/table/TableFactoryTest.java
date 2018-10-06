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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.function.Consumer;

import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableRowSorter;

import io.github.jonestimd.swing.component.ComboBoxCellEditor;
import io.github.jonestimd.swing.table.model.BeanListMultimapTableModel;
import io.github.jonestimd.swing.table.model.ColumnAdapter;
import io.github.jonestimd.swing.table.model.ValidatedBeanListTableModel;
import io.github.jonestimd.swing.validation.ValidatingTextCellEditor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TableFactoryTest {
    private enum ColumnEnum {Abc, Def, Hgi}
    private final Class[] columnClasses = {String.class, ColumnEnum.class, Number.class};
    private final ColumnAdapter[] adapters = {mock(ColumnAdapter.class), mock(ColumnAdapter.class), mock(ColumnAdapter.class)};
    @Mock
    private TableInitializer initializer;
    @Mock
    private Consumer<MouseEvent> mouseEventConsumer;
    @Mock
    private ValidatedBeanListTableModel<Object> validatedModel;
    @Mock
    private BeanListMultimapTableModel<Object, Object> listMultimapModel;
    @InjectMocks
    private TableFactory factory;

    @Before
    public void trainMocks() throws Exception {
        when(validatedModel.getColumnCount()).thenReturn(columnClasses.length);
        when(validatedModel.getColumnName(anyInt())).thenAnswer(invocation -> "column " + invocation.getArguments()[0]);
        when(validatedModel.getColumnClass(anyInt())).thenAnswer(invocation -> columnClasses[(int) invocation.getArguments()[0]]);
        when(validatedModel.getColumnIdentifier(anyInt())).thenAnswer(invocation -> adapters[(int) invocation.getArguments()[0]]);
        when(initializer.initialize(any())).thenAnswer(invocation -> invocation.getArguments()[0]);
    }

    @Test
    public void createSortedTable() throws Exception {
        DecoratedTable<?, ?> table = factory.tableBuilder(validatedModel).sorted().get();

        verify(initializer).initialize(table);
        assertThat(table.getCellSelectionEnabled()).isFalse();
        assertThat(table.getRowSorter().getSortKeys()).isEmpty();
    }

    @Test
    public void createValidatedTable() throws Exception {
        DecoratedTable<?, ?> table = factory.validatedTableBuilder(validatedModel).sorted().get();

        verify(initializer).initialize(table);
        assertThat(table.getCellSelectionEnabled()).isTrue();
        assertThat(table.getRowSorter().getSortKeys()).isEmpty();
        assertThat(table.getColumn(adapters[0]).getCellEditor()).isInstanceOf(ValidatingTextCellEditor.class);
        assertThat(table.getColumn(adapters[1]).getCellEditor()).isInstanceOf(ComboBoxCellEditor.class);
    }

    @Test
    public void createValidatedTableWithSortKey() throws Exception {
        DecoratedTable<?, ?> table = factory.validatedTableBuilder(validatedModel).sortedBy(0).get();

        verify(initializer).initialize(table);
        assertThat(table.getCellSelectionEnabled()).isTrue();
        assertThat(table.getRowSorter().getSortKeys()).hasSize(1);
        assertThat(table.getColumn(adapters[0]).getCellEditor()).isInstanceOf(ValidatingTextCellEditor.class);
        assertThat(table.getColumn(adapters[1]).getCellEditor()).isInstanceOf(ComboBoxCellEditor.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void createSectionTable() throws Exception {
        factory.sectionTableBuilder(listMultimapModel).get();

        verify(initializer).initialize(isA(SectionTable.class));
    }

    @Test
    public void createEnumCellEditor() throws Exception {
        final TableCellEditor editor = TableFactory.createEnumCellEditor(ColumnEnum.class);

        final JComboBox component = (JComboBox) editor.getTableCellEditorComponent(new JTable(), ColumnEnum.Abc, false, 0, 0);
        assertThat(component.getModel().getSize()).isEqualTo(ColumnEnum.values().length);
    }

    @Test
    public void tableBuilder_sorted() throws Exception {
        JTable table = factory.tableBuilder(validatedModel).sorted().get();

        assertThat(table.getRowSorter()).isInstanceOf(TableRowSorter.class);
        assertThat(((TableRowSorter) table.getRowSorter()).getSortsOnUpdates()).isTrue();
    }

    @Test
    public void doubleClickHanlder() throws Exception {
        JTable table = factory.tableBuilder(validatedModel).doubleClickHandler(mouseEventConsumer).get();

        MouseListener[] listeners = table.getMouseListeners();
        MouseListener listener = listeners[listeners.length-1];
        MouseEvent event = new MouseEvent(table, -1, 0L, 0, 0, 0, 2, false, MouseEvent.BUTTON1);
        listener.mouseClicked(event);
        listener.mouseClicked(new MouseEvent(table, -1, 0L, 0, 0, 0, 2, false, MouseEvent.BUTTON2));
        listener.mouseClicked(new MouseEvent(table, -1, 0L, 0, 0, 0, 1, false, MouseEvent.BUTTON1));
        verify(mouseEventConsumer).accept(same(event));
        verifyNoMoreInteractions(mouseEventConsumer);
    }
}