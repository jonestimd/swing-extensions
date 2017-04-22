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

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableRowSorter;

import com.google.common.collect.Lists;
import io.github.jonestimd.mockito.ArgumentCaptorFactory;
import io.github.jonestimd.swing.table.DecoratedTable;
import io.github.jonestimd.swing.table.TableSummary;
import io.github.jonestimd.swing.table.model.ValidatedBeanListTableModel;
import io.github.jonestimd.swing.window.StatusFrame;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ValidatedTablePanelTest {
    private static final String RESOURCE_GROUP = "ValidatedTablePanelTest";
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("test-resources");
    @Mock
    private Action saveAction;
    @Mock
    private ValidatedBeanListTableModel<TestBean> tableModel;
    private DecoratedTable<TestBean, ValidatedBeanListTableModel<TestBean>> table;
    private TestPanel testPanel;
    private List<TestBean> confirmedDeletes = new ArrayList<>();

    @Before
    public void setupMocks() throws Exception {
        when(saveAction.getValue(Action.NAME)).thenReturn("Save");
        when(saveAction.getValue(Action.ACCELERATOR_KEY)).thenReturn(KeyStroke.getKeyStroke("ctrl S"));
        when(saveAction.getValue(Action.MNEMONIC_KEY)).thenReturn((int) 'S');
    }

    private void createPanel() {
        createPanel(RESOURCE_GROUP, tableModel);
    }

    private void createPanel(String resourceGroup, ValidatedBeanListTableModel<TestBean> model) {
        table = new DecoratedTable<>(model);
        testPanel = new TestPanel(table, resourceGroup);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void addsTableModelSummaries() throws Exception {
        ValidatedBeanListTableModel model = mock(ValidatedBeanListTableModel.class, withSettings().extraInterfaces(TableSummary.class));
        createPanel(RESOURCE_GROUP, model);
        final JFrame frame = new JFrame();

        SwingUtilities.invokeAndWait(() -> {
            frame.setContentPane(testPanel);
            testPanel.addNotify();
        });

        verify((TableSummary) model).getSummaryProperties();
    }

    @Test
    public void doesNotAddMenuIfMissingResource() throws Exception {
        createPanel("ValidatedTablePanelTest.noMenu", tableModel);
        final JFrame frame = new JFrame();

        SwingUtilities.invokeAndWait(() -> {
            frame.setContentPane(testPanel);
            testPanel.addNotify();
        });

        assertThat(frame.getJMenuBar().getMenuCount()).isEqualTo(1);
        JToolBar toolBar = (JToolBar) frame.getJMenuBar().getComponent(0);
        assertThat(((JButton) toolBar.getComponentAtIndex(0)).getToolTipText()).isEqualTo("Add");
        assertThat(((JButton) toolBar.getComponentAtIndex(1)).getToolTipText()).isEqualTo("Delete");
        assertThat(((JButton) toolBar.getComponentAtIndex(2)).getToolTipText()).isEqualTo("Save (Ctrl-S)");
        assertThat(((JButton) toolBar.getComponentAtIndex(3)).getToolTipText()).isEqualTo("Reload");
    }

    @Test
    public void addsActionsToMenuAndToolbar() throws Exception {
        createPanel();
        final JFrame frame = new JFrame();

        SwingUtilities.invokeAndWait(() -> {
            frame.setContentPane(testPanel);
            testPanel.addNotify();
        });

        assertThat(frame.getJMenuBar().getMenuCount()).isEqualTo(3);
        JMenu menu = frame.getJMenuBar().getMenu(0);
        assertThat(menu.getText()).isEqualTo("Validated Table Panel Test");
        assertThat(menu.getMnemonic()).isEqualTo('V');
        assertThat(menu.getItemCount()).isEqualTo(4);
        checkMenuItem(menu.getItem(0), 'A', "Add");
        checkMenuItem(menu.getItem(1), 'D', "Delete");
        checkMenuItem(menu.getItem(2), 'S', "Save");
        checkMenuItem(menu.getItem(3), 'R', "Reload");
        assertThat(frame.getJMenuBar().getComponent(1)).isInstanceOf(JSeparator.class);
        JToolBar toolBar = (JToolBar) frame.getJMenuBar().getComponent(2);
        assertThat(toolBar.getComponentCount()).isEqualTo(4);
        assertThat(((JButton) toolBar.getComponentAtIndex(0)).getToolTipText()).isEqualTo("Add (Ctrl-A)");
        assertThat(((JButton) toolBar.getComponentAtIndex(1)).getToolTipText()).isEqualTo("Delete (Ctrl-D)");
        assertThat(((JButton) toolBar.getComponentAtIndex(2)).getToolTipText()).isEqualTo("Save (Ctrl-S)");
        assertThat(((JButton) toolBar.getComponentAtIndex(3)).getToolTipText()).isEqualTo("Reload (Ctrl-R)");
    }

    @Test
    public void getTableModel() throws Exception {
        createPanel();

        assertThat(testPanel.getTableModel()).isSameAs(tableModel);
        assertThat(testPanel.getChangeBuffer()).isSameAs(tableModel);
    }

    @Test
    public void getRowSorter() throws Exception {
        createPanel();
        TableRowSorter<ValidatedBeanListTableModel<TestBean>> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        assertThat(testPanel.getRowSorter()).isSameAs(sorter);
    }

    @Test
    public void deleteEnabledForNonEmptySelection() throws Exception {
        createPanel();
        when(tableModel.getRowCount()).thenReturn(3);
        JButton deleteButton = getToolBarButton(1);

        table.setRowSelectionInterval(1, 1);

        assertThat(deleteButton.isEnabled()).isTrue();
    }

    @Test
    public void deleteDisabledForSelectionOfOnlyPendingDeletes() throws Exception {
        createPanel();
        List<TestBean> beans = Lists.newArrayList(new TestBean(), new TestBean(), new TestBean());
        when(tableModel.getRowCount()).thenReturn(3);
        when(tableModel.getBean(anyInt())).thenAnswer(invocation -> beans.get((Integer) invocation.getArguments()[0]));
        when(tableModel.getPendingDeletes()).thenReturn(Lists.newArrayList(beans.get(1)));
        JButton deleteButton = getToolBarButton(1);

        table.setRowSelectionInterval(1, 1);

        assertThat(deleteButton.isEnabled()).isFalse();
    }

    @Test
    public void deleteActionPerformedWithNoRowsConfirmed() throws Exception {
        createPanel();
        JButton deleteButton = getToolBarButton(1);

        deleteButton.getAction().actionPerformed(new ActionEvent(deleteButton, -1, null));

        verify(tableModel, never()).queueDelete(any(TestBean.class));
    }

    @Test
    public void deleteActionPerformedWithRowsConfirmed() throws Exception {
        createPanel();
        confirmedDeletes.add(new TestBean());
        when(tableModel.isChanged()).thenReturn(true);
        JButton deleteButton = getToolBarButton(1);

        deleteButton.getAction().actionPerformed(new ActionEvent(deleteButton, -1, null));

        verify(tableModel).queueDelete(any(TestBean.class));
        verify(saveAction).setEnabled(true);
    }

    private JButton getToolBarButton(int index) {
        JToolBar toolbar = new JToolBar();
        testPanel.addActions(toolbar);
        return (JButton) toolbar.getComponentAtIndex(index);
    }

    @Test
    public void saveDisabledForNoPendingChanges() throws Exception {
        createPanel();
        when(tableModel.isChanged()).thenReturn(false);
        ArgumentCaptor<TableModelListener> listener = ArgumentCaptorFactory.create();
        verify(tableModel, times(2)).addTableModelListener(listener.capture());

        listener.getValue().tableChanged(new TableModelEvent(tableModel, 0));

        verify(saveAction, times(2)).setEnabled(false);
    }

    @Test
    public void saveEnabledForPendingChangesAndNoErrors() throws Exception {
        createPanel();
        when(tableModel.isChanged()).thenReturn(true);
        when(tableModel.isNoErrors()).thenReturn(true);
        ArgumentCaptor<TableModelListener> listener = ArgumentCaptorFactory.create();
        verify(tableModel, times(2)).addTableModelListener(listener.capture());
        verify(saveAction).setEnabled(false);

        listener.getValue().tableChanged(new TableModelEvent(tableModel, 0));

        verify(saveAction).setEnabled(true);
    }

    @Test
    public void saveDisabledForPendingChangesAndErrors() throws Exception {
        createPanel();
        when(tableModel.isChanged()).thenReturn(true);
        when(tableModel.isNoErrors()).thenReturn(false);
        ArgumentCaptor<TableModelListener> listener = ArgumentCaptorFactory.create();
        verify(tableModel, times(2)).addTableModelListener(listener.capture());

        listener.getValue().tableChanged(new TableModelEvent(tableModel, 0));

        verify(saveAction, times(2)).setEnabled(false);
    }

    @Test
    public void notifyUnsavedChangesIndicator() throws Exception {
        createPanel();
        StatusFrame frame = new StatusFrame(BUNDLE, RESOURCE_GROUP);
        frame.setContentPane(testPanel);
        when(tableModel.isChanged()).thenReturn(true);
        ArgumentCaptor<TableModelListener> listener = ArgumentCaptorFactory.create();
        verify(tableModel, times(2)).addTableModelListener(listener.capture());

        listener.getValue().tableChanged(new TableModelEvent(tableModel, 0));

        assertThat(frame.getTitle()).endsWith("*");
    }

    @Test
    public void getSelectedBean() throws Exception {
        createPanel();
        List<TestBean> beans = Lists.newArrayList(new TestBean(), new TestBean(), new TestBean());
        when(tableModel.getRowCount()).thenReturn(3);
        when(tableModel.getRow(anyInt())).thenAnswer(invocation -> beans.get((Integer) invocation.getArguments()[0]));
        assertThat(testPanel.getSelectedBean()).isNull();

        table.setRowSelectionInterval(1, 1);
        assertThat(testPanel.getSelectedBean()).isSameAs(beans.get(1));

        table.setRowSelectionInterval(1, 2);
        assertThat(testPanel.getSelectedBean()).isSameAs(beans.get(1));
    }

    @Test
    public void isSingleRowSelected() throws Exception {
        createPanel();
        when(tableModel.getRowCount()).thenReturn(3);

        table.setRowSelectionInterval(1, 1);
        assertThat(testPanel.isSingleRowSelected()).isTrue();

        table.setRowSelectionInterval(1, 2);
        assertThat(testPanel.isSingleRowSelected()).isFalse();
    }

    private void checkMenuItem(JMenuItem item, char mnemonic, String text) {
        assertThat(item.getText()).isEqualTo(text);
        assertThat(item.getMnemonic()).isEqualTo(mnemonic);
        assertThat(item.getAccelerator()).isEqualTo(KeyStroke.getKeyStroke("ctrl " + mnemonic));
    }

    private static class TestBean {
    }

    private class TestPanel extends ValidatedTablePanel<TestBean> {
        public TestPanel(DecoratedTable<TestBean, ValidatedBeanListTableModel<TestBean>> table, String resourceGroup) {
            super(BUNDLE, table, resourceGroup);
        }

        @Override
        protected Action createSaveAction() {
            return saveAction;
        }

        @Override
        protected TestBean newBean() {
            return null;
        }

        @Override
        protected List<TestBean> confirmDelete(List<TestBean> items) {
            return confirmedDeletes;
        }

        @Override
        protected List<TestBean> getTableData() {
            return new ArrayList<>();
        }
    }
}