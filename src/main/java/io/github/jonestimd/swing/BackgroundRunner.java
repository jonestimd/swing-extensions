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
package io.github.jonestimd.swing;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import io.github.jonestimd.swing.dialog.ExceptionDialog;

/**
 * Execution order:
 * <ul>
 * <li>AWT thread - {@link StatusIndicator#disableUI(String)}, {@link BackgroundTask#getStatusMessage()}</li>
 * <li>background thread - {@link BackgroundTask#performTask()}</li>
 * <li>one of</li>
 *   <ul>
 *   <li>AWT thread - {@link BackgroundTask#updateUI(Object)}, {@link StatusIndicator#enableUI()}</li>
 *   <li>AWT thread - {@link StatusIndicator#enableUI()}, {@link BackgroundTask#handleException(Throwable)}</li>
 *   </ul>
 * </ul>
 */
public class BackgroundRunner<T> {
    private static final Logger logger = Logger.getLogger(BackgroundRunner.class.getName());
    private static final StatusIndicator DEFAULT_STATUS_INDICATOR = new LoggerStatusIndicator(logger);

    private Runnable taskRunner = new Runnable() {
        public void run() {
            try {
                result = task.performTask();
            }
            catch (Throwable th) {
                throwable = th;
            }
            SwingUtilities.invokeLater(uiRunner);
            logger.fine("TaskRunner complete");
        }
    };
    private Runnable uiRunner = new Runnable() {
        public void run() {
            statusMessage = null;
            if (throwable == null) {
                task.updateUI(result);
                statusIndicator.enableUI();
            }
            else {
                statusIndicator.enableUI();
                if (! task.handleException(throwable)) {
                    logger.log(Level.SEVERE, "Unhandled exception", throwable);
                    new ExceptionDialog(ComponentTreeUtils.findAncestor(owner, Window.class), throwable).setVisible(true);
                }
            }
        }
    };

    private BackgroundTask<T> task;
    private Component owner;
    private volatile String statusMessage;
    private StatusIndicator statusIndicator = DEFAULT_STATUS_INDICATOR;
    private volatile T result;

    private volatile Throwable throwable;

    public BackgroundRunner(BackgroundTask<T> task) {
        this(task, JOptionPane.getRootFrame());
    }

    public BackgroundRunner(BackgroundTask<T> task, Component owner) {
        this.task = task;
        this.owner = owner;
        if (owner instanceof StatusIndicator) {
            statusIndicator = (StatusIndicator) owner;
        }
        else if (owner.getParent() != null) {
            statusIndicator = ComponentTreeUtils.findAncestor(owner, StatusIndicator.class, DEFAULT_STATUS_INDICATOR);
        }
        else {
            owner.addHierarchyListener(new VisibilityHandler());
        }
    }

    public BackgroundRunner(BackgroundTask<T> task, Component owner, StatusIndicator statusIndicator) {
        this.task = task;
        this.owner = owner;
        this.statusIndicator = statusIndicator;
    }

    public void doTask() {
        result = null;
        throwable = null;
        if (SwingUtilities.isEventDispatchThread()) {
            statusMessage = task.getStatusMessage();
            statusIndicator.disableUI(statusMessage);
            new Thread(taskRunner, "BackgroundTask").start();
        }
        else {
            taskRunner.run();
        }
    }

    private class VisibilityHandler implements HierarchyListener {
        public void hierarchyChanged(HierarchyEvent e) {
            Component component = e.getComponent();
            if (component.isShowing()) {
                component.removeHierarchyListener(this);
                statusIndicator = ComponentTreeUtils.findAncestor(component, StatusIndicator.class, DEFAULT_STATUS_INDICATOR);
                if (statusMessage != null) {
                    statusIndicator.disableUI(statusMessage);
                }
            }
       }
    }
}