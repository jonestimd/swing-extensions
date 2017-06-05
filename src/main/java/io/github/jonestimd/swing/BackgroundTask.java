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
package io.github.jonestimd.swing;

import java.awt.Component;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import io.github.jonestimd.swing.dialog.ExceptionDialog;

/**
 * An abstract class for performing long running tasks on a background thread.
 * @param <T> the type of the result of the task
 */
public abstract class BackgroundTask<T> {
    /**
     * @return description of the background task to be displayed to the user.
     */
    public abstract String getStatusMessage();

    /**
     * Execute a long running task (called on a non-Swing thread).
     */
    public abstract T performTask();

    /**
     * Update the UI with the result of the long running task (called on the Swing event thread).
     */
    public abstract void updateUI(T result);

    /**
     * Handle an exception thrown by the long running task (called on the Swing event thread).
     * @return true if the exception has been handled.
     */
    public abstract boolean handleException(Throwable th);


    /**
     * Create a task from callbacks.  An error dialog will be displayed if there is an exception.
     * @param doInBackground the action to perform on the background thread
     * @param updateUI the action to perform on the Swing Event Dispatch Thread
     * @param <T> the type of the task's result
     * @return the new task
     */
    public static <T> BackgroundTask<T> task(Supplier<T> doInBackground, Consumer<T> updateUI) {
        return task(null, doInBackground, updateUI, null);
    }

    /**
     * Create a task from callbacks.
     * @param doInBackground the action to perform on the background thread
     * @param updateUI the action to perform on the Swing Event Dispatch Thread
     * @param onException exception handler (returns true if it handled the exception or false to display an error dialog)
     * @param <T> the type of the task's result
     * @return the new task
     */
    public static <T> BackgroundTask<T> task(Supplier<T> doInBackground, Consumer<T> updateUI, Function<Throwable, Boolean> onException) {
        return task(null, doInBackground, updateUI, onException);
    }

    /**
     * Create a task from callbacks.  An error dialog will be displayed if there is an exception.
     * @param statusMessage the message to display while the task is running
     * @param doInBackground the action to perform on the background thread
     * @param updateUI the action to perform on the Swing Event Dispatch Thread
     * @param <T> the type of the task's result
     * @return the new task
     */
    public static <T> BackgroundTask<T> task(String statusMessage, Supplier<T> doInBackground, Consumer<T> updateUI) {
        return task(statusMessage, doInBackground, updateUI, null);
    }

    /**
     * Create a task from callbacks.
     * @param statusMessage the message to display while the task is running
     * @param doInBackground the action to perform on the background thread
     * @param updateUI the action to perform on the Swing Event Dispatch Thread
     * @param onException exception handler (returns true if it handled the exception or false to display an error dialog)
     * @param <T> the type of the task's result
     * @return the new task
     */
    public static <T> BackgroundTask<T> task(String statusMessage, Supplier<T> doInBackground, Consumer<T> updateUI, Function<Throwable, Boolean> onException) {
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

            @Override
            public boolean handleException(Throwable th) {
                return onException != null && onException.apply(th);
            }
        };
    }

    /**
     * Run this task on a background thread.  Status messages will be sent to the log.  An unowned dialog will be used
     * to display any unhandled exception.
     */
    public CompletableFuture<T> run() {
        return run(LoggerStatusIndicator.INSTANCE, null);
    }

    /**
     * Run this task on a background thread. This method should only be called from the Swing Event Dispatch thread.
     * The UI will be disabled and the status message will be displayed if {@code owner} or one if its ancestors is
     * a {@link StatusIndicator}.
     * @param owner owner component for displaying an error dialog if the task fails
     */
    public CompletableFuture<T> run(Component owner) {
        return run(StatusIndicator.forComponent(owner), owner);
    }

    /**
     * Run this task on a background thread. This method should only be called from the Swing Event Dispatch thread.
     * @param statusIndicator UI component to receive status messages (disabled while the task is running)
     * @param owner owner component for displaying an error dialog if the task fails
     */
    public CompletableFuture<T> run(StatusIndicator statusIndicator, Component owner) {
        statusIndicator.disableUI(getStatusMessage());
        return CompletableFuture.supplyAsync(this::performTask)
                .whenCompleteAsync((result, throwable) -> {
                    if (throwable == null) {
                        try {
                            updateUI(result);
                        } catch (Throwable ex) {
                            Logger.getLogger(BackgroundTask.class.getName()).log(Level.SEVERE, "Error updating UI", ex);
                            ExceptionDialog.show(owner, ex);
                        }
                        statusIndicator.enableUI();
                    }
                    else {
                        if (throwable instanceof CompletionException) throwable = throwable.getCause();
                        statusIndicator.enableUI();
                        if (! handleException(throwable)) {
                            Logger.getLogger(BackgroundTask.class.getName()).log(Level.SEVERE, "Error loading data", throwable);
                            ExceptionDialog.show(owner, throwable);
                        }
                    }
                }, SwingUtilities::invokeLater);
    }
}