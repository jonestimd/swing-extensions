// The MIT License (MIT)
//
// Copyright (c) 2019 Timothy D. Jones
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

import java.awt.Dimension;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import io.github.jonestimd.swing.component.MultiSelectField.Builder;
import io.github.jonestimd.swing.validation.Validator;
import org.junit.Test;

import static io.github.jonestimd.mockito.Matchers.matches;
import static io.github.jonestimd.swing.validation.ValidatedComponent.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ValidatedMultiSelectFieldTest {
    private static final String REQUIRED = "required";
    private ValidatedMultiSelectField field;
    private Validator<List<String>> validator = (items) -> items.isEmpty() ? REQUIRED : null;
    private PropertyChangeListener listener = mock(PropertyChangeListener.class);

    @Test
    public void indicatesValidationError() throws Exception {
        field = (ValidatedMultiSelectField) new Builder(false, true).validator(validator).get();

        assertThat(field.getValidationBorder().isValid()).isFalse();
    }

    protected void newField() {
        field = new ValidatedMultiSelectField(false, true);
        field.setValidator(validator);
    }

    @Test
    public void firesValidationChangeWhenItemsChange() throws Exception {
        newField();
        field.addValidationListener(listener);

        assertThat(field.getValidationMessages()).isEqualTo(REQUIRED);
        field.setItems(Arrays.asList("one", "two"));

        verify(listener).propertyChange(matches(new PropertyChangeEvent(field, VALIDATION_MESSAGES, REQUIRED, null)));
        assertThat(field.getValidationMessages()).isNull();
    }

    @Test
    public void setNonNullBorderWrapsValidationBorder() throws Exception {
        newField();
        field.setSize(new Dimension(30, 16));
        Border border = BorderFactory.createEmptyBorder(1, 1, 1, 1);

        field.setBorder(border);

        assertThat(field.getBorder()).isSameAs(border);
        assertThat(field.getValidationBorder().isValid()).isFalse();
        assertThat(field.getInsets()).isEqualTo(new Insets(1, 1, 1, 17));
    }

    @Test
    public void setNullBorderWrapsValidationBorder() throws Exception {
        newField();
        field.setSize(new Dimension(30, 16));

        field.setBorder(null);

        assertThat(field.getBorder()).isNull();
        assertThat(field.getValidationBorder()).isNotNull();
        assertThat(field.getInsets()).isEqualTo(new Insets(0, 0, 0, 16));
    }
}