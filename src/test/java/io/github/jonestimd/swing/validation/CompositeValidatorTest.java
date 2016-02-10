package io.github.jonestimd.swing.validation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CompositeValidatorTest {
    public static final String VALUE = "value";
    @Mock
    private Validator<String> validator1;
    @Mock
    private Validator<String> validator2;

    @Test
    public void returnsFirstValidationError() throws Exception {
        when(validator1.validate(anyString())).thenReturn("error1");

        String message = new CompositeValidator<>(validator1, validator2).validate(VALUE);

        assertThat(message).isEqualTo("error1");
        verify(validator1).validate(VALUE);
        verifyNoMoreInteractions(validator2);
    }

    @Test
    public void returnsNullIfAllValidationsPass() throws Exception {
        String message = new CompositeValidator<>(validator1, validator2).validate(VALUE);

        assertThat(message).isNull();
        verify(validator1).validate(VALUE);
        verify(validator2).validate(VALUE);
    }
}