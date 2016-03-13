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
package io.github.jonestimd.swing.validation;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
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

import io.github.jonestimd.swing.ComponentTreeUtils;

public class FieldChangeTracker { // TODO separate change tracker from validation tracker
    private static final Predicate<Container> NO_CHILD_TRACKING = container ->
            !(container instanceof JList || container instanceof JTable || container instanceof JComboBox);
    private final Logger logger = Logger.getLogger(FieldChangeHandler.class.getName());

    public static void install(FieldChangeHandler handler, Container container) {
        new FieldChangeTracker(handler).trackFieldChanges(container);
    }

    private Set<Object> changedFields = new HashSet<>();
    private Map<ValidatedComponent, String> validationMessages = new HashMap<>();

    private FieldChangeHandler changeHandler;
    private ValidationHandler validationHandler = new ValidationHandler();
    private ContainerListener containerListener = new ContainerAdapter() {
        @Override
        public void componentAdded(ContainerEvent event) {
            Component component = event.getChild();
            if (component instanceof Container) {
                trackFieldChanges((Container) component);
            }
            else {
                addComponentListener(component);
            }
        }
    };

    public FieldChangeTracker(FieldChangeHandler handler) {
        this.changeHandler = handler;
    }

    public void trackFieldChanges(Container container) {
        ComponentTreeUtils.visitComponentTree(container, this::addComponentListener, NO_CHILD_TRACKING);
        changeHandler.fieldsChanged(false, validationMessages.values());
    }

    protected void addComponentListener(Component component) {
        if (component instanceof ValidatedComponent) {
            ValidatedComponent validatedComponent = (ValidatedComponent) component;
            validatedComponent.addValidationListener(validationHandler);
            String messages = validatedComponent.getValidationMessages();
            if (messages != null) {
                validationMessages.put(validatedComponent, messages);
            }
        }
        if (component instanceof JTextComponent) {
            new TextFieldHandler(((JTextComponent) component));
        }
        else if (component instanceof JComboBox) {
            new ComboBoxHandler((JComboBox) component);
        }
        else if (component instanceof JToggleButton) {
            new ButtonHandler((JToggleButton) component);
        }
        else if (component instanceof JTable) {
            component.addPropertyChangeListener("tableCellEditor", new TableHandler());
        }
        else if (component instanceof JList) {
            ((JList) component).addListSelectionListener(new ListHandler((JList<?>) component));
        }
        else if (component instanceof Container) {
            ((Container) component).addContainerListener(containerListener);
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
            changeHandler.fieldsChanged(changedFields.size() > 0, validationMessages.values());
        }
    }

    private void updateValidationMessages(ValidatedComponent source, String messages) {
        if (messages == null) {
            validationMessages.remove(source);
        }
        else {
            validationMessages.put(source, messages);
        }
        changeHandler.fieldsChanged(changedFields.size() > 0, validationMessages.values());
    }

    private class TextFieldHandler implements DocumentListener {
        private String originalValue;

        private TextFieldHandler(JTextComponent textComponent) {
            this.originalValue = getDocumentText(textComponent.getDocument());
            textComponent.getDocument().addDocumentListener(this);
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

    private class TableHandler implements PropertyChangeListener {
        private ValidatedComponent validatedEditor;

        public void propertyChange(PropertyChangeEvent evt) {
            JTable table = (JTable) evt.getSource();
            if (evt.getNewValue() != null && table.getEditorComponent() instanceof ValidatedComponent) {
                validatedEditor = (ValidatedComponent) table.getEditorComponent();
                updateValidationMessages(validatedEditor, validatedEditor.getValidationMessages());
                validatedEditor.addValidationListener(validationHandler);
            }
            else if (validatedEditor != null) {
                validatedEditor.removeValidationListener(validationHandler);
                updateValidationMessages(validatedEditor, null);
                validatedEditor = null;
            }
        }
    }

    private class ValidationHandler implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            updateValidationMessages((ValidatedComponent) evt.getSource(), (String) evt.getNewValue());
        }
    }

    public interface FieldChangeHandler {
        /**
         * Notification of a change in a tracked input field.
         * @param changed true if any input fields have been modified
         * @param validationMessages the updated validation messages
         */
        void fieldsChanged(boolean changed, Collection<String> validationMessages);
    }
}