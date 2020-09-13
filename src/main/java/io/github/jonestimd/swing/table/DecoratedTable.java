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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EventObject;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.RowSorter.SortKey;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.TableUI;
import javax.swing.plaf.UIResource;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;

import io.github.jonestimd.swing.ComponentDefaults;
import io.github.jonestimd.swing.FocusContainer;
import io.github.jonestimd.swing.table.model.BeanTableModel;
import io.github.jonestimd.swing.table.model.ColumnIdentifier;

/**
 * A table that uses {@link TableDecorator}s to prepare cell renderers and provide consistent styling of cell values.
 * Extends {@link JTable} to provide the following:
 * <ul>
 *     <li>apply a list of {@link TableDecorator}s to cell renderers</li>
 *     <li>display hover effects provided by {@link HoverTableCellRenderer}</li>
 *     <li>handle multiline column headers</li>
 *     <li>ensure uniform row background color when an alternate row color is defined (fixes boolean cells)</li>
 *     <li>improve keyboard handling for cell editing</li>
 *         <ul>
 *              <li>start editing on text input</li>
 *              <li>clear cell and start editing on {@code ctrl-BACKSPACE}</li>
 *              <li>use initial text key for item selection in combo box cell editor</li>
 *              <li>stop editing on {@code ENTER} and remain on current cell</li>
 *         </ul>
 * </ul>
 */
public class DecoratedTable<Bean, Model extends BeanTableModel<Bean>> extends JTable {
    private final List<TableDecorator> decorators = new ArrayList<>();
    private Color evenBackground;
    private Color oddBackground;
    private int headerRows;
    private int hoverRow = -1;
    private int hoverColumn = -1;

    public DecoratedTable(Model dm) {
        super(dm);
        setSurrendersFocusOnKeystroke(true);
        // putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent event) {
                setCursor(event, rowAtPoint(event.getPoint()), columnAtPoint(event.getPoint()));
            }

