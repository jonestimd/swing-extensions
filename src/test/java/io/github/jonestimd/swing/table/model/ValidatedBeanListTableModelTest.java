package io.github.jonestimd.swing.table.model;

import java.util.Arrays;
import java.util.List;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import io.github.jonestimd.swing.validation.BeanPropertyValidator;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static java.util.Collections.*;
import static org.fest.assertions.Assertions.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ValidatedBeanListTableModelTest {
    private ValidatedBeanListTableModel<TestBean> model = new ValidatedBeanListTableModel<>(singletonList(new TestBeanColumnAdapter()));
    private TableModelListener listener = mock(TableModelListener.class);
    private ArgumentCaptor<TableModelEvent> eventCaptor = ArgumentCaptor.forClass(TableModelEvent.class);

    @Test
    public void setBeansClearsErrors() throws Exception {
        model.addTableModelListener(listener);
        model.setBeans(Arrays.asList(new TestBean(null), new TestBean("bean2")));
        assertThat(model.isNoErrors()).isFalse();
        assertThat(model.validateAt(0, 0)).isEqualTo("required");

        model.setBeans(Arrays.asList(new TestBean("bean1"), new TestBean("bean2")));

        assertThat(model.isNoErrors()).isTrue();
        assertThat(model.validateAt(0, 0)).isNull();
        verify(listener, times(2)).tableChanged(any(TableModelEvent.class));
    }

    @Test
    public void setValueAtErrors() throws Exception {
        model.setBeans(Arrays.asList(new TestBean("bean1"), new TestBean("bean2")));
        assertThat(model.isNoErrors()).isTrue();
        model.addTableModelListener(listener);

        model.setValueAt(null, 0, 0);

        assertThat(model.isNoErrors()).isFalse();
        assertThat(model.validateAt(0, 0)).isEqualTo("required");
        verify(listener).tableChanged(eventCaptor.capture());
        verifyEvent(eventCaptor.getValue(), TableModelEvent.UPDATE, 0, 0);
    }

    @Test
    public void setRowAddsErrors() throws Exception {
        model.setBeans(Arrays.asList(new TestBean("bean1"), new TestBean("bean2")));
        assertThat(model.isNoErrors()).isTrue();
        model.addTableModelListener(listener);

        model.setRow(0, new TestBean(null));

        assertThat(model.isNoErrors()).isFalse();
        assertThat(model.validateAt(0, 0)).isEqualTo("required");
        verify(listener).tableChanged(eventCaptor.capture());
        verifyEvent(eventCaptor.getValue(), TableModelEvent.UPDATE, 0, 0);
    }

    @Test
    public void setRowRemovesErrors() throws Exception {
        model.setBeans(Arrays.asList(new TestBean(null), new TestBean("bean2")));
        assertThat(model.isNoErrors()).isFalse();
        model.addTableModelListener(listener);

        model.setRow(0, new TestBean("bean1"));

        assertThat(model.isNoErrors()).isTrue();
        assertThat(model.validateAt(0, 0)).isNull();
        verify(listener).tableChanged(eventCaptor.capture());
        verifyEvent(eventCaptor.getValue(), TableModelEvent.UPDATE, 0, 0);
    }

    @Test
    public void appendRowUpdatesErrors() throws Exception {
        model.setBeans(Arrays.asList(new TestBean("bean1"), new TestBean("bean2")));
        assertThat(model.isNoErrors()).isTrue();
        model.addTableModelListener(listener);

        model.addRow(new TestBean(null));

        assertThat(model.isNoErrors()).isFalse();
        assertThat(model.validateAt(2, 0)).isEqualTo("required");
        verify(listener, times(2)).tableChanged(eventCaptor.capture());
        verifyEvent(eventCaptor.getAllValues().get(0), TableModelEvent.INSERT, 2, 2);
        verifyEvent(eventCaptor.getAllValues().get(1), TableModelEvent.UPDATE, 2, 2);
    }

    @Test
    public void insertRowUpdatesErrors() throws Exception {
        model.setBeans(Arrays.asList(new TestBean("bean1"), new TestBean(null)));
        assertThat(model.isNoErrors()).isFalse();
        assertThat(model.validateAt(1, 0)).isEqualTo("required");
        model.addTableModelListener(listener);

        model.addRow(1, new TestBean("bean2"));

        assertThat(model.isNoErrors()).isFalse();
        assertThat(model.validateAt(1, 0)).isNull();
        assertThat(model.validateAt(2, 0)).isEqualTo("required");
        verify(listener, times(2)).tableChanged(eventCaptor.capture());
        verifyEvent(eventCaptor.getAllValues().get(0), TableModelEvent.INSERT, 1, 1);
        verifyEvent(eventCaptor.getAllValues().get(1), TableModelEvent.UPDATE, 1, 1);
    }

    @Test
    public void removeRowUpdatesErrors() throws Exception {
        model.setBeans(Arrays.asList(new TestBean("bean1"), new TestBean(null), new TestBean("bean2")));
        assertThat(model.isNoErrors()).isFalse();
        assertThat(model.validateAt(1, 0)).isEqualTo("required");
        model.addTableModelListener(listener);

        model.removeRow(model.getRow(0));

        assertThat(model.isNoErrors()).isFalse();
        assertThat(model.validateAt(0, 0)).isEqualTo("required");
        assertThat(model.validateAt(1, 0)).isNull();
        verify(listener).tableChanged(eventCaptor.capture());
        verifyEvent(eventCaptor.getValue(), TableModelEvent.DELETE, 0, 0);
    }

    @Test
    public void removeAllRowUpdatesErrors() throws Exception {
        model.setBeans(Arrays.asList(new TestBean("bean1"), new TestBean(null), new TestBean("bean2")));
        assertThat(model.isNoErrors()).isFalse();
        assertThat(model.validateAt(1, 0)).isEqualTo("required");
        model.addTableModelListener(listener);

        model.removeAll(Arrays.asList(model.getRow(0), model.getRow(1)));

        assertThat(model.isNoErrors()).isTrue();
        assertThat(model.validateAt(0, 0)).isNull();
        verify(listener, times(2)).tableChanged(eventCaptor.capture());
        verifyEvent(eventCaptor.getAllValues().get(0), TableModelEvent.DELETE, 0, 0);
        verifyEvent(eventCaptor.getAllValues().get(1), TableModelEvent.DELETE, 0, 0);
    }

    @Test
    public void revertAddRowUpdatesErrors() throws Exception {
        model.setBeans(Arrays.asList(new TestBean("bean1"), new TestBean("bean2")));
        model.queueAdd(1, new TestBean(null));
        assertThat(model.isNoErrors()).isFalse();
        assertThat(model.validateAt(1, 0)).isEqualTo("required");
        model.addTableModelListener(listener);

        model.revert();

        assertThat(model.isNoErrors()).isTrue();
        assertThat(model.validateAt(1, 0)).isNull();
        verify(listener).tableChanged(eventCaptor.capture());
        verifyEvent(eventCaptor.getAllValues().get(0), TableModelEvent.DELETE, 1, 1);
    }

    @Test
    public void revertSetValueUpdatesErrors() throws Exception {
        model.setBeans(Arrays.asList(new TestBean("bean1"), new TestBean("bean2")));
        model.setValueAt(null, 1, 0);
        assertThat(model.isNoErrors()).isFalse();
        assertThat(model.validateAt(1, 0)).isEqualTo("required");
        model.addTableModelListener(listener);

        model.revert();

        assertThat(model.isNoErrors()).isTrue();
        assertThat(model.validateAt(1, 0)).isNull();
        verify(listener).tableChanged(eventCaptor.capture());
        verifyEvent(eventCaptor.getAllValues().get(0), TableModelEvent.UPDATE, 1, 1);
    }

    private void verifyEvent(TableModelEvent event, int type, int firstRow, int lastRow) {
        assertThat(event.getType()).isEqualTo(type);
        assertThat(event.getFirstRow()).isEqualTo(firstRow);
        assertThat(event.getLastRow()).isEqualTo(lastRow);
    }

    public static class TestBean {
        private String column1;

        public TestBean(String column1) {
            this.column1 = column1;
        }

        public String getColumn1() {
            return column1;
        }

        public void setColumn1(String column1) {
            this.column1 = column1;
        }
    }

    private static class TestBeanColumnAdapter extends TestColumnAdapter<TestBean,String> implements BeanPropertyValidator<TestBean, String> {
        public TestBeanColumnAdapter() {
            super("column1", String.class, TestBean::getColumn1, TestBean::setColumn1);
        }

        public String validate(int selectedIndex, String propertyValue, List<? extends TestBean> beans) {
            return propertyValue == null ? "required" : null;
        }
    }
}
