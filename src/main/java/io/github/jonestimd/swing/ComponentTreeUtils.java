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
import java.util.Optional;
import java.util.function.Predicate;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import io.github.jonestimd.util.JavaPredicates;

public class ComponentTreeUtils {
    public static void visitComponentTree(Container rootComponent, ComponentVisitor<Component> visitor) {
        visitComponentTree(rootComponent, visitor, JavaPredicates.alwaysTrue());
    }

    public static void visitComponentTree(Container rootComponent, ComponentVisitor<Component> visitor, Predicate<Container> descend) {
        visitComponentTree(rootComponent, Component.class, visitor, descend);
    }

    public static <C> void visitComponentTree(Container rootComponent, Class<C> type, ComponentVisitor<? super C> visitor) {
        visitComponentTree(rootComponent, type, visitor, JavaPredicates.alwaysTrue());
    }

    public static <C> void visitComponentTree(Container rootComponent, Class<C> type, ComponentVisitor<? super C> visitor, Predicate<Container> descend) {
        if (type.isInstance(rootComponent)) {
            visitor.visit(type.cast(rootComponent));
        }
        if (descend.test(rootComponent)) {
            Component[] children = getComponents(rootComponent);
            for (Component child : children) {
                if (child instanceof Container) {
                    visitComponentTree((Container) child, type, visitor, descend);
                }
                else if (type.isInstance(child)) {
                    visitor.visit(type.cast(child));
                }
            }
        }
    }

    private static Component[] getComponents(Container container) {
        return container instanceof JMenu ? ((JMenu) container).getMenuComponents() : container.getComponents();
    }

    public static <C extends Component> C findComponent(Container container, Class<C> type) {
        return findComponent(container, type, JavaPredicates.<C>alwaysTrue());
    }

    public static <C extends Component> C findComponent(Container container, Class<C> type, Predicate<C> predicate) {
        Component[] children = getComponents(container);
        for (Component aChildren : children) {
            if (isMatch(aChildren, type, predicate)) {
                return type.cast(aChildren);
            }
            if (aChildren instanceof Container) {
                C component = findComponent((Container) aChildren, type, predicate);
                if (component != null) {
                    return component;
                }
            }
        }
        return null;
    }

    private static <T> boolean isMatch(Component component, Class<T> type, Predicate<T> predicate) {
        return type.isInstance(component) && predicate.test(type.cast(component));
    }

    public static <T> T findAncestor(Component component, Class<T> type) {
        return findAncestor(component, type, null);
    }

    public static <T> T findAncestor(Component component, Class<T> type, T defaultValue) {
        return findAncestor(component, type::isInstance).map(type::cast).orElse(defaultValue);
    }

    public static Optional<Component> findAncestor(Component component, Predicate<Component> condition) {
        while (component != null && ! condition.test(component)) {
            component = getParent(component);
        }
        return Optional.ofNullable(component);
    }

    public static boolean isAncestor(Component component, Component maybeAncestor) {
        if (maybeAncestor != null) {
            while (component != null && component != maybeAncestor) {
                component = getParent(component);
            }
        }
        return maybeAncestor != null && component == maybeAncestor;
    }

    public static Component getParent(Component component) {
        Container parent = component.getParent();
        if (parent == null && component instanceof JPopupMenu) {
            return  ((JPopupMenu) component).getInvoker();
        }
        return parent;
    }
}