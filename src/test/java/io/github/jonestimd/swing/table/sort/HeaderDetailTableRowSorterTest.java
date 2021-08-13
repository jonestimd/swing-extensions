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
package io.github.jonestimd.swing.table.sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.TableModelEvent;

import io.github.jonestimd.swing.table.MixedRowTable;
import io.github.jonestimd.swing.table.model.BufferedHeaderDetailTableModel;
import io.github.jonestimd.swing.table.model.ColumnAdapter;
import io.github.jonestimd.swing.table.model.SingleTypeDetailAdapter;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class HeaderDetailTableRowSorterTest {
    private static final ColumnAdapter<TestBean, String> headerAdapter = new TestColumnAdapter<TestBean>("Header") {
        public String getValue(TestBean bean) {
            return bean.header;
        }

        public void setValue(TestBean bean, String value) {
            bean.header = value;
        }
    };
    private static final ColumnAdapter<String, String> detailColumnAdapter = new TestColumnAdapter<String>("Detail") {
        public String getValue(String bean) {
            return bean;
        }

        public void setValue(String bean, String value) {
        }
    };
    private static final ColumnAdapter<TestBean, String> emptyAdapter = new TestColumnAdapter<TestBean>("Empty") {
        public String getValue(TestBean bean) {
            return "";
        }

        public void setValue(TestBean bean, String value) {
        }
    };
    private TestTableModel tableModel = new TestTableModel();
    private MixedRowTable<TestBean, TestTableModel> table = new MixedRowTable<>(tableModel);
    private TableModelEvent modelEvent;
    private RowSorterEvent sorterEvent;

    @Before
    public void setUp() {
        tableModel.addTableModelListener(e -> modelEvent = e);
    }

    private void addSortListener(HeaderDetailTableRowSorter<?, ?> sorter) {
        sorter.addRowSorterListener(e -> sorterEvent = e);
    }

    @Test
    public void unsortedOrderMatchesModelOrder() throws Exception {
        tableModel.setBeans(Arrays.asList(new TestBean("B", "yy"), new TestBean("A", "xx", "aa")));

        HeaderDetailTableRowSorter<TestBean, TestTableModel> sorter = new HeaderDetailTableRowSorter<>(table);

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            checkConversion(sorter, i, i);
        }
    }

    @Test
    public void unsortedSortKey() throws Exception {
        tableModel.setBeans(Arrays.asList(new TestBean("B", "yy"), new TestBean("A", "xx", "aa")));
        HeaderDetailTableRowSorter<TestBean, TestTableModel> sorter = new HeaderDetailTableRowSorter<>(table);

        sorter.setSortKeys(Collections.singletonList(new SortKey(0, SortOrder.UNSORTED)));

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            checkConversion(sorter, i, i);
        }
    }

    private void checkConversion(HeaderDetailTableRowSorter<TestBean, TestTableModel> sorter, int viewIndex, int modelIndex) {
        assertThat(sorter.convertRowIndexToModel(viewIndex)).isEqualTo(modelIndex);
        assertThat(sorter.convertRowIndexToView(modelIndex)).isEqualTo(viewIndex);
    }

    @Test
    public void sortAscending() throws Exception {
        tableModel.setBeans(Arrays.asList(new TestBean("B", "yy"), new TestBean("A", "xx", "aa"), new TestBean("B", "zz")));
        HeaderDetailTableRowSorter<TestBean, TestTableModel> sorter = new HeaderDetailTableRowSorter<>(table);

        sorter.setSortKeys(Collections.singletonList(new SortKey(0, SortOrder.ASCENDING)));

        checkConversion(sorter, 0, tableModel.getLeadRowForGroup(1));
        checkConversion(sorter, 1, tableModel.getLeadRowForGroup(1)+1);
        checkConversion(sorter, 2, tableModel.getLeadRowForGroup(1)+2);
        checkConversion(sorter, 3, tableModel.getLeadRowForGroup(0));
        checkConversion(sorter, 4, tableModel.getLeadRowForGroup(0)+1);
        checkConversion(sorter, 5, tableModel.getLeadRowForGroup(2));
        checkConversion(sorter, 6, tableModel.getLeadRowForGroup(2) + 1);
    }

    @Test
    public void sortDescending() throws Exception {
        tableModel.setBeans(Arrays.asList(new TestBean("A", "xx", "aa"), new TestBean("B", "yy")));
        HeaderDetailTableRowSorter<TestBean, TestTableModel> sorter = new HeaderDetailTableRowSorter<>(table);

        sorter.setSortKeys(Collections.singletonList(new SortKey(0, SortOrder.DESCENDING)));

        checkConversion(sorter, 0, tableModel.getLeadRowForGroup(1));
        checkConversion(sorter, 1, tableModel.getLeadRowForGroup(1)+1);
        checkConversion(sorter, 2, tableModel.getLeadRowForGroup(0));
        checkConversion(sorter, 3, tableModel.getLeadRowForGroup(0) + 1);
        checkConversion(sorter, 4, tableModel.getLeadRowForGroup(0) + 2);
    }

    @Test
    public void toggleSort() throws Exception {
        tableModel.setBeans(Arrays.asList(new TestBean("Z", "??"), new TestBean("A", "xx", "aa"), new TestBean("B", "yy")));
        HeaderDetailTableRowSorter<TestBean, TestTableModel> sorter = new HeaderDetailTableRowSorter<>(table);

        sorter.toggleSortOrder(0);

        checkConversion(sorter, 0, tableModel.getLeadRowForGroup(1));
        checkConversion(sorter, 1, tableModel.getLeadRowForGroup(1)+1);
        checkConversion(sorter, 2, tableModel.getLeadRowForGroup(1)+2);
        checkConversion(sorter, 3, tableModel.getLeadRowForGroup(2));
        checkConversion(sorter, 4, tableModel.getLeadRowForGroup(2)+1);
        checkConversion(sorter, 5, tableModel.getLeadRowForGroup(0));
        checkConversion(sorter, 6, tableModel.getLeadRowForGroup(0)+1);

        sorter.toggleSortOrder(0);

        checkConversion(sorter, 0, tableModel.getLeadRowForGroup(0));
        checkConversion(sorter, 1, tableModel.getLeadRowForGroup(0)+1);
        checkConversion(sorter, 2, tableModel.getLeadRowForGroup(2));
        checkConversion(sorter, 3, tableModel.getLeadRowForGroup(2)+1);
        checkConversion(sorter, 4, tableModel.getLeadRowForGroup(1));
        checkConversion(sorter, 5, tableModel.getLeadRowForGroup(1) + 1);
        checkConversion(sorter, 6, tableModel.getLeadRowForGroup(1) + 2);
    }

    @Test
    public void appendMiddleBean() throws Exception {
        tableModel.setBeans(Arrays.asList(new TestBean("Z", "yy"), new TestBean("A", "xx", "aa")));
        HeaderDetailTableRowSorter<TestBean, TestTableModel> sorter = new HeaderDetailTableRowSorter<>(table);
        sorter.setSortKeys(Collections.singletonList(new SortKey(0, SortOrder.DESCENDING)));

        tableModel.addBean(new TestBean("C", "??"));
        sorter.rowsInserted(modelEvent.getFirstRow(), modelEvent.getLastRow());
        sorter.toggleSortOrder(0);

        checkConversion(sorter, 0, tableModel.getLeadRowForGroup(1));
        checkConversion(sorter, 1, tableModel.getLeadRowForGroup(1)+1);
        checkConversion(sorter, 2, tableModel.getLeadRowForGroup(1)+2);
        checkConversion(sorter, 3, tableModel.getLeadRowForGroup(2));
        checkConversion(sorter, 4, tableModel.getLeadRowForGroup(2)+1);
        checkConversion(sorter, 5, tableModel.getLeadRowForGroup(0));
        checkConversion(sorter, 6, tableModel.getLeadRowForGroup(0) + 1);
    }

    @Test
    public void appendEndBean() throws Exception {
        tableModel.setBeans(Arrays.asList(new TestBean("X", "yy"), new TestBean("A", "xx", "aa")));
        HeaderDetailTableRowSorter<TestBean, TestTableModel> sorter = new HeaderDetailTableRowSorter<>(table);
        sorter.setSortKeys(Collections.singletonList(new SortKey(0, SortOrder.DESCENDING)));

        tableModel.addBean(new TestBean("Z", "??"));
        sorter.rowsInserted(modelEvent.getFirstRow(), modelEvent.getLastRow());
        sorter.toggleSortOrder(0);

        checkConversion(sorter, 0, tableModel.getLeadRowForGroup(1));
        checkConversion(sorter, 1, tableModel.getLeadRowForGroup(1)+1);
        checkConversion(sorter, 2, tableModel.getLeadRowForGroup(1)+2);
        checkConversion(sorter, 3, tableModel.getLeadRowForGroup(0));
        checkConversion(sorter, 4, tableModel.getLeadRowForGroup(0)+1);
        checkConversion(sorter, 5, tableModel.getLeadRowForGroup(2));
        checkConversion(sorter, 6, tableModel.getLeadRowForGroup(2) + 1);
    }

    @Test
    public void insertBean() throws Exception {
        tableModel.setBeans(Arrays.asList(new TestBean("Z", "yy"), new TestBean("C", "xx", "aa")));
        HeaderDetailTableRowSorter<TestBean, TestTableModel> sorter = new HeaderDetailTableRowSorter<>(table);
        sorter.setSortKeys(Collections.singletonList(new SortKey(0, SortOrder.DESCENDING)));

        tableModel.addBean(1, new TestBean("A", "??"));
        sorter.rowsInserted(modelEvent.getFirstRow(), modelEvent.getLastRow());
        sorter.toggleSortOrder(0);

        checkConversion(sorter, 0, tableModel.getLeadRowForGroup(1));
        checkConversion(sorter, 1, tableModel.getLeadRowForGroup(1)+1);
        checkConversion(sorter, 2, tableModel.getLeadRowForGroup(2));
        checkConversion(sorter, 3, tableModel.getLeadRowForGroup(2)+1);
        checkConversion(sorter, 4, tableModel.getLeadRowForGroup(2)+2);
        checkConversion(sorter, 5, tableModel.getLeadRowForGroup(0));
        checkConversion(sorter, 6, tableModel.getLeadRowForGroup(0) + 1);
    }

    @Test
    public void appendBeans() throws Exception {
        tableModel.setBeans(Arrays.asList(new TestBean("X", "yy"), new TestBean("C", "xx", "aa")));
        HeaderDetailTableRowSorter<TestBean, TestTableModel> sorter = new HeaderDetailTableRowSorter<>(table);
        sorter.setSortKeys(Collections.singletonList(new SortKey(0, SortOrder.DESCENDING)));

        tableModel.addBean(new TestBean("A", "??"));
        int firstRow = modelEvent.getFirstRow();
        tableModel.addBean(new TestBean("M", "##", "$$"));
        sorter.rowsInserted(firstRow, modelEvent.getLastRow());
        sorter.toggleSortOrder(0);

        checkConversion(sorter, 0, tableModel.getLeadRowForGroup(2));
        checkConversion(sorter, 1, tableModel.getLeadRowForGroup(2)+1);
        checkConversion(sorter, 2, tableModel.getLeadRowForGroup(1));
        checkConversion(sorter, 3, tableModel.getLeadRowForGroup(1)+1);
        checkConversion(sorter, 4, tableModel.getLeadRowForGroup(1)+2);
        checkConversion(sorter, 5, tableModel.getLeadRowForGroup(3));
        checkConversion(sorter, 6, tableModel.getLeadRowForGroup(3)+1);
        checkConversion(sorter, 7, tableModel.getLeadRowForGroup(3)+2);
        checkConversion(sorter, 8, tableModel.getLeadRowForGroup(0));
        checkConversion(sorter, 9, tableModel.getLeadRowForGroup(0)+1);
    }

    @Test
    public void insertDetail() throws Exception {
        tableModel.setBeans(Arrays.asList(new TestBean("Z", "yy"), new TestBean("C", "xx", "aa")));
        HeaderDetailTableRowSorter<TestBean, TestTableModel> sorter = new HeaderDetailTableRowSorter<>(table);
        sorter.setSortKeys(Collections.singletonList(new SortKey(0, SortOrder.DESCENDING)));

        tableModel.insertDetail(0, 0, "nn");
        sorter.rowsInserted(modelEvent.getFirstRow(), modelEvent.getLastRow());
        sorter.toggleSortOrder(0);

        checkConversion(sorter, 0, tableModel.getLeadRowForGroup(1));
        checkConversion(sorter, 1, tableModel.getLeadRowForGroup(1)+1);
        checkConversion(sorter, 2, tableModel.getLeadRowForGroup(1)+2);
        checkConversion(sorter, 3, tableModel.getLeadRowForGroup(0));
        checkConversion(sorter, 4, tableModel.getLeadRowForGroup(0)+1);
        checkConversion(sorter, 5, tableModel.getLeadRowForGroup(0)+2);
    }

    @Test
    public void appendDetail() throws Exception {
        tableModel.setBeans(Arrays.asList(new TestBean("Z", "yy"), new TestBean("C", "xx", "aa")));
        HeaderDetailTableRowSorter<TestBean, TestTableModel> sorter = new HeaderDetailTableRowSorter<>(table);
        sorter.setSortKeys(Collections.singletonList(new SortKey(0, SortOrder.DESCENDING)));

        tableModel.insertDetail(1, 2, "nn");
        sorter.rowsInserted(modelEvent.getFirstRow(), modelEvent.getLastRow());
        sorter.toggleSortOrder(0);

        checkConversion(sorter, 0, tableModel.getLeadRowForGroup(1));
        checkConversion(sorter, 1, tableModel.getLeadRowForGroup(1)+1);
        checkConversion(sorter, 2, tableModel.getLeadRowForGroup(1)+2);
        checkConversion(sorter, 3, tableModel.getLeadRowForGroup(1)+3);
        checkConversion(sorter, 4, tableModel.getLeadRowForGroup(0));
        checkConversion(sorter, 5, tableModel.getLeadRowForGroup(0)+1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void firstBeanAfterEndBeanThrowsException() throws Exception {
        HeaderDetailTableRowSorter<TestBean, TestTableModel> sorter = new HeaderDetailTableRowSorter<>(table);
        tableModel.addBean(new TestBean("Z", "yy"));
        tableModel.addBean(new TestBean("C", "xx", "aa"));
        sorter.toggleSortOrder(0);

        sorter.rowsInserted(2, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mixedHeaderDetailInsertThrowsException() throws Exception {
        HeaderDetailTableRowSorter<TestBean, TestTableModel> sorter = new HeaderDetailTableRowSorter<>(table);
        tableModel.addBean(new TestBean("Z", "yy"));
        tableModel.addBean(new TestBean("C", "xx", "aa"));
        sorter.toggleSortOrder(0);

        // detail from bean 0 plus bean 1
        sorter.rowsInserted(1, 5);
    }

    @Test
    public void removeDetails() throws Exception {
        tableModel.setBeans(Arrays.asList(new TestBean("Z", "yy", "bb"), new TestBean("C", "aa")));
        HeaderDetailTableRowSorter<TestBean, TestTableModel> sorter = new HeaderDetailTableRowSorter<>(table);
        sorter.setSortKeys(Collections.singletonList(new SortKey(0, SortOrder.DESCENDING)));

        tableModel.removeDetail(0, 1);
        sorter.rowsDeleted(modelEvent.getFirstRow(), modelEvent.getLastRow());
        sorter.toggleSortOrder(0);

        checkConversion(sorter, 0, tableModel.getLeadRowForGroup(1));
        checkConversion(sorter, 1, tableModel.getLeadRowForGroup(1)+1);
        checkConversion(sorter, 2, tableModel.getLeadRowForGroup(0));
        checkConversion(sorter, 3, tableModel.getLeadRowForGroup(0) + 1);
    }

    @Test
    public void removeMiddleBean() throws Exception {
        tableModel.setBeans(Arrays.asList(new TestBean("Z", "yy"), new TestBean("C", "xx", "aa"), new TestBean("A", "@@")));
        HeaderDetailTableRowSorter<TestBean, TestTableModel> sorter = new HeaderDetailTableRowSorter<>(table);
        sorter.setSortKeys(Collections.singletonList(new SortKey(0, SortOrder.DESCENDING)));

        tableModel.removeBean(tableModel.getBean(1));
        sorter.rowsDeleted(modelEvent.getFirstRow(), modelEvent.getLastRow());
        sorter.toggleSortOrder(0);

        checkConversion(sorter, 0, tableModel.getLeadRowForGroup(1));
        checkConversion(sorter, 1, tableModel.getLeadRowForGroup(1)+1);
        checkConversion(sorter, 2, tableModel.getLeadRowForGroup(0));
        checkConversion(sorter, 3, tableModel.getLeadRowForGroup(0) + 1);
    }

    @Test
    public void removeLastBean() throws Exception {
        tableModel.setBeans(Arrays.asList(new TestBean("Z", "yy"), new TestBean("C", "xx", "aa"), new TestBean("A", "@@")));
        HeaderDetailTableRowSorter<TestBean, TestTableModel> sorter = new HeaderDetailTableRowSorter<>(table);
        sorter.setSortKeys(Collections.singletonList(new SortKey(0, SortOrder.DESCENDING)));

        tableModel.removeBean(tableModel.getBean(2));
        sorter.rowsDeleted(modelEvent.getFirstRow(), modelEvent.getLastRow());
        sorter.toggleSortOrder(0);

        checkConversion(sorter, 0, tableModel.getLeadRowForGroup(1));
        checkConversion(sorter, 1, tableModel.getLeadRowForGroup(1)+1);
        checkConversion(sorter, 2, tableModel.getLeadRowForGroup(1)+2);
        checkConversion(sorter, 3, tableModel.getLeadRowForGroup(0));
        checkConversion(sorter, 4, tableModel.getLeadRowForGroup(0) + 1);
    }

    @Test
    public void updateHeader() throws Exception {
        tableModel.setBeans(Arrays.asList(new TestBean("X", "yy"), new TestBean("C", "xx", "aa"), new TestBean("A", "??")));
        HeaderDetailTableRowSorter<TestBean, TestTableModel> sorter = new HeaderDetailTableRowSorter<>(table);
        sorter.setSortKeys(Collections.singletonList(new SortKey(0, SortOrder.ASCENDING)));

        tableModel.setValueAt("Z", 2, 0);
        sorter.rowsUpdated(modelEvent.getFirstRow(), modelEvent.getLastRow());

        checkConversion(sorter, 0, tableModel.getLeadRowForGroup(2));
        checkConversion(sorter, 1, tableModel.getLeadRowForGroup(2)+1);
        checkConversion(sorter, 2, tableModel.getLeadRowForGroup(0));
        checkConversion(sorter, 3, tableModel.getLeadRowForGroup(0)+1);
        checkConversion(sorter, 4, tableModel.getLeadRowForGroup(1));
        checkConversion(sorter, 5, tableModel.getLeadRowForGroup(1)+1);
        checkConversion(sorter, 6, tableModel.getLeadRowForGroup(1) + 2);
    }

    @Test
    public void updateDetailDoesNothing() throws Exception {
        tableModel.setBeans(Arrays.asList(new TestBean("X", "yy"), new TestBean("C", "xx", "aa"), new TestBean("A", "??")));
        HeaderDetailTableRowSorter<TestBean, TestTableModel> sorter = new HeaderDetailTableRowSorter<>(table);
        sorter.setSortKeys(Collections.singletonList(new SortKey(0, SortOrder.ASCENDING)));
        addSortListener(sorter);

        tableModel.getBean(1).details.set(0, "yy");
        sorter.rowsUpdated(3, 3);

        assertThat(sorterEvent).isNull();
    }

    @Test
    public void updateSortedHeaderColumn() throws Exception {
        tableModel.setBeans(Arrays.asList(new TestBean("X", "yy"), new TestBean("C", "xx", "aa"), new TestBean("A", "??")));
        HeaderDetailTableRowSorter<TestBean, TestTableModel> sorter = new HeaderDetailTableRowSorter<>(table);
        sorter.setSortKeys(Collections.singletonList(new SortKey(0, SortOrder.ASCENDING)));

        tableModel.setValueAt("Z", 2, 0);
        sorter.rowsUpdated(modelEvent.getFirstRow(), modelEvent.getLastRow(), 0);

        checkConversion(sorter, 0, tableModel.getLeadRowForGroup(2));
        checkConversion(sorter, 1, tableModel.getLeadRowForGroup(2)+1);
        checkConversion(sorter, 2, tableModel.getLeadRowForGroup(0));
        checkConversion(sorter, 3, tableModel.getLeadRowForGroup(0)+1);
        checkConversion(sorter, 4, tableModel.getLeadRowForGroup(1));
        checkConversion(sorter, 5, tableModel.getLeadRowForGroup(1)+1);
        checkConversion(sorter, 6, tableModel.getLeadRowForGroup(1) + 2);
    }

    @Test
    public void updateUnsortedHeaderColumn() throws Exception {
        tableModel.setBeans(Arrays.asList(new TestBean("X", "yy"), new TestBean("C", "xx", "aa"), new TestBean("A", "??")));
        HeaderDetailTableRowSorter<TestBean, TestTableModel> sorter = new HeaderDetailTableRowSorter<>(table);
        sorter.setSortKeys(Collections.singletonList(new SortKey(0, SortOrder.ASCENDING)));
        addSortListener(sorter);

        sorter.rowsUpdated(2, 2, 1);

        assertThat(sorterEvent).isNull();
    }

    @Test
    public void updateDetailColumnDoesNothing() throws Exception {
        tableModel.setBeans(Arrays.asList(new TestBean("X", "yy"), new TestBean("C", "xx", "aa"), new TestBean("A", "??")));
        HeaderDetailTableRowSorter<TestBean, TestTableModel> sorter = new HeaderDetailTableRowSorter<>(table);
        sorter.setSortKeys(Collections.singletonList(new SortKey(0, SortOrder.ASCENDING)));
        addSortListener(sorter);

        tableModel.getBean(1).details.set(0, "yy");
        sorter.rowsUpdated(3, 3, 0);

        assertThat(sorterEvent).isNull();
    }

    @Test
    public void filterUnsorted() throws Exception {
        tableModel.setBeans(Arrays.asList(new TestBean("X", "yy"), new TestBean("C", "xx", "aa"), new TestBean("A", "??")));
        HeaderDetailTableRowSorter<TestBean, TestTableModel> sorter = new HeaderDetailTableRowSorter<>(table);
        addSortListener(sorter);

        sorter.setRowFilter(input -> !input.header.equals("X"));

        assertThat(sorter.getViewRowCount()).isEqualTo(5);
        checkConversion(sorter, 0, tableModel.getLeadRowForGroup(1));
        checkConversion(sorter, 1, tableModel.getLeadRowForGroup(1)+1);
        checkConversion(sorter, 2, tableModel.getLeadRowForGroup(1)+2);
        checkConversion(sorter, 3, tableModel.getLeadRowForGroup(2));
        checkConversion(sorter, 4, tableModel.getLeadRowForGroup(2)+1);
        assertThat(sorter.convertRowIndexToView(0)).isEqualTo(-1);
        assertThat(sorter.convertRowIndexToView(1)).isEqualTo(-1);
    }

    @Test
    public void updateFilteredRow() throws Exception {
        tableModel.setBeans(Arrays.asList(new TestBean("X", "yy"), new TestBean("C", "xx", "aa"), new TestBean("A", "??")));
        HeaderDetailTableRowSorter<TestBean, TestTableModel> sorter = new HeaderDetailTableRowSorter<>(table);
        addSortListener(sorter);
        sorter.setRowFilter(input -> ! input.header.equals("X"));
            
        tableModel.getBean(0).header = "W";
        sorter.rowsUpdated(0, 0);

        assertThat(sorter.getViewRowCount()).isEqualTo(7);
        checkConversion(sorter, 0, tableModel.getLeadRowForGroup(0));
        checkConversion(sorter, 1, tableModel.getLeadRowForGroup(0)+1);
        checkConversion(sorter, 2, tableModel.getLeadRowForGroup(1));
        checkConversion(sorter, 3, tableModel.getLeadRowForGroup(1) + 1);
        checkConversion(sorter, 4, tableModel.getLeadRowForGroup(1) + 2);
        checkConversion(sorter, 5, tableModel.getLeadRowForGroup(2));
        checkConversion(sorter, 6, tableModel.getLeadRowForGroup(2) + 1);
    }

    @Test
    public void updateFilteredRowColumn() throws Exception {
        tableModel.setBeans(Arrays.asList(new TestBean("X", "yy"), new TestBean("C", "xx", "aa"), new TestBean("A", "??")));
        HeaderDetailTableRowSorter<TestBean, TestTableModel> sorter = new HeaderDetailTableRowSorter<>(table);
        addSortListener(sorter);
        sorter.setRowFilter(input -> ! input.header.equals("X"));
        tableModel.getBean(0).header = "W";
        sorter.rowsUpdated(0, 0, 0);

        assertThat(sorter.getViewRowCount()).isEqualTo(7);
        checkConversion(sorter, 0, tableModel.getLeadRowForGroup(0));
        checkConversion(sorter, 1, tableModel.getLeadRowForGroup(0)+1);
        checkConversion(sorter, 2, tableModel.getLeadRowForGroup(1));
        checkConversion(sorter, 3, tableModel.getLeadRowForGroup(1) + 1);
        checkConversion(sorter, 4, tableModel.getLeadRowForGroup(1) + 2);
        checkConversion(sorter, 5, tableModel.getLeadRowForGroup(2));
        checkConversion(sorter, 6, tableModel.getLeadRowForGroup(2) + 1);
    }

    @Test
    public void addFilteredRow() throws Exception {
        tableModel.setBeans(Arrays.asList(new TestBean("X", "yy"), new TestBean("C", "xx", "aa"), new TestBean("A", "??")));
        HeaderDetailTableRowSorter<TestBean, TestTableModel> sorter = new HeaderDetailTableRowSorter<>(table);
        addSortListener(sorter);
        sorter.setRowFilter(input -> ! input.header.equalsIgnoreCase("X"));

        tableModel.addBean(2, new TestBean("x", "--", "**"));
        sorter.rowsInserted(modelEvent.getFirstRow(), modelEvent.getLastRow());

        assertThat(sorter.getViewRowCount()).isEqualTo(5);
        assertThat(sorter.getModelRowCount()).isEqualTo(10);
        assertThat(sorter.convertRowIndexToView(0)).isEqualTo(-1);
        assertThat(sorter.convertRowIndexToView(1)).isEqualTo(-1);
        assertThat(sorter.convertRowIndexToView(5)).isEqualTo(-1);
        assertThat(sorter.convertRowIndexToView(6)).isEqualTo(-1);
        assertThat(sorter.convertRowIndexToView(7)).isEqualTo(-1);
        checkConversion(sorter, 0, tableModel.getLeadRowForGroup(1));
        checkConversion(sorter, 1, tableModel.getLeadRowForGroup(1) + 1);
        checkConversion(sorter, 2, tableModel.getLeadRowForGroup(1) + 2);
        checkConversion(sorter, 3, tableModel.getLeadRowForGroup(3));
        checkConversion(sorter, 4, tableModel.getLeadRowForGroup(3) + 1);
    }

    @Test
    public void addFilteredRowWithSort() throws Exception {
        tableModel.setBeans(Arrays.asList(new TestBean("X", "yy"), new TestBean("C", "xx", "aa"), new TestBean("A", "??")));
        HeaderDetailTableRowSorter<TestBean, TestTableModel> sorter = new HeaderDetailTableRowSorter<>(table);
        addSortListener(sorter);
        sorter.setSortKeys(Collections.singletonList(new SortKey(0, SortOrder.ASCENDING)));
        sorter.setRowFilter(input -> ! input.header.equalsIgnoreCase("X"));

        tableModel.addBean(2, new TestBean("x", "--", "**"));
        sorter.rowsInserted(modelEvent.getFirstRow(), modelEvent.getLastRow());

        assertThat(sorter.getViewRowCount()).isEqualTo(5);
        assertThat(sorter.getModelRowCount()).isEqualTo(10);
        assertThat(sorter.convertRowIndexToView(0)).isEqualTo(-1);
        assertThat(sorter.convertRowIndexToView(1)).isEqualTo(-1);
        assertThat(sorter.convertRowIndexToView(5)).isEqualTo(-1);
        assertThat(sorter.convertRowIndexToView(6)).isEqualTo(-1);
        assertThat(sorter.convertRowIndexToView(7)).isEqualTo(-1);
        checkConversion(sorter, 0, tableModel.getLeadRowForGroup(3));
        checkConversion(sorter, 1, tableModel.getLeadRowForGroup(3) + 1);
        checkConversion(sorter, 2, tableModel.getLeadRowForGroup(1));
        checkConversion(sorter, 3, tableModel.getLeadRowForGroup(1) + 1);
        checkConversion(sorter, 4, tableModel.getLeadRowForGroup(1) + 2);
    }

    @Test
    public void addUnfilteredRow() throws Exception {
        tableModel.setBeans(Arrays.asList(new TestBean("X", "yy"), new TestBean("C", "xx", "aa"), new TestBean("A", "??")));
        HeaderDetailTableRowSorter<TestBean, TestTableModel> sorter = new HeaderDetailTableRowSorter<>(table);
        addSortListener(sorter);
        sorter.setRowFilter(input -> ! input.header.equals("X"));

        tableModel.addBean(2, new TestBean("x", "--", "**"));
        sorter.rowsInserted(modelEvent.getFirstRow(), modelEvent.getLastRow());

        assertThat(sorter.getViewRowCount()).isEqualTo(8);
        assertThat(sorter.getModelRowCount()).isEqualTo(10);
        assertThat(sorter.convertRowIndexToView(0)).isEqualTo(-1);
        assertThat(sorter.convertRowIndexToView(1)).isEqualTo(-1);
        checkConversion(sorter, 0, tableModel.getLeadRowForGroup(1));
        checkConversion(sorter, 1, tableModel.getLeadRowForGroup(1) + 1);
        checkConversion(sorter, 2, tableModel.getLeadRowForGroup(1) + 2);
        checkConversion(sorter, 3, tableModel.getLeadRowForGroup(2));
        checkConversion(sorter, 4, tableModel.getLeadRowForGroup(2) + 1);
        checkConversion(sorter, 5, tableModel.getLeadRowForGroup(2) + 2);
        checkConversion(sorter, 6, tableModel.getLeadRowForGroup(3));
        checkConversion(sorter, 7, tableModel.getLeadRowForGroup(3) + 1);
    }

    @Test
    public void addUnfilteredRowWithSort() throws Exception {
        tableModel.setBeans(Arrays.asList(new TestBean("X", "yy"), new TestBean("C", "xx", "aa"), new TestBean("A", "??")));
        HeaderDetailTableRowSorter<TestBean, TestTableModel> sorter = new HeaderDetailTableRowSorter<>(table);
        addSortListener(sorter);
        sorter.setSortKeys(Collections.singletonList(new SortKey(0, SortOrder.ASCENDING)));
        sorter.setRowFilter(input -> ! input.header.equals("X"));

        tableModel.addBean(2, new TestBean("x", "--", "**"));
        sorter.rowsInserted(modelEvent.getFirstRow(), modelEvent.getLastRow());

        assertThat(sorter.getViewRowCount()).isEqualTo(8);
        assertThat(sorter.getModelRowCount()).isEqualTo(10);
        assertThat(sorter.convertRowIndexToView(0)).isEqualTo(-1);
        assertThat(sorter.convertRowIndexToView(1)).isEqualTo(-1);
        checkConversion(sorter, 0, tableModel.getLeadRowForGroup(3));
        checkConversion(sorter, 1, tableModel.getLeadRowForGroup(3) + 1);
        checkConversion(sorter, 2, tableModel.getLeadRowForGroup(1));
        checkConversion(sorter, 3, tableModel.getLeadRowForGroup(1) + 1);
        checkConversion(sorter, 4, tableModel.getLeadRowForGroup(1) + 2);
        checkConversion(sorter, 5, tableModel.getLeadRowForGroup(2));
        checkConversion(sorter, 6, tableModel.getLeadRowForGroup(2) + 1);
        checkConversion(sorter, 7, tableModel.getLeadRowForGroup(2) + 2);
    }

    @Test
    public void removeFilteredBean() throws Exception {
        tableModel.setBeans(Arrays.asList(new TestBean("X", "yy"), new TestBean("C", "xx", "aa"), new TestBean("x", "--", "**"), new TestBean("A", "??")));
        HeaderDetailTableRowSorter<TestBean, TestTableModel> sorter = new HeaderDetailTableRowSorter<>(table);
        addSortListener(sorter);
        sorter.setRowFilter(input -> !input.header.equalsIgnoreCase("X"));

        tableModel.removeBean(tableModel.getBean(2));
        sorter.rowsDeleted(modelEvent.getFirstRow(), modelEvent.getLastRow());

        assertThat(sorter.getViewRowCount()).isEqualTo(5);
        assertThat(sorter.getModelRowCount()).isEqualTo(7);
        assertThat(sorter.convertRowIndexToView(0)).isEqualTo(-1);
        assertThat(sorter.convertRowIndexToView(1)).isEqualTo(-1);
        checkConversion(sorter, 0, tableModel.getLeadRowForGroup(1));
        checkConversion(sorter, 1, tableModel.getLeadRowForGroup(1) + 1);
        checkConversion(sorter, 2, tableModel.getLeadRowForGroup(1) + 2);
        checkConversion(sorter, 3, tableModel.getLeadRowForGroup(2));
        checkConversion(sorter, 4, tableModel.getLeadRowForGroup(2) + 1);
    }

    @Test
    public void removeFilteredDetail() throws Exception {
        tableModel.setBeans(Arrays.asList(new TestBean("X", "yy"), new TestBean("C", "xx", "aa"), new TestBean("x", "--", "**"), new TestBean("A", "??")));
        HeaderDetailTableRowSorter<TestBean, TestTableModel> sorter = new HeaderDetailTableRowSorter<>(table);
        addSortListener(sorter);
        sorter.setRowFilter(input -> ! input.header.equalsIgnoreCase("X"));

        tableModel.removeDetail(2, 0);
        sorter.rowsDeleted(modelEvent.getFirstRow(), modelEvent.getLastRow());

        assertThat(sorter.getViewRowCount()).isEqualTo(5);
        assertThat(sorter.getModelRowCount()).isEqualTo(9);
        assertThat(sorter.convertRowIndexToView(0)).isEqualTo(-1);
        assertThat(sorter.convertRowIndexToView(1)).isEqualTo(-1);
        assertThat(sorter.convertRowIndexToView(5)).isEqualTo(-1);
        assertThat(sorter.convertRowIndexToView(6)).isEqualTo(-1);
        checkConversion(sorter, 0, tableModel.getLeadRowForGroup(1));
        checkConversion(sorter, 1, tableModel.getLeadRowForGroup(1) + 1);
        checkConversion(sorter, 2, tableModel.getLeadRowForGroup(1) + 2);
        checkConversion(sorter, 3, tableModel.getLeadRowForGroup(3));
        checkConversion(sorter, 4, tableModel.getLeadRowForGroup(3) + 1);
    }

    @Test
    public void removeUnfilteredRow() throws Exception {
        tableModel.setBeans(Arrays.asList(new TestBean("X", "yy"), new TestBean("C", "xx", "aa"), new TestBean("x", "--", "**"), new TestBean("A", "??")));
        HeaderDetailTableRowSorter<TestBean, TestTableModel> sorter = new HeaderDetailTableRowSorter<>(table);
        addSortListener(sorter);
        sorter.setRowFilter(input -> ! input.header.equals("X"));

        tableModel.removeBean(tableModel.getBean(2));
        sorter.rowsDeleted(modelEvent.getFirstRow(), modelEvent.getLastRow());

        assertThat(sorter.getViewRowCount()).isEqualTo(5);
        assertThat(sorter.getModelRowCount()).isEqualTo(7);
        assertThat(sorter.convertRowIndexToView(0)).isEqualTo(-1);
        assertThat(sorter.convertRowIndexToView(1)).isEqualTo(-1);
        checkConversion(sorter, 0, tableModel.getLeadRowForGroup(1));
        checkConversion(sorter, 1, tableModel.getLeadRowForGroup(1) + 1);
        checkConversion(sorter, 2, tableModel.getLeadRowForGroup(1) + 2);
        checkConversion(sorter, 3, tableModel.getLeadRowForGroup(2));
        checkConversion(sorter, 4, tableModel.getLeadRowForGroup(2) + 1);
    }

    @Test
    public void removeUnfilteredRowWithSort() throws Exception {
        tableModel.setBeans(Arrays.asList(new TestBean("X", "yy"), new TestBean("C", "xx", "aa"), new TestBean("x", "--", "**"), new TestBean("A", "??")));
        HeaderDetailTableRowSorter<TestBean, TestTableModel> sorter = new HeaderDetailTableRowSorter<>(table);
        addSortListener(sorter);
        sorter.setSortKeys(Collections.singletonList(new SortKey(0, SortOrder.ASCENDING)));
        sorter.setRowFilter(input -> ! input.header.equals("X"));

        tableModel.removeBean(tableModel.getBean(2));
        sorter.rowsDeleted(modelEvent.getFirstRow(), modelEvent.getLastRow());

        assertThat(sorter.getViewRowCount()).isEqualTo(5);
        assertThat(sorter.getModelRowCount()).isEqualTo(7);
        assertThat(sorter.convertRowIndexToView(0)).isEqualTo(-1);
        assertThat(sorter.convertRowIndexToView(1)).isEqualTo(-1);
        checkConversion(sorter, 0, tableModel.getLeadRowForGroup(2));
        checkConversion(sorter, 1, tableModel.getLeadRowForGroup(2) + 1);
        checkConversion(sorter, 2, tableModel.getLeadRowForGroup(1));
        checkConversion(sorter, 3, tableModel.getLeadRowForGroup(1) + 1);
        checkConversion(sorter, 4, tableModel.getLeadRowForGroup(1) + 2);
    }

    private static class TestBean {
        public String header;
        public List<String> details;

        public TestBean(String header, String ... details) {
            this.header = header;
            this.details = new ArrayList<>(Arrays.asList(details));
        }

        public String toString() {
            return String.format("{%s %s}", header, details);
        }
    }

    private static abstract class TestColumnAdapter<T> implements ColumnAdapter<T, String> {
        private final String name;

        protected TestColumnAdapter(String name) {
            this.name = name;
        }

        public String getColumnId() {
            return name;
        }

        public String getResource(String resourceId, String defaultValue) {
            return null;
        }

        public java.lang.String getName() {
            return name;
        }

        public Class<String> getType() {
            return String.class;
        }

        public boolean isEditable(T row) {
            return true;
        }
    }

    private static class TestDetailAdapter extends SingleTypeDetailAdapter<TestBean> {
        public List<?> getDetails(TestBean bean, int subRowTypeIndex) {
            return bean.details;
        }

        @Override
        public int appendDetail(TestBean bean) {
            throw new UnsupportedOperationException();
        }
    }

    private static class TestTableModel extends BufferedHeaderDetailTableModel<TestBean> {
        public TestTableModel() {
            super(new TestDetailAdapter(),
                Arrays.asList(headerAdapter, emptyAdapter),
                Collections.singletonList(Arrays.asList(detailColumnAdapter, detailColumnAdapter)));
        }

        public void insertDetail(int beanIndex, int detailIndex, String detail) {
            TestBean bean = getBean(beanIndex);
            bean.details.add(detailIndex, detail);
            fireSubRowInserted(bean, detailIndex+1);
        }

        public void removeDetail(int beanIndex, int detailIndex) {
            TestBean bean = getBean(beanIndex);
            removeSubRow(bean, bean.details.get(detailIndex));
        }
    }
}
