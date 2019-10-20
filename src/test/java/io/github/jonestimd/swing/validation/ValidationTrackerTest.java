package io.github.jonestimd.swing.validation;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import io.github.jonestimd.mockito.Matchers;
import io.github.jonestimd.swing.validation.ValidationTracker.ValidationChangeHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ValidationTrackerTest {
    private static final String REQUIRED_MESSAGE = "required";
    private final JPanel panel = new JPanel();
    @Mock
    private ValidationChangeHandler handler;
    private InOrder inOrder;

    @Before
    public void setup() throws Exception {
        ValidationTracker.install(handler, panel);
        inOrder = inOrder(handler);
    }

    @Test
    public void trackValidationMessages() throws Exception {
        ValidatedTextField field = new ValidatedTextField(new RequiredValidator(REQUIRED_MESSAGE));
        inOrder.verify(handler).validationChanged(Matchers.isEmpty());

        panel.add(field);
        inOrder.verify(handler).validationChanged(Matchers.containsOnly(REQUIRED_MESSAGE));

        field.setText("something");
        inOrder.verify(handler).validationChanged(Matchers.isEmpty());

        panel.remove(field);
        field.setText("something");
        inOrder.verify(handler).validationChanged(Matchers.isEmpty());
    }

    @Test
    public void trackAddedComponents() throws Exception {
        ValidatedTextField field = new ValidatedTextField(new RequiredValidator(REQUIRED_MESSAGE));
        inOrder.verify(handler).validationChanged(Matchers.isEmpty());

        panel.add(field);
        inOrder.verify(handler).validationChanged(Matchers.containsOnly(REQUIRED_MESSAGE));

        field.setText("text");
        inOrder.verify(handler).validationChanged(Matchers.isEmpty());

        field.setText("");
        inOrder.verify(handler).validationChanged(Matchers.containsOnly(REQUIRED_MESSAGE));

        panel.remove(field);
        inOrder.verify(handler).validationChanged(Matchers.isEmpty());
    }

    @Test
    public void ignoreValidationOnHiddenComponent() throws Exception {
        ValidatedTextField field = new ValidatedTextField(new RequiredValidator(REQUIRED_MESSAGE));
        panel.add(field);
        JFrame frame = new JFrame("JUnit test");
        SwingUtilities.invokeAndWait(() -> {
            frame.getContentPane().add(panel);
            frame.pack();
            frame.setVisible(true);
        });

        SwingUtilities.invokeAndWait(() -> field.setVisible(false));

        inOrder.verify(handler).validationChanged(Matchers.containsOnly(REQUIRED_MESSAGE));
        inOrder.verify(handler).validationChanged(Matchers.isEmpty());
        frame.dispose();
    }

    @Test
    public void trackNestedAddedComponents() throws Exception {
        ValidatedTextField field = new ValidatedTextField(new RequiredValidator(REQUIRED_MESSAGE));
        inOrder.verify(handler).validationChanged(Matchers.isEmpty());

        JPanel nested = new JPanel();
        nested.add(field);
        panel.add(nested);
        inOrder.verify(handler).validationChanged(Matchers.containsOnly(REQUIRED_MESSAGE));

        field.setText("text");
        inOrder.verify(handler).validationChanged(Matchers.isEmpty());

        field.setText("");
        inOrder.verify(handler).validationChanged(Matchers.containsOnly(REQUIRED_MESSAGE));

        panel.remove(nested);
        inOrder.verify(handler).validationChanged(Matchers.isEmpty());
    }
}