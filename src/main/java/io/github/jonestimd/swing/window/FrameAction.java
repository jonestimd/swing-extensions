// The MIT License (MIT)
//
// Copyright (c) 2019 Timothy D. Jones
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
package io.github.jonestimd.swing.window;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import io.github.jonestimd.swing.action.LocalizedAction;

/**
 * A UI action that opens a new window.
 * @param <E> the class providing information about the requested window
 * @see FrameManager
 */
public class FrameAction<E extends WindowInfo> extends LocalizedAction implements ApplicationWindowAction<E> {
    private final E windowInfo;
    private FrameManager<E> frameManager;

    public FrameAction(ResourceBundle bundle, String resourcePrefix, FrameManager<E> frameManager, E windowInfo) {
        super(bundle, resourcePrefix);
        this.frameManager = frameManager;
        this.windowInfo = windowInfo;
    }

    public void actionPerformed(ActionEvent e) {
        frameManager.onWindowEvent(this);
    }

    @Override
    public E getWindowInfo() {
        return windowInfo;
    }
}