package io.github.jonestimd.swing.validation;

import org.junit.Test;

import static org.fest.assertions.Assertions.*;

public class ValidatorTest {
    public static final String REQUIRED = "required";
    public static final String MIN_LENGTH = "min length";
    private Validator<String> required = value -> value == null ? REQUIRED : null;
    private Validator<String> minLength = value -> value == null || value.length() < 5 ? MIN_LENGTH : null;

    @Test
    public void emptyValidatorReturnsNull() throws Exception {
        assertThat(Validator.empty().validate(null)).isNull();
    }

    @Test
    public void addConcatenatesErrors() throws Exception {
        String messages = required.add(minLength).validate(null);

        assertThat(messages).isEqualTo(REQUIRED + System.lineSeparator() + MIN_LENGTH);
    }

    @Test
    public void addSkipsNulls() throws Exception {
        String messages = required.add(minLength).validate("1234");

        assertThat(messages).isEqualTo(MIN_LENGTH);
    }

    @Test
    public void addReturnsNullForNoErrors() throws Exception {
        String messages = required.add(minLength).validate("12345");

        assertThat(messages).isNull();
    }

    @Test
    public void thenErrorReturnsFirstError() throws Exception {
        String messages = required.then(minLength).validate(null);

        assertThat(messages).isEqualTo(REQUIRED);
    }

    @Test
    public void thenSkips() throws Exception {
        String messages = required.then(minLength).validate("1234");

        assertThat(messages).isEqualTo(MIN_LENGTH);
    }

    @Test
    public void thenReturnsNullForNoErrors() throws Exception {
        String messages = required.then(minLength).validate("12345");

        assertThat(messages).isNull();
    }
}