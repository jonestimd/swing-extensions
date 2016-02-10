package io.github.jonestimd.swing.validation;

import org.junit.Test;

import static org.fest.assertions.Assertions.*;

public class RequiredValidatorTest {
    public static final String MESSAGE = "message";

    @Test
    public void returnsMessageForNull() throws Exception {
        assertThat(new RequiredValidator(MESSAGE).validate(null)).isEqualTo(MESSAGE);
    }

    @Test
    public void returnsMessageForEmptyString() throws Exception {
        assertThat(new RequiredValidator(MESSAGE).validate("")).isEqualTo(MESSAGE);
        assertThat(new RequiredValidator(MESSAGE).validate(" ")).isEqualTo(MESSAGE);
    }

    @Test
    public void returnsNullForNonEmptyString() throws Exception {
        assertThat(new RequiredValidator(MESSAGE).validate("x")).isNull();
    }
}