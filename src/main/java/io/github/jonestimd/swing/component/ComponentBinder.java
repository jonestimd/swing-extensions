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
package io.github.jonestimd.swing.component;

import java.beans.PropertyChangeListener;
import java.text.Format;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import io.github.jonestimd.beans.ObservableBean;
import io.github.jonestimd.swing.DocumentChangeHandler;
import io.github.jonestimd.swing.DocumentConsumerAdapter;

public class ComponentBinder {
    private static final String LISTENER_PROPERTY = "com.jonestim.swing.component.ComponentBinder.listener";
    private static final String SOURCE_PROPERTY = "com.jonestim.swing.component.ComponentBinder.source";

    /**
     * Add a listener that updates a text field when a bean property changes.
     * @param source the source bean
     * @param propertyName the bean property name
     * @param currentValue the current value of the bean property
     * @param field the field to update (typically not editable)
     * @param format formatter for the value
     * @return the text field
     */
    public static <T extends JTextField> T bind(ObservableBean source, String propertyName, Object currentValue, T field, Format format) {
        PropertyChangeListener listener = (PropertyChangeListener) field.getClientProperty(LISTENER_PROPERTY);
        if (listener == null) {
            listener = event -> field.setText(format.format(event.getNewValue()));
            field.putClientProperty(LISTENER_PROPERTY, listener);
        }
        ObservableBean oldSource = (ObservableBean) field.getClientProperty(SOURCE_PROPERTY);
        if (oldSource != null) {
            oldSource.removePropertyChangeListener(propertyName, listener);
        }
        field.putClientProperty(SOURCE_PROPERTY, source);
        source.addPropertyChangeListener(propertyName, listener);
        field.setText(format.format(currentValue));
        return field;
    }

    /**
     * Add a listener that calls a {@code Consumer} when a text field changes.
     * @param field the text field
     * @param consumer the consumer to call
     * @param <T> the text field class
     * @return the text field
     */
    public static <T extends JTextComponent> T bind(T field, Consumer<String> consumer) {
        field.getDocument().addDocumentListener(new DocumentConsumerAdapter(consumer));
        return field;
    }

    /**
     * Add a listener that calls a {@code Consumer} when a text field changes.
     * @param field the text field
     * @param parser a function that parses the field value
     * @param consumer the consumer to call
     * @param <T> the text field class
     * @param <V> the consumer value class
     * @return the text field
     */
    public static <T extends JTextComponent, V> T bind(T field, Function<String, V> parser, Consumer<V> consumer) {
        field.getDocument().addDocumentListener(new DocumentConsumerAdapter(text -> consumer.accept(parser.apply(text))));
        return field;
    }

    /**
     * Add a listener that calls a {@code Consumer} when a password field changes.
     * @param field the text field
     * @param consumer the consumer to call
     * @param <T> the password field class
     * @return the password field
     */
    public static <T extends JPasswordField> T bind(T field, Consumer<String> consumer) {
        field.getDocument().addDocumentListener(new DocumentChangeHandler(() -> consumer.accept(new String(field.getPassword()))));
        return field;
    }
}
