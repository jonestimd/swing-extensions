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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.SwingUtilities;

import io.github.jonestimd.swing.dialog.ExceptionDialog;

public interface BackgroundTask<T> {

    /**
     * @return ResourceBundle key for description of the background task.
     */
    String getStatusMessage();

    /**
     * Execute a long running task (called on a non-Swing thread).
     */
    T performTask();

    /**
     * Update the UI with the result of the long running task (called on the Swing event thread).
     */
    void updateUI(T result);

    /**
     * Handle an exception thrown by the long running task (called on the Swing event thread).
     * @return true if the exception has been handled.
     */
    default boolean handleException(Throwable th) {
        return false;
    }

    static <T> BackgroundTask<T> task(Supplier<T> doInBackground, Consumer<T> updateUI) {
        return task(null, doInBackground, updateUI);
    }

    static <T> BackgroundTask<T> task(String statusMessage, Supplier<T> doInBackground, Consumer<T> updateUI) {
        return new BackgroundTask<T>() {
            @Override
            public String getStatusMessage() {
                return statusMessage;
            }

            @Override
            public T performTask() {
                return doInBackground.get();
            }

            @Override
            public void updateUI(T result) {
                updateUI.accept(result);
            }
        };
    }

    /**
     *  Run a task on a background thread. This method should only be called from the Swing Event Dispatch thread.
     *  Requires that {@code owner} or one if its ancestors is a {@link StatusIndicator}.
     *  @param task the task to run
     *  @param owner owner component for displaying an error dialog if the task fails
     */
    static <T> CompletableFuture<T> run(BackgroundTask<T> task, Component owner) {
        StatusIndicator statusIndicator = ComponentTreeUtils.findAncestor(owner, StatusIndicator.class);
        return run(task, statusIndicator, owner);
    }

    /**
     *  Run a task on a background thread. This method should only be called from the Swing Event Dispatch thread.
     *  @param task the task to run
     *  @param statusIndicator UI component to receive status messages (disabled while {@code task} is running)
     *  @param owner owner component for displaying an error dialog if the task fails
     */
    static <T> CompletableFuture<T> run(BackgroundTask<T> task, StatusIndicator statusIndicator, Component owner) {
        statusIndicator.disableUI(task.getStatusMessage());
        return CompletableFuture.supplyAsync(task::performTask)
                .whenCompleteAsync((result, throwable) -> {
                    if (throwable == null) {
                        task.updateUI(result);
                        statusIndicator.enableUI();
                    }
                    else {
                        if (throwable instanceof CompletionException) throwable = throwable.getCause();
                        statusIndicator.enableUI();
                        if (! task.handleException(throwable)) {
                            new ExceptionDialog(ComponentTreeUtils.findAncestor(owner, Window.class), throwable).setVisible(true);
                        }
                    }
                }, SwingUtilities::invokeLater);
    }
}
