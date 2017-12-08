package io.github.jonestimd.swing.table.model;

import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import com.google.common.collect.Lists;
import io.github.jonestimd.swing.table.SectionTable;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static io.github.jonestimd.mockito.Matchers.*;
import static io.github.jonestimd.mockito.Matchers.matches;
import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BeanListMultimapTableModelTest {
    private static final ColumnAdapter<TestBean, String> BEAN_NAME_ADAPTER = new TestColumnAdapter<>("Name", String.class, TestBean::getName);
    private static final ColumnAdapter<TestBean, String> BEAN_VALUE_ADAPTER = new TestColumnAdapter<>("Value", String.class, TestBean::getValue, TestBean::setValue);
    @Mock
    private TableDataProvider<TestBean> dataProvider;
    @Mock
    private TableModelListener modelListener;
    @Mock
    private ColumnAdapter<TestBean, String> mockColumnAdapter;

    private TestGroup group1 = new TestGroup("group1");
    private TestGroup group2 = new TestGroup("group2");
    private TestGroup group3 = new TestGroup("group3");

    @Test
    public void columnIdentifier() throws Exception {
        BeanListMultimapTableModel<TestGroup, TestBean> model = newTableModel();

        assertThat(model.getColumnCount()).isEqualTo(2);
        assertThat(model.getColumnIdentifier(0)).isSameAs(BEAN_NAME_ADAPTER);
        assertThat(model.getColumnIdentifier(1)).isSameAs(BEAN_VALUE_ADAPTER);
    }

    @Test
    public void columnName() throws Exception {
        BeanListMultimapTableModel<TestGroup, TestBean> model = newTableModel();

        assertThat(model.getColumnName(0)).isSameAs(BEAN_NAME_ADAPTER.getName());
        assertThat(model.getColumnName(1)).isSameAs(BEAN_VALUE_ADAPTER.getName());
    }

    @Test
    public void columnClass() throws Exception {
        BeanListMultimapTableModel<TestGroup, TestBean> model = newTableModel();

        assertThat(model.getColumnClass(0)).isSameAs(BEAN_NAME_ADAPTER.getType());
        assertThat(model.getColumnClass(1)).isSameAs(BEAN_VALUE_ADAPTER.getType());
    }

    @Test
    public void setBeansGroupsRows() throws Exception {
        BeanListMultimapTableModel<TestGroup, TestBean> tableModel = newTableModel();
        List<TestBean> beans = Arrays.asList(
                new TestBean(group1, "bean1a", "x"),
                new TestBean(group2, "bean2b", "x"),
                new TestBean(group1, "bean1b", "x"),
                new TestBean(group2, "bean2a", "x"));

        tableModel.setBeans(beans);

        assertThat(tableModel.getRowCount()).isEqualTo(6);
        assertThat(tableModel.getBeanCount()).isEqualTo(4);
        assertThat(tableModel.getBeans()).containsOnlyElementsOf(beans);
        assertThat(tableModel.getSections()).containsExactly(group1, group2);
        assertThat(tableModel.getGroup(0)).containsExactly(beans.get(0), beans.get(2));
        assertThat(tableModel.getBeans(group1)).containsExactly(beans.get(0), beans.get(2));
        assertThat(tableModel.getGroup(1)).containsExactly(beans.get(1), beans.get(3));
        assertThat(tableModel.getBeans(group2)).containsExactly(beans.get(1), beans.get(3));
        for (int i = 0; i < 6; i++) {
            assertThat(tableModel.getSectionRow(i)).isEqualTo((i / 3) * 3);
        }
        assertThat(tableModel.getValue(beans.get(0), 0)).isEqualTo(beans.get(0).name);
        assertThat(tableModel.getValue(beans.get(0), 1)).isEqualTo(beans.get(0).value);
        assertSectionRow(tableModel, 0, group1);
        assertBeanRow(tableModel, 1, beans.get(0));
        assertBeanRow(tableModel, 2, beans.get(2));
        assertSectionRow(tableModel, 3, group2);
        assertBeanRow(tableModel, 4, beans.get(1));
        assertBeanRow(tableModel, 5, beans.get(3));
        verify(modelListener).tableChanged(matches(new TableModelEvent(tableModel)));
        verify(dataProvider).setBeans(containsOnly(beans));
    }

    @Test
    public void putAppendsToExistingGroup() throws Exception {
        BeanListMultimapTableModel<TestGroup, TestBean> tableModel = newTableModel();
        tableModel.setBeans(Arrays.asList(
                new TestBean(group1, "bean1a", "x"),
                new TestBean(group2, "bean2b", "x"),
                new TestBean(group1, "bean1b", "x"),
                new TestBean(group2, "bean2a", "x")));
        reset(modelListener);

        TestBean bean = new TestBean(group1, "bean0", "x");
        tableModel.put(group1, bean);

        assertThat(tableModel.getRowCount()).isEqualTo(7);
        assertBeanRow(tableModel, 3, bean);
        verify(modelListener).tableChanged(matches(new TableModelEvent(tableModel, 3, 3, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT)));
        verify(dataProvider).addBean(bean);
    }

    @Test
    public void putInsertsNewGroup() throws Exception {
        BeanListMultimapTableModel<TestGroup, TestBean> tableModel = newTableModel();
        tableModel.setBeans(Arrays.asList(
                new TestBean(group1, "bean1a", "x"),
                new TestBean(group3, "bean2b", "x"),
                new TestBean(group1, "bean1b", "x"),
                new TestBean(group3, "bean2a", "x")));

        TestBean bean = new TestBean(group2, "bean0", "x");
        tableModel.put(group2, bean);

        assertThat(tableModel.getRowCount()).isEqualTo(8);
        assertSectionRow(tableModel, 3, group2);
        assertBeanRow(tableModel, 4, bean);
        verify(modelListener).tableChanged(matches(new TableModelEvent(tableModel, 3, 4, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT)));
        verify(dataProvider).addBean(bean);
    }

    @Test
    public void removeAll() throws Exception {
        BeanListMultimapTableModel<TestGroup, TestBean> tableModel = newTableModel();
        List<TestBean> beans = Arrays.asList(
                new TestBean(group1, "bean1a", "x"),
                new TestBean(group2, "bean2b", "x"),
                new TestBean(group1, "bean1b", "x"),
                new TestBean(group2, "bean2a", "x"));
        tableModel.setBeans(beans);

        List<TestBean> result = tableModel.removeAll(group1);

        assertThat(result).containsOnly(beans.get(0), beans.get(2));
        assertThat(tableModel.getSections()).containsOnly(group2);
        assertThat(tableModel.getBeanCount()).isEqualTo(2);
        assertThat(tableModel.getRowCount()).isEqualTo(3);
        verify(modelListener).tableChanged(matches(new TableModelEvent(tableModel, 0, 2, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE)));
    }

    @Test
    public void putAllAddsNewGroup() throws Exception {
        BeanListMultimapTableModel<TestGroup, TestBean> tableModel = newTableModel();
        List<TestBean> beans = Arrays.asList(
                new TestBean(group1, "bean1a", "x"),
                new TestBean(group3, "bean3b", "x"),
                new TestBean(group1, "bean1b", "x"),
                new TestBean(group3, "bean3a", "x"));
        tableModel.setBeans(beans);

        tableModel.putAll(group2, Lists.newArrayList(new TestBean(group2, "bean2a", "y")));

        assertThat(tableModel.getSections()).containsExactly(group1, group2, group3);
        assertThat(tableModel.getBeanCount()).isEqualTo(5);
        assertThat(tableModel.getRowCount()).isEqualTo(8);
        verify(modelListener).tableChanged(matches(new TableModelEvent(tableModel, 3, 4, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT)));
    }

    @Test
    public void addBeanAddsBeansToGroup() throws Exception {
        TestBean bean = new TestBean(group3, "bean3c", "y");
        BeanListMultimapTableModel<TestGroup, TestBean> tableModel = newTableModel();
        List<TestBean> beans = Arrays.asList(
                new TestBean(group1, "bean1a", "x"),
                new TestBean(group3, "bean3b", "x"),
                new TestBean(group1, "bean1b", "x"),
                new TestBean(group3, "bean3a", "x"));
        tableModel.setBeans(beans);

        tableModel.addBean(bean);

        assertThat(tableModel.getSections()).containsExactly(group1, group3);
        assertThat(tableModel.getBeans(group3)).contains(bean);
        assertThat(tableModel.getBeanCount()).isEqualTo(5);
        assertThat(tableModel.getRowCount()).isEqualTo(7);
        verify(modelListener).tableChanged(matches(new TableModelEvent(tableModel, 6, 6, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT)));
    }

    @Test
    public void putAllAddsBeansToGroup() throws Exception {
        BeanListMultimapTableModel<TestGroup, TestBean> tableModel = newTableModel();
        List<TestBean> beans = Arrays.asList(
                new TestBean(group1, "bean1a", "x"),
                new TestBean(group3, "bean3b", "x"),
                new TestBean(group1, "bean1b", "x"),
                new TestBean(group3, "bean3a", "x"));
        tableModel.setBeans(beans);

        tableModel.putAll(group3, Lists.newArrayList(new TestBean(group3, "bean3c", "y"), new TestBean(group3, "bean3d", "y")));

        assertThat(tableModel.getSections()).containsExactly(group1, group3);
        assertThat(tableModel.getBeanCount()).isEqualTo(6);
        assertThat(tableModel.getRowCount()).isEqualTo(8);
        verify(modelListener).tableChanged(matches(new TableModelEvent(tableModel, 6, 7, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT)));
    }

    @Test
    public void getRow() throws Exception {
        BeanListMultimapTableModel<TestGroup, TestBean> tableModel = newTableModel();
        List<TestBean> beans = Arrays.asList(
                new TestBean(group1, "bean1a", "x"),
                new TestBean(group2, "bean2b", "x"),
                new TestBean(group1, "bean1b", "x"),
                new TestBean(group2, "bean2a", "x"));
        tableModel.setBeans(beans);

        assertThat(tableModel.getBean(0)).isNull();
        assertThat(tableModel.getBean(1)).isSameAs(beans.get(0));
        assertThat(tableModel.getBean(2)).isSameAs(beans.get(2));
        assertThat(tableModel.getBean(3)).isNull();
        assertThat(tableModel.getBean(4)).isSameAs(beans.get(1));
        assertThat(tableModel.getBean(5)).isSameAs(beans.get(3));
    }

    @Test
    public void removeRow() throws Exception {
        BeanListMultimapTableModel<TestGroup, TestBean> tableModel = newTableModel();
        List<TestBean> beans = Arrays.asList(
                new TestBean(group1, "bean1a", "x"),
                new TestBean(group2, "bean2b", "x"),
                new TestBean(group1, "bean1b", "x"),
                new TestBean(group2, "bean2a", "x"));
        tableModel.setBeans(beans);
        reset(modelListener);

        tableModel.remove(4);

        assertThat(tableModel.getRowCount()).isEqualTo(5);
        assertThat(tableModel.getBean(0)).isNull();
        assertThat(tableModel.getBean(1).name).isEqualTo("bean1a");
        assertThat(tableModel.getBean(2).name).isEqualTo("bean1b");
        assertThat(tableModel.getBean(3)).isNull();
        assertThat(tableModel.getBean(4).name).isEqualTo("bean2a");
        verify(modelListener).tableChanged(matches(new TableModelEvent(tableModel, 4, 4, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE)));
        verify(dataProvider).removeBean(beans.get(1));
    }

    @Test
    public void removeRowDeletesGroupIfEmpty() throws Exception {
        BeanListMultimapTableModel<TestGroup, TestBean> tableModel = newTableModel();
        List<TestBean> beans = Arrays.asList(
                new TestBean(group1, "bean1a", "x"),
                new TestBean(group2, "bean2b", "x"),
                new TestBean(group1, "bean1b", "x"),
                new TestBean(group2, "bean2a", "x"));
        tableModel.setBeans(beans);
        reset(modelListener);

        tableModel.remove(1);
        tableModel.remove(1);

        assertThat(tableModel.getRowCount()).isEqualTo(3);
        assertThat(tableModel.getBean(0)).isNull();
        assertThat(tableModel.getBean(1).name).isEqualTo("bean2b");
        assertThat(tableModel.getBean(2).name).isEqualTo("bean2a");
        verify(modelListener).tableChanged(matches(new TableModelEvent(tableModel, 1, 1, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE)));
        verify(modelListener).tableChanged(matches(new TableModelEvent(tableModel, 0, 1, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE)));
        verify(dataProvider).removeBean(beans.get(0));
        verify(dataProvider).removeBean(beans.get(2));
    }

    @Test
    public void indexOf() throws Exception {
        BeanListMultimapTableModel<TestGroup, TestBean> tableModel = newTableModel();
        List<TestBean> beans = Arrays.asList(
                new TestBean(group1, "bean1a", "x"),
                new TestBean(group2, "bean2b", "x"),
                new TestBean(group1, "bean1b", "x"),
                new TestBean(group2, "bean2a", "x"));
        tableModel.setBeans(beans);

        assertThat(tableModel.indexOf(bean -> bean.name.equals("bean2b"))).isEqualTo(4);
        assertThat(tableModel.indexOf(bean -> bean.name.equals("bean2a"))).isEqualTo(5);
    }

    @Test
    public void isCellEditable() throws Exception {
        BeanListMultimapTableModel<TestGroup, TestBean> tableModel = newTableModel();
        List<TestBean> beans = Arrays.asList(
                new TestBean(group1, "bean1a", "x"),
                new TestBean(group1, "bean1b", "x"));
        tableModel.setBeans(beans);

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            assertThat(tableModel.isCellEditable(i, 0)).isFalse();
            assertThat(tableModel.isCellEditable(i, 1)).isTrue();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void setValueAtThrowsExceptionForSectionRow() throws Exception {
        BeanListMultimapTableModel<TestGroup, TestBean> tableModel = newTableModel();
        List<TestBean> beans = Arrays.asList(
                new TestBean(group1, "bean1a", "x"),
                new TestBean(group1, "bean1b", "x"));
        tableModel.setBeans(beans);

        tableModel.setValueAt("x", 0, 0);
    }

    @Test
    public void setValueAt() throws Exception {
        BeanListMultimapTableModel<TestGroup, TestBean> tableModel = newTableModel();
        List<TestBean> beans = Arrays.asList(
                new TestBean(group1, "bean1a", "x"),
                new TestBean(group1, "bean1b", "x"));
        tableModel.setBeans(beans);

        tableModel.setValueAt("x", 1, 1);

        assertThat(beans.get(1).getValue()).isEqualTo("x");
    }

    @Test
    public void updateBeansAddsRows() throws Exception {
        TestBean bean1 = new TestBean(group1, "bean1c", "x");
        TestBean bean2 = new TestBean(group2, "bean2a", "x");
        BeanListMultimapTableModel<TestGroup, TestBean> tableModel = newTableModel();
        List<TestBean> beans = Arrays.asList(
                new TestBean(group1, "bean1a", "x"),
                new TestBean(group1, "bean1b", "x"));
        tableModel.setBeans(beans);

        tableModel.updateBeans(Arrays.asList(bean1, bean2), TestBean::equals);

        assertThat(tableModel.getBeanCount()).isEqualTo(4);
        assertThat(tableModel.getBeans(group1)).containsOnly(beans.get(0), beans.get(1), bean1);
        assertThat(tableModel.getBeans(group2)).containsOnly(bean2);
        verify(modelListener).tableChanged(matches(new TableModelEvent(tableModel, 3, 3, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT)));
        verify(modelListener).tableChanged(matches(new TableModelEvent(tableModel, 4, 5, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT)));
    }

    @Test
    public void updateBeansReplacesRows() throws Exception {
        TestBean bean1 = new TestBean(group1, "bean1b", "y");
        BeanListMultimapTableModel<TestGroup, TestBean> tableModel = newTableModel();
        List<TestBean> beans = Arrays.asList(
                new TestBean(group1, "bean1a", "x"),
                new TestBean(group1, "bean1b", "x"));
        tableModel.setBeans(beans);

        tableModel.updateBeans(singletonList(bean1), (b1, b2) -> b1.name.equals(b2.name));

        assertThat(tableModel.getBeanCount()).isEqualTo(2);
        assertThat(tableModel.getBeans(group1)).containsOnly(beans.get(0), bean1);
        verify(modelListener).tableChanged(matches(new TableModelEvent(tableModel, 2, 2, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE)));
    }

    @Test
    public void getCursorCallsColumnAdapter() throws Exception {
        MouseEvent event = mock(MouseEvent.class);
        JTable table = mock(JTable.class);
        TestBean bean1 = new TestBean(group1, "bean1b", "y");
        BeanListMultimapTableModel<TestGroup, TestBean> tableModel = newTableModel(mockColumnAdapter);
        List<TestBean> beans = Arrays.asList(
                new TestBean(group1, "bean1a", "x"),
                new TestBean(group1, "bean1b", "x"));
        tableModel.setBeans(beans);

        tableModel.getCursor(event, table, 1, 0);

        verify(mockColumnAdapter).getCursor(event, table, beans.get(0));
    }

    @Test
    public void handleClickCallsColumnAdapter() throws Exception {
        MouseEvent event = mock(MouseEvent.class);
        JTable table = mock(JTable.class);
        TestBean bean1 = new TestBean(group1, "bean1b", "y");
        BeanListMultimapTableModel<TestGroup, TestBean> tableModel = newTableModel(mockColumnAdapter);
        List<TestBean> beans = Arrays.asList(
                new TestBean(group1, "bean1a", "x"),
                new TestBean(group1, "bean1b", "x"));
        tableModel.setBeans(beans);

        tableModel.handleClick(event, table, 1, 0);

        verify(mockColumnAdapter).handleClick(event, table, beans.get(0));
    }

    @Test
    public void notifyDataProviders() throws Exception {
        TestBean bean1 = new TestBean(group1, "bean1b", "y");
        BeanListMultimapTableModel<TestGroup, TestBean> tableModel = newTableModel();

        tableModel.notifyDataProviders(bean1, "column id", "x");

        verify(dataProvider).updateBean(bean1, "column id", "x");
    }

    @Test
    @Ignore
    public void viewTable() throws Exception {
        final BeanListMultimapTableModel<TestGroup, TestBean> tableModel = newTableModel();
        TestGroup group = new TestGroup("really long group name that overflows the cell");
        tableModel.setBeans(Arrays.asList(
                new TestBean(group, "bean1a", "x"),
                new TestBean(group2, "bean2b", "x"),
                new TestBean(group, "bean1b", "x"),
                new TestBean(group2, "bean2a", "x")));

        System.setProperty("swing.defaultlaf", "com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
        SwingUtilities.invokeAndWait(() -> {
            JDialog frame = new JDialog(JOptionPane.getRootFrame(), "Test", true);
            frame.setContentPane(new JScrollPane(new SectionTable<>(tableModel)));
            frame.pack();
            frame.setVisible(true);
        });
    }

    private void assertBeanRow(BeanListMultimapTableModel<TestGroup, TestBean> tableModel, int index, TestBean bean) {
        assertThat(tableModel.getValueAt(index, 0)).isEqualTo(bean.name);
        assertThat(tableModel.getValueAt(index, 1)).isEqualTo(bean.value);
        assertThat(tableModel.getSectionName(index)).isEqualTo(bean.group.groupName);
        assertThat(tableModel.isSectionRow(index)).isFalse();
    }

    private void assertSectionRow(BeanListMultimapTableModel<TestGroup, TestBean> tableModel, int index, TestGroup group) {
        assertThat(tableModel.getValueAt(index, 0)).isNull();
        assertThat(tableModel.getValueAt(index, 1)).isNull();
        assertThat(tableModel.isSectionRow(index)).isTrue();
        assertThat(tableModel.getSectionName(index)).isEqualTo(group.groupName);
    }

    private BeanListMultimapTableModel<TestGroup, TestBean> newTableModel() {
        BeanListMultimapTableModel<TestGroup, TestBean> model = new BeanListMultimapTableModel<>(
                Arrays.asList(BEAN_NAME_ADAPTER, BEAN_VALUE_ADAPTER),
                Collections.singleton(dataProvider), TestBean::getGroup, TestGroup::getGroupName);
        model.addTableModelListener(modelListener);
        return model;
    }

    @SafeVarargs
    private final BeanListMultimapTableModel<TestGroup, TestBean> newTableModel(ColumnAdapter<TestBean, ?>... columnAdapters) {
        BeanListMultimapTableModel<TestGroup, TestBean> model = new BeanListMultimapTableModel<>(
                Arrays.asList(columnAdapters),
                Collections.singleton(dataProvider), TestBean::getGroup, TestGroup::getGroupName);
        model.addTableModelListener(modelListener);
        return model;
    }

    private static class TestGroup {
        private final String groupName;

        private TestGroup(String groupName) {
            this.groupName = groupName;
        }

        public String getGroupName() {
            return groupName;
        }
    }

    private static class TestBean {
        private TestGroup group;
        private final String name;
        private String value;

        public TestBean(TestGroup group, String name, String value) {
            this.group = group;
            this.name = name;
            this.value = value;
        }

        public TestGroup getGroup() {
            return group;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String beanValue) {
            this.value = beanValue;
        }
    }
}
