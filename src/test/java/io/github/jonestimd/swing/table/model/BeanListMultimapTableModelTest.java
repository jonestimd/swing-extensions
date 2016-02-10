package io.github.jonestimd.swing.table.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import com.google.common.collect.Multimaps;
import io.github.jonestimd.swing.table.SectionTable;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static io.github.jonestimd.mockito.Matchers.*;
import static io.github.jonestimd.mockito.Matchers.matches;
import static org.fest.assertions.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BeanListMultimapTableModelTest {
    private static final com.google.common.base.Function<TestBean, TestGroup> GET_GROUP = input -> input.group;
    private static final Function<TestGroup, String> GET_GROUP_NAME = input -> input.groupName;
    private static final ColumnAdapter<TestBean, String> BEAN_NAME_ADAPTER = new TestColumnAdapter("Name") {
        @Override
        public String getValue(TestBean testBean) {
            return testBean.beanName;
        }
    };
    private static final ColumnAdapter<TestBean, String> BEAN_VALUE_ADAPTER = new TestColumnAdapter("Value") {
        @Override
        public String getValue(TestBean testBean) {
            return testBean.beanValue;
        }
    };
    @Mock
    private TableDataProvider<TestBean> dataProvider;
    @Mock
    private TableModelListener modelListener;

    private TestGroup group1 = new TestGroup("group1");
    private TestGroup group2 = new TestGroup("group2");
    private TestGroup group3 = new TestGroup("group3");

    @Test
    public void setBeansGroupsRows() throws Exception {
        BeanListMultimapTableModel<TestGroup, TestBean> tableModel = newTableModel();
        List<TestBean> beans = Arrays.asList(
                new TestBean(group1, "bean1a", "x"),
                new TestBean(group2, "bean2b", "x"),
                new TestBean(group1, "bean1b", "x"),
                new TestBean(group2, "bean2a", "x"));

        tableModel.setBeans(Multimaps.index(beans, GET_GROUP));

        assertThat(tableModel.getRowCount()).isEqualTo(6);
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
        tableModel.setBeans(Multimaps.index(Arrays.asList(
                new TestBean(group1, "bean1a", "x"),
                new TestBean(group2, "bean2b", "x"),
                new TestBean(group1, "bean1b", "x"),
                new TestBean(group2, "bean2a", "x")), GET_GROUP));
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
        tableModel.setBeans(Multimaps.index(Arrays.asList(
                new TestBean(group1, "bean1a", "x"),
                new TestBean(group3, "bean2b", "x"),
                new TestBean(group1, "bean1b", "x"),
                new TestBean(group3, "bean2a", "x")), GET_GROUP));

        TestBean bean = new TestBean(group2, "bean0", "x");
        tableModel.put(group2, bean);

        assertThat(tableModel.getRowCount()).isEqualTo(8);
        assertSectionRow(tableModel, 3, group2);
        assertBeanRow(tableModel, 4, bean);
        verify(modelListener).tableChanged(matches(new TableModelEvent(tableModel, 3, 4, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT)));
        verify(dataProvider).addBean(bean);
    }

    @Test
    public void getBeans() throws Exception {
        BeanListMultimapTableModel<TestGroup, TestBean> tableModel = newTableModel();
        List<TestBean> beans = Arrays.asList(
                new TestBean(group1, "bean1a", "x"),
                new TestBean(group2, "bean2b", "x"),
                new TestBean(group1, "bean1b", "x"),
                new TestBean(group2, "bean2a", "x"));
        tableModel.setBeans(Multimaps.index(beans, GET_GROUP));

        List<TestBean> result = tableModel.getBeans();

        assertThat(result.stream().filter(Predicate.isEqual(null)).count()).isEqualTo(0);
        assertThat(result).hasSize(beans.size());
    }

    @Test
    public void getRow() throws Exception {
        BeanListMultimapTableModel<TestGroup, TestBean> tableModel = newTableModel();
        List<TestBean> beans = Arrays.asList(
                new TestBean(group1, "bean1a", "x"),
                new TestBean(group2, "bean2b", "x"),
                new TestBean(group1, "bean1b", "x"),
                new TestBean(group2, "bean2a", "x"));
        tableModel.setBeans(Multimaps.index(beans, GET_GROUP));

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
        tableModel.setBeans(Multimaps.index(beans, GET_GROUP));
        reset(modelListener);

        tableModel.remove(4);

        assertThat(tableModel.getRowCount()).isEqualTo(5);
        assertThat(tableModel.getBean(0)).isNull();
        assertThat(tableModel.getBean(1).beanName).isEqualTo("bean1a");
        assertThat(tableModel.getBean(2).beanName).isEqualTo("bean1b");
        assertThat(tableModel.getBean(3)).isNull();
        assertThat(tableModel.getBean(4).beanName).isEqualTo("bean2a");
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
        tableModel.setBeans(Multimaps.index(beans, GET_GROUP));
        reset(modelListener);

        tableModel.remove(1);
        tableModel.remove(1);

        assertThat(tableModel.getRowCount()).isEqualTo(3);
        assertThat(tableModel.getBean(0)).isNull();
        assertThat(tableModel.getBean(1).beanName).isEqualTo("bean2b");
        assertThat(tableModel.getBean(2).beanName).isEqualTo("bean2a");
        verify(modelListener).tableChanged(matches(new TableModelEvent(tableModel, 1, 1, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE)));
        verify(modelListener).tableChanged(matches(new TableModelEvent(tableModel, 0, 1, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE)));
        verify(dataProvider).removeBean(beans.get(0));
        verify(dataProvider).removeBean(beans.get(2));
    }

    @Test
    @Ignore
    public void viewTable() throws Exception {
        final BeanListMultimapTableModel<TestGroup, TestBean> tableModel = newTableModel();
        TestGroup group = new TestGroup("really long group name that overflows the cell");
        tableModel.setBeans(Multimaps.index(Arrays.asList(
                new TestBean(group, "bean1a", "x"),
                new TestBean(group2, "bean2b", "x"),
                new TestBean(group, "bean1b", "x"),
                new TestBean(group2, "bean2a", "x")), GET_GROUP));

        System.setProperty("swing.defaultlaf", "com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
        SwingUtilities.invokeAndWait(() -> {
            JDialog frame = new JDialog(JOptionPane.getRootFrame(), "Test", true);
            frame.setContentPane(new JScrollPane(new SectionTable<>(tableModel)));
            frame.pack();
            frame.setVisible(true);
        });
    }

    private void assertBeanRow(BeanListMultimapTableModel<TestGroup, TestBean> tableModel, int index, TestBean bean) {
        assertThat(tableModel.getValueAt(index, 0)).isEqualTo(bean.beanName);
        assertThat(tableModel.getValueAt(index, 1)).isEqualTo(bean.beanValue);
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
                Collections.singleton(dataProvider), GET_GROUP_NAME);
        model.addTableModelListener(modelListener);
        return model;
    }

    private static class TestGroup {
        private final String groupName;

        private TestGroup(String groupName) {
            this.groupName = groupName;
        }
    }

    private static class TestBean {
        private TestGroup group;
        private final String beanName;
        private final String beanValue;

        private TestBean(TestGroup group, String beanName, String beanValue) {
            this.group = group;
            this.beanName = beanName;
            this.beanValue = beanValue;
        }
    }

    private static abstract class TestColumnAdapter implements ColumnAdapter<TestBean, String> {
        private final String columnId;

        protected TestColumnAdapter(String columnId) {
            this.columnId = columnId;
        }

        @Override
        public String getColumnId() {
            return columnId;
        }

        @Override
        public String getResource(String name, String defaultValue) {
            return defaultValue;
        }

        @Override
        public String getName() {
            return columnId;
        }

        @Override
        public Class<String> getType() {
            return String.class;
        }

        @Override
        public boolean isEditable(TestBean row) {
            return false;
        }

        @Override
        public void setValue(TestBean testBean, String s) {
            throw new UnsupportedOperationException();
        }
    }
}
