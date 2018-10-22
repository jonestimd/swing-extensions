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
package io.github.jonestimd.swing.component;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import io.github.jonestimd.swing.ChangeBuffer;
import io.github.jonestimd.swing.ComponentFactory;
import io.github.jonestimd.swing.ComponentTreeUtils;
import io.github.jonestimd.swing.UnsavedChangesIndicator;
import io.github.jonestimd.swing.action.ActionAdapter;
import io.github.jonestimd.swing.table.DecoratedTable;
import io.github.jonestimd.swing.table.TableSummary;
import io.github.jonestimd.swing.table.TableSummaryPanel;
import io.github.jonestimd.swing.table.action.AddRowAction;
import io.github.jonestimd.swing.table.action.ReloadTableAction;
import io.github.jonestimd.swing.table.model.ValidatedBeanListTableModel;
import io.github.jonestimd.swing.window.StatusFrame;

/**
 * A {@link ValidatedPanel} that displays a {@link DecoratedTable} (for a {@link ValidatedBeanListTableModel}) and a
 * {@link TableSummaryPanel}.  The following actions are added to a toolbar on the frame's menu bar.  The actions
 * are initialized using the specified resources from the resource bundle.
 * <table>
 *     <tr><th>Action</th><th>Description</th><th>Resource Bundle Keys</th></tr>
 *     <tr><td rowspan="3">Add</td><td rowspan="3">add a new row to the table</td>
 *          <td><em>resourceGroup</em>.action.new.mnemonicAndName</td></tr>
 *     <tr><td><em>resourceGroup</em>.action.new.iconImage</td></tr>
 *     <tr><td><em>resourceGroup</em>.action.new.accelerator (optional)</td></tr>
 *     <tr><td rowspan="3">Delete</td><td rowspan="3">mark a row as pending deletion</td>
 *          <td><em>resourceGroup</em>.action.delete.mnemonicAndName</td></tr>
 *     <tr><td><em>resourceGroup</em>.action.delete.iconImage</td></tr>
 *     <tr><td><em>resourceGroup</em>.action.delete.accelerator (optional)</td></tr>
 *     <tr><td>Save</td><td>save pending changes</td><td></td></tr>
 *     <tr><td rowspan="4">Reload</td><td rowspan="4">reload the table data and discard pending changes</td>
 *          <td><em>resourceGroup</em>.action.reload.mnemonicAndName</td></tr>
 *     <tr><td><em>resourceGroup</em>.action.reload.iconImage</td></tr>
 *     <tr><td><em>resourceGroup</em>.action.reload.accelerator (optional)</td></tr>
 *     <tr><td><em>resourceGroup</em>.action.reload.status.initialize (optional)</td></tr>
 * </table>
 * <p>If the resource bundle contains the key <em>resourceGroup</em>.menu.mnemonicAndName then the actions are also added
 * to the specified menu on the frame's menu bar.</p>
 * <p>This panel includes a table model listener that enables/disables the save action and that notifies an ancestor
 * container that implements {@link UnsavedChangesIndicator} (e.g. a {@link StatusFrame}).</p>
 * @param <T> class of the beans in the {@link ValidatedBeanListTableModel}
 */
public abstract class ValidatedTablePanel<T> extends ValidatedPanel {
    private final ValidatedBeanListTableModel<T> tableModel;
    private final DecoratedTable<T, ? extends ValidatedBeanListTableModel<T>> table;
    protected final ResourceBundle bundle;
    protected final String resourceGroup;
    private final TableSummaryPanel tableSummaryPanel = new TableSummaryPanel();
    private Action addAction;
    private Action deleteAction;
    private Action saveAction;
    private Action reloadAction;

    /**
     * Constructs a new {@code ValidatedTablePanel} using resources from {@code bundle}.
     * @param bundle the resource bundle to use for UI components
     * @param table the table to include on the panel
     * @param resourceGroup a string used to look up component resources
     */
    @SuppressWarnings("unchecked")
    protected ValidatedTablePanel(ResourceBundle bundle, DecoratedTable<T, ? extends ValidatedBeanListTableModel<T>> table, String resourceGroup) {
        super(bundle, 1, new JScrollPane(table));
        this.bundle = bundle;
        this.table = table;
        this.tableModel = table.getModel();
        this.resourceGroup = resourceGroup;
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.getSelectionModel().addListSelectionListener(event -> {
            if (! event.getValueIsAdjusting()) {
                tableSelectionChanged();
            }
        });
        tableModel.addTableModelListener(event -> {
            UnsavedChangesIndicator indicator = ComponentTreeUtils.findAncestor(ValidatedTablePanel.this, UnsavedChangesIndicator.class);
            if (indicator != null) { // table may not be visible
                indicator.setUnsavedChanges(tableModel.isChanged());
            }
            saveAction.setEnabled(tableModel.isChanged() && tableModel.isNoErrors());
        });
        createActions();
        addTableSummaryPanel();
    }

    private void createActions() {
        addAction = new AddRowAction<>(bundle, resourceGroup + ".action.new", tableModel, table, this::newBean);
        deleteAction = new DeleteAction();
        saveAction = createSaveAction();
        saveAction.setEnabled(false);
        reloadAction = new ReloadActionHandler();
    }

    protected abstract Action createSaveAction();

