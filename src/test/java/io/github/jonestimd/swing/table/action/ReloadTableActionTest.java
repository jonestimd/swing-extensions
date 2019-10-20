// The MIT License (MIT)
//
// Copyright (c) 2017 Timothy D. Jones
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
package io.github.jonestimd.swing.table.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumnModel;

import io.github.jonestimd.swing.table.DecoratedTable;
import io.github.jonestimd.swing.table.model.BeanListTableModel;
import io.github.jonestimd.swing.table.model.BufferedBeanListTableModel;
import io.github.jonestimd.swing.window.StatusFrame;
import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.Robot;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ReloadTableActionTest {
    private static final ResourceBundle BUNDLE = new ListResourceBundle() {
        @Override
        protected Object[][] getContents() {
            return new String[][] {{ "reload.mnemonicAndName", "RReload" }};
        }
    };
    @Mock
    private DecoratedTable<String, BeanListTableModel<String>> table;
    @Mock
    private ListSelectionModel selectionModel;
    @Mock
    private ListSelectionModel columnSelectionModel;
    @Mock
    private TableColumnModel columnModel;
    @Mock
    private BufferedBeanListTableModel<String> tableModel;
    private StatusFrame window;
    private JPanel owner = new JPanel();
    private Robot robot;

    @Before
    public void setUp() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            window = new StatusFrame(ResourceBundle.getBundle("test-resources"), "StatusFrameTest");
            window.getContentPane().add(owner);
        });
        when(table.getSelectionModel()).thenReturn(selectionModel);
        when(table.getColumnModel()).thenReturn(columnModel);
        when(columnModel.getSelectionModel()).thenReturn(columnSelectionModel);
        robot = BasicRobot.robotWithNewAwtHierarchy();
    }

    @After
    public void cleanUp() throws Exception {
        robot.cleanUp();
        SwingUtilities.invokeAndWait(window::dispose);
    }

    @Test
    public void confirmActionReturnsTrueForNoChanges() throws Exception {
        TestAction action = new TestAction();
        when(tableModel.isChanged()).thenReturn(false);

        assertThat(action.confirmAction(null)).isTrue();
    }

    @Test
    public void confirmActionReturnsTrueForChangesAndConfirmed() throws Exception {
        TestAction action = new TestAction();
        when(tableModel.isChanged()).thenReturn(true);
        List<Boolean> holder = new ArrayList<>();

        SwingUtilities.invokeLater(() -> holder.add(action.confirmAction(null)));

        JOptionPane dialog = robot.finder().findByType(JOptionPane.class);
        JButton button = robot.finder().find(JButtonMatcher.withText("Discard Changes"));
        SwingUtilities.invokeAndWait(button::doClick);
        robot.waitForIdle();
        assertThat(holder).containsExactly(true);
    }

    @Test
    public void confirmActionReturnsFalseForChangesAndNotConfirmed() throws Exception {
        TestAction action = new TestAction();
        when(tableModel.isChanged()).thenReturn(true);
        List<Boolean> holder = new ArrayList<>();

        SwingUtilities.invokeLater(() -> holder.add(action.confirmAction(null)));

        JOptionPane dialog = robot.finder().findByType(JOptionPane.class);
        JButton button = robot.finder().find(JButtonMatcher.withText("Cancel"));
        SwingUtilities.invokeAndWait(button::doClick);
        robot.waitForIdle();
        assertThat(holder).containsExactly(false);
    }

    @Test
    public void updateUiAllowsEmptyTable() throws Exception {
        TestAction action = new TestAction();
        when(table.getSelectedRow()).thenReturn(-1);
        when(table.getSelectedColumn()).thenReturn(-1);
        when(table.getRowCount()).thenReturn(0);

        action.updateUI(Arrays.asList("abc", "def"));

        verify(selectionModel, never()).setSelectionInterval(anyInt(), anyInt());
        verify(columnSelectionModel, never()).setSelectionInterval(anyInt(), anyInt());
    }

    @Test
    public void updateUiSelectsFirstRowIfNoSelectionBeforeLoad() throws Exception {
        TestAction action = new TestAction();
        when(table.getSelectedRow()).thenReturn(-1);
        when(table.getSelectedColumn()).thenReturn(-1);
        when(table.getRowCount()).thenReturn(2);

        action.updateUI(Arrays.asList("abc", "def"));

        verify(selectionModel).setSelectionInterval(0, 0);
        verify(columnSelectionModel).setSelectionInterval(0, 0);
    }

    @Test
    public void updateUiRetainsSelection() throws Exception {
        TestAction action = new TestAction();
        when(table.getSelectedRow()).thenReturn(1);
        when(table.getSelectedColumn()).thenReturn(1);
        when(table.getRowCount()).thenReturn(2);

        action.updateUI(Arrays.asList("abc", "def"));

        verify(selectionModel).setSelectionInterval(1, 1);
        verify(columnSelectionModel).setSelectionInterval(1, 1);
    }

    private class TestAction extends ReloadTableAction<String> {
        public TestAction() {
            super(owner, BUNDLE, "reload", table, tableModel);
        }

        @Override
        protected List<String> performTask() {
            return null;
        }
    }
}