// The MIT License (MIT)
//
// Copyright (c) 2021 Timothy D. Jones
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
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.github.jonestimd.swing.ComponentDefaults;
import io.github.jonestimd.swing.table.model.BufferedHeaderDetailTableModel;
import io.github.jonestimd.swing.table.model.ColumnAdapter;
import io.github.jonestimd.swing.table.model.EmptyColumnAdapter;
import io.github.jonestimd.swing.table.model.SingleTypeDetailAdapter;
import io.github.jonestimd.swing.table.model.TestColumnAdapter;
import io.github.jonestimd.swing.table.sort.HeaderDetailTableRowSorter;
import org.junit.Test;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;

public class MixedRowTableTest {
    private Color evenBackground = ComponentDefaults.getColor("Table.alternateRowColor");
    private Color oddBackground = ComponentDefaults.getColor("Table.background");
    private final ColumnAdapter<TestSummaryBean, String> summaryColumnAdapter1 =
            new TestColumnAdapter<>("Summary Name", "summaryName", String.class, TestSummaryBean::getName, TestSummaryBean::setName);
    private final ColumnAdapter<TestSummaryBean, Integer> summaryColumnAdapter2 =
            new TestColumnAdapter<>("Summary Number", "summaryNumber", Integer.class, TestSummaryBean::getNumber, TestSummaryBean::setNumber);
    private final ColumnAdapter<TestDetailBean, Integer> detailColumnAdapter =
            new TestColumnAdapter<>("Detail Id", "detailId", Integer.class, TestDetailBean::getId, TestDetailBean::setId);
    private int nextId = 1;

    @Test
    public void testSetModel() throws Exception {
        MixedRowTable<TestSummaryBean, BufferedHeaderDetailTableModel<TestSummaryBean>> table = new MixedRowTable<>(newModel(0));
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel(0);

        table.setModel(model);

        assertThat(table.getColumnModel().getColumnCount()).isEqualTo(2);
        assertThat(table.getColumnModel().getColumn(0).getHeaderValue()).isEqualTo("Summary Name");
        assertThat(((MixedRowTableColumn) table.getColumnModel().getColumn(0)).getSubColumn(0).getHeaderValue()).isEqualTo("Detail Id");
        MixedRowTableColumn column = (MixedRowTableColumn) table.getColumnModel().getColumn(0);
        assertThat(column.getHeaderValue()).isEqualTo("Summary Name");
        assertThat(column.getSubColumn(0).getHeaderValue()).isEqualTo("Detail Id");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetModelThrowsException() throws Exception {
        MixedRowTable<TestSummaryBean, BufferedHeaderDetailTableModel<TestSummaryBean>> table = new MixedRowTable<>(newModel(0));

        table.setModel(new DefaultTableModel());
    }

    @Test
    public void testConstructorWithModel() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel(0);

        MixedRowTable<TestSummaryBean, BufferedHeaderDetailTableModel<TestSummaryBean>> table = new MixedRowTable<>(model);

        assertThat(table.getColumnModel().getColumnCount()).isEqualTo(2);
        assertThat(table.getColumnModel().getColumn(0).getHeaderValue()).isEqualTo("Summary Name");
        assertThat(((MixedRowTableColumn) table.getColumnModel().getColumn(0)).getSubColumn(0).getHeaderValue()).isEqualTo("Detail Id");
        MixedRowTableColumn column = (MixedRowTableColumn) table.getColumnModel().getColumn(0);
        assertThat(column.getHeaderValue()).isEqualTo("Summary Name");
        assertThat(column.getSubColumn(0).getHeaderValue()).isEqualTo("Detail Id");
    }

    @Test
    public void testAddColumnCreatesMixedRowTableColumn() throws Exception {
        MixedRowTable<TestSummaryBean, BufferedHeaderDetailTableModel<TestSummaryBean>> table = new MixedRowTable<>(newModel(0));

        TableColumn column = new TableColumn(0);
        column.setIdentifier("new column");
        table.addColumn(column);

        assertThat(table.getColumnModel().getColumnCount()).isEqualTo(3);
        Enumeration<TableColumn> columns = table.getColumnModel().getColumns();
        while (columns.hasMoreElements()) {
            assertThat(columns.nextElement().getClass()).isEqualTo(MixedRowTableColumn.class);
        }
    }

