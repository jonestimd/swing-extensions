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

public class TextField<T extends JTextField> {
    private final T textField;

    public TextField(T textField) {
        this.textField = textField;
    }

    public TextField<T> readOnly() {
        textField.setEditable(false);
        return this;
    }

    public TextField<T> rightAligned() {
        textField.setHorizontalAlignment(JTextField.RIGHT);
        return this;
    }

    public T get() {
        return textField;
    }

    public static TextField<JFormattedTextField> formatted(Format format) {
        return new TextField<>(new JFormattedTextField(format));
    }

    public static TextField<JTextField> plain() {
        return new TextField<>(new JTextField());
    }

    public static Validated validated(ResourceBundle bundle, String resourcePrefix) {
        return new Validated(bundle, resourcePrefix);
    }

    public static class Validated {
        private final ResourceBundle bundle;
        private final String resourcePrefix;
        private final List<Validator<String>> validators = new ArrayList<>();

        private Validated(ResourceBundle bundle, String resourcePrefix) {
            this.bundle = bundle;
            this.resourcePrefix = resourcePrefix;
        }

        public Validated required(String message) {
            validators.add(new RequiredValidator(bundle.getString(resourcePrefix + message)));
            return this;
        }

        public Validated numeric(String message) {
            validators.add(new NumberValidator(bundle.getString(resourcePrefix + message)));
            return this;
        }

        public Validated positiveNumber(String message) {
            validators.add(new PositiveNumberValidator(bundle.getString(resourcePrefix + message)));
            return this;
        }

        public ValidatedTextField get() {
            Validator<String> validator = validators.size() == 1 ? validators.get(0) : new CompositeValidator<>(validators);
            return new ValidatedTextField(validator);
        }

        public TextField<ValidatedTextField> configure() {
            return new TextField<>(get());
        }
    }
}
