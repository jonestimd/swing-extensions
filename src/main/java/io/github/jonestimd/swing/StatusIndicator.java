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

/**
 * This interface is for providing UI feedback while a background task is running.
 */
public interface StatusIndicator {
    /**
     * Change the status message to indicate the progress of a background task.
     */
    void setStatusMessage(String message);

    /**
     * Disable the UI while a background task is running.
     * @param message the message to display to the user while the task is running.
     */
    void disableUI(String message);

    /**
     * Enable the UI after a background task has completed.
     */
    void enableUI();

    /**
     * Get or create a status indicator for a component.  This method should only be called from the Swing event
     * dispatch thread.
     * @return the ancestor {@link StatusIndicator}, a {@link DeferredStatusIndicator} if the component is not showing
     *         or {@link LoggerStatusIndicator#INSTANCE}
     */
    static StatusIndicator forComponent(Component component) {
        if (component == null) return LoggerStatusIndicator.INSTANCE;
        StatusIndicator indicator = ComponentTreeUtils.findAncestor(component, StatusIndicator.class);
        if (indicator == null && !component.isShowing()) return new DeferredStatusIndicator(component);
        return indicator == null ? LoggerStatusIndicator.INSTANCE : indicator;
    }
}
