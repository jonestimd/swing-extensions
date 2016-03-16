package io.github.jonestimd.swing.component;

import java.text.NumberFormat;
import java.util.ResourceBundle;

import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.text.NumberFormatter;

import io.github.jonestimd.swing.validation.RequiredValidator;
import io.github.jonestimd.swing.validation.ValidatedTextField;
import org.junit.Test;

import static org.fest.assertions.Assertions.*;

public class TextFieldTest {
    private final ResourceBundle bundle = ResourceBundle.getBundle("test-resources");

    @Test
    public void readonlySetsEditableFalse() throws Exception {
        JTextField field = new TextField<>(new JTextField()).readOnly().get();

        assertThat(field.isEditable()).isFalse();
    }

    @Test
    public void rightAlignedSetsHorizontalAlignment() throws Exception {
        JTextField field = new TextField<>(new JTextField()).rightAligned().get();

        assertThat(field.getHorizontalAlignment()).isEqualTo(JTextField.RIGHT);
    }

    @Test
    public void plainCreatesDefaultJTextField() throws Exception {
        final JTextField field = TextField.plain().get();

        assertThat(field.isEditable()).isTrue();
        assertThat(field.getHorizontalAlignment()).isEqualTo(JTextField.LEADING);
    }

    @Test
    public void formattedCreatesJFormattedTextField() throws Exception {
        final JFormattedTextField field = TextField.formatted(NumberFormat.getCurrencyInstance()).get();

        assertThat(field.getFormatter()).isInstanceOf(NumberFormatter.class);
    }

    @Test
    public void requiredValidation() throws Exception {
        final ValidatedTextField field = TextField.validated(bundle, "TextFieldTest.").required("required.message").get();

        assertThat(field.getValidationMessages()).contains("Required value");
    }

    @Test
    public void numericValidation() throws Exception {
        final ValidatedTextField field = TextField.validated(bundle, "TextFieldTest.").numeric("numeric.message").get();
        field.setText("abc");

        assertThat(field.getValidationMessages()).contains("Invalid number");
    }

    @Test
    public void positiveNumberValidation() throws Exception {
        final ValidatedTextField field = TextField.validated(bundle, "TextFieldTest.").positiveNumber("numeric.message").get();
        field.setText("-1");

        assertThat(field.getValidationMessages()).contains("Invalid number");
    }

    @Test
    public void addCustomValidator() throws Exception {
        final ValidatedTextField field = TextField.validated(bundle, "TextFieldTest.").add(new RequiredValidator("Missing value")).get();

        assertThat(field.getValidationMessages()).contains("Missing value");
    }

    @Test
    public void multipleValidators() throws Exception {
        final ValidatedTextField field = TextField.validated(bundle, "TextFieldTest.")
                .required("required.message")
                .numeric("numeric.message").get();

        assertThat(field.getValidationMessages()).contains("Required value");

        field.setText("abc");
        assertThat(field.getValidationMessages()).contains("Invalid number");

        field.setText("123");
        assertThat(field.getValidationMessages()).isNull();
    }

    @Test
    public void configure() throws Exception {
        final ValidatedTextField field = TextField.validated(bundle, "TextFieldTest.").required("required.message")
            .configure().rightAligned().get();

        assertThat(field.getValidationMessages()).contains("Required value");
        assertThat(field.getHorizontalAlignment()).isEqualTo(JTextField.RIGHT);
    }
}