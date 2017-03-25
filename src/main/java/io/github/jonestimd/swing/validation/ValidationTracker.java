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
package io.github.jonestimd.swing.validation;

import java.awt.Component;
import java.awt.Container;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTable;

import com.google.common.collect.Lists;

/**
 * This class tracks the validation messages for all of the {@link ValidatedComponent}s in a component hierarchy.
 * Whenever the list of validation messages changes, a notification is sent to the {@link ValidationChangeHandler}.
 */
public class ValidationTracker extends ContainerTracker {
    private final Map<JTable, TableHandler> tableHandlers = new HashMap<>();

    public static ValidationTracker install(ValidationChangeHandler handler, Container container) {
        ValidationTracker tracker = new ValidationTracker(handler);
        tracker.trackFieldChanges(container);
        return tracker;
    }

    private Map<ValidatedComponent, String> validationMessages = new HashMap<>();

    private ValidationChangeHandler changeHandler;
    private ValidationHandler validationHandler = new ValidationHandler();

    public ValidationTracker(ValidationChangeHandler handler) {
        this.changeHandler = handler;
    }

    @Override
    public void trackFieldChanges(Container container) {
        super.trackFieldChanges(container);
        changeHandler.validationChanged(getValidationMessages());
    }

    @Override
    public void untrackFieldChanges(Container container) {
        super.untrackFieldChanges(container);
        changeHandler.validationChanged(getValidationMessages());
    }

    @Override
    protected void componentAdded(Component component) {
        if (component instanceof ValidatedComponent) {
            ValidatedComponent validatedComponent = (ValidatedComponent) component;
            validatedComponent.addValidationListener(validationHandler);
            String messages = validatedComponent.getValidationMessages();
            if (messages != null) {
                validationMessages.put(validatedComponent, messages);
            }
        }
        if (component instanceof JTable) {
            TableHandler tableHandler = new TableHandler();
            tableHandlers.put((JTable) component, tableHandler);
            component.addPropertyChangeListener("tableCellEditor", tableHandler);
        }
        else {
            super.componentAdded(component);
        }
    }

    @Override
    protected void componentRemoved(Component component) {
        if (component instanceof ValidatedComponent) {
            ValidatedComponent validatedComponent = (ValidatedComponent) component;
            validatedComponent.removeValidationListener(validationHandler);
            validationMessages.remove(validatedComponent);
        }
        if (component instanceof JTable) {
            component.removePropertyChangeListener("tableCellEditor", tableHandlers.remove(component));
        }
        else {
            super.componentRemoved(component);
        }
    }

    private void updateValidationMessages(ValidatedComponent source, String messages) {
        if (messages == null) {
            validationMessages.remove(source);
        }
        else {
            validationMessages.put(source, messages);
        }
        changeHandler.validationChanged(validationMessages.values());
    }

    public Collection<String> getValidationMessages() {
        return Lists.newArrayList(validationMessages.values());
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

    public interface ValidationChangeHandler {
        /**
         * Notification of a change in a validation.
         * @param validationMessages the updated validation messages
         */
        void validationChanged(Collection<String> validationMessages);
    }
}