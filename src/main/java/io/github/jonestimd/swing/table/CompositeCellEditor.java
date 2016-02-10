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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellEditor;

import io.github.jonestimd.swing.FocusContainer;
import io.github.jonestimd.swing.table.model.ChangeBufferTableModel;

import static java.util.Collections.*;

/**
 * Base class for Cell editors with multiple input fields.  This class handles the keyboard navigation between the fields.
 * For compatibility with {@link ChangeBufferTableModel}, editing is performed on a copy of the table cell value.
 * @param <T> class of the table cell value
 */
public abstract class CompositeCellEditor<T> extends AbstractCellEditor implements TableCellEditor {
    private static final String NEXT_FIELD = "next field";
    private static final String PREVIOUS_FIELD = "previous field";
    private static final List<String> NEXT_COLUMN_KEYS =
            unmodifiableList(Arrays.asList("selectNextColumn", "selectNextColumnCell"));
    private static final List<String> PREVIOUS_COLUMN_KEYS =
            unmodifiableList(Arrays.asList("selectPreviousColumn", "selectPreviousColumnCell"));
    private final FieldPanel editorComponent = new FieldPanel();
    private final Function<T, T> copyFunction;
    private List<KeyStroke> nextCellKeys;
    private List<KeyStroke> previousCellKeys;
    private int focusIndex = 0;
    private T bean;

    /**
     * @param copyFunction a function used to create a copy of the table cell value for editing
     */
    protected CompositeCellEditor(Function<T, T> copyFunction) {
        this.copyFunction = copyFunction;
        editorComponent.setBorder(new LineBorder(Color.black));
        editorComponent.getActionMap().put(NEXT_FIELD, new NavigateAction(1));
        editorComponent.getActionMap().put(PREVIOUS_FIELD, new NavigateAction(-1));
        editorComponent.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                JComponent component = (JComponent) editorComponent.getComponent(focusIndex);
                component.requestFocus();
            }
        });
    }

    private void updateInputMap() {
        InputMap inputMap = editorComponent.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        if (focusIndex < editorComponent.getComponentCount()-1) {
            nextCellKeys.forEach(ks -> inputMap.put(ks, NEXT_FIELD));
        }
        else {
            nextCellKeys.forEach(inputMap::remove);
        }
        if (focusIndex > 0) {
            previousCellKeys.forEach(ks -> inputMap.put(ks, PREVIOUS_FIELD));
        }
        else {
            previousCellKeys.forEach(inputMap::remove);
        }
    }

    /**
     * Add nested input fields for the  table cell editor.
     */
    protected void addFields(JComponent... fields) {
        for (JComponent field : fields) {
            field.setBorder(BorderFactory.createEmptyBorder());
            editorComponent.add(field);
        }
    }

    /**
     * Creates a copy of the table cell value and initializes the input fields.
     */
    @SuppressWarnings("unchecked")
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        bean = copyFunction.apply((T) value);
        prepareEditor(table, bean, isSelected, row, column);
        focusIndex = getInitialFocus();
        if (nextCellKeys == null) {
            initializeNavigationKeys(table);
        }

        updateInputMap();
        return editorComponent;
    }

    private void initializeNavigationKeys(JTable table) {
        nextCellKeys = new ArrayList<>();
        previousCellKeys = new ArrayList<>();
        InputMap inputMap = table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        for (KeyStroke keyStroke : inputMap.allKeys()) {
            String key = inputMap.get(keyStroke).toString();
            if (NEXT_COLUMN_KEYS.contains(key)) {
                nextCellKeys.add(keyStroke);
            }
            else if (PREVIOUS_COLUMN_KEYS.contains(key)) {
                previousCellKeys.add(keyStroke);
            }
        }
    }

    /**
     * @return the index of the nested input field to receive focus when editing starts.
     */
    protected abstract int getInitialFocus();

    /**
     * Initializes the nested input fields based on the table cell value.
     */
    protected abstract void prepareEditor(JTable table, T value, boolean isSelected, int row, int column);

    @SuppressWarnings("unchecked")
    public T getCellEditorValue() {
        updateCellEditorValue(bean);
        return  bean;
    }

    /**
     * Updates the bean with the field values of the editor.
     * @param bean a copy of the table cell value (see {@link #getTableCellEditorComponent(JTable, Object, boolean, int, int)})
     */
    protected abstract void updateCellEditorValue(T bean);

    private class FieldPanel extends JPanel implements FocusContainer {
        public FieldPanel() {
            super(new GridLayout(1, 0));
        }

        @Override
        public Component getFocusField() {
            return getComponent(focusIndex);
        }
    }

    private class NavigateAction extends AbstractAction {
        private final int increment;

        private NavigateAction(int increment) {
            this.increment = increment;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            focusIndex += increment;
            editorComponent.getComponent(focusIndex).requestFocus();
            updateInputMap();
        }
    }
}