    @Test
    public void getCellEditorUsesDefaultEditor() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel(1, (i) -> newBean(2));
        MixedRowTable<TestSummaryBean, BufferedHeaderDetailTableModel<TestSummaryBean>> table = new MixedRowTable<>(model);

        assertThat(table.getCellEditor(0, 0).getClass().getName()).isEqualTo("io.github.jonestimd.swing.table.MixedRowTable$GenericCellEditor");
        assertThat(table.getCellEditor(1, 0).getClass().getName()).isEqualTo("io.github.jonestimd.swing.table.MixedRowTable$NumberCellEditor");
        assertThat(table.getCellEditor(2, 0).getClass().getName()).isEqualTo("io.github.jonestimd.swing.table.MixedRowTable$NumberCellEditor");
    }

    @Test
    public void getCellEditorUsesColumnEditor() throws Exception {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = newModel(1, (i) -> newBean(2));
        MixedRowTable<TestSummaryBean, BufferedHeaderDetailTableModel<TestSummaryBean>> table = new MixedRowTable<>(model);
        MixedRowTableColumn column = (MixedRowTableColumn) table.getColumnModel().getColumn(0);
        final DefaultCellEditor cellEditor = new DefaultCellEditor(new JTextField());
        column.getSubColumn(0).setCellEditor(cellEditor);

        assertThat(table.getCellEditor(0, 0).getClass().getName()).isEqualTo("io.github.jonestimd.swing.table.MixedRowTable$GenericCellEditor");
        assertThat(table.getCellEditor(1, 0)).isSameAs(cellEditor);
        assertThat(table.getCellEditor(2, 0)).isSameAs(cellEditor);
    }

    @Test
    public void getRowBackgroundWithoutSorter() throws Exception {
        MixedRowTable<TestSummaryBean, BufferedHeaderDetailTableModel<TestSummaryBean>> table = new MixedRowTable<>(newModel(2, this::newBean));

        assertThat(table.getRowBackground(0)).isEqualTo(evenBackground);
        assertThat(table.getRowBackground(1)).isEqualTo(evenBackground);
        assertThat(table.getRowBackground(2)).isEqualTo(oddBackground);
        assertThat(table.getRowBackground(3)).isEqualTo(oddBackground);
        assertThat(table.getRowBackground(4)).isEqualTo(oddBackground);
    }

    @Test
    public void getRowBackgroundWithSorter() throws Exception {
        MixedRowTable<TestSummaryBean, BufferedHeaderDetailTableModel<TestSummaryBean>> table = new MixedRowTable<>(newModel(2, this::newBean));
        HeaderDetailTableRowSorter<?, ?> sorter = new HeaderDetailTableRowSorter<>(table);
        sorter.setSortKeys(Lists.newArrayList(new SortKey(0, SortOrder.DESCENDING)));
        table.setRowSorter(sorter);

        assertThat(table.getRowBackground(0)).isEqualTo(evenBackground);
        assertThat(table.getRowBackground(1)).isEqualTo(evenBackground);
        assertThat(table.getRowBackground(2)).isEqualTo(evenBackground);
        assertThat(table.getRowBackground(3)).isEqualTo(oddBackground);
        assertThat(table.getRowBackground(4)).isEqualTo(oddBackground);
    }

    @Test
    public void selectRowAt() throws Exception {
        MixedRowTable<TestSummaryBean, BufferedHeaderDetailTableModel<TestSummaryBean>> table = new MixedRowTable<>(newModel(1));
        table.setRowSelectionInterval(0, 0);
        table.setColumnSelectionInterval(1, 1);

        table.selectRowAt(1);

        assertThat(table.getSelectedRow()).isEqualTo(1);
        assertThat(table.getSelectedColumn()).isEqualTo(0);
        assertThat(table.getLeadSelectionModelIndex()).isEqualTo(1);
    }

    @Test
    public void editSummaryCell() throws Exception {
        MixedRowTable<TestSummaryBean, BufferedHeaderDetailTableModel<TestSummaryBean>> table = new MixedRowTable<>(newModel(1));
        final TableCellEditor editor = table.getDefaultEditor(String.class);

        Component component = editor.getTableCellEditorComponent(table, "", false, 0, 0);
        ((JTextField) component).setText("new value");
        assertThat(editor.stopCellEditing()).isTrue();

        assertThat(editor.getCellEditorValue()).isEqualTo("new value");
    }

    @Test
    public void editSummaryCellEmptyString() throws Exception {
        MixedRowTable<TestSummaryBean, BufferedHeaderDetailTableModel<TestSummaryBean>> table = new MixedRowTable<>(newModel(1));
        final TableCellEditor editor = table.getDefaultEditor(String.class);

        Component component = editor.getTableCellEditorComponent(table, "old value", false, 0, 0);
        ((JTextField) component).setText("");
        assertThat(editor.stopCellEditing()).isTrue();

        assertThat(editor.getCellEditorValue()).isEqualTo("");
    }

    @Test
    public void editSubRowCell() throws Exception {
        MixedRowTable<TestSummaryBean, BufferedHeaderDetailTableModel<TestSummaryBean>> table = new MixedRowTable<>(newModel(1));
        final TableCellEditor editor = table.getDefaultEditor(String.class);

        Component component = editor.getTableCellEditorComponent(table, "", false, 1, 0);
        ((JTextField) component).setText("99");
        assertThat(editor.stopCellEditing()).isTrue();

        assertThat(editor.getCellEditorValue()).isEqualTo(99);
    }

    @Test
    public void editSubRowCellInvalidValue() throws Exception {
        MixedRowTable<TestSummaryBean, BufferedHeaderDetailTableModel<TestSummaryBean>> table = new MixedRowTable<>(newModel(1));
        final TableCellEditor editor = table.getDefaultEditor(String.class);

        Component component = editor.getTableCellEditorComponent(table, "", false, 1, 0);
        ((JTextField) component).setText("");
        assertThat(editor.stopCellEditing()).isFalse();

        assertThat(editor.getCellEditorValue()).isNull();
    }

    @Test
    public void getSelectedItemsReturnsUniqueBeans() throws Exception {
        MixedRowTable<TestSummaryBean, BufferedHeaderDetailTableModel<TestSummaryBean>> table = new MixedRowTable<>(newModel(3));
        table.setRowSelectionInterval(1, 4);

        List<TestSummaryBean> items = table.getSelectedItems();

        assertThat(items).isEqualTo(table.getModel().getBeans());
    }

    private TestSummaryBean newBean(int size) {
        TestDetailBean[] details = Stream.generate(TestDetailBean::new).limit(size + 1).toArray(TestDetailBean[]::new);
        return new TestSummaryBean(details);
    }

    private BufferedHeaderDetailTableModel<TestSummaryBean> newModel(int beanCount) {
        return newModel(beanCount, (i) -> new TestSummaryBean(new TestDetailBean()));
    }

    private BufferedHeaderDetailTableModel<TestSummaryBean> newModel(int beanCount, IntFunction<TestSummaryBean> beanSupplier) {
        BufferedHeaderDetailTableModel<TestSummaryBean> model = new BufferedHeaderDetailTableModel<>(new TestDetailAdapter(),
                ImmutableList.of(summaryColumnAdapter1, summaryColumnAdapter2),
                singletonList(ImmutableList.of(detailColumnAdapter, new EmptyColumnAdapter<>("dummy1", String.class))));
        model.setBeans(IntStream.range(0, beanCount).mapToObj(beanSupplier).collect(Collectors.toList()));
        return model;
    }

    private class TestDetailBean {
        private Integer id = nextId++;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }
    }

    private class TestSummaryBean {
        private List<TestDetailBean> details;
        private String name = "summary " + nextId++;
        private Integer number;

        private TestSummaryBean(TestDetailBean ... details) {
            this.details = Arrays.asList(details);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getNumber() {
            return number;
        }

        public void setNumber(Integer number) {
            this.number = number;
        }
    }

    private class TestDetailAdapter extends SingleTypeDetailAdapter<TestSummaryBean> {
        public List<?> getDetails(TestSummaryBean bean, int subRowTypeIndex) {
            return bean.details;
        }

        @Override
        public int appendDetail(TestSummaryBean bean) {
            throw new UnsupportedOperationException();
        }
    }
}