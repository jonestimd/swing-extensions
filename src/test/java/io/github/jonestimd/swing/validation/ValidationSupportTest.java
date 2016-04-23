package io.github.jonestimd.swing.validation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JTextField;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static io.github.jonestimd.mockito.Matchers.matches;
import static org.fest.assertions.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ValidationSupportTest {
    public static final String MESSAGE = "required";
    @Mock
    private PropertyChangeListener listener;

    @Test
    public void validateValue() throws Exception {
        JTextField source = new JTextField();
        ValidationSupport<String> validationSupport = new ValidationSupport<>(source, new RequiredValidator(MESSAGE));

        assertThat(validationSupport.validateValue(null)).isEqualTo(MESSAGE);
        assertThat(validationSupport.getMessages()).isEqualTo(MESSAGE);
        assertThat(validationSupport.validateValue("something")).isNull();
        assertThat(validationSupport.getMessages()).isNull();
    }

    @Test
    public void validateValueFiresPropertyChange() throws Exception {
        JTextField source = new JTextField();
        ValidationSupport<String> validationSupport = new ValidationSupport<>(source, new RequiredValidator(MESSAGE));
        validationSupport.addValidationListener(listener);

        assertThat(validationSupport.validateValue(null)).isEqualTo(MESSAGE);
        assertThat(validationSupport.validateValue("something")).isNull();

        verify(listener).propertyChange(matches(new PropertyChangeEvent(source, ValidatedComponent.VALIDATION_MESSAGES, null, MESSAGE)));
        verify(listener).propertyChange(matches(new PropertyChangeEvent(source, ValidatedComponent.VALIDATION_MESSAGES, MESSAGE, null)));
    }

    @Test
    public void removeValidationListener() throws Exception {
        JTextField source = new JTextField();
        ValidationSupport<String> validationSupport = new ValidationSupport<>(source, new RequiredValidator(MESSAGE));
        validationSupport.addValidationListener(listener);
        validationSupport.removeValidationListener(listener);

        assertThat(validationSupport.validateValue(null)).isEqualTo(MESSAGE);
        assertThat(validationSupport.validateValue("something")).isNull();

        verifyZeroInteractions(listener);
    }
}