package io.github.jonestimd.swing.validation;

import java.util.Collection;

import javax.swing.JPanel;

import io.github.jonestimd.swing.validation.ValidationTracker.ValidationChangeHandler;
import org.junit.Test;

import static org.fest.assertions.Assertions.*;

public class ValidationTrackerTest {
    private static final String REQUIRED_MESSAGE = "required";
    private final JPanel panel = new JPanel();
    private int changes = 0;
    private Collection<String> validationMessages;

    @Test
    public void trackValidationMessages() throws Exception {
        ValidatedTextField field = new ValidatedTextField(new RequiredValidator(REQUIRED_MESSAGE));
        panel.add(field);
        ValidationTracker.install(new Handler(), panel);
        assertThat(validationMessages).containsOnly(REQUIRED_MESSAGE);

        field.setText("something");
        assertThat(changes).isEqualTo(2);
        assertThat(validationMessages).isEmpty();

        panel.remove(field);
        field.setText("something");

        assertThat(changes).isEqualTo(3);
    }

    @Test
    public void trackAddedComponents() throws Exception {
        ValidatedTextField field = new ValidatedTextField(new RequiredValidator(REQUIRED_MESSAGE));
        ValidationTracker.install(new Handler(), panel);
        panel.add(field);

        field.setText("text");
        assertThat(changes).isEqualTo(3);
        assertThat(validationMessages).isEmpty();

        field.setText("");
        assertThat(changes).isEqualTo(4);
        assertThat(validationMessages).containsOnly(REQUIRED_MESSAGE);

        panel.remove(field);
        assertThat(changes).isEqualTo(5);
        assertThat(validationMessages).isEmpty();
    }

    @Test
    public void trackNestedAddedComponents() throws Exception {
        ValidatedTextField field = new ValidatedTextField(new RequiredValidator(REQUIRED_MESSAGE));
        ValidationTracker.install(new Handler(), panel);
        JPanel nested = new JPanel();
        nested.add(field);
        panel.add(nested);

        field.setText("text");
        assertThat(changes).isEqualTo(3);
        assertThat(validationMessages).isEmpty();

        field.setText("");
        assertThat(changes).isEqualTo(4);
        assertThat(validationMessages).containsOnly(REQUIRED_MESSAGE);

        panel.remove(nested);
        assertThat(changes).isEqualTo(5);
        assertThat(validationMessages).isEmpty();
    }

    private class Handler implements ValidationChangeHandler {
        @Override
        public void validationChanged(Collection<String> validationMessages) {
            changes++;
            ValidationTrackerTest.this.validationMessages = validationMessages;
        }
    }
}