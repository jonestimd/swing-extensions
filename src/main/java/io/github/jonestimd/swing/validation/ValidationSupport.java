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
package io.github.jonestimd.swing.validation;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.JComponent;

/**
 * A helper class for sending notifications to validation listeners.
 * @param <T> the class of the value being validated
 */
public class ValidationSupport<T> {
    private final PropertyChangeSupport changeSupport;
    private Validator<T> validator;
    private String messages;

    public ValidationSupport(JComponent source) {
        this(source, Validator.empty());
    }

    public ValidationSupport(JComponent source, Validator<T> validator) {
        this.changeSupport = new PropertyChangeSupport(source);
        this.validator = validator;
    }

    public String setValidator(Validator<T> validator, T value) {
        this.validator = validator;
        return validateValue(value);
    }

    public String validateValue(T value) {
        String oldValue = messages;
        messages = validator.validate(value);
        changeSupport.firePropertyChange(ValidatedComponent.VALIDATION_MESSAGES, oldValue, messages);
        return messages;
    }

    public String getMessages() {
        return messages;
    }

    public void addValidationListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(ValidatedComponent.VALIDATION_MESSAGES, listener);
    }

    public void removeValidationListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(ValidatedComponent.VALIDATION_MESSAGES, listener);
    }
}
