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
package io.github.jonestimd.swing.window;

import java.util.LinkedList;
import java.util.List;

/**
 * Publishes {@link ApplicationWindowEvent}s to registered {@link WindowEventListener}s.
 * <p>
 * <strong>Note:</strong>  This class is not thread-safe.  It is intended for use only on the AWT event thread.
 * @see FrameManager
 * @see FrameAction
 * @see WindowEventListener
 */
public class WindowEventPublisher<E extends WindowInfo> {
    private List<WindowEventListener<E>> listeners = new LinkedList<>();

    public void register(WindowEventListener<E> listener) {
        listeners.add(listener);
    }

    public void unregister(WindowEventListener<E> listener) {
        listeners.remove(listener);
    }

    public <T extends ApplicationWindowEvent<E>> void publishEvent(T event) {
        for (WindowEventListener<E> windowEventListener : listeners) {
            windowEventListener.onWindowEvent(event);
        }
    }
}