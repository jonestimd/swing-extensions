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
package io.github.jonestimd.swing.window;

import java.util.EventObject;

/**
 * An event fired by a {@link FrameAction} to indicate that the user has requested a new window.
 * @param <E> the class providing information about the requested window
 * @see FrameManager
 */
public class ApplicationWindowEvent<E extends WindowInfo> extends EventObject {
    private final E windowInfo;

    public ApplicationWindowEvent(Object source, E windowInfo) {
        super(source);
        this.windowInfo = windowInfo;
    }

    public E getWindowInfo() {
        return windowInfo;
    }

    /**
     * Used to find an existing non-singleton window that matches this event.  If a match is found then that window
     * is raised to the front instead of creating a new window.
     * @param frame an existing non-singleton window
     * @return the default implementation always returns false
     */
    public boolean matches(StatusFrame frame) {
        return false;
    }
}