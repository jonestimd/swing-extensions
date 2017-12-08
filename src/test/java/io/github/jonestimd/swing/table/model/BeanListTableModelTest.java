package io.github.jonestimd.swing.table.model;

import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static io.github.jonestimd.mockito.Matchers.matches;
import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BeanListTableModelTest {
    public static final String COLUMN_ID = "columnId";
    @Mock
    private ColumnAdapter<Object, String> columnAdapter;
    @Mock
    private ColumnAdapter<Object, String> providerColumnAdapter;
    @Mock
    private TableDataProvider<Object> dataProvider;
    @Mock
    private TableModelListener tableModelListener;
    @Captor
    private ArgumentCaptor<PropertyChangeListener> stateListenerCaptor;

    private BeanListTableModel<Object> model;

    @Before
    public void createModel() {
        when(columnAdapter.getColumnId()).thenReturn(COLUMN_ID);
        doReturn(singletonList(providerColumnAdapter)).when(dataProvider).getColumnAdapters();
        model = new BeanListTableModel<>(singletonList(columnAdapter), singletonList(dataProvider));
        model.addTableModelListener(tableModelListener);
        verify(dataProvider).addStateChangeListener(stateListenerCaptor.capture());
    }

    @Test
    public void setValueUpdatesDataProvider() throws Exception {
        final Object row = new Object();
        when(columnAdapter.getValue(row)).thenReturn("old value");
        when(dataProvider.updateBean(any(), anyString(), any())).thenReturn(false);
        model.addRow(row);

        model.setValue("new value", model.indexOf(row), 0);

        verify(columnAdapter).setValue(row, "new value");
        verify(dataProvider).updateBean(row, COLUMN_ID, "old value");
        verify(tableModelListener).tableChanged(matches(new TableModelEvent(model, 0, 0, 0, TableModelEvent.UPDATE)));
    }

    @Test
    public void setValueFiresTableChangeForDataProviderColumns() throws Exception {
        final Object row = new Object();
        when(columnAdapter.getValue(row)).thenReturn("old value");
        when(dataProvider.updateBean(any(), anyString(), any())).thenReturn(true);
        model.addRow(row);

        model.setValue("new value", model.indexOf(row), 0);

        verify(columnAdapter).setValue(row, "new value");
        verify(dataProvider).updateBean(row, COLUMN_ID, "old value");
        verify(tableModelListener).tableChanged(matches(new TableModelEvent(model, 0, 0, 0, TableModelEvent.UPDATE)));
        verify(tableModelListener).tableChanged(matches(new TableModelEvent(model, 0, 0, 1, TableModelEvent.UPDATE)));
    }

    @Test
    public void setBeansUpdatesDataProvider() throws Exception {
        List<Object> beans = singletonList(new Object());

        model.setBeans(beans);

        verify(dataProvider).setBeans(same(beans));
    }

    @Test
    public void addRowUpdatesDataProvider() throws Exception {
        Object row = new Object();

        model.addRow(0, row);

        verify(dataProvider).addBean(same(row));
    }

    @Test
    public void removeRowUpdatesDataProvider() throws Exception {
        Object row = new Object();

        model.addRow(row);
        model.removeRow(row);

        verify(dataProvider).addBean(same(row));
        verify(dataProvider).removeBean(same(row));
    }

    @Test
    public void dataProviderStateChangeDoesNotFireTableModelEventWhenTableIsEmpty() throws Exception {
        stateListenerCaptor.getValue().propertyChange(new PropertyChangeEvent(dataProvider, "state", null, null));

        verifyZeroInteractions(tableModelListener);
    }

    @Test
    public void dataProviderStateChangeFiresTableModelEventWhenTableIsNotEmpty() throws Exception {
        model.addRow(new Object());
        reset(tableModelListener);

        stateListenerCaptor.getValue().propertyChange(new PropertyChangeEvent(dataProvider, "state", null, null));

        ArgumentCaptor<TableModelEvent> captor = ArgumentCaptor.forClass(TableModelEvent.class);
        verify(tableModelListener).tableChanged(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(TableModelEvent.UPDATE);
        assertThat(captor.getValue().getColumn()).isEqualTo(1);
    }

    @Test
    public void setRowReplacesRow() throws Exception {
        model.addRow(BigDecimal.ONE);
        model.addRow(BigDecimal.ZERO);
        reset(tableModelListener);

        model.setRow(1, BigDecimal.TEN);

        assertThat(model.getBeanCount()).isEqualTo(2);
        assertThat(model.getBean(0)).isSameAs(BigDecimal.ONE);
        assertThat(model.getBean(1)).isSameAs(BigDecimal.TEN);
        verify(tableModelListener).tableChanged(matches(new TableModelEvent(model, 1, 1, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE)));
        verifyNoMoreInteractions(tableModelListener);
    }

    @Test
    public void updateBeansAddsMissingRows() throws Exception {
        model.addRow(BigDecimal.ONE);
        reset(tableModelListener);

        model.updateBeans(singletonList(BigDecimal.TEN), Object::equals);

        assertThat(model.getBeanCount()).isEqualTo(2);
        assertThat(model.getBean(0)).isSameAs(BigDecimal.ONE);
        assertThat(model.getBean(1)).isSameAs(BigDecimal.TEN);
        verify(tableModelListener).tableChanged(matches(new TableModelEvent(model, 1, 1, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT)));
        verifyNoMoreInteractions(tableModelListener);
    }

    @Test
    public void updateBeansReplacesRows() throws Exception {
        model.addRow("one");
        reset(tableModelListener);

        model.updateBeans(Arrays.asList("ONE", "two"), (s1, s2) -> s1.toString().equalsIgnoreCase(s2.toString()));

        assertThat(model.getBeanCount()).isEqualTo(2);
        assertThat(model.getBean(0)).isEqualTo("ONE");
        assertThat(model.getBean(1)).isEqualTo("two");
        verify(tableModelListener).tableChanged(matches(new TableModelEvent(model, 0, 0, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE)));
        verify(tableModelListener).tableChanged(matches(new TableModelEvent(model, 1, 1, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT)));
        verifyNoMoreInteractions(tableModelListener);
    }

    @Test
    public void notifyDataProvidersDelegatesToAdapter() throws Exception {
        String bean = "row";
        String columnId = "column Id";
        String oldValue = "old value";

        model.notifyDataProviders(bean, columnId, oldValue);

        verify(dataProvider).updateBean(bean, columnId, oldValue);
    }

    @Test
    public void getCursorCallsColumnAdapter() throws Exception {
        final MouseEvent event = mock(MouseEvent.class);
        final JTable table = mock(JTable.class);
        model.addRow("one");
        model.addRow("two");

        model.getCursor(event, table, 1, 0);

        verify(columnAdapter).getCursor(event, table, "two");
    }

    @Test
    public void handleClickCallsColumnAdapter() throws Exception {
        final MouseEvent event = mock(MouseEvent.class);
        final JTable table = mock(JTable.class);
        model.addRow("one");
        model.addRow("two");

        model.handleClick(event, table, 1, 0);

        verify(columnAdapter).handleClick(event, table, "two");
    }
}
