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
package io.github.jonestimd.swing.dialog;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.ResourceBundle;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import io.github.jonestimd.swing.ButtonBarFactory;
import io.github.jonestimd.swing.ComponentFactory;
import io.github.jonestimd.swing.ComponentTreeUtils;
import io.github.jonestimd.swing.action.CancelAction;
import io.github.jonestimd.swing.action.MnemonicAction;
import io.github.jonestimd.swing.layout.GridBagBuilder;

public abstract class FormDialog extends MessageDialog {
    protected static final int BUTTON_BAR_BORDER = 10;
    protected final JPanel formPanel = new JPanel();
    private final CancelAction cancelAction = CancelAction.install(this);
    private final Action saveAction;
    private final JButton saveButton;
    private final Box buttonBar;
    private final JTextArea statusArea;
    private final JScrollPane statusScrollPane;

    /**
     * Create a document modal dialog.
     */
    protected FormDialog(Window owner, String title, ResourceBundle bundle) {
        this(owner, title, ModalityType.DOCUMENT_MODAL, bundle);
    }

    protected FormDialog(Window owner, String title, ModalityType modalityType, ResourceBundle bundle) {
        super(owner, title, modalityType);
        this.saveAction = new MnemonicAction(bundle.getString("action.save.mnemonicAndName")) {
            public void actionPerformed(ActionEvent e) {
                onSave();
            }
        };
        this.saveButton = new JButton(saveAction);
        formPanel.setBorder(new EmptyBorder(BUTTON_BAR_BORDER, BUTTON_BAR_BORDER, 0, BUTTON_BAR_BORDER));
        buttonBar = new ButtonBarFactory().alignRight().border(BUTTON_BAR_BORDER).add(saveButton, new JButton(cancelAction)).get();
        statusArea = ComponentFactory.createValidationStatusArea(1, bundle);

        GridBagBuilder builder = new GridBagBuilder(getContentPane(), bundle, "");
        builder.append(getFormPanel());
        builder.append(buttonBar);
        builder.append(statusArea);
        statusScrollPane = ComponentTreeUtils.findAncestor(statusArea, JScrollPane.class);
        statusScrollPane.setVisible(false);
    }

    protected int getStatusHeight() {
        return statusScrollPane.isVisible() ? statusArea.getPreferredSize().height : 0;
    }

    protected JPanel getFormPanel() {
        return formPanel;
    }

    protected void addButton(Action action) {
        buttonBar.add(new JButton(action), buttonBar.getComponentCount()-1);
        buttonBar.add(Box.createHorizontalStrut(ButtonBarFactory.BUTTON_GAP), buttonBar.getComponentCount()-1);
    }

    protected void setSaveEnabled(boolean enabled) {
        saveAction.setEnabled(enabled);
    }

    protected void onSave() {
        dispose();
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            cancelAction.reset();
            getRootPane().setDefaultButton(saveButton);
        }
        super.setVisible(visible);
    }

    public boolean isCancelled() {
        return cancelAction.isCancelled();
    }

    protected void setStatusText(Collection<String> messages) {
        int statusHeight = getStatusHeight();
        statusArea.setRows(messages.size());
        statusArea.setText(String.join("\n", messages));
        statusScrollPane.setVisible(!messages.isEmpty());
        if (isVisible() && statusHeight != getStatusHeight()) {
            int deltaHeight = getStatusHeight() - statusHeight;
            setSize(getWidth(), getHeight() + deltaHeight);
        }
    }
}