            @Override
            public void mouseMoved(MouseEvent event) {
                int row = rowAtPoint(event.getPoint());
                int column = columnAtPoint(event.getPoint());
                setCursor(event, row, column);
                clearHoverEffect(row, column);
                setHoverEffect(row, column);
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                handleClick(event);
            }
        });
    }

    private void setCursor(MouseEvent event, int row, int column) {
        if ((row != -1) && (column != -1)) {
            setCursor(getModel().getCursor(event, this, convertRowIndexToModel(row), convertColumnIndexToModel(column)));
        }
    }

    private void clearHoverEffect(int row, int column) {
        if (hoverRow >= 0 && (row != hoverRow || column != hoverColumn)) {
            paintImmediately(getCellRect(hoverRow, hoverColumn, false));
            hoverRow = hoverColumn = -1;
        }
    }

    private void setHoverEffect(int row, int column) {
        if (row >= 0 && column >= 0 && hoverEffect(convertRowIndexToModel(row), convertColumnIndexToModel(column))) {
            hoverRow = row;
            hoverColumn = column;
            paintImmediately(getCellRect(row, column, false));
        }
    }

    protected boolean hoverEffect(int modelRow, int modelColumn) {
        return decorators.stream().anyMatch(decorator -> decorator instanceof TableHoverDecorator
                && ((TableHoverDecorator) decorator).hoverEffect(this, modelRow, modelColumn));
    }

    private void handleClick(MouseEvent event) {
        int hitColumnIndex = columnAtPoint(event.getPoint());
        int hitRowIndex = rowAtPoint(event.getPoint());
        if ((hitColumnIndex != -1) && (hitRowIndex != -1)) {
            int modelRow = convertRowIndexToModel(hitRowIndex);
            int modelColumn = convertColumnIndexToModel(hitColumnIndex);
            getModel().handleClick(event, this, modelRow, modelColumn);
            for (TableDecorator decorator : decorators) {
                if (decorator instanceof TableMouseDecorator) {
                    ((TableMouseDecorator) decorator).onClick(event, this, modelRow, modelColumn);
                }
            }
        }
    }

    public int getHoverRow() {
        return hoverRow;
    }

    public int getHoverColumn() {
        return hoverColumn;
    }

    @Override
    public void setModel(TableModel dataModel) {
        boolean setSortKeys = getRowSorter() != null && getAutoCreateRowSorter();
        List<? extends SortKey> sortKeys = setSortKeys ? getRowSorter().getSortKeys() : Collections.emptyList();
        super.setModel(dataModel);
        if (setSortKeys) getRowSorter().setSortKeys(sortKeys);
    }

    /**
     * Overridden to set background colors.
     */
    @Override
    public void setUI(TableUI ui) {
        evenBackground = ComponentDefaults.getColor("Table.alternateRowColor");
        oddBackground = ComponentDefaults.getColor("Table.background");
        super.setUI(ui);
    }

    /**
     * Set the background color for odd rows.
     */
    @Override
    public void setBackground(Color color) {
        super.setBackground(color);
        oddBackground = color;
    }

    /**
     * Get the background color for even rows.
     */
    public Color getAlternateBackground() {
        return evenBackground;
    }

    /**
     * Set the background color for even rows.
     */
    public void setAlternateBackground(Color background) {
        this.evenBackground = background;
    }

    /**
     * Overridden to handle multiline column headers.
     */
    @Override
    public void addColumn(TableColumn aColumn) {
        super.addColumn(aColumn);
        if (getModel() instanceof ColumnIdentifier) {
            aColumn.setIdentifier(((ColumnIdentifier) getModel()).getColumnIdentifier(aColumn.getModelIndex()));
        }
        int columnHeaderRows = aColumn.getHeaderValue().toString().split("\n").length;
        if (columnHeaderRows > 1) {
            aColumn.setHeaderValue("<html><center>" + aColumn.getHeaderValue().toString().replaceAll("\n", "<br>") + "</center></html>");
        }
        headerRows = Math.max(headerRows, columnHeaderRows);
    }

    /**
     * Overridden to handle multiline column headers.
     */
    @Override
    public void setTableHeader(JTableHeader tableHeader) {
        super.setTableHeader(tableHeader);
        if (headerRows > 1 && tableHeader != null) {
            JComponent renderer = (JComponent) tableHeader.getDefaultRenderer().getTableCellRendererComponent(this, "x", false, false, 0, 0);
            Dimension size = renderer.getPreferredSize();
            size.height -= renderer.getInsets().top + renderer.getInsets().bottom;
            size.height *= headerRows;
            size.height += renderer.getInsets().top + renderer.getInsets().bottom;
            tableHeader.setPreferredSize(size);
        }
    }

    /**
     * Get the selected beans.
     */
    public List<Bean> getSelectedItems() {
        return getBeanIndexes(getSelectedRows()).mapToObj(getModel()::getBean).collect(Collectors.toList());
    }

    protected IntStream getBeanIndexes(int[] viewIndexes) {
        return Arrays.stream(viewIndexes).map(this::convertRowIndexToModel);
    }

    private Component getEditorField() {
        Component component = super.getEditorComponent();
        return component instanceof FocusContainer ? ((FocusContainer) component).getFocusField() : component;
    }

    /**
     * Overridden to improve keyboard handling for starting/stopping edit.
     */
    @Override
    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
        putClientProperty("JTable.autoStartsEdit", isAutoStartEdit(e));
        int row = -1;
        if (getCellEditor() != null) {
            row = convertRowIndexToModel(getSelectedRow());
        }
        boolean result = super.processKeyBinding(ks, e, condition, pressed);
        // pass key that started the edit to editor
        if (condition == WHEN_FOCUSED && getEditorField() instanceof JComboBox) {
            JComboBox<?> editorComponent = (JComboBox<?>) getEditorField();
            if (editorComponent.isEditable()) {
                JTextComponent textEditor = (JTextComponent) editorComponent.getEditor().getEditorComponent();
                setEditorText(textEditor, e.getKeyChar());
            }
            else {
                editorComponent.selectWithKeyChar(e.getKeyChar());
            }
        }
        if (getCellEditor() != null) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                int column = getSelectedColumn();
                getCellEditor().stopCellEditing();
                selectViewRow(row);
                setColumnSelectionInterval(column, column);
            }
        }
        else if (row >= 0) selectViewRow(row);
        return result;
    }

    private void selectViewRow(int modelRow) {
        int viewRow = convertRowIndexToView(modelRow);
        setRowSelectionInterval(viewRow, viewRow);
        scrollRectToVisible(getCellRect(viewRow, getSelectedColumn(), true));
    }

    /**
     * Overridden to handle initial keystroke on a {@link JTextComponent} editor.
     */
    @Override
    public boolean editCellAt(int row, int column, EventObject e) {
        boolean startEdit = startEdit(e, row, column) && super.editCellAt(row, column, e);
        if (startEdit && e instanceof KeyEvent && getEditorField() instanceof JTextComponent) {
            if (((KeyEvent) e).getKeyCode() == KeyEvent.VK_DELETE) {
                ((JTextComponent) getEditorField()).setCaretPosition(0);
            }
            else if (((KeyEvent) e).getKeyCode() != KeyEvent.VK_BACK_SPACE && !(getEditorField() instanceof JFormattedTextField)) {
                ((JTextComponent) getEditorField()).selectAll();
            }
        }
        return startEdit;
    }

    private boolean startEdit(EventObject e, int row, int column) {
        if (e instanceof MouseEvent) {
            MouseEvent event = (MouseEvent) e;
            int modelRow = convertRowIndexToModel(row);
            int modelColumn = convertColumnIndexToModel(column);
            return decorators.stream().filter(decorator -> decorator instanceof TableMouseDecorator)
                .allMatch(decorator -> ((TableMouseDecorator) decorator).startEdit(event, this, modelRow, modelColumn));
        }
        return true;
    }

    private boolean isAutoStartEdit(KeyEvent e) {
        return !e.isControlDown() && !e.isAltDown() || isCtrlBackspace(e);
    }

    @SuppressWarnings("deprecation")
    private boolean isCtrlBackspace(KeyEvent e) {
        return e.getKeyCode() == KeyEvent.VK_BACK_SPACE && e.getModifiers() == KeyEvent.CTRL_MASK;
    }

    /**
     * Handle first keystroke when starting edit.  Backspace deletes the last character in the cell.
     * Otherwise, any printable character replaces the entire text of the cell.
     */
    protected void setEditorText(JTextComponent editor, char ch) {
        if (ch == KeyEvent.VK_BACK_SPACE) {
            String text = editor.getText();
            if (text.length() > 0) {
                editor.setText(text.substring(0, text.length() - 1));
            }
        }
        else if (!Character.isISOControl(ch)) {
            editor.setText(Character.toString(ch));
        }
    }

    /**
     * Overridden to set background, foreground and apply {@link TableDecorator}s.
     */
    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        if (renderer instanceof JComponent) {
            ((JComponent) renderer).setToolTipText(null);
            // force DefaultTableCellRenderer to use table background and foreground
            ((JComponent) renderer).setBackground(getRowBackground(row));
            ((JComponent) renderer).setForeground(getForeground());
        }
        JComponent component = (JComponent) super.prepareRenderer(renderer, row, column);
        int modelRow = convertRowIndexToModel(row);
        int modelColumn = convertColumnIndexToModel(column);
        for (TableDecorator decorator : decorators) {
            decorator.prepareRenderer(this, component, modelRow, modelColumn);
        }
        return component;
    }

    /**
     * Used by {@link #prepareRenderer(TableCellRenderer, int, int)} to determine the background color of a row.
     * @return the background color of the row.
     */
    protected Color getRowBackground(int row) {
        if (row % 2 == 0) {
            return evenBackground;
        }
        return oddBackground;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Model getModel() {
        return (Model) super.getModel();
    }

    public List<TableDecorator> getDecorators() {
        return decorators;
    }

    public void setDecorators(List<TableDecorator> decorators) {
        this.decorators.clear();
        this.decorators.addAll(decorators);
    }

    /**
     * Overridden to fix background color of boolean cells.
     */
    @Override
    protected void createDefaultRenderers() {
        super.createDefaultRenderers();
        setDefaultRenderer(Boolean.class, new BooleanRenderer());
    }

    /**
     * Copy of JTable.BooleanRenderer that only sets the background when selected.  This is the only way
     * to make the background of boolean cells match the rest of the row when using alternating backgrounds.
     */
    private static class BooleanRenderer extends JCheckBox implements TableCellRenderer, UIResource {
        private static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

        public BooleanRenderer() {
            super();
            setHorizontalAlignment(JLabel.CENTER);
            setBorderPainted(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
            }
            setSelected(Boolean.TRUE.equals(value));
            setBorder(hasFocus ? UIManager.getBorder("Table.focusCellHighlightBorder") : noFocusBorder);
            return this;
        }
    }
}