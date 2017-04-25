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

/**
 * This class saves status messages and the enabled state until a component is showing within a {@link StatusIndicator}.
 * Once the component is showing, messages and state changes are passed to its containing {@link StatusIndicator}.
 */
public class DeferredStatusIndicator implements StatusIndicator {
    private StatusIndicator delegate;
    private boolean disabled = false;
    private String statusMessage;

    public DeferredStatusIndicator(Component component) {
        VisibilityHandler.addCallback(component, this::onShowing);
    }

    private void onShowing(Component component) {
        delegate = ComponentTreeUtils.findAncestor(component, StatusIndicator.class);
        if (disabled) delegate.disableUI(statusMessage);
        else if (statusMessage != null) delegate.setStatusMessage(statusMessage);
    }

    @Override
    public void setStatusMessage(String message) {
        this.statusMessage = message;
        if (delegate != null) delegate.setStatusMessage(message);
        else LoggerStatusIndicator.INSTANCE.setStatusMessage(message);
    }

    @Override
    public void disableUI(String message) {
        this.disabled = true;
        this.statusMessage = message;
        if (delegate != null) delegate.disableUI(message);
        else LoggerStatusIndicator.INSTANCE.setStatusMessage(message);
    }

    @Override
    public void enableUI() {
        this.disabled = false;
        if (delegate != null) delegate.enableUI();
    }
}
