package io.github.jonestimd.swing.table.filter;

import java.util.function.BiPredicate;

import javax.swing.JTextField;
import javax.swing.RowFilter.Entry;
import javax.swing.table.TableRowSorter;

import io.github.jonestimd.swing.table.model.BeanTableModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PredicateRowFilterTest {
    @Mock
    private TableRowSorter<BeanTableModel<String>> rowSorter;
    @Mock
    private BiPredicate<String, String> predicate;
    @Mock
    private Entry<BeanTableModel<String>, Integer> entry;
    @Mock
    private BeanTableModel<String> model;
    private JTextField filterField = new JTextField();

    @Test
    public void updatesRowSorterWhenFilterFieldChanges() throws Exception {
        PredicateRowFilter.install(rowSorter, filterField, predicate);

        filterField.setText("xxx");

        verify(rowSorter, times(1)).allRowsChanged();
    }

    @Test
    public void include() throws Exception {
        when(entry.getModel()).thenReturn(model);
        when(entry.getIdentifier()).thenReturn(1, 2);
        when(model.getBean(anyInt())).thenReturn("row1", "row2");
        when(predicate.test(anyString(), anyString())).thenReturn(true, false);
        PredicateRowFilter<String> rowFilter = new PredicateRowFilter<>(filterField, predicate);
        filterField.setText("criteria");

        assertThat(rowFilter.include(entry)).isTrue();
        assertThat(rowFilter.include(entry)).isFalse();

        verify(predicate).test("row1", "criteria");
        verify(predicate).test("row2", "criteria");
    }
}