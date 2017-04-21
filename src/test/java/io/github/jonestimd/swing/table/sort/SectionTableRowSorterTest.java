package io.github.jonestimd.swing.table.sort;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Function;

import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.TableModelEvent;

import io.github.jonestimd.swing.table.SectionTable;
import io.github.jonestimd.swing.table.model.BeanListMultimapTableModel;
import io.github.jonestimd.swing.table.model.ColumnAdapter;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.*;

public class SectionTableRowSorterTest {
    private static final ColumnAdapter<TestBean, String> NAME_ADAPTER = new TestColumnAdapter<TestBean>("Name") {
        public String getValue(TestBean bean) {
            return bean.name;
        }

        public void setValue(TestBean bean, String value) {
            bean.name = value;
        }
    };
    private static final ColumnAdapter<TestBean, String> VALUE_ADAPTER = new TestColumnAdapter<TestBean>("Value") {
        public String getValue(TestBean bean) {
            return bean.value;
        }

        public void setValue(TestBean bean, String value) {
            bean.value = value;
        }
    };
    @SuppressWarnings("unchecked")
    private BeanListMultimapTableModel<String, TestBean> tableModel = new BeanListMultimapTableModel<>(
            Arrays.asList(NAME_ADAPTER, VALUE_ADAPTER), Collections.emptyList(), null, Function.identity());
    private SectionTable<TestBean, BeanListMultimapTableModel<String, TestBean>> table = new SectionTable<>(tableModel);
    private TableModelEvent modelEvent;
    private RowSorterEvent sorterEvent;

    @Before
    public void setUp() {
        tableModel.addTableModelListener(event -> modelEvent = event);
    }

    private void addSortListener(RowSorter<?> sorter) {
        sorter.addRowSorterListener(event -> sorterEvent = event);
    }

    @Test
    public void unsortedOrderMatchesModelOrder() throws Exception {
        tableModel.put("1", new TestBean("B", "yy"));
        tableModel.put("2", new TestBean("A", "xx"));

        SectionTableRowSorter<TestBean, BeanListMultimapTableModel<String, TestBean>> sorter = newSorter();

        checkSection(sorter, 0, "1");
        checkConversion(sorter, 1, "B");
        checkSection(sorter, 2, "2");
        checkConversion(sorter, 3, "A");
    }

    private SectionTableRowSorter<TestBean, BeanListMultimapTableModel<String, TestBean>> newSorter() {
        return new SectionTableRowSorter<>(table);
    }

    @Test
    public void unsortedSortKey() throws Exception {
        tableModel.put("1", new TestBean("B", "yy"));
        tableModel.put("2", new TestBean("C", "xx"));
        tableModel.put("2", new TestBean("A", "xx"));
        SectionTableRowSorter<TestBean, BeanListMultimapTableModel<String, TestBean>> sorter = newSorter();

        sorter.setSortKeys(Collections.singletonList(new SortKey(0, SortOrder.UNSORTED)));

        checkSection(sorter, 0, "1");
        checkConversion(sorter, 1, "B");
        checkSection(sorter, 2, "2");
        checkConversion(sorter, 3, "C");
        checkConversion(sorter, 4, "A");
    }

    private void checkConversion(RowSorter<?> sorter, int viewIndex, String cellValue) {
        assertThat(tableModel.getValueAt(sorter.convertRowIndexToModel(viewIndex), 0)).isEqualTo(cellValue);
    }

    @Test
    public void sortAscending() throws Exception {
        tableModel.put("1", new TestBean("B", "yy"));
        tableModel.put("1", new TestBean("D", "yy"));
        tableModel.put("2", new TestBean("C", "xx"));
        tableModel.put("2", new TestBean("A", "xx"));
        SectionTableRowSorter<TestBean, BeanListMultimapTableModel<String, TestBean>> sorter = newSorter();

        sorter.setSortKeys(Collections.singletonList(new SortKey(0, SortOrder.ASCENDING)));

        checkSection(sorter, 0, "1");
        checkConversion(sorter, 1, "B");
        checkConversion(sorter, 2, "D");
        checkSection(sorter, 3, "2");
        checkConversion(sorter, 4, "A");
        checkConversion(sorter, 5, "C");
    }

