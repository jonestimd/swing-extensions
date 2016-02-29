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

import org.junit.Test;

import static org.fest.assertions.Assertions.*;
import static org.mockito.Mockito.*;

public class BeanPropertyValidatorTest {
    @Test
    public void emptyValidator() throws Exception {
        assertThat(BeanPropertyValidator.empty().validate(-1, null, null)).isNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void fromValidator() throws Exception {
        Validator<String> validator = mock(Validator.class);
        when(validator.validate("valid")).thenReturn(null);
        when(validator.validate("invalid")).thenReturn("error");

        BeanPropertyValidator from = BeanPropertyValidator.from(validator);

        assertThat(from.validate(-1, "valid", null)).isNull();
        assertThat(from.validate(-1, "invalid", null)).isEqualTo("error");
    }
}