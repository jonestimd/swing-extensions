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
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.function.Consumer;

/**
 * This class can be used to invoke a callback when a component becomes visible on the screen.
 * @see Component#isShowing()
 */
public class VisibilityHandler implements HierarchyListener {
    private final Consumer<Component> onShowing;

    public static void addCallback(Component component, Consumer<Component> onShowing) {
        if (!component.isShowing()) component.addHierarchyListener(new VisibilityHandler(onShowing));
    }

    public VisibilityHandler(Consumer<Component> onShowing) {
        this.onShowing = onShowing;
    }

    @Override
    public void hierarchyChanged(HierarchyEvent event) {
        Component component = event.getComponent();
        if (component.isShowing()) {
            component.removeHierarchyListener(this);
            onShowing.accept(component);
        }
    }
}
