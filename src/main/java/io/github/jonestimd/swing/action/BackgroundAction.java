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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import io.github.jonestimd.swing.BackgroundRunner;
import io.github.jonestimd.swing.BackgroundTask;

public abstract class BackgroundAction<T> extends MnemonicAction implements BackgroundTask<T> {
    private final Component owner;
    private final String statusMessage;

    protected BackgroundAction(Component owner, ResourceBundle bundle, String resourcePrefix) {
        super(bundle, resourcePrefix);
        this.owner = owner;
        statusMessage = bundle.containsKey(resourcePrefix + ".status.initialize") ?
                bundle.getString(resourcePrefix + ".status.initialize") : null;
    }

    public final void actionPerformed(ActionEvent event) {
        if (confirmAction(event)) {
            new BackgroundRunner<T>(this, owner).doTask();
        }
    }

    protected abstract boolean confirmAction(ActionEvent event);

    public String getStatusMessage() {
        return statusMessage;
    }
}