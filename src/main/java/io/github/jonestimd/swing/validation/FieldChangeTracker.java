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
package io.github.jonestimd.swing.validation;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

/**
 * This class tracks changes to input fields within a component hierarchy and notifies a listener when any of the
 * input fields has been modified.  It can be used to enable a save action when some of the fields on a form
 * have been modified.
 */
public class FieldChangeTracker extends ContainerTracker {
    private final Logger logger = Logger.getLogger(FieldChangeHandler.class.getName());
    private final Map<JTextComponent, TextFieldHandler> textFieldHandlers = new HashMap<>();
    private final Map<JComboBox, ComboBoxHandler> comboBoxHandlers = new HashMap<>();
    private final Map<JToggleButton, ButtonHandler> buttonHandlers = new HashMap<>();
    private final Map<JList, ListHandler> listHandlers = new HashMap<>();

    public static FieldChangeTracker install(FieldChangeHandler handler, Container container) {
        FieldChangeTracker tracker = new FieldChangeTracker(handler);
        tracker.trackFieldChanges(container);
        return tracker;
    }

    private Set<Object> changedFields = new HashSet<>();

    private FieldChangeHandler changeHandler;

    public FieldChangeTracker(FieldChangeHandler handler) {
        this.changeHandler = handler;
    }

    @Override
    protected void componentAdded(Component component) {
        if (component instanceof JTextComponent) {
            new TextFieldHandler(((JTextComponent) component));
        }
        else if (component instanceof JComboBox) {
            new ComboBoxHandler((JComboBox) component);
        }
        else if (component instanceof JToggleButton) {
            new ButtonHandler((JToggleButton) component);
        }
        else if (component instanceof JList) {
            ListHandler listHandler = new ListHandler((JList<?>) component);
            listHandlers.put((JList) component, listHandler);
            ((JList) component).addListSelectionListener(listHandler);
        }
        else if (!(component instanceof JTable)) {
            super.componentAdded(component);
        }
    }

    @Override
    protected void componentRemoved(Component component) {
        if (component instanceof JTextComponent) {
            ((JTextComponent) component).getDocument().removeDocumentListener(textFieldHandlers.remove(component));
        }
        else if (component instanceof JComboBox) {
            ((JComboBox) component).removeItemListener(comboBoxHandlers.remove(component));
        }
        else if (component instanceof JToggleButton) {
            ((JToggleButton) component).removeChangeListener(buttonHandlers.remove(component));
        }
        else if (component instanceof JList) {
            ((JList) component).removeListSelectionListener(listHandlers.get(component));
        }
        else if (!(component instanceof JTable)) {
            super.componentRemoved(component);
        }
    }

    private String getDocumentText(Document document) {
        try {
            return document.getText(0, document.getLength());
        } catch (BadLocationException ex) {
            logger.log(Level.SEVERE, "How did we get here?", ex);
        }
        return "";
    }

    private void compareValues(Object source, Object oldValue, Object newValue) {
        int oldSize = changedFields.size();
        if (changedFields.contains(source) && Objects.equals(oldValue, newValue)) {
            changedFields.remove(source);
        } else if (! Objects.equals(oldValue, newValue)) {
            changedFields.add(source);
        }
        if (changedFields.size() != oldSize) {
            changeHandler.fieldsChanged(isChanged());
        }
    }

    public boolean isChanged() {
        return !changedFields.isEmpty();
    }

    public void resetChanges() {
        changedFields.clear();
    }

    private class TextFieldHandler implements DocumentListener {
        private String originalValue;

        private TextFieldHandler(JTextComponent textComponent) {
            this.originalValue = getDocumentText(textComponent.getDocument());
            textComponent.getDocument().addDocumentListener(this);
            textFieldHandlers.put(textComponent, this);
        }

        private void documentChange(DocumentEvent event) {
            Document document = event.getDocument();
            String newText = getDocumentText(document);
            compareValues(document, originalValue, newText);
        }

        public void insertUpdate(DocumentEvent event) {
            documentChange(event);
        }

        public void removeUpdate(DocumentEvent event) {
            documentChange(event);
        }

        public void changedUpdate(DocumentEvent event) {
            documentChange(event);
        }
    }

    private class ComboBoxHandler implements ItemListener {
        Object originalValue;

        private ComboBoxHandler(JComboBox comboBox) {
            this.originalValue = comboBox.getSelectedItem();
            comboBox.addItemListener(this);
            comboBoxHandlers.put(comboBox, this);
        }

        public void itemStateChanged(ItemEvent event) {
            if (event.getStateChange() == ItemEvent.SELECTED || event.getStateChange() == ItemEvent.DESELECTED) {
                Object source = event.getSource();
                Object newValue = event.getStateChange() == ItemEvent.DESELECTED ? null : event.getItem();
                compareValues(source, originalValue, newValue);
            }
        }
    }

    private class ButtonHandler implements ChangeListener {
        private boolean originalValue;

        private ButtonHandler(JToggleButton button) {
            this.originalValue = button.isSelected();
            button.addChangeListener(this);
            buttonHandlers.put(button, this);
        }

        public void stateChanged(ChangeEvent event) {
            JToggleButton source = (JToggleButton) event.getSource();
            boolean newValue = source.isSelected();
            compareValues(source, originalValue, newValue);
        }
    }

    private class ListHandler implements ListSelectionListener {
        private final JList<?> list;

        public ListHandler(JList<?> list) {
            this.list = list;
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            compareValues(list, null, list.getSelectedValue());
        }
    }

    /**
     * The interface for receiving notifications when the form has changes.
     */
    public interface FieldChangeHandler {
        /**
         * Notification of a change in a tracked input field.
         * @param changed true if any input fields have been modified
         */
        void fieldsChanged(boolean changed);
    }
}