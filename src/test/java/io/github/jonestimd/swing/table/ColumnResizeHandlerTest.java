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

import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import io.github.jonestimd.swing.ClientProperty;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ColumnResizeHandlerTest {
    private static final int COLUMN_COUNT = 3;
    private static final int FIXED_WIDTH_COLUMN = 1;
    private final int[] calculatorWidths = {50, 100, 200};
    @Mock
    private TableModel model;
    @Mock
    private JTableHeader header;
    @Mock
    private ColumnConfiguration configuration;
    @Mock
    private ColumnWidthCalculator widthCalculator;
    @Captor
    private ArgumentCaptor<MouseListener> listener;

    private JTable table;

    @Before
    public void setupModel() {
        when(model.getColumnCount()).thenReturn(COLUMN_COUNT);
        table = new JTable(model);
        table.setTableHeader(header);
        when(widthCalculator.isFixedWidth(any(TableColumn.class)))
                .thenAnswer(invocation -> ((TableColumn) invocation.getArguments()[0]).getModelIndex() == FIXED_WIDTH_COLUMN);
    }

    @Test
    public void initializesColumnWidthsUsingWidthCalculator() throws Exception {
        int tableHeight = table.getPreferredScrollableViewportSize().height;
        when(configuration.getWidth(any(TableColumn.class))).thenReturn(null);
        when(configuration.getIndex(any(TableColumn.class))).thenReturn(-1);
        when(widthCalculator.preferredWidth(any(TableColumn.class))).thenAnswer(byColumnIndex(calculatorWidths));

        ColumnResizeHandler resizeHandler = new ColumnResizeHandler(table, configuration, widthCalculator);

        verifyConfiguration();
        verifyWidthCalculator(1);
        assertThat(table.getClientProperty(ClientProperty.SETTINGS_PERSISTER)).isSameAs(resizeHandler);
        verifyColumnWidths(calculatorWidths);
        assertThat(table.getPreferredScrollableViewportSize().width).isEqualTo(350);
        assertThat(table.getPreferredScrollableViewportSize().height).isEqualTo(tableHeight);
    }

    @Test
    public void initializeColumnWidthUsingConfiguration() throws Exception {
        int tableHeight = table.getPreferredScrollableViewportSize().height;
        int[] configurationWidths = {55, 105, 205};
        when(configuration.getIndex(any(TableColumn.class))).thenReturn(-1);
        when(configuration.getWidth(any(TableColumn.class))).thenAnswer(byColumnIndex(configurationWidths));
        when(widthCalculator.preferredWidth(any(TableColumn.class))).thenAnswer(byColumnIndex(calculatorWidths));

        ColumnResizeHandler resizeHandler = new ColumnResizeHandler(table, configuration, widthCalculator);

        verifyConfiguration();
        verifyWidthCalculator(0);
        assertThat(table.getClientProperty(ClientProperty.SETTINGS_PERSISTER)).isSameAs(resizeHandler);
        verifyColumnWidths(configurationWidths);
        assertThat(table.getPreferredScrollableViewportSize().width).isEqualTo(365);
        assertThat(table.getPreferredScrollableViewportSize().height).isEqualTo(tableHeight);
    }

    private void verifyColumnWidths(int... widths) {
        assertThat(table.getColumnModel().getColumn(0).getPreferredWidth()).isEqualTo(widths[0]);
        checkFixedWidth(1, widths[1]);
        assertThat(table.getColumnModel().getColumn(2).getPreferredWidth()).isEqualTo(widths[2]);
    }

    @Test
    public void restoresColumnOrderFromConfiguration() throws Exception {
        List<Integer> order = Arrays.asList(0, 1, 2);
        Collections.shuffle(order);
        when(configuration.getIndex(any(TableColumn.class))).thenAnswer(byColumnIndex(order));
        when(configuration.getWidth(any(TableColumn.class))).thenReturn(null);
        when(widthCalculator.preferredWidth(any(TableColumn.class))).thenReturn(100);

        ColumnResizeHandler resizeHandler = new ColumnResizeHandler(table, configuration, widthCalculator);

        assertThat(table.getClientProperty(ClientProperty.SETTINGS_PERSISTER)).isSameAs(resizeHandler);
        assertThat(table.convertColumnIndexToView(0)).isEqualTo(order.get(0));
        assertThat(table.convertColumnIndexToView(1)).isEqualTo(order.get(1));
        assertThat(table.convertColumnIndexToView(2)).isEqualTo(order.get(2));
    }

    @Test
    public void ignoresColumnIndexOutOfRange() throws Exception {
        List<Integer> order = Arrays.asList(3, -1, 0);
        when(configuration.getIndex(any(TableColumn.class))).thenAnswer(byColumnIndex(order));
        when(configuration.getWidth(any(TableColumn.class))).thenReturn(null);
        when(widthCalculator.preferredWidth(any(TableColumn.class))).thenReturn(100);

        ColumnResizeHandler resizeHandler = new ColumnResizeHandler(table, configuration, widthCalculator);

        assertThat(table.getClientProperty(ClientProperty.SETTINGS_PERSISTER)).isSameAs(resizeHandler);
        assertThat(table.convertColumnIndexToView(0)).isEqualTo(1);
        assertThat(table.convertColumnIndexToView(1)).isEqualTo(2);
        assertThat(table.convertColumnIndexToView(2)).isEqualTo(0);
    }

    @Test(expected = NoSuchElementException.class)
    public void errorForRemovingColumnFromChangeListener() throws Exception {
        table.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
            @Override
            public void columnAdded(TableColumnModelEvent e) {
            }

            @Override
            public void columnRemoved(TableColumnModelEvent e) {
            }

            @Override
            public void columnMoved(TableColumnModelEvent e) {
                if (e.getFromIndex() == 0) {
                    TableColumnModel columnModel = table.getColumnModel();
                    columnModel.removeColumn(columnModel.getColumn(2));
                }
            }

            @Override
            public void columnMarginChanged(ChangeEvent e) {
            }

            @Override
            public void columnSelectionChanged(ListSelectionEvent e) {
            }
        });
        List<Integer> order = Arrays.asList(1, 0, 2);
        when(configuration.getIndex(any(TableColumn.class))).thenAnswer(byColumnIndex(order));
        when(configuration.getWidth(any(TableColumn.class))).thenReturn(null);
        when(widthCalculator.preferredWidth(any(TableColumn.class))).thenReturn(100);

        new ColumnResizeHandler(table, configuration, widthCalculator);
    }

    @Test
    public void ignoresHeaderEventsWhenNotResizing() throws Exception {
        when(configuration.getWidth(any(TableColumn.class))).thenReturn(null);
        when(configuration.getIndex(any(TableColumn.class))).thenReturn(-1);
        when(widthCalculator.preferredWidth(any(TableColumn.class))).thenAnswer(byColumnIndex(calculatorWidths));
        new ColumnResizeHandler(table, configuration, widthCalculator);

        beginColumnResize(null);

        verifyColumnWidths(calculatorWidths);
    }

    @Test
    public void resizeFixedWidthColumnUpdatesMinMaxAndPreferredWidths() throws Exception {
        when(configuration.getWidth(any(TableColumn.class))).thenReturn(null);
        when(configuration.getIndex(any(TableColumn.class))).thenReturn(-1);
        when(widthCalculator.preferredWidth(any(TableColumn.class))).thenAnswer(byColumnIndex(calculatorWidths));
        new ColumnResizeHandler(table, configuration, widthCalculator);
        TableColumnModel columnModel = table.getColumnModel();

        beginColumnResize(columnModel.getColumn(FIXED_WIDTH_COLUMN));

        assertThat(columnModel.getColumn(FIXED_WIDTH_COLUMN).getMinWidth()).isEqualTo(15);
        assertThat(columnModel.getColumn(FIXED_WIDTH_COLUMN).getMaxWidth()).isEqualTo(Integer.MAX_VALUE);

        int newWidth = calculatorWidths[FIXED_WIDTH_COLUMN] + 10;
        endColumnResize(FIXED_WIDTH_COLUMN, newWidth);

        checkFixedWidth(FIXED_WIDTH_COLUMN, newWidth);
    }

    @Test
    public void resizeNonFixedWidthColumnUpdatesNothing() throws Exception {
        final int columnIndex = COLUMN_COUNT - 1;
        when(configuration.getWidth(any(TableColumn.class))).thenReturn(null);
        when(configuration.getIndex(any(TableColumn.class))).thenReturn(-1);
        when(widthCalculator.preferredWidth(any(TableColumn.class))).thenAnswer(byColumnIndex(calculatorWidths));
        new ColumnResizeHandler(table, configuration, widthCalculator);
        TableColumnModel columnModel = table.getColumnModel();

        beginColumnResize(columnModel.getColumn(columnIndex));
        int newWidth = calculatorWidths[columnIndex] + 10;
        endColumnResize(columnIndex, newWidth);

        assertThat(columnModel.getColumn(columnIndex).getWidth()).isEqualTo(newWidth);
        assertThat(columnModel.getColumn(columnIndex).getPreferredWidth()).isEqualTo(calculatorWidths[columnIndex]);
        assertThat(columnModel.getColumn(columnIndex).getMinWidth()).isEqualTo(15);
        assertThat(columnModel.getColumn(columnIndex).getMaxWidth()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    public void resizeColumnFollowedByOnlyFixedWidthColumnsUpdatesLastColumn() throws Exception {
        final int columnIndex = 0;
        when(configuration.getWidth(any(TableColumn.class))).thenReturn(null);
        when(configuration.getIndex(any(TableColumn.class))).thenReturn(-1);
        when(widthCalculator.preferredWidth(any(TableColumn.class))).thenAnswer(byColumnIndex(calculatorWidths));
        new ColumnResizeHandler(table, configuration, widthCalculator);
        TableColumnModel columnModel = table.getColumnModel();
        makeFixedWidth(COLUMN_COUNT-1);

        beginColumnResize(columnModel.getColumn(columnIndex));

        assertThat(columnModel.getColumn(COLUMN_COUNT-1).getMinWidth()).isEqualTo(15);
        assertThat(columnModel.getColumn(COLUMN_COUNT-1).getMaxWidth()).isEqualTo(Integer.MAX_VALUE);

        int newWidth = calculatorWidths[columnIndex] - 10;
        int lastColumnWidth = calculatorWidths[COLUMN_COUNT - 1] + 10;
        columnModel.getColumn(COLUMN_COUNT-1).setWidth(lastColumnWidth);
        endColumnResize(columnIndex, newWidth);

        assertThat(columnModel.getColumn(columnIndex).getWidth()).isEqualTo(newWidth);
        assertThat(columnModel.getColumn(columnIndex).getPreferredWidth()).isEqualTo(calculatorWidths[columnIndex]);
        assertThat(columnModel.getColumn(columnIndex).getMinWidth()).isEqualTo(15);
        assertThat(columnModel.getColumn(columnIndex).getMaxWidth()).isEqualTo(Integer.MAX_VALUE);
        checkFixedWidth(COLUMN_COUNT-1, lastColumnWidth);
    }

    @Test
    public void saveSettingsSavesToConfiguration() throws Exception {
        when(configuration.getWidth(any(TableColumn.class))).thenReturn(null);
        when(configuration.getIndex(any(TableColumn.class))).thenReturn(-1);
        when(widthCalculator.preferredWidth(any(TableColumn.class))).thenAnswer(byColumnIndex(calculatorWidths));
        ColumnResizeHandler resizeHandler = new ColumnResizeHandler(table, configuration, widthCalculator);

        resizeHandler.saveSettings();

        TableColumnModel columnModel = table.getColumnModel();
        InOrder order = inOrder(configuration);
        for (int i = 0; i < COLUMN_COUNT; i++) {
            TableColumn column = columnModel.getColumn(i);
            order.verify(configuration).setWidth(column, column.getWidth());
            order.verify(configuration).setIndex(column, i);
        }
    }

    private void makeFixedWidth(int columnIndex) {
        TableColumnModel columnModel = table.getColumnModel();
        int width = columnModel.getColumn(columnIndex).getWidth();
        columnModel.getColumn(columnIndex).setMinWidth(width);
        columnModel.getColumn(columnIndex).setMaxWidth(width);
    }

    private void beginColumnResize(TableColumn column) {
        when(header.getResizingColumn()).thenReturn(column);
        verify(header).addMouseListener(listener.capture());
        listener.getValue().mousePressed(null);
    }

    private void endColumnResize(int columnIndex, int newWidth) {
        table.getColumnModel().getColumn(columnIndex).setWidth(newWidth);
        listener.getValue().mouseReleased(null);
    }

    private Answer<?> byColumnIndex(int[] values) {
        return invocation -> values[((TableColumn)invocation.getArguments()[0]).getModelIndex()];
    }

    private Answer<?> byColumnIndex(List<Integer> values) {
        return invocation -> values.get(((TableColumn)invocation.getArguments()[0]).getModelIndex());
    }

    private void verifyConfiguration() {
        for (int i = 0; i < calculatorWidths.length; i++) {
            verify(configuration).getWidth(columnAt(i));
        }
    }

    private void verifyWidthCalculator(int widthCount) {
        for (int i = 0; i < COLUMN_COUNT; i++) {
            verify(widthCalculator).isFixedWidth(columnAt(i));
            verify(widthCalculator, times(widthCount)).preferredWidth(columnAt(i));
        }
    }

    private void checkFixedWidth(int columnIndex, int expectedWidth) {
        assertThat(table.getColumnModel().getColumn(columnIndex).getWidth()).isEqualTo(expectedWidth);
        assertThat(table.getColumnModel().getColumn(columnIndex).getMinWidth()).isEqualTo(expectedWidth);
        assertThat(table.getColumnModel().getColumn(columnIndex).getMaxWidth()).isEqualTo(expectedWidth);
        assertThat(table.getColumnModel().getColumn(columnIndex).getPreferredWidth()).isEqualTo(expectedWidth);
    }

    private TableColumn columnAt(int index) {
        return argThat(o -> o.getModelIndex() == index);
    }
}