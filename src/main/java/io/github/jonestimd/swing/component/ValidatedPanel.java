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
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.JTextArea;

import io.github.jonestimd.swing.ChangeBuffer;
import io.github.jonestimd.swing.ComponentFactory;
import io.github.jonestimd.swing.SwingResource;
import io.github.jonestimd.swing.dialog.Dialogs;
import io.github.jonestimd.swing.validation.ValidatedComponent;
import io.github.jonestimd.swing.validation.ValidationTracker;
import io.github.jonestimd.swing.validation.ValidationTracker.ValidationChangeHandler;
import io.github.jonestimd.swing.window.ConfirmCloseAdapter;

/**
 * A component that displays a {@code form} and validation messages for {@link ValidatedComponent}s contained in
 * the {@code form}.  This component uses a {@link BorderLayout} with the {@code form} in the center position
 * and the validation messages in the south position.  {@link ValidatedComponent}s can be added to the {@code form}
 * at any time and their validation messages will automatically be displayed.
 */
public abstract class ValidatedPanel extends MenuActionPanel {
    private JTextArea statusArea;

    /**
     * Construct a new validated panel.
     * @param bundle provides the background color for the message area
     * (see {@link SwingResource#VALIDATION_MESSAGE_BACKGROUND})
     * @param statusRows the number visible rows for the message area
     * @param form the container for the {@link ValidatedComponent}s
     */
    public ValidatedPanel(ResourceBundle bundle, int statusRows, JComponent form) {
        statusArea = ComponentFactory.newValidationStatusArea(statusRows, bundle);
        statusArea.setVisible(false);
        setLayout(new BorderLayout());
        add(statusArea, BorderLayout.SOUTH);
        add(form, BorderLayout.CENTER);
        ValidationTracker.install(new ValidationHandler(), form);
    }

    protected abstract ChangeBuffer getChangeBuffer();

    @Override
    public void addNotify() {
        super.addNotify();
        ConfirmCloseAdapter.install((Window) getTopLevelAncestor(), this::confirmClose);
    }

    protected boolean confirmClose(WindowEvent event) {
        if (getChangeBuffer().isChanged()) {
            if (Dialogs.confirmDiscardChanges(this)) {
                getChangeBuffer().revert();
                return true;
            }
            return false;
        }
        return true;
    }

    private class ValidationHandler implements ValidationChangeHandler {
        @Override
        public void validationChanged(Collection<String> validationMessages) {
            statusArea.setVisible(! validationMessages.isEmpty());
            statusArea.setText(String.join("\n", validationMessages));
        }
    }
}