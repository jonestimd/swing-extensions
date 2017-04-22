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

import java.util.function.BiFunction;

import com.google.common.collect.Lists;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class UniqueValueValidatorTest {
    private static final String REQUIRED_MESSAGE = "required";
    private static final String UNIQUE_MESSAGE = "not unique";

    @Test
    public void validateSameGroup() throws Exception {
        UniqueValueValidator<Integer> validator = new UniqueValueValidator<>(Object::toString, REQUIRED_MESSAGE, UNIQUE_MESSAGE);

        assertThat(validator.validate(1, null, Lists.newArrayList(1, 2, 3))).isEqualTo(REQUIRED_MESSAGE);
        assertThat(validator.validate(1, "", Lists.newArrayList(1, 2, 3))).isEqualTo(REQUIRED_MESSAGE);
        assertThat(validator.validate(1, "1", Lists.newArrayList(1, 2, 3))).isEqualTo(UNIQUE_MESSAGE);
        assertThat(validator.validate(0, "1", Lists.newArrayList(1, 2, 3))).isEqualTo(null);
    }

    @Test
    public void validateNotSameGroup() throws Exception {
        BiFunction<Object, Object, Boolean> alwaysFalse = (v1, v2) -> false;
        UniqueValueValidator<Integer> validator = new UniqueValueValidator<>(Object::toString, alwaysFalse, REQUIRED_MESSAGE, UNIQUE_MESSAGE);

        assertThat(validator.validate(1, null, Lists.newArrayList(1, 2, 3))).isEqualTo(REQUIRED_MESSAGE);
        assertThat(validator.validate(1, "", Lists.newArrayList(1, 2, 3))).isEqualTo(REQUIRED_MESSAGE);
        assertThat(validator.validate(1, "1", Lists.newArrayList(1, 2, 3))).isEqualTo(null);
        assertThat(validator.validate(0, "1", Lists.newArrayList(1, 2, 3))).isEqualTo(null);
    }
}