    @SuppressWarnings("unchecked")
    protected TableRowSorter<ValidatedBeanListTableModel<T>> getRowSorter() {
        return ((TableRowSorter) getTable().getRowSorter());
    }

    private void addTableSummaryPanel() {
        add(tableSummaryPanel, BorderLayout.NORTH);
        if (tableModel instanceof TableSummary) {
            addSummaries((TableSummary) tableModel);
        }
    }

    /**
     * Add an item to the summary panel.
     * @param tableSummary the summary item
     */
    public void addSummaries(TableSummary tableSummary) {
        tableSummaryPanel.addSummaries(tableSummary);
    }

    public ValidatedBeanListTableModel<T> getTableModel() {
        return tableModel;
    }

    protected DecoratedTable<T, ? extends ValidatedBeanListTableModel<T>> getTable() {
        return table;
    }

    /**
     * Override to create a bean for a new row in the table.
     * @return the new bean
     */
    protected abstract T newBean();

    /**
     * Override to confirm deleting items from the table.  The input list can be modified and used as the return value.
     * Only items in the returned list will actually be deleted.
     * @param items the items selected for deletion.
     * @return the items that have been confirmed for deletion
     */
    protected abstract List<T> confirmDelete(List<T> items);

    protected boolean isDeleteEnabled(List<T> selectionMinusPendingDeletes) {
        return ! selectionMinusPendingDeletes.isEmpty();
    }

    protected List<T> getSelectionMinusPendingDeletes() {
        List<T> selectedItems = table.getSelectedItems();
        selectedItems.removeAll(tableModel.getPendingDeletes());
        return selectedItems;
    }

    /**
     * Adds a menu and toolbar to the frame's menu bar.
     * @param menuBar the frame's menu bar
     * @see #addActions(JMenu)
     * @see #addActions(JToolBar)
     */
    @Override
    protected void initializeMenu(JMenuBar menuBar) {
        addMenu(menuBar);
        menuBar.add(createToolBar());
        reloadAction.actionPerformed(new ActionEvent(this, -1, null));
    }

    /**
     * Creates the toolbar to add to the frame's menu bar.
     * @return the new toolbar
     * @see #addActions(JToolBar)
     */
    protected JToolBar createToolBar() {
        JToolBar toolbar = ComponentFactory.newMenuToolBar();
        addActions(toolbar);
        return toolbar;
    }

    /**
     * Adds the {@code add}, {@code delete}, {@code save} and {@code reload} actions to the toolbar.
     * @param toolbar the toolbar on the frame's menu bar
     */
    protected void addActions(JToolBar toolbar) {
        toolbar.add(ComponentFactory.newToolbarButton(addAction));
        toolbar.add(ComponentFactory.newToolbarButton(deleteAction));
        toolbar.add(ComponentFactory.newToolbarButton(saveAction));
        toolbar.add(ComponentFactory.newToolbarButton(reloadAction));
    }

    private void addMenu(JMenuBar menuBar) {
        String mnemonicAndNameKey = resourceGroup + ".menu.mnemonicAndName";
        if (bundle.containsKey(mnemonicAndNameKey)) {
            JMenu menu = ComponentFactory.newMenu(bundle, mnemonicAndNameKey);
            addActions(menu);
            menuBar.add(menu, 0);
            menuBar.add(ComponentFactory.newMenuBarSeparator());
        }
    }

    /**
     * Adds the {@code add}, {@code delete}, {@code save} and {@code reload} actions to the menu.
     * @param menu the menu from the frame's menu bar
     */
    protected void addActions(JMenu menu) {
        menu.add(new JMenuItem(addAction));
        menu.add(new JMenuItem(deleteAction));
        menu.add(new JMenuItem(saveAction));
        menu.add(new JMenuItem(reloadAction));
    }

    protected ChangeBuffer getChangeBuffer() {
        return tableModel;
    }

    protected boolean isSingleRowSelected() {
        return table.getSelectedRowCount() == 1;
    }

    public T getSelectedBean() {
        return table.getSelectedRow() >= 0 ? tableModel.getRow(getSelectedRow()) : null;
    }

    private int getSelectedRow() {
        return table.convertRowIndexToModel(table.getSelectedRow());
    }

    /**
     * Override to return the table data.
     */
    protected abstract List<T> getTableData();

    /**
     * Updates the enabled state of actions.  May be overridden to handle changes to table selection.
     */
    protected void tableSelectionChanged() {
        deleteAction.setEnabled(isDeleteEnabled(getSelectionMinusPendingDeletes()));
    }

    private class DeleteAction extends AbstractAction {
        private DeleteAction() {
            ActionAdapter.initialize(this, bundle, resourceGroup + ".action.delete");
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent event) {
            List<T> selectedItems = getSelectionMinusPendingDeletes();
            List<T> items = confirmDelete(selectedItems);
            if (!items.isEmpty()) {
                items.forEach(tableModel::queueDelete);
                saveAction.setEnabled(tableModel.isChanged());
                table.requestFocus();
            }
        }
    }

    private class ReloadActionHandler extends ReloadTableAction<T> {
        public ReloadActionHandler() {
            super(ValidatedTablePanel.this, bundle, resourceGroup + ".action.reload", table, tableModel);
        }

        @Override
        public List<T> performTask() {
            return getTableData();
        }
    }
}