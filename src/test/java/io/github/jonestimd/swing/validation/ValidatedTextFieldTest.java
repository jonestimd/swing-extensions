// The MIT License (MIT)
//
// Copyright (c) 2020 Timothy D. Jones
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

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class ValidatedTextFieldTest {
    public static final String MESSAGE = "required";
    private final Validator<String> validator = new RequiredValidator(MESSAGE);

    @Test
    public void setNonNullBorderWrappsValidationBorder() throws Exception {
        ValidatedTextField field = new ValidatedTextField(validator);
        Border border = BorderFactory.createEmptyBorder();

        field.setBorder(border);

        assertThat(field.getBorder()).isInstanceOf(CompoundBorder.class);
        CompoundBorder compoundBorder = (CompoundBorder) field.getBorder();
        assertThat(compoundBorder.getInsideBorder()).isInstanceOf(ValidationBorder.class);
        assertThat(compoundBorder.getOutsideBorder()).isSameAs(border);
    }

    @Test
    public void setNullBorderWrapsValidationBorder() throws Exception {
        ValidatedTextField field = new ValidatedTextField(validator);

        field.setBorder(null);

        assertThat(field.getBorder()).isInstanceOf(ValidationBorder.class);
    }

    @Test
    public void validatesValueWhenTextChanges() throws Exception {
        ValidatedTextField field = new ValidatedTextField(validator);
        field.setBorder(null);
        assertThat(((ValidationBorder) field.getBorder()).isValid()).isFalse();
        assertThat(field.getValidationMessages()).isEqualTo(MESSAGE);

        field.setText("abc");

        assertThat(((ValidationBorder) field.getBorder()).isValid()).isTrue();
        assertThat(field.getValidationMessages()).isNull();
    }

    @Test
    public void returnsDefaultCursorOverValidationIndicator() throws Exception {
        ValidatedTextField field = new ValidatedTextField(validator);
        field.validateValue();
        assertThat(field.getCursor()).isNotEqualTo(Cursor.getDefaultCursor());
        for (MouseMotionListener listener : field.getMouseMotionListeners()) {
            listener.mouseMoved(new MouseEvent(field, MouseEvent.MOUSE_MOVED, 0L, 0, field.getWidth(), 0, 0, false));
        }

        assertThat(field.getCursor()).isEqualTo(Cursor.getDefaultCursor());
    }

    @Test
    public void getTooltipOverIndicatorReturnsValidationMessage() throws Exception {
        ValidatedTextField field = new ValidatedTextField(validator);
        field.validateValue();
        assertThat(field.getToolTipText()).isNull();
        for (MouseMotionListener listener : field.getMouseMotionListeners()) {
            listener.mouseMoved(new MouseEvent(field, MouseEvent.MOUSE_MOVED, 0L, 0, field.getWidth(), 0, 0, false));
        }

        assertThat(field.getToolTipText()).isEqualTo(MESSAGE);
    }

    @Test
    public void settingNotEditableClearsError() throws Exception {
        ValidatedTextField field = new ValidatedTextField(validator);
        field.setBorder(null);
        field.validateValue();
        assertThat(field.getValidationMessages()).isEqualTo(MESSAGE);

        field.setEditable(false);

        assertThat(field.getValidationMessages()).isNull();
        assertThat(((ValidationBorder) field.getBorder()).isValid()).isTrue();
    }

    @Test
    public void settingEditableShowsError() throws Exception {
        ValidatedTextField field = new ValidatedTextField(validator);
        field.setEditable(false);
        field.setBorder(null);
        field.validateValue();
        assertThat(field.getValidationMessages()).isNull();

        field.setEditable(true);

        assertThat(field.getValidationMessages()).isEqualTo(MESSAGE);
        assertThat(((ValidationBorder) field.getBorder()).isValid()).isFalse();
    }
}