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

import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Strings;

public interface Validator<T> {
    static <T> Validator<T> empty() {
        return bean -> null;
    }

    String validate(T value);

    /**
     * Apply another validation if this validation passes.
     * @param other the other validator
     * @return a new {@code Validator} that returns the error from {@code this} or {@code other}
     */
    default Validator<T> then(Validator<T> other) {
        return value -> {
            String message = validate(value);
            return message == null ? other.validate(value) : message;
        };
    }

    /**
     * Apply another validation after this validation.  Uses {@code System.lineSeparator()} as the message separator.
     * @param other the other validator
     * @return a new {@code Validator} that returns the combined messages of {@code this} and {@code other}
     */
    default Validator<T> add(Validator<T> other) {
        return add(other, System.lineSeparator());
    }

    /**
     * Apply another validation after this validation.
     * @param other the other validator
     * @param separator the separator to use when joining the error messages
     * @return a new {@code Validator} that returns the combined messages of {@code this} and {@code other}
     */
    default Validator<T> add(Validator<T> other, String separator) {
        return value -> Strings.emptyToNull(Stream.of(validate(value), other.validate(value))
                .filter(message -> message != null)
                .collect(Collectors.joining(separator)));
    }
}