package io.github.jonestimd.swing.validation;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class PositiveNumberValidatorTest {
    private static final String MESSAGE = "not a positive number";
    private final PositiveNumberValidator validator = new PositiveNumberValidator(MESSAGE);

    @Test
    public void returnsNullForNullValue() throws Exception {
        assertThat(validator.validate(null)).isNull();
    }

    @Test
    public void returnsNullForEmptyString() throws Exception {
        assertThat(validator.validate("")).isNull();
    }

    @Test
    public void returnsNullForZero() throws Exception {
        assertThat(validator.validate("0.0")).isNull();
    }

    @Test
    public void returnsNullForPositiveNumber() throws Exception {
        assertThat(validator.validate("123.456")).isNull();
    }

    @Test
    public void returnsMessageForInvalidNumber() throws Exception {
        assertThat(validator.validate("123abc")).isEqualTo(MESSAGE);
    }

    @Test
    public void returnsMessageForNegativeNumber() throws Exception {
        assertThat(validator.validate("-123.456")).isEqualTo(MESSAGE);
    }
}