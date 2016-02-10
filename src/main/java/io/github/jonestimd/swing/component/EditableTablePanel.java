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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableRowSorter;

import io.github.jonestimd.swing.ChangeBuffer;
import io.github.jonestimd.swing.ComponentFactory;
import io.github.jonestimd.swing.ComponentTreeUtils;
import io.github.jonestimd.swing.UnsavedChangesIndicator;
import io.github.jonestimd.swing.action.MnemonicAction;
import io.github.jonestimd.swing.table.DecoratedTable;
import io.github.jonestimd.swing.table.TableSummary;
import io.github.jonestimd.swing.table.TableSummaryPanel;
import io.github.jonestimd.swing.table.action.AddRowAction;
import io.github.jonestimd.swing.table.action.ReloadTableAction;
import io.github.jonestimd.swing.table.model.BeanListTableModel;
import io.github.jonestimd.swing.table.model.ValidatedBeanListTableModel;

/**
 * A panel containing a {@link DecoratedTable} (for a {@link BeanListTableModel}) and validation status area.
 * Adds a toolbar containing New, Save and Reload actions to the menu bar when added to a {@link JFrame}.
 * @param <T> class of the beans in the {@link BeanListTableModel}
 */
public abstract class EditableTablePanel<T> extends ValidatedPanel {
    private final ValidatedBeanListTableModel<T> tableModel;
    private final DecoratedTable<T, ValidatedBeanListTableModel<T>> table;
    protected final ResourceBundle bundle;
    protected final String resourceGroup;
    private final TableSummaryPanel tableSummaryPanel = new TableSummaryPanel();
    private Action addAction;
    private Action deleteAction;
    private Action saveAction;
    private Action reloadAction;
    private final TableModelListener changeHandler = new TableModelListener() {
        public void tableChanged(TableModelEvent e) {
            UnsavedChangesIndicator indicator = ComponentTreeUtils.findAncestor(EditableTablePanel.this, UnsavedChangesIndicator.class);
            if (indicator != null) { // table may not be visible
                indicator.setUnsavedChanges(tableModel.isChanged());
            }
            saveAction.setEnabled(tableModel.isChanged() && tableModel.isNoErrors());
        }
    };

    @SuppressWarnings("unchecked")
    protected EditableTablePanel(ResourceBundle bundle, DecoratedTable<T, ValidatedBeanListTableModel<T>> table, String resourceGroup) {
        super(bundle, 1);
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
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    tableDoubleClicked();
                }
            }
        });
        tableModel.addTableModelListener(changeHandler);
        createActions();
        setForm(new JScrollPane(table));
        addTableSummaryPanel();
    }

    private void createActions() {
        addAction = new AddRowAction<T>(bundle, String.format("action.%s.new", resourceGroup), tableModel, table, this::newBean);
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

    public void addSummaries(TableSummary tableSummary) {
        tableSummaryPanel.addSummaries(tableSummary);
    }

    public ValidatedBeanListTableModel<T> getTableModel() {
        return tableModel;
    }

    protected DecoratedTable<T, ValidatedBeanListTableModel<T>> getTable() {
        return table;
    }

    protected abstract T newBean();
    protected abstract List<T> confirmDelete(List<T> items);

    protected boolean isDeleteEnabled(List<T> selectionMinusPendingDeletes) {
        return ! selectionMinusPendingDeletes.isEmpty();
    }

    protected List<T> getSelectionMinusPendingDeletes() {
        List<T> selectedItems = table.getSelectedItems();
        selectedItems.removeAll(tableModel.getPendingDeletes());
        return selectedItems;
    }

    @Override
    protected void initializeMenu(JMenuBar menuBar) {
        addMenu(menuBar);
        menuBar.add(createToolBar());
        reloadAction.actionPerformed(new ActionEvent(this, -1, null));
    }

    protected JToolBar createToolBar() {
        JToolBar toolbar = ComponentFactory.newMenuToolBar();
        addActions(toolbar);
        return toolbar;
    }

    protected void addActions(JToolBar toolbar) {
        toolbar.add(ComponentFactory.newToolbarButton(addAction));
        toolbar.add(ComponentFactory.newToolbarButton(deleteAction));
        toolbar.add(ComponentFactory.newToolbarButton(saveAction));
        toolbar.add(ComponentFactory.newToolbarButton(reloadAction));
    }

    private void addMenu(JMenuBar menuBar) {
        String mnemonicAndNameKey = String.format("menu.%s.mnemonicAndName", resourceGroup);
        if (bundle.containsKey(mnemonicAndNameKey)) {
            JMenu menu = ComponentFactory.createMenu(bundle, mnemonicAndNameKey);
            addActions(menu);
            menuBar.add(menu, 0);
            menuBar.add(ComponentFactory.newMenuBarSeparator());
        }
    }

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
        return tableModel.getRow(getSelectedRow());
    }

    private int getSelectedRow() {
        return table.convertRowIndexToModel(table.getSelectedRow());
    }

    /**
     * Load the table data.
     */
    protected abstract List<T> getTableData();

    /**
     * Notify subclasses when the table selection changes.
     */
    protected void tableSelectionChanged() {
        deleteAction.setEnabled(isDeleteEnabled(getSelectionMinusPendingDeletes()));
    }

    /**
     * Notify subclasses when the table is double clicked.
     */
    protected void tableDoubleClicked() {}

    private class DeleteAction extends MnemonicAction {
        private DeleteAction() {
            super(bundle, String.format("action.%s.delete", resourceGroup));
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent event) {
            List<T> selectedItems = getSelectionMinusPendingDeletes();
            List<T> items = confirmDelete(selectedItems);
            if (!items.isEmpty()) {
                deleteAll(items);
                saveAction.setEnabled(tableModel.isChanged());
                table.requestFocus();
            }
        }

        private void deleteAll(List<T> items) {
            for (T item : items) {
                tableModel.queueDelete(item);
            }
        }
    }

    private class ReloadActionHandler extends ReloadTableAction<T> {
        public ReloadActionHandler() {
            super(EditableTablePanel.this, bundle, String.format("action.%s.reload", resourceGroup), table, tableModel);
        }

        @Override
        public List<T> performTask() {
            return getTableData();
        }
    }
}