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

import java.text.Format;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JFormattedTextField;
import javax.swing.JTextField;

import io.github.jonestimd.swing.validation.CompositeValidator;
import io.github.jonestimd.swing.validation.NumberValidator;
import io.github.jonestimd.swing.validation.PositiveNumberValidator;
import io.github.jonestimd.swing.validation.RequiredValidator;
import io.github.jonestimd.swing.validation.ValidatedTextField;
import io.github.jonestimd.swing.validation.Validator;

/**
 * Factory for initializing a {@link JTextField}.
 * @param <T> the class of the text field
 */
public class TextField<T extends JTextField> {
    private final T textField;

    public TextField(T textField) {
        this.textField = textField;
    }

    /**
     * Set the text field to read only.
     * @return this {@code TextField} for further configuration
     */
    public TextField<T> readOnly() {
        textField.setEditable(false);
        return this;
    }

    /**
     * Set the text field to be right aligned.
     * @return this {@code TextField} for further configuration
     */
    public TextField<T> rightAligned() {
        textField.setHorizontalAlignment(JTextField.RIGHT);
        return this;
    }

    /**
     * @return the configured text field
     */
    public T get() {
        return textField;
    }

    /**
     * Create a {@link JFormattedTextField} using the specified format.
     * @return a {@code TextField} for further configuration
     */
    public static TextField<JFormattedTextField> formatted(Format format) {
        return new TextField<>(new JFormattedTextField(format));
    }

    /**
     * Create a {@link JTextField} with the default configuration.
     * @return a {@code TextField} for further configuration
     */
    public static TextField<JTextField> plain() {
        return new TextField<>(new JTextField());
    }

    /**
     * Create a factory for initializing a validated text field.
     * @param bundle the resource bundle for validation messages
     * @param resourcePrefix the resource key prefix for validation messages
     * @return an instance of {@link Validated} for further configuration.
     */
    public static Validated validated(ResourceBundle bundle, String resourcePrefix) {
        return new Validated(bundle, resourcePrefix);
    }

    /**
     * Factory class for creating a {@link ValidatedTextField}.
     */
    public static class Validated {
        private final ResourceBundle bundle;
        private final String resourcePrefix;
        private final List<Validator<String>> validators = new ArrayList<>();

        private Validated(ResourceBundle bundle, String resourcePrefix) {
            this.bundle = bundle;
            this.resourcePrefix = resourcePrefix;
        }

        /**
         * Add a {@link RequiredValidator}.
         * @param message the resource key suffix for the validation error message
         * @return this {@code Validated} factory for further configuration
         */
        public Validated required(String message) {
            validators.add(new RequiredValidator(bundle.getString(resourcePrefix + message)));
            return this;
        }

        /**
         * Add a {@link NumberValidator}.
         * @param message the resource key suffix for the validation error message
         * @return this {@code Validated} factory for further configuration
         */
        public Validated numeric(String message) {
            validators.add(new NumberValidator(bundle.getString(resourcePrefix + message)));
            return this;
        }

        /**
         * Add a {@link PositiveNumberValidator}.
         * @param message the resource key suffix for the validation error message
         * @return this {@code Validated} factory for further configuration
         */
        public Validated positiveNumber(String message) {
            validators.add(new PositiveNumberValidator(bundle.getString(resourcePrefix + message)));
            return this;
        }

        /**
         * Add the specified validator to the text field.
         * @return this {@code Validated} factory for further configuration
         */
        public Validated add(Validator<String> validator) {
            validators.add(validator);
            return this;
        }

        /**
         * Create and configure the {@link ValidatedTextField}.
         */
        public ValidatedTextField get() {
            Validator<String> validator = validators.size() == 1 ? validators.get(0) : new CompositeValidator<>(validators);
            return new ValidatedTextField(validator);
        }

        /**
         * Create and initialize the {@link ValidatedTextField}.
         * @return an instance of {@link TextField} for further configuration
         */
        public TextField<ValidatedTextField> configure() {
            return new TextField<>(get());
        }
    }
}
