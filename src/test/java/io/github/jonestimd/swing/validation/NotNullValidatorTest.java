package io.github.jonestimd.swing.validation;

import java.util.Date;

import org.junit.Test;

import static org.fest.assertions.Assertions.*;

public class NotNullValidatorTest {
    private static final String MESSAGE = "error";

    @Test
    public void returnsMessageForNullValue() throws Exception {
        assertThat(new NotNullValidator<Date>(MESSAGE).validate(null)).isEqualTo(MESSAGE);
    }

    @Test
    public void returnsNullForNonNullValue() throws Exception {
        assertThat(new NotNullValidator<Date>(MESSAGE).validate(new Date())).isNull();
    }
}