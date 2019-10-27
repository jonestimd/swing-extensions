// The MIT License (MIT)
//
// Copyright (c) 2018 Timothy D. Jones
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
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Collections;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JWindow;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.Highlighter.HighlightPainter;

import io.github.jonestimd.swing.JFrameRobotTest;
import io.github.jonestimd.swing.component.ListField;
import io.github.jonestimd.swing.component.ListField.ItemValidator;
import org.assertj.core.util.Lists;
import org.junit.Test;

import static io.github.jonestimd.util.TestUtil.*;
import static org.assertj.core.api.Assertions.*;

public class PopupListTableCellEditorTest extends JFrameRobotTest {
    public static final int WINDOW_WIDTH = 400;
    public static final int WINDOW_HEIGHT = 300;
    private DefaultTableModel tableModel = new DefaultTableModel(new String[]{"List"}, 20);
    private JTable table = new JTable(tableModel);
    private PopupListTableCellEditor<String> cellEditor;


    @Override
    protected JPanel createContentPane() {
        table.getColumnModel().getColumn(0).setCellRenderer(new MultiSelectTableCellRenderer<String>(true));
        table.getColumnModel().getColumn(0).setCellEditor(cellEditor);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(table, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        return panel;
    }

    @Test
    public void showsPopup() throws Exception {
        cellEditor = PopupListTableCellEditor.builder(String::toString, String::toString).build();
        showWindow();
        robot.focus(table);

        robot.pressAndReleaseKeys(KeyEvent.VK_DOWN, KeyEvent.VK_SPACE);

        JWindow popupWindow = (JWindow) robot.finder().find(c -> c.getClass() == JWindow.class);
        assertThat(popupWindow).isNotNull();
    }

    @Test
    public void initializesListWithCellValue() throws Exception {
        tableModel.setValueAt(Lists.newArrayList("Apple", "Banana"), 0, 0);
        cellEditor = PopupListTableCellEditor.builder(String::toString, String::toString).build();
        showWindow();
        robot.focus(table);

        robot.pressAndReleaseKeys(KeyEvent.VK_DOWN, KeyEvent.VK_SPACE);

        JWindow popupWindow = (JWindow) robot.finder().find(c -> c.getClass() == JWindow.class);
        ListField field = robot.finder().findByType(popupWindow, ListField.class);
        assertThat(field.getText()).isEqualTo("Apple\nBanana");
    }

    @Test
    public void commitUpdatesTableModel() throws Exception {
        cellEditor = PopupListTableCellEditor.builder(String::toString, String::toString).build();
        showWindow();
        robot.focus(table);
        robot.pressAndReleaseKeys(KeyEvent.VK_DOWN, KeyEvent.VK_SPACE);
        JWindow popupWindow = (JWindow) robot.finder().find(c -> c.getClass() == JWindow.class);

        robot.enterText("Apple\nOrange\n");
        robot.pressAndReleaseKey(KeyEvent.VK_ENTER, KeyEvent.CTRL_MASK);

        assertCondition(() -> !popupWindow.isShowing(), "popup JWindow closed", 1000L);
        assertThat(tableModel.getValueAt(0, 0)).isEqualTo(Arrays.asList("Apple", "Orange"));
    }

    @Test
    public void cancelDoesNotUpdateTableModel() throws Exception {
        cellEditor = PopupListTableCellEditor.builder(String::toString, String::toString).build();
        showWindow();
        robot.focus(table);
        robot.pressAndReleaseKeys(KeyEvent.VK_DOWN, KeyEvent.VK_SPACE);
        JWindow popupWindow = (JWindow) robot.finder().find(c -> c.getClass() == JWindow.class);

        robot.enterText("Apple\nOrange\n");
        robot.pressAndReleaseKey(KeyEvent.VK_ESCAPE);

        assertCondition(() -> !popupWindow.isShowing(), "popup window closed", 1000L);
        assertThat(tableModel.getValueAt(0, 0)).isNull();
    }

    @Test
    public void stopCellEditingReturnsFalseForInvalidList() throws Exception {
        cellEditor = PopupListTableCellEditor.builder(String::toString, String::toString).build();
        showWindow();
        robot.focus(table);
        robot.pressAndReleaseKeys(KeyEvent.VK_DOWN, KeyEvent.VK_SPACE);
        JWindow popupWindow = (JWindow) robot.finder().find(c -> c.getClass() == JWindow.class);

        robot.enterText("Apple\n\n");

        assertThat(cellEditor.stopCellEditing()).isFalse();
        assertThat(popupWindow.isShowing()).isTrue();
    }

    @Test
    public void stopCellEditingReturnsTrueForValidList() throws Exception {
        cellEditor = PopupListTableCellEditor.builder(String::toString, String::toString).build();
        showWindow();
        robot.focus(table);
        robot.pressAndReleaseKeys(KeyEvent.VK_DOWN, KeyEvent.VK_SPACE);
        JWindow popupWindow = (JWindow) robot.finder().find(c -> c.getClass() == JWindow.class);

        robot.enterText("Apple\nPeach\n");

        assertThat(cellEditor.stopCellEditing()).isTrue();
        assertThat(popupWindow.isShowing()).isFalse();
        assertThat(tableModel.getValueAt(0, 0)).isEqualTo(Collections.emptyList());
    }

    @Test
    public void popupMovesWithWindow() throws Exception {
        cellEditor = PopupListTableCellEditor.builder(String::toString, String::toString).build();
        showWindow();
        robot.focus(table);
        robot.pressAndReleaseKeys(KeyEvent.VK_DOWN, KeyEvent.VK_SPACE);
        JWindow popupWindow = (JWindow) robot.finder().find(c -> c.getClass() == JWindow.class);

        window.setLocation(window.getX()+50, window.getY());

        robot.waitForIdle();
        assertThat(popupWindow.getLocationOnScreen()).isEqualTo(table.getLocationOnScreen());
    }

    @Test
    public void builderSetsValidator() throws Exception {
        ItemValidator validator = (items, index) -> items.get(index).contains("x");

        cellEditor = PopupListTableCellEditor.builder(String::toString, String::toString).validator(validator).build();

        ListField textArea = getField(cellEditor, "textArea", ListField.class);
        assertThat(getField(textArea, "validator", ItemValidator.class)).isSameAs(validator);
    }

    @Test
    public void builderSetsErrorPainter() throws Exception {
        HighlightPainter errorPainter = (g, p0, p1, bounds, c) -> {};

        cellEditor = PopupListTableCellEditor.builder(String::toString, String::toString).errorPainter(errorPainter).build();

        ListField textArea = getField(cellEditor, "textArea", ListField.class);
        assertThat(getField(textArea, "errorPainter", HighlightPainter.class)).isSameAs(errorPainter);
    }

    @Test
    public void builderSetsRows() throws Exception {
        final int rows = 10;

        cellEditor = PopupListTableCellEditor.builder(String::toString, String::toString).rows(rows).build();

        ListField textArea = getField(cellEditor, "textArea", ListField.class);
        assertThat(textArea.getRows()).isEqualTo(rows);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());

        DefaultTableModel tableModel = new DefaultTableModel(new String[]{"List"}, 20);
        JTable table = new JTable(tableModel);
        table.getColumnModel().getColumn(0).setCellRenderer(new MultiSelectTableCellRenderer<String>(true));
        table.getColumnModel().getColumn(0).setCellEditor(PopupListTableCellEditor.builder(String::toString, String::toString).build());
        frame.getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);

        frame.pack();
        frame.setSize(new Dimension(500, 400));
        frame.setLocation(100, 500);
        frame.setVisible(true);
    }
}