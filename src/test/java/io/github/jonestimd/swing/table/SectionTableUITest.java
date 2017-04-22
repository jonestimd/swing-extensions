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

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.CellRendererPane;
import javax.swing.JPanel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SectionTableUITest {
    private static final int ROW_COUNT = 6;
    private static final int COLUMN_COUNT = 5;
    private static final Color SECTION_ROW_BACKGROUND = Color.yellow;
    private static final int COLUMN_WIDTH = 20;
    private static final int ROW_HEIGHT = 10;
    @Mock
    private SectionTable<?, ?> table;
    @Mock
    private JTableHeader tableHeader;
    @Mock
    private TableColumnModel columnModel;
    @Mock
    private TableColumn column;
    @Mock
    private Graphics2D g2d;
    @Mock
    private Graphics2D childGraphics;
    @Mock
    private TableCellRenderer cellRenderer;
    @Mock
    private Component rendererComponent;
    private int getParentCount = 0;
    private CellRendererPane rendererPane;

    private Answer<Rectangle> cellRectAnswer = invocation -> {
        int row = (int) invocation.getArguments()[0];
        int column = (int) invocation.getArguments()[1];
        return new Rectangle(column * COLUMN_WIDTH, row * ROW_HEIGHT, COLUMN_WIDTH, ROW_HEIGHT);
    };
    private Answer<Integer> rowAtPointAnswer = invocation -> {
        Point point = (Point) invocation.getArguments()[0];
        return point.y >= ROW_COUNT * ROW_HEIGHT ? -1 : point.y / ROW_HEIGHT;
    };
    private Answer<Boolean> isSectionRowAnswer = invocation -> (int) invocation.getArguments()[0] % 3 == 0;
    private Answer<Object> getParentAnswer = invocation -> getParentCount++ < 2 * ROW_COUNT + 1 ? null : rendererPane;

    @Before
    public void setupTable() throws Exception {
        when(g2d.create(anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(childGraphics);
        when(table.getComponentOrientation()).thenReturn(ComponentOrientation.LEFT_TO_RIGHT);
        when(table.getColumnModel()).thenReturn(columnModel);
        when(columnModel.getColumn(anyInt())).thenReturn(column);
        when(table.getCellRenderer(anyInt(), anyInt())).thenReturn(cellRenderer);
        when(table.prepareRenderer(same(cellRenderer), anyInt(), anyInt())).thenReturn(rendererComponent);
        when(table.getRowCount()).thenReturn(ROW_COUNT);
        when(table.getColumnCount()).thenReturn(COLUMN_COUNT);
        when(rendererComponent.getBackground()).thenReturn(SECTION_ROW_BACKGROUND);
        doAnswer(invocation -> rendererPane = (CellRendererPane) invocation.getArguments()[0])
                .when(table).add(any(CellRendererPane.class));
    }

    @Test
    public void createUI() throws Exception {
        assertThat(SectionTableUI.createUI(null)).isInstanceOf(SectionTableUI.class);
    }

    @Test
    public void paintFillsBackgroundForTransparentCells() throws Exception {
        final int clipWidth = COLUMN_WIDTH * COLUMN_COUNT - 1;
        final int clipHeight = ROW_HEIGHT * ROW_COUNT - 1;
        when(rendererComponent.isOpaque()).thenReturn(false);
        when(table.getBounds()).thenReturn(new Rectangle(0, 0, clipWidth, clipHeight));
        when(g2d.getClipBounds()).thenReturn(new Rectangle(0, 0, clipWidth, clipHeight));
        when(table.rowAtPoint(any(Point.class))).thenAnswer(rowAtPointAnswer);
        when(table.isSectionRow(anyInt())).thenAnswer(isSectionRowAnswer);
        when(table.getCellRect(anyInt(), anyInt(), anyBoolean())).thenAnswer(cellRectAnswer);
        when(rendererComponent.getParent()).thenAnswer(getParentAnswer);
        SectionTableUI tableUI = new SectionTableUI();
        tableUI.installUI(table);

        tableUI.paint(g2d, table);

        verify(g2d, times(2)).setColor(SECTION_ROW_BACKGROUND);
        verify(g2d).fillRect(0, 0, COLUMN_COUNT * COLUMN_WIDTH, ROW_HEIGHT);
        verify(g2d).fillRect(0, 3 * ROW_HEIGHT, COLUMN_COUNT * COLUMN_WIDTH, ROW_HEIGHT);
    }

    @Test
    public void paintDoesNotFillBackgroundForOpaqueCells() throws Exception {
        final int clipWidth = COLUMN_WIDTH * COLUMN_COUNT - 1;
        final int clipHeight = ROW_HEIGHT * ROW_COUNT - 1;
        when(rendererComponent.isOpaque()).thenReturn(true);
        when(table.getBounds()).thenReturn(new Rectangle(0, 0, clipWidth, clipHeight));
        when(g2d.getClipBounds()).thenReturn(new Rectangle(0, 0, clipWidth, clipHeight));
        when(table.rowAtPoint(any(Point.class))).thenAnswer(rowAtPointAnswer);
        when(table.isSectionRow(anyInt())).thenAnswer(isSectionRowAnswer);
        when(table.getCellRect(anyInt(), anyInt(), anyBoolean())).thenAnswer(cellRectAnswer);
        when(rendererComponent.getParent()).thenAnswer(getParentAnswer);
        SectionTableUI tableUI = new SectionTableUI();
        tableUI.installUI(table);

        tableUI.paint(g2d, table);

        verify(g2d, never()).setColor(SECTION_ROW_BACKGROUND);
        verify(g2d, never()).fillRect(0, 0, COLUMN_COUNT * COLUMN_WIDTH, ROW_HEIGHT);
        verify(g2d, never()).fillRect(0, 3 * ROW_HEIGHT, COLUMN_COUNT * COLUMN_WIDTH, ROW_HEIGHT);
    }

    @Test
    public void paintHandlesClipLargerThanTable() throws Exception {
        final int clipWidth = COLUMN_WIDTH * COLUMN_COUNT - 1;
        final int clipHeight = ROW_HEIGHT * ROW_COUNT - 1;
        when(rendererComponent.isOpaque()).thenReturn(false);
        when(table.getTableHeader()).thenReturn(tableHeader);
        when(table.getBounds()).thenReturn(new Rectangle(0, 0, clipWidth, clipHeight));
        when(g2d.getClipBounds()).thenReturn(new Rectangle(0, 0, clipWidth, clipHeight+10));
        when(table.rowAtPoint(any(Point.class))).thenAnswer(rowAtPointAnswer);
        when(table.isSectionRow(anyInt())).thenAnswer(isSectionRowAnswer);
        when(table.getCellRect(anyInt(), anyInt(), anyBoolean())).thenAnswer(cellRectAnswer);
        when(rendererComponent.getParent()).thenAnswer(getParentAnswer);
        SectionTableUI tableUI = new SectionTableUI();
        tableUI.installUI(table);

        tableUI.paint(g2d, table);

        verify(g2d, times(2)).setColor(SECTION_ROW_BACKGROUND);
        verify(g2d).fillRect(0, 0, COLUMN_COUNT * COLUMN_WIDTH, ROW_HEIGHT);
        verify(g2d).fillRect(0, 3 * ROW_HEIGHT, COLUMN_COUNT * COLUMN_WIDTH, ROW_HEIGHT);
    }

    @Test
    public void paintDoesNotFillBackgroundForNoIntersection() throws Exception {
        final int clipWidth = COLUMN_WIDTH * COLUMN_COUNT - 1;
        final int clipHeight = ROW_HEIGHT * ROW_COUNT - 1;
        when(table.getBounds()).thenReturn(new Rectangle(0, 0, clipWidth, clipHeight));
        when(g2d.getClipBounds()).thenReturn(new Rectangle(clipWidth, clipHeight, 10, 10));
        SectionTableUI tableUI = new SectionTableUI();
        tableUI.installUI(table);

        tableUI.paint(g2d, table);

        assertThat(getParentCount).isEqualTo(0);
        verify(table, never()).rowAtPoint(any(Point.class));
        verify(table, never()).isSectionRow(anyInt());
        verify(table, never()).getCellRect(anyInt(), anyInt(), anyBoolean());
        verify(g2d, never()).setColor(SECTION_ROW_BACKGROUND);
        verify(g2d, never()).fillRect(0, 0, COLUMN_COUNT * COLUMN_WIDTH, ROW_HEIGHT);
        verify(g2d, never()).fillRect(0, 3 * ROW_HEIGHT, COLUMN_COUNT * COLUMN_WIDTH, ROW_HEIGHT);
    }

    @Test
    public void paintDoesNotFillBackgroundDuringColumnReordering() throws Exception {
        final int clipWidth = COLUMN_WIDTH * COLUMN_COUNT - 1;
        final int clipHeight = ROW_HEIGHT * ROW_COUNT - 1;
        final JPanel panel = new JPanel();
        when(table.getParent()).thenReturn(panel);
        when(table.getTableHeader()).thenReturn(tableHeader);
        when(tableHeader.getDraggedColumn()).thenReturn(mock(TableColumn.class));
        when(table.getBounds()).thenReturn(new Rectangle(0, 0, clipWidth, clipHeight));
        when(table.rowAtPoint(any(Point.class))).thenAnswer(rowAtPointAnswer);
        when(table.getCellRect(anyInt(), anyInt(), anyBoolean())).thenAnswer(cellRectAnswer);
        when(g2d.getClipBounds()).thenReturn(new Rectangle(0, 0, clipWidth, clipHeight));
        SectionTableUI tableUI = new SectionTableUI();
        tableUI.installUI(table);

        tableUI.paint(g2d, table);

        assertThat(getParentCount).isEqualTo(0);
        verify(table, never()).isSectionRow(anyInt());
        verify(g2d, never()).setColor(SECTION_ROW_BACKGROUND);
        verify(g2d, never()).fillRect(0, 0, COLUMN_COUNT * COLUMN_WIDTH, ROW_HEIGHT);
        verify(g2d, never()).fillRect(0, 3 * ROW_HEIGHT, COLUMN_COUNT * COLUMN_WIDTH, ROW_HEIGHT);
    }
}