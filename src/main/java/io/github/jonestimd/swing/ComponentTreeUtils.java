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
import java.awt.Container;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import io.github.jonestimd.util.JavaPredicates;

/**
 * Utility methods for traversing a Swing component hierarchy.
 */
public class ComponentTreeUtils {
    /**
     * Traverse a container and all its descendants, passing each component to a consumer.
     * @param rootComponent the container
     * @param visitor the consumer to receive each component
     */
    public static void visitComponentTree(Container rootComponent, Consumer<Component> visitor) {
        visitComponentTree(rootComponent, visitor, JavaPredicates.alwaysTrue());
    }

    /**
     * Traverse a container and some of its descendants, passing each component to a consumer.
     * @param rootComponent the container
     * @param visitor the consumer to receive each component
     * @param descend a predicate that controls which containers to traverse
     */
    public static void visitComponentTree(Container rootComponent, Consumer<Component> visitor, Predicate<Container> descend) {
        visitComponentTree(rootComponent, Component.class, visitor, descend);
    }

    /**
     * Traverse a container and all its descendants, passing components matching a type to a consumer.
     * @param rootComponent the container
     * @param type the type of components to pass to {@code visitor}
     * @param visitor the consumer to receive matching components
     */
    public static <C> void visitComponentTree(Container rootComponent, Class<C> type, Consumer<? super C> visitor) {
        visitComponentTree(rootComponent, type, visitor, JavaPredicates.alwaysTrue());
    }

    /**
     * Traverse a container and some of its descendants, passing components matching a type to a consumer.
     * @param rootComponent the container
     * @param type the type of components to pass to {@code visitor}
     * @param visitor the consumer to receive matching components
     * @param descend a predicate that controls which containers to traverse
     */
    public static <C> void visitComponentTree(Container rootComponent, Class<C> type, Consumer<? super C> visitor, Predicate<Container> descend) {
        if (type.isInstance(rootComponent)) {
            visitor.accept(type.cast(rootComponent));
        }
        if (descend.test(rootComponent)) {
            Component[] children = getComponents(rootComponent);
            for (Component child : children) {
                if (child instanceof Container) {
                    visitComponentTree((Container) child, type, visitor, descend);
                }
                else if (type.isInstance(child)) {
                    visitor.accept(type.cast(child));
                }
            }
        }
    }

    private static Component[] getComponents(Container container) {
        return container instanceof JMenu ? ((JMenu) container).getMenuComponents() : container.getComponents();
    }

    /**
     * Find a component in a container.
     * @param container the container to search
     * @param type the class of the component to find
     * @return the first descendant component of the specified type
     */
    public static <C extends Component> C findComponent(Container container, Class<C> type) {
        return findComponent(container, type, JavaPredicates.alwaysTrue());
    }

    /**
     * Find a component in a container.
     * @param container the container to search
     * @param type the class of the component
     * @param predicate a condition to be met by the component
     * @return the first descendant component of the specified type for which {@code predicate} is true
     */
    public static <C extends Component> C findComponent(Container container, Class<C> type, Predicate<C> predicate) {
        Component[] children = getComponents(container);
        for (Component child : children) {
            if (isMatch(child, type, predicate)) {
                return type.cast(child);
            }
            if (child instanceof Container) {
                C component = findComponent((Container) child, type, predicate);
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

    /**
     * Find an ancestor of a component.
     * @param component the starting component
     * @param type the required type of the ancestor
     * @return the first ancestor of the specified type
     */
    public static <T> T findAncestor(Component component, Class<T> type) {
        return findAncestor(component, type, null);
    }

    /**
     * Find an ancestor of a component.
     * @param component the starting component
     * @param type the required type of the ancestor
     * @param defaultValue the value to use of no ancestor is found
     * @return the first ancestor of the specified type or {@code defaultValue}
     */
    public static <T> T findAncestor(Component component, Class<T> type, T defaultValue) {
        return findAncestor(component, type::isInstance).map(type::cast).orElse(defaultValue);
    }

    /**
     * Find an ancestor of a component that matches a condition.
     * @param component the starting component
     * @param condition the condition to match
     * @return the first ancestor for which {@code condition} is true
     */
    public static Optional<Component> findAncestor(Component component, Predicate<Component> condition) {
        while (component != null && ! condition.test(component)) {
            component = getParent(component);
        }
        return Optional.ofNullable(component);
    }

    /**
     * Check if one component is the ancestor of another component.
     * @param component the descendant component
     * @param maybeAncestor the potential ancestor component
     * @return true if {@code maybeAncestor} is an ancestor of {@code component}
     */
    public static boolean isAncestor(Component component, Component maybeAncestor) {
        if (maybeAncestor != null) {
            while (component != null && component != maybeAncestor) {
                component = getParent(component);
            }
        }
        return maybeAncestor != null && component == maybeAncestor;
    }

    /**
     * Return the parent of a component. If the component is a popup menu and its parent is null then return its invoker.
     */
    public static Component getParent(Component component) {
        Container parent = component.getParent();
        if (parent == null && component instanceof JPopupMenu) {
            return  ((JPopupMenu) component).getInvoker();
        }
        return parent;
    }
}