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
package io.github.jonestimd.swing.action;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import io.github.jonestimd.swing.BackgroundRunner;
import io.github.jonestimd.swing.BackgroundTask;

public abstract class DialogAction extends MnemonicAction {
    private ResourceBundle bundle;
    private String initializeMessageKey;
    private String updateMessageKey;

    protected DialogAction(ResourceBundle bundle, String resourcePrefix) {
        super(bundle, resourcePrefix);
        this.bundle = bundle;
        this.initializeMessageKey = resourcePrefix + ".status.initialize";
        this.updateMessageKey = resourcePrefix + ".status.save";
    }

    public final void actionPerformed(ActionEvent event) {
        JComponent source = (JComponent) event.getSource();
        new BackgroundRunner<>(new InitializeTask(source), source).doTask();
    }

    public boolean handleLoadException(Throwable th) {
        return false;
    }

    public boolean handleSaveException(Throwable th) {
        return false;
    }

    /**
     * Load data to populate the dialog (called from a background thread).
     */
    protected abstract void loadDialogData();

    /**
     * Called from the Swing event thread.
     * @param owner the component that invoked the action
     * @return true to save changes (i.e. call {@link #saveDialogData} and {@link #setSaveResultOnUI})
     */
    protected abstract boolean displayDialog(JComponent owner);
    /**
     * Save changes (called from a background thread).
     */
    protected abstract void saveDialogData();
    /**
     * Update the UI (called from the Swing event thread).
     */
    protected abstract void setSaveResultOnUI();

    private class InitializeTask implements BackgroundTask<Void> { // TODO clean up handler API
        private final JComponent owner;

        public InitializeTask(JComponent owner) {
            this.owner = owner;
        }

        public String getStatusMessage() {
            return bundle.getString(initializeMessageKey);
        }

        public Void performTask() {
            loadDialogData();
            return null;
        }

        public void updateUI(Void notUsed) {
            SwingUtilities.invokeLater(new DialogTask(owner));
        }

        public boolean handleException(Throwable th) {
            return handleLoadException(th);
        }
    }

    private class DialogTask implements Runnable {
        private final JComponent owner;

        public DialogTask(JComponent owner) {
            this.owner = owner;
        }

        @Override
        public void run() {
            if (displayDialog(owner)) {
                new BackgroundRunner<>(new UpdateTask(), owner).doTask();
            }
        }
    }

    private class UpdateTask implements BackgroundTask<Void> {
        public String getStatusMessage() {
            return bundle.getString(updateMessageKey);
        }

        public Void performTask() {
            saveDialogData();
            return null;
        }

        public void updateUI(Void notUsed) {
            setSaveResultOnUI();
        }

        public boolean handleException(Throwable th) {
            return handleSaveException(th);
        }
    }
}