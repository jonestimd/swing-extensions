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
import java.awt.Container;

import javax.swing.LayoutFocusTraversalPolicy;
import javax.swing.text.JTextComponent;

public class EditableComponentFocusTraversalPolicy extends LayoutFocusTraversalPolicy {
    @Override
    public Component getComponentAfter(Container aContainer, Component aComponent) {
        Component component = super.getComponentAfter(aContainer, aComponent);
        while (isReadOnly(component)) {
            component = super.getComponentAfter(aContainer, component);
        }
        return component;
    }

    @Override
    public Component getComponentBefore(Container aContainer, Component aComponent) {
        Component component = super.getComponentBefore(aContainer, aComponent);
        while (isReadOnly(component)) {
            component = super.getComponentBefore(aContainer, component);
        }
        return component;
    }

    protected boolean isReadOnly(Component component) {
        return component instanceof JTextComponent && ! ((JTextComponent) component).isEditable();
    }
}
