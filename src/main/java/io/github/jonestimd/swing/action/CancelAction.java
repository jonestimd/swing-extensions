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

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Optional;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;
import javax.swing.WindowConstants;

import io.github.jonestimd.swing.ComponentFactory;

public class CancelAction extends AbstractAction {
    private static final String CLOSE = "close";
    private final Window window;
    private Optional<ConfirmClose> confirmClose = Optional.empty();
    private boolean cancelled = false;

    public static CancelAction install(JDialog dialog) {
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        return initialize(dialog, new CancelAction(dialog));
    }

    protected static <T extends Window & RootPaneContainer> CancelAction initialize(T window, CancelAction action) {
        InputMap inputMap = window.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), CLOSE);
        ActionMap actionMap = window.getRootPane().getActionMap();
        actionMap.put(CLOSE, action);
        return action;
    }

    protected CancelAction(Window window) {
        super(ComponentFactory.DEFAULT_BUNDLE.getString("action.cancel.name"));
        this.window = window;
        window.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                actionPerformed(null);
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (confirmClose.map(ConfirmClose::confirmClose).orElse(true)) {
            cancelled = true;
            window.dispose();
        }
    }

    public void setConfirmClose(ConfirmClose confirmClose) {
        this.confirmClose = Optional.of(confirmClose);
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void reset() {
        this.cancelled = false;
    }
}
