// The MIT License (MIT)
//
// Copyright (c) 2017 Timothy D. Jones
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

import java.awt.Cursor;
import java.awt.Dialog.ModalityType;

import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import io.github.jonestimd.AsyncTest;
import org.junit.After;
import org.junit.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.*;

public class MessageDialogTest {
    private static final long SWING_TIMEOUT = 5000L;
    private MessageDialog dialog;
    private JTextField field;

    @After
    public void disposeDialog() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            dialog.setVisible(false);
            dialog.dispose();
        });
    }

    private void createDialog() {
        dialog = new MessageDialog(JOptionPane.getRootFrame(), ModalityType.DOCUMENT_MODAL);
        dialog.getContentPane().add(field = new JTextField());
    }

    @Test
    public void noFocusAfterDisableUI() throws Exception {
        createDialog();
        SwingUtilities.invokeLater(() -> dialog.setVisible(true));

        SwingUtilities.invokeAndWait(() -> dialog.disableUI("wait..."));
        // AsyncTest.timeout(SWING_TIMEOUT, dialog.getGlassPane()::isFocusOwner);
        SwingUtilities.invokeAndWait(dialog::enableUI);

        AsyncTest.timeout(SWING_TIMEOUT, () -> !dialog.getGlassPane().isFocusOwner());
    }

    @Test
    public void disableUIAllowsNullMessage() throws Exception {
        createDialog();
        SwingUtilities.invokeLater(() -> dialog.setVisible(true));

        SwingUtilities.invokeAndWait(() -> dialog.disableUI(null));
        AsyncTest.timeout(SWING_TIMEOUT, dialog.getGlassPane()::isFocusOwner);
        SwingUtilities.invokeAndWait(dialog::enableUI);

        AsyncTest.timeout(SWING_TIMEOUT, () -> !dialog.getGlassPane().isFocusOwner());
    }

    @Test
    public void setsWaitCursorOnGlassPane() throws Exception {
        createDialog();

        assertThat(dialog.getGlassPane().getCursor()).isEqualTo(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    @Test
    public void enableUIRestoresFocusOwner() throws Exception {
        createDialog();
        SwingUtilities.invokeLater(() -> dialog.setVisible(true));
        SwingUtilities.invokeLater(() -> field.requestFocus());
        AsyncTest.timeout(SWING_TIMEOUT, field::isFocusOwner);

        SwingUtilities.invokeAndWait(() -> dialog.disableUI("wait..."));
        AsyncTest.timeout(SWING_TIMEOUT, dialog.getGlassPane()::isFocusOwner);
        SwingUtilities.invokeAndWait(dialog::enableUI);

        AsyncTest.timeout(SWING_TIMEOUT, field::isFocusOwner);
    }
}