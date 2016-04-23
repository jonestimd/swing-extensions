// The MIT License (MIT)
//
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
package io.github.jonestimd.swing.validation;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.function.Predicate;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTable;

import io.github.jonestimd.swing.ComponentTreeUtils;

public abstract class ContainerTracker {
    private static final Predicate<Container> NO_CHILD_TRACKING = container ->
            !(container instanceof JList || container instanceof JTable || container instanceof JComboBox);
    private ContainerListener containerListener = new ContainerListener() {
        @Override
        public void componentAdded(ContainerEvent event) {
            Component component = event.getChild();
            if (component instanceof Container) {
                trackFieldChanges((Container) component);
            }
            else {
                ContainerTracker.this.componentAdded(component);
            }
        }

        @Override
        public void componentRemoved(ContainerEvent event) {
            Component component = event.getChild();
            if (component instanceof Container) {
                untrackFieldChanges((Container) component);
            }
            else {
                ContainerTracker.this.componentRemoved(component);
            }
        }
    };

    public void trackFieldChanges(Container container) {
        ComponentTreeUtils.visitComponentTree(container, this::componentAdded, NO_CHILD_TRACKING);
    }

    public void untrackFieldChanges(Container container) {
        ComponentTreeUtils.visitComponentTree(container, this::componentRemoved, NO_CHILD_TRACKING);
    }

    protected void componentAdded(Component component) {
        if (component instanceof Container) {
            ((Container) component).addContainerListener(containerListener);
        }
    }

    protected void componentRemoved(Component component) {
        if (component instanceof Container) {
            ((Container) component).removeContainerListener(containerListener);
        }
    }
}
