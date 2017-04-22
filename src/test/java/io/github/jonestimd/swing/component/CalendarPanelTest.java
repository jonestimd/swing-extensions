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
package io.github.jonestimd.swing.component;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.font.TextAttribute;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellRenderer;

import io.github.jonestimd.swing.ComponentTreeUtils;
import org.junit.Before;
import org.junit.Test;

import static io.github.jonestimd.swing.ComponentFactory.*;
import static org.assertj.core.api.Assertions.*;

public class CalendarPanelTest {
    private Date date;

    @Before
    public void initializeDate() throws Exception {
        date = new SimpleDateFormat("MM/dd/yyyy").parse("01/01/2016");
    }

    @Test
    public void usesColorsFromResourceBundle() throws Exception {
        CalendarPanel panel = new CalendarPanel(date);

        assertThat(panel.getDate()).isEqualTo(date);
        assertThat(panel.getBackground()).isEqualTo(new Color(255,255,224));
        assertThat(panel.isOpaque()).isTrue();
        JTable table = ComponentTreeUtils.findComponent(panel, JTable.class);
        assertThat(table.getCellSelectionEnabled()).isTrue();
        assertThat(table.getColumnCount()).isEqualTo(7);
        assertThat(table.getRowCount()).isEqualTo(6);
        assertThat(table.getColumnModel().getColumn(0).getHeaderValue()).isEqualTo("Sun");
        assertThat(table.getValueAt(0, 0)).isEqualTo(startOfWeek(date));
    }

    @Test
    public void allowsNullInitialDate() throws Exception {
        CalendarPanel panel = new CalendarPanel(null);

        assertThat(panel.getDate()).isNull();
    }

    @Test
    public void getValueOutsideTableRangeReturnsNull() throws Exception {
        CalendarPanel panel = new CalendarPanel(date);
        JTable table = ComponentTreeUtils.findComponent(panel, JTable.class);

        assertThat(table.getModel().getValueAt(0, 8)).isNull();
        assertThat(table.getModel().getValueAt(7, 0)).isNull();
    }

    @Test
    public void selectNextMonth() throws Exception {
        CalendarPanel panel = new CalendarPanel(date);
        Box navigationPanel = (Box) panel.getComponent(0);
        PreviousNextPanel monthPanel = (PreviousNextPanel) navigationPanel.getComponent(0);

        monthPanel.getPreviousNextListeners()[0].selectNext(monthPanel);

        JTable table = ComponentTreeUtils.findComponent(panel, JTable.class);
        assertThat(table.getValueAt(0, 0)).isEqualTo(startOfWeek(add(date, Calendar.MONTH, 1)));
    }

    @Test
    public void selectPreviousMonth() throws Exception {
        CalendarPanel panel = new CalendarPanel(date);
        Box navigationPanel = (Box) panel.getComponent(0);
        PreviousNextPanel monthPanel = (PreviousNextPanel) navigationPanel.getComponent(0);

        monthPanel.getPreviousNextListeners()[0].selectPrevious(monthPanel);

        JTable table = ComponentTreeUtils.findComponent(panel, JTable.class);
        assertThat(table.getValueAt(0, 0)).isEqualTo(startOfWeek(add(date, Calendar.MONTH, -1)));
    }

    @Test
    public void selectNextYear() throws Exception {
        CalendarPanel panel = new CalendarPanel(date);
        Box navigationPanel = (Box) panel.getComponent(0);
        PreviousNextPanel yearPanel = (PreviousNextPanel) navigationPanel.getComponent(1);

        yearPanel.getPreviousNextListeners()[0].selectNext(yearPanel);

        JTable table = ComponentTreeUtils.findComponent(panel, JTable.class);
        assertThat(table.getValueAt(0, 0)).isEqualTo(startOfWeek(add(date, Calendar.YEAR, 1)));
    }

    @Test
    public void selectPreviousYear() throws Exception {
        CalendarPanel panel = new CalendarPanel(date);
        Box navigationPanel = (Box) panel.getComponent(0);
        PreviousNextPanel yearPanel = (PreviousNextPanel) navigationPanel.getComponent(1);

        yearPanel.getPreviousNextListeners()[0].selectPrevious(yearPanel);

        JTable table = ComponentTreeUtils.findComponent(panel, JTable.class);
        assertThat(table.getValueAt(0, 0)).isEqualTo(startOfWeek(add(date, Calendar.YEAR, -1)));
    }

    @Test
    public void dateCellRendererHighlightsSelectedDate() throws Exception {
        CalendarPanel panel = new CalendarPanel(date);
        JTable table = ComponentTreeUtils.findComponent(panel, JTable.class);
        TableCellRenderer renderer = table.getDefaultRenderer(Date.class);

        JComponent component = (JComponent) renderer.getTableCellRendererComponent(table, date, true, false, 0, 0);

        assertThat(component.getBackground()).isEqualTo(table.getSelectionBackground());
        assertThat(component.getForeground()).isEqualTo(table.getSelectionForeground());
        assertThat(component.getBorder()).isInstanceOf(LineBorder.class);
        assertThat(((LineBorder) component.getBorder()).getLineColor()).isEqualTo(DEFAULT_BUNDLE.getObject("calendarPanel.selected.border"));
    }