    private void checkSection(SectionTableRowSorter<TestBean, BeanListMultimapTableModel<String, TestBean>> sorter, int viewIndex, String name) {
        assertThat(tableModel.getSectionName(sorter.convertRowIndexToModel(viewIndex))).isEqualTo(name);
        assertThat(tableModel.isSectionRow(sorter.convertRowIndexToModel(viewIndex))).isTrue();
    }

    @Test
    public void sortDescending() throws Exception {
        tableModel.put("1", new TestBean("B", "yy"));
        tableModel.put("1", new TestBean("D", "yy"));
        tableModel.put("2", new TestBean("C", "xx"));
        tableModel.put("2", new TestBean("A", "xx"));
        SectionTableRowSorter<TestBean, BeanListMultimapTableModel<String, TestBean>> sorter = newSorter();

        sorter.setSortKeys(Collections.singletonList(new SortKey(0, SortOrder.DESCENDING)));

        checkSection(sorter, 0, "1");
        checkConversion(sorter, 1, "D");
        checkConversion(sorter, 2, "B");
        checkSection(sorter, 3, "2");
        checkConversion(sorter, 4, "C");
        checkConversion(sorter, 5, "A");
    }

    @Test
    public void toggleSort() throws Exception {
        tableModel.put("1", new TestBean("B", "yy"));
        tableModel.put("1", new TestBean("D", "yy"));
        tableModel.put("2", new TestBean("C", "xx"));
        tableModel.put("2", new TestBean("A", "xx"));
        SectionTableRowSorter<TestBean, BeanListMultimapTableModel<String, TestBean>> sorter = newSorter();

        sorter.toggleSortOrder(0);

        checkSection(sorter, 0, "1");
        checkConversion(sorter, 1, "B");
        checkConversion(sorter, 2, "D");
        checkSection(sorter, 3, "2");
        checkConversion(sorter, 4, "A");
        checkConversion(sorter, 5, "C");

        sorter.toggleSortOrder(0);

        checkSection(sorter, 0, "1");
        checkConversion(sorter, 1, "D");
        checkConversion(sorter, 2, "B");
        checkSection(sorter, 3, "2");
        checkConversion(sorter, 4, "C");
        checkConversion(sorter, 5, "A");
    }

    @Test
    public void insertGroup() throws Exception {
        tableModel.put("1", new TestBean("B", "yy"));
        tableModel.put("1", new TestBean("D", "yy"));
        tableModel.put("3", new TestBean("C", "xx"));
        tableModel.put("3", new TestBean("A", "xx"));
        SectionTableRowSorter<TestBean, BeanListMultimapTableModel<String, TestBean>> sorter = newSorter();
        sorter.setSortKeys(Collections.singletonList(new SortKey(0, SortOrder.DESCENDING)));

        tableModel.put("2", new TestBean("Z", "??"));
        sorter.rowsInserted(modelEvent.getFirstRow(), modelEvent.getLastRow());
        sorter.toggleSortOrder(0);

        checkSection(sorter, 0, "1");
        checkConversion(sorter, 1, "B");
        checkConversion(sorter, 2, "D");
        checkSection(sorter, 3, "2");
        checkConversion(sorter, 4, "Z");
        checkSection(sorter, 5, "3");
        checkConversion(sorter, 6, "A");
        checkConversion(sorter, 7, "C");
    }

    @Test
    public void appendEndGroup() throws Exception {
        tableModel.put("1", new TestBean("B", "yy"));
        tableModel.put("2", new TestBean("Z", "xx"));
        SectionTableRowSorter<TestBean, BeanListMultimapTableModel<String, TestBean>> sorter = newSorter();
        sorter.setSortKeys(Collections.singletonList(new SortKey(0, SortOrder.DESCENDING)));

        tableModel.put("2", new TestBean("A", "??"));
        sorter.rowsInserted(modelEvent.getFirstRow(), modelEvent.getLastRow());
        sorter.toggleSortOrder(0);

        checkSection(sorter, 0, "1");
        checkConversion(sorter, 1, "B");
        checkSection(sorter, 2, "2");
        checkConversion(sorter, 3, "A");
        checkConversion(sorter, 4, "Z");
    }

