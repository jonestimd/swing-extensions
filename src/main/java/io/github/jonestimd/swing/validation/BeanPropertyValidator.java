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

import java.util.List;

import io.github.jonestimd.swing.table.model.ValidatedBeanListTableModel;

/**
 * Validates a property of a bean from a list of beans of the same type.  The list can be used to validate requirements
 * such as uniqueness.
 * @param <T> the class of the bean
 * @param <V> the class of the bean property
 * @see ValidatedBeanListTableModel
 */
public interface BeanPropertyValidator<T, V> {
    /**
     * Create an validator that always returns null.
     */
    static <T, V> BeanPropertyValidator<T, V> empty() {
        return (index, value, beans) -> null;
    }

    /**
     * Create a validator that returns the result of a {@link Validator}.
     * @param validator the delegate validator
     * @param <T> the class of the bean
     * @param <V> the class of the bean property
     */
    static <T, V> BeanPropertyValidator<T, V> from(Validator<V> validator) {
        return (index, value, beans) -> validator.validate(value);
    }

    /**
     * Validate a property of a bean from a list of beans.
     * @param selectedIndex the index of the bean within the list of beans
     * @param propertyValue the value of the property to be validated
     * @param beans the list of beans to use for validation
     * @return the validation error message or null if the bean property value is valid
     */
    String validate(int selectedIndex, V propertyValue, List<? extends T> beans);
}