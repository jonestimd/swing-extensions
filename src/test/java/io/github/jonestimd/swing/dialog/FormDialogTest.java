// The MIT License (MIT)
//
// Copyright (c) 2016 Timothy D. Jones
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
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.JButton;
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

public class FormDialogTest {
    private static final long SWING_TIMEOUT = 500L;
    public static final String REQUIRED_MESSAGE = "value required";
    private final ResourceBundle bundle = ResourceBundle.getBundle("test-resources");
    private FormDialog dialog;

    @After
    public void disposeDialog() throws Exception {
        SwingUtilities.invokeAndWait(() -> dialog.dispose());
    }

    @Test
    public void layout() throws Exception {
        dialog = new FormDialog(JOptionPane.getRootFrame(), "", bundle);

        assertThat(getDialogComponent(0)).isSameAs(dialog.getFormPanel());
        assertThat(getDialogComponent(2)).isInstanceOf(JScrollPane.class);
        assertThat((getDialogComponent(2)).isVisible()).isFalse();
        assertThat(((JScrollPane) getDialogComponent(2)).getViewport().getView()).isInstanceOf(JTextArea.class);
        assertThat(getButton(0).getText()).isEqualTo("Save");
        assertThat(getButton(1).getText()).isEqualTo("Cancel");
    }

    @Test
    public void addButtonInsertsButtonBeforeCancel() throws Exception {
        dialog = new FormDialog(JOptionPane.getRootFrame(), "", bundle);
        AbstractAction action = new AbstractAction("custom") {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        };

        dialog.addButton(action);

        assertThat(getButton(0).getText()).isEqualTo("Save");
        assertThat(getButton(1).getText()).isEqualTo("custom");
        assertThat(getButton(2).getText()).isEqualTo("Cancel");
    }

    @Test
    public void cancelButtonSetsCancelled() throws Exception {
        dialog = new FormDialog(JOptionPane.getRootFrame(), "", bundle);

        showDialog();

        SwingUtilities.invokeAndWait(() -> getButton(1).doClick());
        AsyncTest.timeout(SWING_TIMEOUT, () -> ! dialog.isVisible());
        assertThat(dialog.isCancelled()).isTrue();
    }

    @Test
    public void setVisibleResetsCancelAction() throws Exception {
        dialog = new FormDialog(JOptionPane.getRootFrame(), "", bundle);
        getButton(1).doClick();

        showDialog();

        assertThat(dialog.isCancelled()).isFalse();
        SwingUtilities.invokeAndWait(() -> dialog.setVisible(false));
    }

    @Test
    public void setVisibleSetsDefaultButton() throws Exception {
        dialog = new FormDialog(JOptionPane.getRootFrame(), "", bundle);

        showDialog();

        assertThat(dialog.getRootPane().getDefaultButton()).isSameAs(getButton(0));
        SwingUtilities.invokeAndWait(() -> dialog.setVisible(false));
    }

    @Test
    public void saveButtonClosesDialog() throws Exception {
        dialog = new FormDialog(JOptionPane.getRootFrame(), "", bundle);

        showDialog();

        getButton(0).setEnabled(true);
        SwingUtilities.invokeAndWait(() -> getButton(0).doClick());
        AsyncTest.timeout(SWING_TIMEOUT, () -> ! dialog.isVisible());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void validation() throws Exception {
        dialog = new FormDialog(JOptionPane.getRootFrame(), "", bundle);
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

    private void showDialog() {
        SwingUtilities.invokeLater(() -> {
            dialog.pack();
            dialog.setVisible(true);
        });
        AsyncTest.timeout(SWING_TIMEOUT, () -> dialog.isVisible());
    }

    private JButton getButton(int index) {
        return (JButton) getDialogComponent(1).getComponent(index);
    }

    private Container getDialogComponent(int index) {
        Container contentPane = dialog.getContentPane();
        return (Container) contentPane.getComponent(index);
    }
}