    @Test
    public void appendMiddleGroup() throws Exception {
        tableModel.put("1", new TestBean("B", "yy"));
        tableModel.put("2", new TestBean("X", "xx"));
        tableModel.put("3", new TestBean("A", "xx"));
        SectionTableRowSorter<TestBean, BeanListMultimapTableModel<String, TestBean>> sorter = newSorter();
        sorter.setSortKeys(Collections.singletonList(new SortKey(0, SortOrder.DESCENDING)));

        tableModel.put("2", new TestBean("C", "??"));
        sorter.rowsInserted(modelEvent.getFirstRow(), modelEvent.getLastRow());
        sorter.toggleSortOrder(0);

        checkSection(sorter, 0, "1");
        checkConversion(sorter, 1, "B");
        checkSection(sorter, 2, "2");
        checkConversion(sorter, 3, "C");
        checkConversion(sorter, 4, "X");
        checkSection(sorter, 5, "3");
        checkConversion(sorter, 6, "A");
    }

    @Test
    public void appendGroups() throws Exception {
        tableModel.put("1", new TestBean("B", "yy"));
        tableModel.put("2", new TestBean("A", "xx"));
        SectionTableRowSorter<TestBean, BeanListMultimapTableModel<String, TestBean>> sorter = newSorter();
        sorter.setSortKeys(Collections.singletonList(new SortKey(0, SortOrder.DESCENDING)));

        tableModel.put("3", new TestBean("C", "??"));
        int firstRow = modelEvent.getFirstRow();
        tableModel.put("4", new TestBean("M", "$$"));
        sorter.rowsInserted(firstRow, modelEvent.getLastRow());
        sorter.toggleSortOrder(0);

        checkSection(sorter, 0, "1");
        checkConversion(sorter, 1, "B");
        checkSection(sorter, 2, "2");
        checkConversion(sorter, 3, "A");
        checkSection(sorter, 4, "3");
        checkConversion(sorter, 5, "C");
        checkSection(sorter, 6, "4");
        checkConversion(sorter, 7, "M");
    }

    @Test
    public void removeBean() throws Exception {
        TestBean bean = new TestBean("B", "yy");
        tableModel.put("1", new TestBean("D", "yy"));
        tableModel.put("1", bean);
        tableModel.put("2", new TestBean("A", "yy"));
        tableModel.put("2", new TestBean("B", "xx"));
        SectionTableRowSorter<TestBean, BeanListMultimapTableModel<String, TestBean>> sorter = newSorter();
        sorter.setSortKeys(Collections.singletonList(new SortKey(0, SortOrder.DESCENDING)));

        tableModel.remove(2);
        sorter.rowsDeleted(modelEvent.getFirstRow(), modelEvent.getLastRow());
        sorter.toggleSortOrder(0);

        checkSection(sorter, 0, "1");
        checkConversion(sorter, 1, "D");
        checkSection(sorter, 2, "2");
        checkConversion(sorter, 3, "A");
        checkConversion(sorter, 4, "B");
    }

    @Test
    public void removeMiddleGroup() throws Exception {
        TestBean bean = new TestBean("A", "xx");
        tableModel.put("1", new TestBean("B", "yy"));
        tableModel.put("1", new TestBean("C", "yy"));
        tableModel.put("2", bean);
        tableModel.put("3", new TestBean("X", "yy"));
        tableModel.put("3", new TestBean("B", "yy"));
        SectionTableRowSorter<TestBean, BeanListMultimapTableModel<String, TestBean>> sorter = newSorter();
        sorter.setSortKeys(Collections.singletonList(new SortKey(0, SortOrder.DESCENDING)));

        tableModel.remove(4);
        sorter.rowsDeleted(modelEvent.getFirstRow(), modelEvent.getLastRow());
        sorter.toggleSortOrder(0);

        checkSection(sorter, 0, "1");
        checkConversion(sorter, 1, "B");
        checkConversion(sorter, 2, "C");
        checkSection(sorter, 3, "3");
        checkConversion(sorter, 4, "B");
        checkConversion(sorter, 5, "X");
    }

    @Test
    public void removeFirstGroup() throws Exception {
        TestBean bean = new TestBean("A", "xx");
        tableModel.put("0", bean);
        tableModel.put("1", new TestBean("C", "yy"));
        tableModel.put("1", new TestBean("B", "yy"));
        SectionTableRowSorter<TestBean, BeanListMultimapTableModel<String, TestBean>> sorter = newSorter();
        sorter.setSortKeys(Collections.singletonList(new SortKey(0, SortOrder.DESCENDING)));

        tableModel.remove(1);
        sorter.rowsDeleted(modelEvent.getFirstRow(), modelEvent.getLastRow());
        sorter.toggleSortOrder(0);

        checkSection(sorter, 0, "1");
        checkConversion(sorter, 1, "B");
        checkConversion(sorter, 2, "C");
    }

