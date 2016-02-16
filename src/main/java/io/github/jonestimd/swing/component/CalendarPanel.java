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
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.text.DateFormatSymbols;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import static io.github.jonestimd.swing.ComponentFactory.*;

/**
 * A panel that displays a calendar for selecting a date.  The calendar shows a single month and indicates the current
 * date and the currently selected date.  The panel also includes controls for selecting the month and year to display
 * in the calendar.  Property change events are fired when the selected date changes.
 */
public class CalendarPanel extends Box {
    private static final String SELECTION_COMPLETE = "selectionComplete";
    private static final String CANCEL_SELECTION = "cancelSelection";
    private static final String NEXT_MONTH = "navigateToNextMonth";
    private static final String PREVIOUS_MONTH = "navigateToPreviousMonth";

    /** The property name used for change events. */
    public static final String DATE_PROPERTY = "date";

    private Calendar navigationCalendar = Calendar.getInstance();
    private Date selectedDate;
    private Color monthBackground = new Color(0xffffe0); // TODO use resource bundle
    private Color adjacentBackground = new Color(0xdfdfc0); // TODO use resource bundle
    private JLabel yearLabel = createYearLabel();
    private JComboBox monthComboBox = createMonthComboBox();
    private final Action selectAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            setDate(tableModel.getSelectedDate());
        }
    };
    private final Action cancelAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            setDate(selectedDate);
        }
    };
    private final Action nextMonthAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            navigationCalendar.add(Calendar.MONTH, 1);
            updateCalendar();
        }
    };
    private final Action previousMonthAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            navigationCalendar.add(Calendar.MONTH, -1);
            updateCalendar();
        }
    };

    private CalendarTableModel tableModel;
    private JTable calendarTable;

    public CalendarPanel(Date initialValue) {
        super(BoxLayout.Y_AXIS);
        setBackground(monthBackground);
        setOpaque(true);

        Box navigationPanel = Box.createHorizontalBox();
        add(navigationPanel);
        navigationPanel.add(createPreviousNextPanel(monthComboBox, new MonthHandler(), "calendar.tooltip.month"));
        navigationPanel.add(createPreviousNextPanel(yearLabel, new YearHandler(), "calendar.tooltip.year"));

        createTable();
        add(calendarTable.getTableHeader());
        add(calendarTable);
        setDate(initialValue);
    }

    /**
     * Overridden to set focus to the calendar contained in this panel.
     */
    @Override
    public void requestFocus() {
        super.requestFocus();
        calendarTable.requestFocus();
    }

    private PreviousNextPanel createPreviousNextPanel(Component valueComponent,
            PreviousNextListener handler, String resourcePrefix) {
        PreviousNextPanel previousNextPanel = new PreviousNextPanel(valueComponent,
                DEFAULT_BUNDLE.getString(resourcePrefix + ".previous"), DEFAULT_BUNDLE.getString(resourcePrefix + ".next"));
        previousNextPanel.addPreviousNextListener(handler);
        return previousNextPanel;
    }

    private JLabel createYearLabel() {
        JLabel label = new JLabel(String.format("%tY", navigationCalendar.getTime()));
        label.setBorder(new EmptyBorder(0, 2, 0, 2));
        return label;
    }

    private JComboBox<String> createMonthComboBox() {
        String[] months = DateFormatSymbols.getInstance().getMonths();
        months = Arrays.copyOfRange(months,
                navigationCalendar.getMinimum(Calendar.MONTH), navigationCalendar.getMaximum(Calendar.MONTH)+1);
        JComboBox<String> comboBox = new JComboBox<>(months);
        comboBox.addItemListener(new MonthChangeListener());
        return comboBox;
    }

    private void createTable() {
        tableModel = new CalendarTableModel();
        calendarTable = new JTable(tableModel);
        calendarTable.getTableHeader().setReorderingAllowed(false);
        calendarTable.getTableHeader().setResizingAllowed(false);
        calendarTable.setCellSelectionEnabled(true);
        calendarTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        calendarTable.setDefaultRenderer(Date.class, new CellRenderer());
        calendarTable.setToolTipText(DEFAULT_BUNDLE.getString("calendar.tooltip"));
        calendarTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                setDate(tableModel.getSelectedDate());
            }
        });

        ActionMap actionMap = new ActionMap();
        actionMap.setParent(calendarTable.getActionMap());
        actionMap.put(SELECTION_COMPLETE, selectAction);
        actionMap.put(CANCEL_SELECTION, cancelAction);
        actionMap.put(NEXT_MONTH, nextMonthAction);
        actionMap.put(PREVIOUS_MONTH, previousMonthAction);
        calendarTable.setActionMap(actionMap);
        InputMap inputMap = new InputMap();
        inputMap.setParent(calendarTable.getInputMap());
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), SELECTION_COMPLETE);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), CANCEL_SELECTION);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0), NEXT_MONTH);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0), PREVIOUS_MONTH);
        calendarTable.setInputMap(WHEN_FOCUSED, inputMap);
        setPreferredWidth();
    }

    private void setPreferredWidth() {
        int max = 0;
        for (int i=0; i<calendarTable.getColumnCount(); i++) {
            max = Math.max(max, getColumnWidth(i));
        }
        max += calendarTable.getColumnModel().getColumnMargin() + 1;
        for (int i=0; i<calendarTable.getColumnCount(); i++) {
            calendarTable.getColumnModel().getColumn(i).setPreferredWidth(max);
        }
    }

    private int getColumnWidth(int index) {
        TableCellRenderer renderer = calendarTable.getColumnModel().getColumn(index).getHeaderRenderer();
        if (renderer == null) {
            renderer = calendarTable.getTableHeader().getDefaultRenderer();
        }
        Component component = renderer.getTableCellRendererComponent(calendarTable, calendarTable.getColumnName(index), false, false, 0, index);
        return component.getPreferredSize().width;
    }

    /**
     * @return the currently selected date
     */
    public Date getDate() {
        return selectedDate;
    }

    /**
     * Set the selected date and update the month and year on the panel so the date is visible.
     * @param value the selected date
     */
    public void setDate(Date value) {
        selectedDate = value == null ? null : truncate(value);
        updateNavigationCalendar(value == null ? new Date() : value);
        updateCalendar();
        firePropertyChange(DATE_PROPERTY, null, selectedDate);
    }

    private Date truncate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private Calendar updateNavigationCalendar(Date date) {
        navigationCalendar.setTime(date);
        navigationCalendar.set(Calendar.DATE, 1);
        navigationCalendar.set(Calendar.HOUR_OF_DAY, 0);
        navigationCalendar.set(Calendar.MINUTE, 0);
        navigationCalendar.set(Calendar.SECOND, 0);
        navigationCalendar.set(Calendar.MILLISECOND, 0);
        return navigationCalendar;
    }

    private class MonthHandler implements PreviousNextListener {
        public void selectNext(PreviousNextPanel source) {
            navigationCalendar.add(Calendar.MONTH, 1);
            updateCalendar();
        }

        public void selectPrevious(PreviousNextPanel source) {
            navigationCalendar.add(Calendar.MONTH, -1);
            updateCalendar();
        }
    }

    private class MonthChangeListener implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            navigationCalendar.set(Calendar.MONTH, monthComboBox.getSelectedIndex() + navigationCalendar.getMinimum(Calendar.MONTH));
            tableModel.populateTable();
        }
    }

    private class YearHandler implements PreviousNextListener {
        public void selectNext(PreviousNextPanel source) {
            navigationCalendar.add(Calendar.YEAR, 1);
            updateCalendar();
        }

        public void selectPrevious(PreviousNextPanel source) {
            navigationCalendar.add(Calendar.YEAR, -1);
            updateCalendar();
        }
    }

    private void updateCalendar() {
        monthComboBox.setSelectedIndex(navigationCalendar.get(Calendar.MONTH) - navigationCalendar.getMinimum(Calendar.MONTH));
        yearLabel.setText(String.format("%tY", navigationCalendar.getTime()));
        tableModel.populateTable();
    }

    private class CellRenderer extends DefaultTableCellRenderer {
        private Border selectedBorder = BorderFactory.createLineBorder(Color.RED);
        private Calendar calendar = Calendar.getInstance();

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            calendar.setTime((Date) value);
            if (calendar.get(Calendar.MONTH) == navigationCalendar.get(Calendar.MONTH)) {
                setForeground(table.getForeground());
                setBackground(monthBackground);
            }
            else {
                setForeground(Color.DARK_GRAY);
                setBackground(adjacentBackground);
            }
            super.getTableCellRendererComponent(table, String.format("%te", value), isSelected, hasFocus, row, column);
            setHorizontalAlignment(CENTER);
            if (calendar.getTime().equals(truncate(new Date()))) {
                setFont(getFont().deriveFont(Font.BOLD, AffineTransform.getScaleInstance(1.1d, 1.1d)));
            }
            if (isSelected) {
                setBorder(selectedBorder);
            }
            return this;
        }
    }

    private class CalendarTableModel extends AbstractTableModel {
        private String[] weekdays;
        private Date[][] dates;

        public CalendarTableModel() {
            weekdays = new DateFormatSymbols().getShortWeekdays();
            weekdays = Arrays.copyOfRange(weekdays, navigationCalendar.getFirstDayOfWeek(), weekdays.length);
            int rowCount = (Calendar.getInstance().getMaximum(Calendar.DATE) / weekdays.length) + 2;
            dates = new Date[rowCount][weekdays.length];
            for (int i = 0; i < dates.length; i++) {
                dates[i] = new Date[weekdays.length];
            }
        }

        public Class<?> getColumnClass(int columnIndex) {
            return Date.class;
        }

        public String getColumnName(int column) {
            return column < weekdays.length ? weekdays[column] : null;
        }

        public int getColumnCount() {
            return weekdays.length;
        }

        public int getRowCount() {
            return dates.length;
        }

        public void populateTable() {
            calendarTable.clearSelection();
            Calendar calendar = (Calendar) navigationCalendar.clone();
            calendar.add(Calendar.DATE, -(calendar.get(Calendar.DAY_OF_WEEK) - calendar.getFirstDayOfWeek()));
            for (int week = 0; week < dates.length; week++) {
                for (int dow = 0; dow < dates[week].length; dow++) {
                    Date date = calendar.getTime();
                    dates[week][dow] = date;
                    if (date.equals(selectedDate)) {
                        calendarTable.setColumnSelectionInterval(dow, dow);
                        calendarTable.setRowSelectionInterval(week, week);
                    }
                    calendar.add(Calendar.DATE, 1);
                }
            }
            fireTableRowsUpdated(0, dates.length-1);
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex < dates.length && columnIndex < weekdays.length) {
                return dates[rowIndex][columnIndex];
            }
            return null;
        }

        public Date getSelectedDate() {
            if (calendarTable.getSelectedRowCount() == 0 || calendarTable.getSelectedColumnCount() == 0) {
                return null;
            }
            return dates[calendarTable.getSelectedRow()][calendarTable.getSelectedColumn()];
        }
    }
}