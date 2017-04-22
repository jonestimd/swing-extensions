package io.github.jonestimd.swing.validation;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class NumberValidatorTest {
    public static final String MESSAGE = "invalid number";
    private NumberValidator validator = new NumberValidator(MESSAGE);

    @Test
    public void returnsNullForEmptyValue() throws Exception {
        assertThat(validator.validate("")).isNull();
        assertThat(validator.validate(null)).isNull();
    }

    @Test
    public void returnsNullForValidNumber() throws Exception {
        assertThat(validator.validate("01.01")).isNull();
        assertThat(validator.validate("+01.01")).isNull();
        assertThat(validator.validate("-01.01")).isNull();
    }

    @Test
    public void returnsMessgaeForInvalidNumber() throws Exception {
        assertThat(validator.validate("a01.01")).isEqualTo(MESSAGE);
    }
}