    @Test
    public void updateGroup() throws Exception {
        tableModel.put("1", new TestBean("C", "yy"));
        tableModel.put("2", new TestBean("A", "xx"));
        tableModel.put("3", new TestBean("B", "zz"));
        SectionTableRowSorter<TestBean, BeanListMultimapTableModel<String, TestBean>> sorter = newSorter();
        sorter.setSortKeys(Collections.singletonList(new SortKey(0, SortOrder.ASCENDING)));

        tableModel.putAll("9", tableModel.removeAll("2"));
        sorter.rowsUpdated(modelEvent.getFirstRow(), modelEvent.getLastRow());

        checkSection(sorter, 0, "1");
        checkConversion(sorter, 1, "C");
        checkSection(sorter, 2, "3");
        checkConversion(sorter, 3, "B");
        checkSection(sorter, 4, "9");
        checkConversion(sorter, 5, "A");
    }

    @Test
    public void updateSortedColumn() throws Exception {
        tableModel.put("1", new TestBean("C", "yy"));
        tableModel.put("1", new TestBean("D", "yy"));
        tableModel.put("2", new TestBean("A", "xx"));
        SectionTableRowSorter<TestBean, BeanListMultimapTableModel<String, TestBean>> sorter = newSorter();
        sorter.setSortKeys(Collections.singletonList(new SortKey(0, SortOrder.ASCENDING)));

        tableModel.setValueAt("Z", 1, 0);
        sorter.rowsUpdated(modelEvent.getFirstRow(), modelEvent.getLastRow(), 0);

        checkSection(sorter, 0, "1");
        checkConversion(sorter, 1, "D");
        checkConversion(sorter, 2, "Z");
        checkSection(sorter, 3, "2");
        checkConversion(sorter, 4, "A");
    }

    @Test
    public void updateUnsortedColumn() throws Exception {
        tableModel.put("1", new TestBean("C", "yy"));
        tableModel.put("2", new TestBean("A", "xx"));
        SectionTableRowSorter<TestBean, BeanListMultimapTableModel<String, TestBean>> sorter = newSorter();
        sorter.setSortKeys(Collections.singletonList(new SortKey(0, SortOrder.ASCENDING)));
        addSortListener(sorter);

        sorter.rowsUpdated(2, 2, 1);

        assertThat(sorterEvent).isNull();
    }

    @Test
    public void filterUnsorted() throws Exception {
        tableModel.put("1", new TestBean("C", "yy"));
        tableModel.put("1", new TestBean("D", "yy"));
        tableModel.put("2", new TestBean("A", "xx"));
        SectionTableRowSorter<TestBean, BeanListMultimapTableModel<String, TestBean>> sorter = newSorter();

        sorter.setRowFilter(input -> !"D".equalsIgnoreCase(input.name));

        assertThat(sorter.getModelRowCount()).isEqualTo(tableModel.getRowCount());
        assertThat(sorter.getViewRowCount()).isEqualTo(4);
        assertThat(sorter.convertRowIndexToView(2)).isEqualTo(-1);
        assertThat(sorter.convertRowIndexToModel(0)).isEqualTo(0);
        assertThat(sorter.convertRowIndexToModel(1)).isEqualTo(1);
        assertThat(sorter.convertRowIndexToModel(2)).isEqualTo(3);
        assertThat(sorter.convertRowIndexToModel(3)).isEqualTo(4);
    }

