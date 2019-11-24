// The MIT License (MIT)
//
// Copyright (c) 2019 Timothy D. Jones
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JWindow;
import javax.swing.table.DefaultTableModel;

import io.github.jonestimd.swing.JFrameRobotTest;
import org.junit.Test;

import static java.awt.event.InputEvent.*;
import static java.awt.event.KeyEvent.*;
import static org.assertj.core.api.Assertions.*;

public class ColorTableCellEditorTest extends JFrameRobotTest {
    public static final int WINDOW_WIDTH = 400;
    public static final int WINDOW_HEIGHT = 300;
    private DefaultTableModel tableModel = new DefaultTableModel(new String[]{"Color", "Other"}, 20);
    private JTable table = new JTable(tableModel);
    private ColorTableCellEditor cellEditor;


    @Override
    protected JPanel createContentPane() {
        table.getColumnModel().getColumn(0).setCellRenderer(new ColorTableCellRenderer());
        table.getColumnModel().getColumn(0).setCellEditor(cellEditor);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(table, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        return panel;
    }

    @Test
    public void showsPopup() throws Exception {
        cellEditor = new ColorTableCellEditor();
        showWindow();
        robot.focus(table);

        robot.pressAndReleaseKeys(VK_DOWN, VK_SPACE);

        JWindow popupWindow = (JWindow) robot.finder().find(c -> c.getClass() == JWindow.class);
        assertThat(popupWindow).isNotNull();
    }

    @Test
    public void initializesChooserCellValue() throws Exception {
        tableModel.setValueAt(Color.MAGENTA, 0, 0);
        cellEditor = new ColorTableCellEditor();
        showWindow();
        robot.focus(table);

        robot.pressAndReleaseKeys(VK_DOWN, VK_SPACE);

        JWindow popupWindow = (JWindow) robot.finder().find(c -> c.getClass() == JWindow.class);
        JColorChooser field = robot.finder().findByType(popupWindow, JColorChooser.class);
        assertThat(field.getColor()).isEqualTo(Color.MAGENTA);
    }

    @Test
    public void commitUpdatesTableModel() throws Exception {
        tableModel.setValueAt(Color.BLACK, 0, 0);
        cellEditor = new ColorTableCellEditor();
        showWindow();
        robot.focus(table);
        robot.pressAndReleaseKeys(VK_DOWN, VK_SPACE);
        JWindow popupWindow = (JWindow) robot.finder().find(c -> c.getClass() == JWindow.class);

        robot.pressAndReleaseKeys(VK_TAB, VK_TAB,
                VK_DOWN, VK_DOWN, VK_DOWN, VK_DOWN,
                VK_RIGHT, VK_RIGHT, VK_RIGHT, VK_RIGHT,
                VK_RIGHT, VK_RIGHT, VK_RIGHT, VK_RIGHT,
                VK_RIGHT, VK_RIGHT, VK_RIGHT, VK_RIGHT, VK_SPACE);
        robot.pressAndReleaseKey(VK_ENTER);

        assertCondition(() -> !popupWindow.isShowing(), "popup JWindow closed", 1000L);
        assertThat(tableModel.getValueAt(0, 0)).isEqualTo(new Color(255, 0, 204));
    }

    @Test
    public void cancelDoesNotUpdateTableModel() throws Exception {
        tableModel.setValueAt(Color.BLACK, 0, 0);
        cellEditor = new ColorTableCellEditor();
        showWindow();
        robot.focus(table);
        robot.pressAndReleaseKeys(VK_DOWN, VK_SPACE);
        JWindow popupWindow = (JWindow) robot.finder().find(c -> c.getClass() == JWindow.class);

        robot.click(popupWindow, new Point(238, 125));
        robot.pressAndReleaseKey(VK_ESCAPE);

        assertCondition(() -> !popupWindow.isShowing(), "popup window closed", 1000L);
        assertThat(tableModel.getValueAt(0, 0)).isEqualTo(Color.BLACK);
    }

    @Test
    public void handlesChooserMnemonics() throws Exception {
        tableModel.setValueAt(Color.BLACK, 0, 0);
        cellEditor = new ColorTableCellEditor();
        showWindow();
        robot.focus(table);
        robot.pressAndReleaseKeys(VK_DOWN, VK_SPACE);

        robot.pressAndReleaseKey(VK_G, ALT_MASK);

        JTabbedPane tabbedPane = robot.finder().findByType(JTabbedPane.class);
        assertThat(tabbedPane.getTitleAt(tabbedPane.getSelectedIndex())).isEqualTo("RGB");
    }
}