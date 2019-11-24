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
package io.github.jonestimd.swing.dialog;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import io.github.jonestimd.swing.ButtonBarFactory;
import io.github.jonestimd.swing.ComponentFactory;
import io.github.jonestimd.swing.ComponentResources;
import io.github.jonestimd.swing.action.CancelAction;
import io.github.jonestimd.swing.action.MnemonicAction;
import io.github.jonestimd.swing.validation.FieldChangeTracker.FieldChangeHandler;
import io.github.jonestimd.swing.validation.ValidationTracker;

/**
 * A dialog with save and cancel buttons and a validation status area.  The save action is disabled when there
 * are validation errors.  Other conditions for enabling the save action can be added using {@link #addSaveCondition(BooleanSupplier)}.
 */
public class ValidatedDialog extends MessageDialog {
    protected static final int BUTTON_BAR_BORDER = 10;
    protected static final String SAVE_MNEMONIC_AND_NAME = ComponentResources.lookupString("action.save.mnemonicAndName");
    protected final JPanel formPanel = new JPanel();
    protected final CancelAction cancelAction = CancelAction.install(this);
    protected final Action saveAction;
    private final JButton saveButton;
    protected final JComponent buttonBar;
    private final JTextArea statusArea;
    private final JScrollPane statusScrollPane;
    private final ValidationTracker validationTracker;
    private final List<Consumer<Collection<String>>> validationListeners = new LinkedList<>();
    private boolean resizePending = false;
    private final List<BooleanSupplier> saveConditions = new ArrayList<>();

    /**
     * Create a document modal dialog.
     */
    public ValidatedDialog(Window owner, String title, ResourceBundle bundle) {
        this(owner, title, ModalityType.DOCUMENT_MODAL, bundle);
    }

    public ValidatedDialog(Window owner, String title, ModalityType modalityType, ResourceBundle bundle) {
        super(owner, title, modalityType);
        this.saveAction = new MnemonicAction(SAVE_MNEMONIC_AND_NAME) {
            public void actionPerformed(ActionEvent e) {
                onSave();
            }
        };
        this.saveButton = new JButton(saveAction);
        formPanel.setBorder(new EmptyBorder(BUTTON_BAR_BORDER, BUTTON_BAR_BORDER, 0, BUTTON_BAR_BORDER));
        buttonBar = new ButtonBarFactory().alignRight().border(BUTTON_BAR_BORDER).add(saveButton, new JButton(cancelAction)).get();
        statusArea = ComponentFactory.newValidationStatusArea(1, bundle);
        statusScrollPane = new JScrollPane(statusArea);

        setContentPane(Box.createVerticalBox());
        getContentPane().add(formPanel);
        getContentPane().add(buttonBar);
        getContentPane().add(statusScrollPane);
        statusScrollPane.setVisible(false);
        validationTracker = ValidationTracker.install(this::validationChanged, getFormPanel());
        addSaveCondition(() -> getValidationMessages().isEmpty());
    }

    /**
     * Add a condition for enabling the save action.
     * All save conditions must be true for the action to be enabled.
     */
    public void addSaveCondition(BooleanSupplier condition) {
        saveConditions.add(condition);
    }

    protected int getStatusHeight() {
        return statusScrollPane.isVisible() ? statusArea.getPreferredSize().height : 0;
    }

    protected JPanel getFormPanel() {
        return formPanel;
    }

    protected void addValidationListener(Consumer<Collection<String>> listener) {
        validationListeners.add(listener);
    }

    /**
     * Add a button to the button bar.  The button is added before the Cancel button.
     */
    protected void addButton(Action action) {
        buttonBar.add(new JButton(action), buttonBar.getComponentCount()-1);
    }

    /**
     * Update the enabled state of the save action.
     */
    public void updateSaveEnabled() {
        saveAction.setEnabled(isSaveEnabled());
    }

    /**
     * Get the enabled state of the save action.
     */
    public boolean isSaveEnabled() {
        return saveConditions.stream().allMatch(BooleanSupplier::getAsBoolean);
    }

    /**
     * Dispose the dialog when the save button is clicked.
     */
    protected void onSave() {
        dispose();
    }

    /**
     * Overridden to reset the cancel action.
     */
    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            cancelAction.reset();
            getRootPane().setDefaultButton(saveButton);
        }
        super.setVisible(visible);
    }

    public Collection<String> getValidationMessages() {
        return validationTracker.getValidationMessages();
    }

    /**
     * @return true if the dialog was cancelled
     */
    public boolean isCancelled() {
        return cancelAction.isCancelled();
    }

    /**
     * Implementation of {@link FieldChangeHandler}.
     */
    private void validationChanged(Collection<String> validationMessages) {
        updateSaveEnabled();
        setStatusText(validationMessages);
        for (Consumer<Collection<String>> listener : validationListeners) {
            listener.accept(validationMessages);
        }
    }

    /**
     * Set the messages in the validation status area.
     */
    protected void setStatusText(Collection<String> messages) {
        if (! resizePending) {
            resizePending = true;
            SwingUtilities.invokeLater(new ResizeTask(getStatusHeight()));
        }
        statusArea.setRows(messages.size());
        statusArea.setText(String.join("\n", messages));
        statusScrollPane.setVisible(!messages.isEmpty());
    }

    private class ResizeTask implements Runnable {
        private final int statusHeight;

        private ResizeTask(int statusHeight) {
            this.statusHeight = statusHeight;
        }

        @Override
        public void run() {
            if (isVisible() && statusHeight != getStatusHeight()) {
                int deltaHeight = getStatusHeight() - statusHeight;
                setSize(getWidth(), getHeight() + deltaHeight);
            }
            resizePending = false;
        }
    }
}
