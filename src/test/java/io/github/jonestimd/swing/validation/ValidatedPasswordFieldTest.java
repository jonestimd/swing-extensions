package io.github.jonestimd.swing.validation;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class ValidatedPasswordFieldTest {
    public static final String MESSAGE = "required";

    @Test
    public void setNonNullBorderWrappsValidationBorder() throws Exception {
        ValidatedPasswordField field = new ValidatedPasswordField(MESSAGE);
        Border border = BorderFactory.createEmptyBorder();

        field.setBorder(border);

        assertThat(field.getBorder()).isInstanceOf(CompoundBorder.class);
        CompoundBorder compoundBorder = (CompoundBorder) field.getBorder();
        assertThat(compoundBorder.getInsideBorder()).isInstanceOf(ValidationBorder.class);
        assertThat(compoundBorder.getOutsideBorder()).isSameAs(border);
    }

    @Test
    public void setNullBorderWrappsValidationBorder() throws Exception {
        ValidatedPasswordField field = new ValidatedPasswordField(MESSAGE);

        field.setBorder(null);

        assertThat(field.getBorder()).isInstanceOf(ValidationBorder.class);
    }

    @Test
    public void validatesValueWhenTextChanges() throws Exception {
        ValidatedPasswordField field = new ValidatedPasswordField(MESSAGE);
        field.setBorder(null);
        assertThat(((ValidationBorder) field.getBorder()).isValid()).isFalse();
        assertThat(field.getValidationMessages()).isEqualTo(MESSAGE);

        field.setText("abc");

        assertThat(((ValidationBorder) field.getBorder()).isValid()).isTrue();
        assertThat(field.getValidationMessages()).isNull();
    }

    @Test
    public void returnsDefaultCursorOverValidationIndicator() throws Exception {
        ValidatedPasswordField field = new ValidatedPasswordField(MESSAGE);
        field.validateValue();
        assertThat(field.getCursor()).isNotEqualTo(Cursor.getDefaultCursor());
        for (MouseMotionListener listener : field.getMouseMotionListeners()) {
            listener.mouseMoved(new MouseEvent(field, MouseEvent.MOUSE_MOVED, 0L, 0, field.getWidth(), 0, 0, false));
        }

        assertThat(field.getCursor()).isEqualTo(Cursor.getDefaultCursor());
    }

    @Test
    public void getTooltipOverIndicatorReturnsValidationMessage() throws Exception {
        ValidatedPasswordField field = new ValidatedPasswordField(MESSAGE);
        field.validateValue();
        assertThat(field.getToolTipText()).isNull();
        for (MouseMotionListener listener : field.getMouseMotionListeners()) {
            listener.mouseMoved(new MouseEvent(field, MouseEvent.MOUSE_MOVED, 0L, 0, field.getWidth(), 0, 0, false));
        }

        assertThat(field.getToolTipText()).isEqualTo(MESSAGE);
    }
}