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
package io.github.jonestimd.swing.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.github.jonestimd.swing.ComponentDefaults;
import io.github.jonestimd.swing.table.model.BeanListTableModel;
import io.github.jonestimd.swing.table.model.BeanTableModel;
import io.github.jonestimd.swing.table.model.ColumnAdapter;
import io.github.jonestimd.swing.table.model.TestColumnAdapter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static java.awt.event.InputEvent.*;
import static java.awt.event.KeyEvent.*;
import static java.util.Collections.*;
import static javax.swing.JComponent.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DecoratedTableTest {
    @Mock
    private ComboBoxModel<String> comboBoxModel;

    private TestColumnAdapter<TestBean, String> columnAdapter1 = new TestColumnAdapter<>("Column 1", String.class, TestBean::getValue, TestBean::setValue);
    private TestColumnAdapter<TestBean, Boolean> columnAdapter2 = new TestColumnAdapter<>("Column 2", Boolean.class, TestBean::isFlag, TestBean::setFlag);
    @Mock
    private TestColumnAdapter<TestBean, Boolean> mockAdapter1;
    private Color evenBackground = ComponentDefaults.getColor("Table.alternateRowColor");
    private Color oddBackground = ComponentDefaults.getColor("Table.background");

    @Test
    public void mouseMovedSetsCursor() throws Exception {
        MouseEvent event = mockEvent(0, 0);
        when(mockAdapter1.getName()).thenReturn("name");
        DecoratedTable<TestBean, BeanListTableModel<TestBean>> table = createTable(ImmutableList.of(mockAdapter1));
        table.getModel().setBeans(createBeans("one", "two"));
        MouseMotionListener[] listeners = table.getListeners(MouseMotionListener.class);

        listeners[listeners.length-1].mouseMoved(event);

        verify(mockAdapter1).getCursor(event, table, table.getModel().getBean(0));
    }

    @Test
    public void mouseMovedIgnoredForXOutOfBounds() throws Exception {
        MouseEvent event = mockEvent(-10, 0);
        when(mockAdapter1.getName()).thenReturn("name");
        DecoratedTable<TestBean, BeanListTableModel<TestBean>> table = createTable(ImmutableList.of(mockAdapter1));
        table.getModel().setBeans(createBeans("one", "two"));
        MouseMotionListener[] listeners = table.getListeners(MouseMotionListener.class);

        listeners[listeners.length-1].mouseMoved(event);

        verify(mockAdapter1, never()).getCursor(event, table, table.getModel().getBean(0));
    }

    @Test
    public void mouseMovedIgnoredForYOutOfBounds() throws Exception {
        MouseEvent event = mockEvent(0, -50);
        when(mockAdapter1.getName()).thenReturn("name");
        DecoratedTable<TestBean, BeanListTableModel<TestBean>> table = createTable(ImmutableList.of(mockAdapter1));
        table.getModel().setBeans(createBeans("one", "two"));
        MouseMotionListener[] listeners = table.getListeners(MouseMotionListener.class);

        listeners[listeners.length-1].mouseMoved(event);

        verify(mockAdapter1, never()).getCursor(event, table, table.getModel().getBean(0));
    }

    @Test
    public void mouseDraggedSetsCursor() throws Exception {
        MouseEvent event = mockEvent(0, 0);
        when(mockAdapter1.getName()).thenReturn("name");
        DecoratedTable<TestBean, BeanListTableModel<TestBean>> table = createTable(ImmutableList.of(mockAdapter1));
        table.getModel().setBeans(createBeans("one", "two"));
        MouseMotionListener[] listeners = table.getListeners(MouseMotionListener.class);

        listeners[listeners.length-1].mouseDragged(event);

        verify(mockAdapter1).getCursor(event, table, table.getModel().getBean(0));
    }

    @Test
    public void mouseClickCallsColumnHandler() throws Exception {
        MouseEvent event = mockEvent(0, 0);
        when(mockAdapter1.getName()).thenReturn("name");
        DecoratedTable<TestBean, BeanListTableModel<TestBean>> table = createTable(ImmutableList.of(mockAdapter1));
        table.getModel().setBeans(createBeans("one", "two"));
        MouseListener[] listeners = table.getListeners(MouseListener.class);

        listeners[listeners.length-1].mouseClicked(event);

        verify(mockAdapter1).handleClick(event, table, table.getModel().getBean(0));
    }

    @Test
    public void mouseClickIgnoredForXOutOfBounds() throws Exception {
        MouseEvent event = mockEvent(-10, 0);
        when(mockAdapter1.getName()).thenReturn("name");
        DecoratedTable<TestBean, BeanListTableModel<TestBean>> table = createTable(ImmutableList.of(mockAdapter1));
        table.getModel().setBeans(createBeans("one", "two"));
        MouseListener[] listeners = table.getListeners(MouseListener.class);

        listeners[listeners.length-1].mouseClicked(event);

        verify(mockAdapter1, never()).handleClick(event, table, table.getModel().getBean(0));
    }

    @Test
    public void mouseClickIgnoredForYOutOfBounds() throws Exception {
        MouseEvent event = mockEvent(0, -50);
        when(mockAdapter1.getName()).thenReturn("name");
        DecoratedTable<TestBean, BeanListTableModel<TestBean>> table = createTable(ImmutableList.of(mockAdapter1));
        table.getModel().setBeans(createBeans("one", "two"));
        MouseListener[] listeners = table.getListeners(MouseListener.class);

        listeners[listeners.length-1].mouseClicked(event);

        verify(mockAdapter1, never()).handleClick(event, table, table.getModel().getBean(0));
    }

    @Test
    public void setModelRestoresSortKeysForAutoCreateSorterTrue() throws Exception {
        DecoratedTable<TestBean, BeanListTableModel<TestBean>> table = newTable("bean1", "bean2");
        table.setAutoCreateRowSorter(true);
        SortKey sortKey = new SortKey(0, SortOrder.DESCENDING);
        table.getRowSorter().setSortKeys(Collections.singletonList(sortKey));

        table.setModel(new BeanListTableModel<>(ImmutableList.of(columnAdapter1, columnAdapter2)));

        assertThat(table.getRowSorter().getSortKeys()).isEqualTo(Collections.singletonList(sortKey));
    }

    @Test
    public void setModelDoesNotRestoresSortKeysForAutoCreateSorterFalse() throws Exception {
        DecoratedTable<TestBean, BeanListTableModel<TestBean>> table = newTable("bean1", "bean2");
        SortKey sortKey = new SortKey(0, SortOrder.DESCENDING);
        table.setRowSorter(new TableRowSorter<>(table.getModel()));
        table.getRowSorter().setSortKeys(Collections.singletonList(sortKey));

        table.setModel(new BeanListTableModel<>(ImmutableList.of(columnAdapter1, columnAdapter2)));

        assertThat(table.getRowSorter().getSortKeys()).isEmpty();
    }

    private MouseEvent mockEvent(int x, int y) {
        MouseEvent event = mock(MouseEvent.class);
        when(event.getPoint()).thenReturn(new Point(x, y));
        return event;
    }

    @Test
    public void alternatesRowColors() throws Exception {
        DecoratedTable<TestBean, BeanListTableModel<TestBean>> table = newTable("bean1", "bean2");

        assertThat(table.getColumn(columnAdapter1).getModelIndex()).isEqualTo(0);
        assertThat(table.getRowBackground(0)).isEqualTo(table.getAlternateBackground());
        assertThat(table.getRowBackground(1)).isEqualTo(table.getBackground());
        assertThat(table.getTableHeader().getPreferredSize().height).isEqualTo(19);
    }

    @Test
    public void defaultBackgroundColors() throws Exception {
        DecoratedTable<TestBean, BeanListTableModel<TestBean>> table = newTable("bean1");

        assertThat(table.getBackground()).isEqualTo(oddBackground);
        assertThat(table.getAlternateBackground()).isEqualTo(evenBackground);
    }

    @Test
    public void overrideAlternateBackground() throws Exception {
        DecoratedTable<TestBean, BeanListTableModel<TestBean>> table = newTable("bean1", "bean2");

        table.setAlternateBackground(Color.PINK);

        assertThat(table.getRowBackground(0)).isEqualTo(Color.PINK);
    }

    @Test
    public void booleanCellRenderer() throws Exception {
        DecoratedTable<TestBean, BeanListTableModel<TestBean>> table = newTable("bean1", "bean2");

        table.clearSelection();
        checkRendererColors("unselected even row", table, 0, 1, table.getForeground(), evenBackground);
        checkRendererColors("unselected odd row", table, 1, 1, table.getForeground(), oddBackground);

        selectCell(table, 0, 1);
        checkRendererColors("selected even row", table, 0, 1, table.getSelectionForeground(), table.getSelectionBackground());
    }

    private void checkRendererColors(String description, DecoratedTable<?, ?> table, int row, int column, Color foreground, Color background) {
        Component component = table.prepareRenderer(table.getCellRenderer(row, column), row, column);

        assertThat(component.getForeground()).as(description).isEqualTo(foreground);
        assertThat(component.getBackground()).isEqualTo(background);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void multiRowHeaders() throws Exception {
        BeanTableModel<DecoratedTableTest> model = mock(BeanTableModel.class);
        when(model.getColumnCount()).thenReturn(3);
        when(model.getColumnName(0)).thenReturn("Column 1");
        when(model.getColumnName(1)).thenReturn("Column 2\nOverflows\n3 lines");
        when(model.getColumnName(2)).thenReturn("Column 3\nLine 2");

        DecoratedTable<DecoratedTableTest, BeanTableModel<DecoratedTableTest>> table = new DecoratedTable<>(model);

        assertThat(table.getTableHeader().getPreferredSize().height).isEqualTo(49);
        assertThat(table.getColumnModel().getColumn(0).getHeaderValue()).isEqualTo("Column 1");
        assertThat(table.getColumnModel().getColumn(1).getHeaderValue()).isEqualTo("<html><center>Column 2<br>Overflows<br>3 lines</center></html>");
        assertThat(table.getColumnModel().getColumn(2).getHeaderValue()).isEqualTo("<html><center>Column 3<br>Line 2</center></html>");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void setTableHeaderAllowsNull() throws Exception {
        BeanTableModel<DecoratedTableTest> model = mock(BeanTableModel.class);
        when(model.getColumnCount()).thenReturn(1);
        when(model.getColumnName(0)).thenReturn("Line 1\nLine 2");
        DecoratedTable<DecoratedTableTest, BeanTableModel<DecoratedTableTest>> table = new DecoratedTable<>(model);

        table.setTableHeader(null);
        assertThat(table.getTableHeader()).isNull();
        table.setTableHeader(new JTableHeader());

        assertThat(table.getTableHeader().getPreferredSize().height).isEqualTo(34);
    }

    @Test
    public void getSelectedItems() throws Exception {
        DecoratedTable<TestBean, BeanListTableModel<TestBean>> table = createTable();
        table.getModel().setBeans(Lists.newArrayList(new TestBean("bean1"), new TestBean("bean2"), new TestBean("bean2")));
        assertThat(table.getSelectedItems()).isEmpty();
        table.getSelectionModel().setSelectionInterval(1, 2);

        assertThat(table.getSelectedItems()).isEqualTo(table.getModel().getBeans().subList(1, 3));
    }

    @Test
    public void processKeyBindingSetsAutoStartEdit() throws Exception {
        DecoratedTable<TestBean, BeanListTableModel<TestBean>> table = createTable();

        table.processKeyBinding(KeyStroke.getKeyStroke('x'), new KeyEvent(table, KEY_TYPED, 0L, 0, VK_UNDEFINED, 'x'), WHEN_FOCUSED, true);
        assertThat(table.getClientProperty("JTable.autoStartsEdit")).isEqualTo(true);

        table.processKeyBinding(KeyStroke.getKeyStroke('x'), new KeyEvent(table, KEY_TYPED, 0L, CTRL_DOWN_MASK, VK_UNDEFINED, 'x'), WHEN_FOCUSED, true);
        assertThat(table.getClientProperty("JTable.autoStartsEdit")).isEqualTo(false);

        table.processKeyBinding(KeyStroke.getKeyStroke('x'), new KeyEvent(table, KEY_TYPED, 0L, KeyEvent.ALT_DOWN_MASK, VK_UNDEFINED, 'x'), WHEN_FOCUSED, true);
        assertThat(table.getClientProperty("JTable.autoStartsEdit")).isEqualTo(false);

        table.processKeyBinding(KeyStroke.getKeyStroke('x'), new KeyEvent(table, KEY_PRESSED, 0L, CTRL_DOWN_MASK, VK_BACK_SPACE, CHAR_UNDEFINED), WHEN_IN_FOCUSED_WINDOW, true);
        assertThat(table.getClientProperty("JTable.autoStartsEdit")).isEqualTo(true);
    }

    @Test
    public void processKeyBindingSelectsItemInComboBox() throws Exception {
        final JComboBox<String> editor = new JComboBox<>(new String[]{"abc", "def", "xyz"});
        final DecoratedTable<TestBean, BeanListTableModel<TestBean>> table = newTable("bean1");
        final KeyEvent event = new KeyEvent(table, KEY_TYPED, 0L, 0, VK_UNDEFINED, 'x');
        table.setDefaultEditor(String.class, new DefaultCellEditor(editor));

        SwingUtilities.invokeAndWait(() -> {
            table.editCellAt(0, 0, event);
            table.processKeyBinding(KeyStroke.getKeyStroke('x'), event, WHEN_FOCUSED, true);
        });

        assertThat(editor.getSelectedItem()).isEqualTo("xyz");
    }

    @Test
    public void processKeyBindingSetsEditableComboBoxText() throws Exception {
        processKeyBinding("replace text", 'x', "bean1", "x");

        processKeyBinding("no change for empty text", '\b', "", "");

        processKeyBinding("back space deletes last char", '\b', "bean1", "bean");

        processKeyBinding("ignore control char", '\u007f', "bean1", "bean1");
    }

    private void processKeyBinding(String description, char keyChar, String cellValue, String expectedEditorText) throws Exception {
        final JComboBox<String> editor = new JComboBox<>(new String[]{"abc", "def", "xyz"});
        editor.setEditable(true);
        final DecoratedTable<TestBean, BeanListTableModel<TestBean>> table = newTable(cellValue);
        table.setDefaultEditor(String.class, new DefaultCellEditor(editor));
        final KeyEvent event = new KeyEvent(table, KEY_TYPED, 0L, 0, VK_UNDEFINED, keyChar);

        SwingUtilities.invokeAndWait(() -> {
            table.editCellAt(0, 0, event);
            table.processKeyBinding(KeyStroke.getKeyStroke(keyChar), event, WHEN_FOCUSED, true);
        });

        assertThat(editor.getEditor().getItem()).as(description).isEqualTo(expectedEditorText);
    }

    @Test
    public void processKeyBindingIgnoresEnterIfNotEditing() throws Exception {
        final TableCellEditor cellEditor = mock(TableCellEditor.class);
        final DecoratedTable<TestBean, BeanListTableModel<TestBean>> table = newTable("bean1", "bean2");
        table.setDefaultEditor(String.class, cellEditor);
        selectCell(table, 0, 0);

        SwingUtilities.invokeAndWait(() -> {
            final KeyEvent event = new KeyEvent(table, KEY_RELEASED, 0L, 0, VK_ENTER, CHAR_UNDEFINED);
            table.processKeyBinding(KeyStroke.getKeyStroke("released ENTER"), event, WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, true);

            verifyNoInteractions(cellEditor);
        });
    }

    private TableRowSorter<BeanListTableModel<TestBean>> newRowSorter(DecoratedTable<TestBean, BeanListTableModel<TestBean>> table) {
        TableRowSorter<BeanListTableModel<TestBean>> sorter = new TableRowSorter<>(table.getModel());
        sorter.setSortKeys(Collections.singletonList(new SortKey(0, SortOrder.ASCENDING)));
        sorter.setSortsOnUpdates(true);
        return sorter;
    }

    @Test
    public void processKeyBindingEndsEditOnEnter() throws Exception {
        when(comboBoxModel.getSelectedItem()).thenReturn("xyz");
        final JComboBox<String> editor = new JComboBox<>(comboBoxModel);
        final DecoratedTable<TestBean, BeanListTableModel<TestBean>> table = newTable("abc", "def");
        table.setRowSorter(newRowSorter(table));
        table.setDefaultEditor(String.class, new DefaultCellEditor(editor));
        selectCell(table, 0, 0);
        table.editCellAt(0, 0);
        final KeyEvent event = new KeyEvent(editor, KEY_RELEASED, 0L, 0, VK_ENTER, CHAR_UNDEFINED);

        SwingUtilities.invokeAndWait(() -> {
            table.processKeyBinding(KeyStroke.getKeyStroke("released ENTER"), event, WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, true);
        });

        assertThat(table.getEditorComponent()).isNull();
        assertThat(table.getSelectedRow()).isEqualTo(table.convertRowIndexToView(0));
        assertThat(table.getSelectedColumn()).isEqualTo(0);
    }

    @Test
    public void editCellAtIgnoresReadOnlyCell() throws Exception {
        final DecoratedTable<TestBean, BeanListTableModel<TestBean>> table = new DecoratedTable<>(new BeanListTableModel<>(
                singletonList(new TestColumnAdapter<>("column1", String.class, TestBean::getValue))));
        table.getModel().setBeans(createBeans("bean1", "bean2"));
        final KeyEvent event = new KeyEvent(table, KEY_PRESSED, 0L, 0, VK_DELETE, CHAR_UNDEFINED);

        SwingUtilities.invokeAndWait(() -> {
            assertThat(table.editCellAt(0, 0, event)).isFalse();
        });
    }

    @Test
    public void editCellAtSetsCaretToStartOfTextFieldForDelete() throws Exception {
        final JTextField editor = new JTextField();
        final DecoratedTable<TestBean, BeanListTableModel<TestBean>> table = newTable("bean1", "bean2");
        table.setDefaultEditor(String.class, new DefaultCellEditor(editor));
        selectCell(table, 0, 0);
        final KeyEvent event = new KeyEvent(table, KEY_PRESSED, 0L, 0, VK_DELETE, CHAR_UNDEFINED);

        SwingUtilities.invokeAndWait(() -> {
            table.editCellAt(0, 0, event);
        });

        assertThat(editor.getCaret().getDot()).isEqualTo(0);
    }

    @Test
    public void editCellAtReplacesTextForCharKeystroke() throws Exception {
        final JTextField editor = new JTextField();
        final DecoratedTable<TestBean, BeanListTableModel<TestBean>> table = newTable("bean1", "bean2");
        table.setDefaultEditor(String.class, new DefaultCellEditor(editor));
        selectCell(table, 0, 0);
        final KeyEvent event = new KeyEvent(table, KEY_TYPED, 0L, 0, VK_UNDEFINED, 'x');

        SwingUtilities.invokeAndWait(() -> {
            table.editCellAt(0, 0, event);
        });

        assertThat(editor.getSelectedText()).isEqualTo("bean1");
    }

    @Test
    public void editCellAtDoesNotSelectTextForBackspace() throws Exception {
        final JTextField editor = new JTextField();
        final DecoratedTable<TestBean, BeanListTableModel<TestBean>> table = newTable("bean1", "bean2");
        table.setDefaultEditor(String.class, new DefaultCellEditor(editor));
        selectCell(table, 0, 0);
        final KeyEvent event = new KeyEvent(table, KEY_PRESSED, 0L, 0, VK_BACK_SPACE, CHAR_UNDEFINED);

        SwingUtilities.invokeAndWait(() -> {
            table.editCellAt(0, 0, event);
        });

        assertThat(editor.getSelectedText()).isNull();
        assertThat(editor.getCaretPosition()).isEqualTo(5);
    }

    @Test
    public void editCellAtDoesNotSelectTextJFormattedTextField() throws Exception {
        final DecoratedTable<TestBean, BeanListTableModel<TestBean>> table = newTable("bean1", "bean2");
        final JFormattedTextField editor = new JFormattedTextField();
        table.setDefaultEditor(String.class, new DefaultCellEditor(editor));
        selectCell(table, 0, 0);
        final KeyEvent event = new KeyEvent(table, KEY_TYPED, 0L, 0, VK_UNDEFINED, 'x');

        SwingUtilities.invokeAndWait(() -> {
            table.editCellAt(0, 0, event);
        });

        assertThat(editor.getSelectedText()).isNull();
        assertThat(editor.getCaretPosition()).isEqualTo(5);
    }

    @Test
    public void prepareRendererSetsForegroundAndBackground() throws Exception {
        final TableCellRenderer renderer1 = mock(TableCellRenderer.class);
        final TableCellRenderer renderer2 = mock(DefaultTableCellRenderer.class);
        final DecoratedTable<TestBean, BeanListTableModel<TestBean>> table = newTable("bean1", "bean2");

        table.prepareRenderer(renderer1, 0, 0);
        table.prepareRenderer(renderer2, 0, 0);
        table.prepareRenderer(renderer2, 1, 0);

        verify(renderer1).getTableCellRendererComponent(table, "bean1", false, false, 0, 0);
        verifyNoMoreInteractions(renderer1);
        InOrder inOrder = inOrder(renderer2);
        inOrder.verify((JComponent) renderer2).setBackground(table.getAlternateBackground());
        inOrder.verify((JComponent) renderer2).setForeground(table.getForeground());
        inOrder.verify(renderer2).getTableCellRendererComponent(table, "bean1", false, false, 0, 0);
        inOrder.verify((JComponent) renderer2).setBackground(table.getBackground());
        inOrder.verify((JComponent) renderer2).setForeground(table.getForeground());
        inOrder.verify(renderer2).getTableCellRendererComponent(table, "bean2", false, false, 1, 0);
    }

    @Test
    public void prepareRendererInitializesTooltipText() throws Exception {
        final TableCellRenderer renderer1 = mock(TableCellRenderer.class);
        final TableCellRenderer renderer2 = mock(DefaultTableCellRenderer.class);
        final DecoratedTable<TestBean, BeanListTableModel<TestBean>> table = newTable("bean1", "bean2");

        table.prepareRenderer(renderer1, 0, 0);
        table.prepareRenderer(renderer2, 1, 0);

        verify(renderer1).getTableCellRendererComponent(table, "bean1", false, false, 0, 0);
        verifyNoMoreInteractions(renderer1);
        InOrder inOrder = inOrder(renderer2);
        inOrder.verify((JComponent) renderer2).setToolTipText(null);
        inOrder.verify(renderer2).getTableCellRendererComponent(table, "bean2", false, false, 1, 0);
    }

    @Test
    public void prepareRendererAppliesDecorators() throws Exception {
        final TableCellRenderer renderer = mock(TableCellRenderer.class);
        final JLabel rendererComponent = new JLabel();
        final TableDecorator decorator = mock(TableDecorator.class);
        final DecoratedTable<TestBean, BeanListTableModel<TestBean>> table = newTable("bean1", "bean2");
        table.setDecorators(Lists.newArrayList(decorator));
        when(renderer.getTableCellRendererComponent(same(table), anyString(), anyBoolean(), anyBoolean(), eq(0), eq(0)))
                .thenReturn(rendererComponent);

        table.prepareRenderer(renderer, 0, 0);

        verify(decorator).prepareRenderer(table, rendererComponent, 0, 0);
    }

    private void selectCell(DecoratedTable<TestBean, BeanListTableModel<TestBean>> table, int row, int column) {
        table.setRowSelectionInterval(row, row);
        table.setColumnSelectionInterval(column, column);
    }

    private DecoratedTable<TestBean, BeanListTableModel<TestBean>> newTable(String... beanValues) {
        final DecoratedTable<TestBean, BeanListTableModel<TestBean>> table = createTable();
        table.getModel().setBeans(createBeans(beanValues));
        return table;
    }

    private List<TestBean> createBeans(String... beanValues) {
        return Stream.of(beanValues).map(TestBean::new).collect(Collectors.toList());
    }

    private DecoratedTable<TestBean, BeanListTableModel<TestBean>> createTable() {
        return createTable(ImmutableList.of(columnAdapter1, columnAdapter2));
    }

    private DecoratedTable<TestBean, BeanListTableModel<TestBean>> createTable(List<ColumnAdapter<TestBean, ?>> columnAdapters) {
        return new DecoratedTable<>(new BeanListTableModel<>(columnAdapters));
    }

    private static class TestBean {
        private String value;
        private boolean flag;

        public TestBean(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public boolean isFlag() {
            return flag;
        }

        public void setFlag(boolean flag) {
            this.flag = flag;
        }
    }
}