    @Test
    public void dateCellRendererDistinguishesAdjacentMonths() throws Exception {
        CalendarPanel panel = new CalendarPanel(date);
        JTable table = ComponentTreeUtils.findComponent(panel, JTable.class);
        TableCellRenderer renderer = table.getDefaultRenderer(Date.class);

        JComponent component = (JComponent) renderer.getTableCellRendererComponent(table, startOfWeek(date), false, false, 0, 0);

        assertThat(component.getBackground()).isEqualTo(DEFAULT_BUNDLE.getObject("calendarPanel.month.adjacent.background"));
        assertThat(component.getForeground()).isEqualTo(DEFAULT_BUNDLE.getObject("calendarPanel.month.adjacent.foreground"));
        assertThat(component.getBorder()).isInstanceOf(EmptyBorder.class);
    }

    @Test
    public void dateCellRendererUnselectedDate() throws Exception {
        CalendarPanel panel = new CalendarPanel(date);
        JTable table = ComponentTreeUtils.findComponent(panel, JTable.class);
        TableCellRenderer renderer = table.getDefaultRenderer(Date.class);

        JComponent component = (JComponent) renderer.getTableCellRendererComponent(table, date, false, false, 0, 0);

        assertThat(component.getBackground()).isEqualTo(DEFAULT_BUNDLE.getObject("calendarPanel.month.background"));
        assertThat(component.getForeground()).isEqualTo(DEFAULT_BUNDLE.getObject("calendarPanel.month.foreground"));
        assertThat(component.getBorder()).isInstanceOf(EmptyBorder.class);
    }

    @Test
    public void dateCellRendererShowsTodayInBold() throws Exception {
        CalendarPanel panel = new CalendarPanel(new Date());
        JTable table = ComponentTreeUtils.findComponent(panel, JTable.class);
        TableCellRenderer renderer = table.getDefaultRenderer(Date.class);

        JComponent component = (JComponent) renderer.getTableCellRendererComponent(table, truncate(new Date()), false, false, 0, 0);

        assertThat(component.getFont().getAttributes().get(TextAttribute.WEIGHT)).isEqualTo(TextAttribute.WEIGHT_BOLD);
    }

    @Test
    public void enterSelectsDate() throws Exception {
        CalendarPanel panel = new CalendarPanel(date);
        JTable table = ComponentTreeUtils.findComponent(panel, JTable.class);
        Object key = table.getInputMap(JComponent.WHEN_FOCUSED).get(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        Action action = table.getActionMap().get(key);
        table.setRowSelectionInterval(1, 1);
        table.setColumnSelectionInterval(5, 5);

        action.actionPerformed(null);

        assertThat(panel.getDate()).isEqualTo(add(date, Calendar.DAY_OF_MONTH, 7));
    }

    @Test
    public void cancelKeepsOriginalDate() throws Exception {
        CalendarPanel panel = new CalendarPanel(date);
        JTable table = ComponentTreeUtils.findComponent(panel, JTable.class);
        Object key = table.getInputMap(JComponent.WHEN_FOCUSED).get(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
        Action action = table.getActionMap().get(key);
        table.setRowSelectionInterval(1, 1);
        table.setColumnSelectionInterval(5, 5);

        action.actionPerformed(null);

        assertThat(panel.getDate()).isEqualTo(date);
    }

    @Test
    public void pageUpShowsPreviousMonth() throws Exception {
        CalendarPanel panel = new CalendarPanel(date);
        JTable table = ComponentTreeUtils.findComponent(panel, JTable.class);
        Object key = table.getInputMap(JComponent.WHEN_FOCUSED).get(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0));
        Action action = table.getActionMap().get(key);
        table.setRowSelectionInterval(1, 1);
        table.setColumnSelectionInterval(5, 5);

        action.actionPerformed(null);

        assertThat(table.getValueAt(0, 0)).isEqualTo(startOfWeek(add(date, Calendar.MONTH, -1)));
    }

    @Test
    public void pageDownShowsNextMonth() throws Exception {
        CalendarPanel panel = new CalendarPanel(date);
        JTable table = ComponentTreeUtils.findComponent(panel, JTable.class);
        Object key = table.getInputMap(JComponent.WHEN_FOCUSED).get(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0));
        Action action = table.getActionMap().get(key);
        table.setRowSelectionInterval(1, 1);
        table.setColumnSelectionInterval(5, 5);

        action.actionPerformed(null);

        assertThat(table.getValueAt(0, 0)).isEqualTo(startOfWeek(add(date, Calendar.MONTH, 1)));
    }

    private Date add(Date date, int field, int value) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(field, value);
        return calendar.getTime();
    }

    private Date startOfWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        return calendar.getTime();
    }

    private Date truncate(Date date) {
        Calendar calendar = Calendar.getInstance();
        for (int i = Calendar.HOUR_OF_DAY; i <= Calendar.MILLISECOND; i++) {
            calendar.set(i, 0);
        }
        return calendar.getTime();
    }
}