// The MIT License (MIT)
//
// Copyright (c) 2017 Timothy D. Jones
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

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.RootPaneContainer;
import javax.swing.WindowConstants;

import io.github.jonestimd.swing.ComponentResources;

/**
 * A UI action that closes a window.  Can be used with {@link ConfirmClose} to display a confirmation dialog before
 * closing the window.
 */
public class CancelAction extends AbstractAction {
    private static final String CLOSE = "close";
    private final Window window;
    private ConfirmClose confirmClose = null;
    private boolean cancelled = false;

    public static CancelAction install(JDialog dialog) {
        return install(ComponentResources.BUNDLE, dialog);
    }

    public static CancelAction install(ResourceBundle bundle, JDialog dialog) {
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        return initialize(dialog, new CancelAction(bundle, dialog));
    }

    protected static <T extends Window & RootPaneContainer> CancelAction initialize(T window, CancelAction action) {
        ActionUtils.install(window.getRootPane(), action, CLOSE, "ESCAPE");
        return action;
    }

    protected CancelAction(ResourceBundle bundle, Window window) {
        super(bundle.getString("action.cancel.name"));
        this.window = window;
        window.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                actionPerformed(null);
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (confirmClose == null || confirmClose.confirmClose()) {
            cancelled = true;
            window.dispose();
        }
    }

    public void setConfirmClose(ConfirmClose confirmClose) {
        this.confirmClose = confirmClose;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void reset() {
        this.cancelled = false;
    }
}