    @Test
    public void removeFiltering() throws Exception {
        tableModel.put("1", new TestBean("C", "yy"));
        tableModel.put("1", new TestBean("D", "yy"));
        tableModel.put("2", new TestBean("A", "xx"));
        SectionTableRowSorter<TestBean, BeanListMultimapTableModel<String, TestBean>> sorter = newSorter();
        sorter.setRowFilter(input -> !"D".equalsIgnoreCase(input.name));

        sorter.setRowFilter(null);

        assertThat(sorter.getModelRowCount()).isEqualTo(tableModel.getRowCount());
        assertThat(sorter.getViewRowCount()).isEqualTo(tableModel.getRowCount());
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            assertThat(sorter.convertRowIndexToModel(i)).isEqualTo(i);
        }
    }

    @Test
    public void hideEntirelyFilteredSection() throws Exception {
        tableModel.put("1", new TestBean("C", "yy"));
        tableModel.put("1", new TestBean("D", "yy"));
        tableModel.put("2", new TestBean("A", "xx"));
        SectionTableRowSorter<TestBean, BeanListMultimapTableModel<String, TestBean>> sorter = newSorter();

        sorter.setRowFilter(input -> !"A".equalsIgnoreCase(input.name));

        assertThat(sorter.getModelRowCount()).isEqualTo(tableModel.getRowCount());
        assertThat(sorter.getViewRowCount()).isEqualTo(3);
        assertThat(sorter.convertRowIndexToView(3)).isEqualTo(-1);
        assertThat(sorter.convertRowIndexToView(4)).isEqualTo(-1);
        assertThat(sorter.convertRowIndexToModel(0)).isEqualTo(0);
        assertThat(sorter.convertRowIndexToModel(1)).isEqualTo(1);
        assertThat(sorter.convertRowIndexToModel(2)).isEqualTo(2);
    }

    @Test
    public void updateFilteredRow() throws Exception {
        tableModel.put("1", new TestBean("C", "yy"));
        tableModel.put("1", new TestBean("D", "yy"));
        tableModel.put("2", new TestBean("A", "xx"));
        tableModel.put("3", new TestBean("E", "xx"));
        SectionTableRowSorter<TestBean, BeanListMultimapTableModel<String, TestBean>> sorter = newSorter();
        sorter.setRowFilter(input -> !"A".equalsIgnoreCase(input.name));

        tableModel.setValueAt("B", 4, 0);
        sorter.rowsUpdated(modelEvent.getFirstRow(), modelEvent.getLastRow());

        assertThat(sorter.getModelRowCount()).isEqualTo(tableModel.getRowCount());
        assertThat(sorter.getViewRowCount()).isEqualTo(tableModel.getRowCount());
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            assertThat(sorter.convertRowIndexToModel(i)).isEqualTo(i);
        }
    }

    @Test
    public void updateFilteredRowColumn() throws Exception {
        tableModel.put("1", new TestBean("C", "yy"));
        tableModel.put("1", new TestBean("D", "yy"));
        tableModel.put("2", new TestBean("A", "xx"));
        SectionTableRowSorter<TestBean, BeanListMultimapTableModel<String, TestBean>> sorter = newSorter();
        sorter.setRowFilter(input -> !"A".equalsIgnoreCase(input.name));

        tableModel.setValueAt("zz", 4, 1);
        sorter.rowsUpdated(modelEvent.getFirstRow(), modelEvent.getLastRow(), modelEvent.getColumn());

        assertThat(sorter.getModelRowCount()).isEqualTo(tableModel.getRowCount());
        assertThat(sorter.getViewRowCount()).isEqualTo(3);
        assertThat(sorter.convertRowIndexToView(3)).isEqualTo(-1);
        assertThat(sorter.convertRowIndexToView(4)).isEqualTo(-1);
        assertThat(sorter.convertRowIndexToModel(0)).isEqualTo(0);
        assertThat(sorter.convertRowIndexToModel(1)).isEqualTo(1);
        assertThat(sorter.convertRowIndexToModel(2)).isEqualTo(2);
    }

    @Test
    public void addFilteredRowToHiddenGroup() throws Exception {
        tableModel.put("1", new TestBean("C", "yy"));
        tableModel.put("1", new TestBean("D", "yy"));
        tableModel.put("2", new TestBean("A", "xx"));
        tableModel.put("3", new TestBean("Z", "uu"));
        SectionTableRowSorter<TestBean, BeanListMultimapTableModel<String, TestBean>> sorter = newSorter();
        sorter.setRowFilter(input -> !"A".equalsIgnoreCase(input.name));

        tableModel.put("2", new TestBean("a", "zz"));
        sorter.rowsInserted(modelEvent.getFirstRow(), modelEvent.getLastRow());

        assertThat(sorter.getModelRowCount()).isEqualTo(tableModel.getRowCount());
        assertThat(sorter.getViewRowCount()).isEqualTo(5);
        assertThat(sorter.convertRowIndexToView(3)).isEqualTo(-1);
        assertThat(sorter.convertRowIndexToView(4)).isEqualTo(-1);
        assertThat(sorter.convertRowIndexToView(5)).isEqualTo(-1);
        assertThat(sorter.convertRowIndexToModel(0)).isEqualTo(0);
        assertThat(sorter.convertRowIndexToModel(1)).isEqualTo(1);
        assertThat(sorter.convertRowIndexToModel(2)).isEqualTo(2);
        assertThat(sorter.convertRowIndexToModel(3)).isEqualTo(6);
        assertThat(sorter.convertRowIndexToModel(4)).isEqualTo(7);
    }

    @Test
    public void addUnfilteredRowToHiddenGroup() throws Exception {
        tableModel.put("1", new TestBean("C", "yy"));
        tableModel.put("1", new TestBean("D", "yy"));
        tableModel.put("2", new TestBean("A", "xx"));
        tableModel.put("3", new TestBean("Z", "uu"));
        SectionTableRowSorter<TestBean, BeanListMultimapTableModel<String, TestBean>> sorter = newSorter();
        sorter.setRowFilter(input -> !"A".equalsIgnoreCase(input.name));

        tableModel.put("2", new TestBean("E", "zz"));
        sorter.rowsInserted(modelEvent.getFirstRow(), modelEvent.getLastRow());

        assertThat(sorter.getModelRowCount()).isEqualTo(tableModel.getRowCount());
        assertThat(sorter.getViewRowCount()).isEqualTo(7);
        assertThat(sorter.convertRowIndexToView(4)).isEqualTo(-1);
        assertThat(sorter.convertRowIndexToModel(0)).isEqualTo(0);
        assertThat(sorter.convertRowIndexToModel(1)).isEqualTo(1);
        assertThat(sorter.convertRowIndexToModel(2)).isEqualTo(2);
        assertThat(sorter.convertRowIndexToModel(3)).isEqualTo(3);
        assertThat(sorter.convertRowIndexToModel(4)).isEqualTo(5);
        assertThat(sorter.convertRowIndexToModel(5)).isEqualTo(6);
        assertThat(sorter.convertRowIndexToModel(6)).isEqualTo(7);
    }

    @Test
    public void addUnfilteredRowToHiddenGroupWithSort() throws Exception {
        tableModel.put("1", new TestBean("Z", "yy"));
        tableModel.put("1", new TestBean("D", "yy"));
        tableModel.put("2", new TestBean("A", "xx"));
        tableModel.put("3", new TestBean("C", "uu"));
        SectionTableRowSorter<TestBean, BeanListMultimapTableModel<String, TestBean>> sorter = newSorter();
        sorter.setSortKeys(Collections.singletonList(new SortKey(0, SortOrder.ASCENDING)));
        sorter.setRowFilter(input -> !"A".equalsIgnoreCase(input.name));

        tableModel.put("2", new TestBean("E", "zz"));
        sorter.rowsInserted(modelEvent.getFirstRow(), modelEvent.getLastRow());

        assertThat(sorter.getModelRowCount()).isEqualTo(tableModel.getRowCount());
        assertThat(sorter.getViewRowCount()).isEqualTo(7);
        assertThat(sorter.convertRowIndexToView(4)).isEqualTo(-1);
        assertThat(sorter.convertRowIndexToModel(0)).isEqualTo(0);
        assertThat(sorter.convertRowIndexToModel(1)).isEqualTo(2);
        assertThat(sorter.convertRowIndexToModel(2)).isEqualTo(1);
        assertThat(sorter.convertRowIndexToModel(3)).isEqualTo(3);
        assertThat(sorter.convertRowIndexToModel(4)).isEqualTo(5);
        assertThat(sorter.convertRowIndexToModel(5)).isEqualTo(6);
        assertThat(sorter.convertRowIndexToModel(6)).isEqualTo(7);
    }

    @Test
    public void addFilteredRowToVisibleGroup() throws Exception {
        tableModel.put("1", new TestBean("C", "yy"));
        tableModel.put("1", new TestBean("D", "yy"));
        tableModel.put("2", new TestBean("A", "xx"));
        tableModel.put("3", new TestBean("Z", "uu"));
        SectionTableRowSorter<TestBean, BeanListMultimapTableModel<String, TestBean>> sorter = newSorter();
        sorter.setRowFilter(input -> !"A".equalsIgnoreCase(input.name));

        tableModel.put("1", new TestBean("a", "zz"));
        sorter.rowsInserted(modelEvent.getFirstRow(), modelEvent.getLastRow());

        assertThat(sorter.getModelRowCount()).isEqualTo(tableModel.getRowCount());
        assertThat(sorter.getViewRowCount()).isEqualTo(5);
        assertThat(sorter.convertRowIndexToView(3)).isEqualTo(-1);
        assertThat(sorter.convertRowIndexToView(4)).isEqualTo(-1);
        assertThat(sorter.convertRowIndexToView(5)).isEqualTo(-1);
        assertThat(sorter.convertRowIndexToModel(0)).isEqualTo(0);
        assertThat(sorter.convertRowIndexToModel(1)).isEqualTo(1);
        assertThat(sorter.convertRowIndexToModel(2)).isEqualTo(2);
        assertThat(sorter.convertRowIndexToModel(3)).isEqualTo(6);
        assertThat(sorter.convertRowIndexToModel(4)).isEqualTo(7);
    }

    @Test
    public void removeFilteredRow() throws Exception {
        final TestBean bean = new TestBean("A", "xx");
        tableModel.put("1", new TestBean("C", "yy"));
        tableModel.put("1", new TestBean("D", "yy"));
        tableModel.put("2", bean);
        tableModel.put("3", new TestBean("Z", "uu"));
        SectionTableRowSorter<TestBean, BeanListMultimapTableModel<String, TestBean>> sorter = newSorter();
        sorter.setRowFilter(input -> !"A".equalsIgnoreCase(input.name));

        tableModel.remove(4);
        sorter.rowsDeleted(modelEvent.getFirstRow(), modelEvent.getLastRow());

        assertThat(sorter.getModelRowCount()).isEqualTo(tableModel.getRowCount());
        assertThat(sorter.getViewRowCount()).isEqualTo(tableModel.getRowCount());
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            assertThat(sorter.convertRowIndexToModel(i)).isEqualTo(i);
        }
    }

    @Test
    public void removeFilteredRows() throws Exception {
        tableModel.put("1", new TestBean("C", "yy"));
        tableModel.put("1", new TestBean("D", "yy"));
        tableModel.put("2", new TestBean("A", "xx"));
        tableModel.put("2", new TestBean("a", "uu"));
        tableModel.put("3", new TestBean("Z", "uu"));
        SectionTableRowSorter<TestBean, BeanListMultimapTableModel<String, TestBean>> sorter = newSorter();
        sorter.setRowFilter(input -> !"A".equalsIgnoreCase(input.name));

        tableModel.removeAll("2");
        sorter.rowsDeleted(modelEvent.getFirstRow(), modelEvent.getLastRow());

        assertThat(sorter.getModelRowCount()).isEqualTo(tableModel.getRowCount());
        assertThat(sorter.getViewRowCount()).isEqualTo(tableModel.getRowCount());
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            assertThat(sorter.convertRowIndexToModel(i)).isEqualTo(i);
        }
    }

    @Test
    public void removeUnfilteredRow() throws Exception {
        final TestBean bean = new TestBean("B", "uu");
        tableModel.put("1", new TestBean("C", "yy"));
        tableModel.put("1", new TestBean("D", "yy"));
        tableModel.put("2", new TestBean("A", "xx"));
        tableModel.put("2", bean);
        tableModel.put("3", new TestBean("Z", "uu"));
        SectionTableRowSorter<TestBean, BeanListMultimapTableModel<String, TestBean>> sorter = newSorter();
        sorter.setRowFilter(input -> !"A".equalsIgnoreCase(input.name));

        tableModel.remove(5);
        sorter.rowsDeleted(modelEvent.getFirstRow(), modelEvent.getLastRow());

        assertThat(sorter.getModelRowCount()).isEqualTo(tableModel.getRowCount());
        assertThat(sorter.getViewRowCount()).isEqualTo(5);
        assertThat(sorter.convertRowIndexToView(3)).isEqualTo(-1);
        assertThat(sorter.convertRowIndexToView(4)).isEqualTo(-1);
        assertThat(sorter.convertRowIndexToModel(0)).isEqualTo(0);
        assertThat(sorter.convertRowIndexToModel(1)).isEqualTo(1);
        assertThat(sorter.convertRowIndexToModel(2)).isEqualTo(2);
        assertThat(sorter.convertRowIndexToModel(3)).isEqualTo(5);
        assertThat(sorter.convertRowIndexToModel(4)).isEqualTo(6);
    }

    private static class TestBean {
        public String name;
        public String value;

        public TestBean(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String toString() {
            return String.format("TestBean{name=%s,value=%s}", name, value);
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
}
