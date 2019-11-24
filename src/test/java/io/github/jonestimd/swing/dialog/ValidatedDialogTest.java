// The MIT License (MIT)
//
// Copyright (c) 2019 Timothy D. Jones
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
package io.github.jonestimd.swing.dialog;

import java.awt.Container;
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import io.github.jonestimd.AsyncTest;
import io.github.jonestimd.mockito.Matchers;
import io.github.jonestimd.swing.validation.RequiredValidator;
import io.github.jonestimd.swing.validation.ValidatedTextField;
import org.junit.After;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ValidatedDialogTest {
    private static final long SWING_TIMEOUT = 500L;
    public static final String REQUIRED_MESSAGE = "value required";
    private final ResourceBundle bundle = ResourceBundle.getBundle("test-resources");
    private ValidatedDialog dialog;

    @After
    public void disposeDialog() throws Exception {
        SwingUtilities.invokeAndWait(() -> dialog.dispose());
    }

    private void showDialog() {
        SwingUtilities.invokeLater(() -> {
            dialog.pack();
            dialog.setVisible(true);
        });
        AsyncTest.timeout(SWING_TIMEOUT, () -> dialog.isVisible());
    }

    private Container getDialogComponent(int index) {
        Container contentPane = dialog.getContentPane();
        return (Container) contentPane.getComponent(index);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void validation() throws Exception {
        dialog = new ValidatedDialog(JOptionPane.getRootFrame(), "", bundle);
        Consumer<Collection<String>> validationListener = mock(Consumer.class);
        dialog.addValidationListener(validationListener);
        ValidatedTextField field = new ValidatedTextField(new RequiredValidator(REQUIRED_MESSAGE));
        dialog.getFormPanel().add(field);

        showDialog();

        JScrollPane scrollPane = (JScrollPane) getDialogComponent(2);
        assertThat(scrollPane.isVisible()).isTrue();
        JTextArea statusArea = (JTextArea) scrollPane.getViewport().getView();
        assertThat(statusArea.getText()).isEqualTo(REQUIRED_MESSAGE);
        assertThat(dialog.getValidationMessages()).containsOnly(REQUIRED_MESSAGE);

        SwingUtilities.invokeAndWait(() -> field.setText("value"));

        AsyncTest.timeout(SWING_TIMEOUT, () -> ! scrollPane.isVisible());
        verify(validationListener).accept(Matchers.isEmpty());
        verify(validationListener).accept(Matchers.containsOnly(REQUIRED_MESSAGE));
        assertThat(dialog.getValidationMessages()).isEmpty();
    }
}