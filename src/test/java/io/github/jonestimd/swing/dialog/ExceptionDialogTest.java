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

import java.awt.event.KeyEvent;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import io.github.jonestimd.AsyncTest;
import io.github.jonestimd.swing.ComponentResources;
import org.junit.Test;

import static java.awt.event.KeyEvent.*;
import static org.assertj.core.api.Assertions.*;

public class ExceptionDialogTest {
    public static final long SWING_TIMEOUT = 500L;

    @Test
    public void defaultBundle() throws Exception {
        ExceptionDialog dialog = new ExceptionDialog(JOptionPane.getRootFrame(), new RuntimeException());

        assertThat(dialog.getTitle()).isEqualTo("Unexpected Exception");
        JLabel label = (JLabel) dialog.getContentPane().getComponent(0);
        assertThat(label.getText()).isEqualTo("Unexpected exception:");
        JScrollPane scrollPane = (JScrollPane) dialog.getContentPane().getComponent(1);
        JTextArea textArea = (JTextArea) scrollPane.getViewport().getView();
        assertThat(textArea.getRows()).isEqualTo(ComponentResources.getInt("exceptionDialog.exception.rows"));
        assertThat(textArea.getColumns()).isEqualTo(ComponentResources.getInt("exceptionDialog.exception.columns"));
    }

    @Test
    public void nullBundle() throws Exception {
        ExceptionDialog dialog = new ExceptionDialog(JOptionPane.getRootFrame(), null, "", new RuntimeException());

        assertThat(dialog.getTitle()).isEqualTo("Unexpected Exception");
        JLabel label = (JLabel) dialog.getContentPane().getComponent(0);
        assertThat(label.getText()).isEqualTo("Unexpected exception:");
        JScrollPane scrollPane = (JScrollPane) dialog.getContentPane().getComponent(1);
        JTextArea textArea = (JTextArea) scrollPane.getViewport().getView();
        assertThat(textArea.getRows()).isEqualTo(ComponentResources.getInt("exceptionDialog.exception.rows"));
        assertThat(textArea.getColumns()).isEqualTo(ComponentResources.getInt("exceptionDialog.exception.columns"));
    }

    @Test
    public void escapeClosesDialog() throws Exception {
        ExceptionDialog dialog = new ExceptionDialog(JOptionPane.getRootFrame(), null, null, new RuntimeException());

        SwingUtilities.invokeLater(() -> dialog.setVisible(true));

        AsyncTest.timeout(SWING_TIMEOUT, dialog::isVisible);
        KeyEvent event = new KeyEvent(dialog.getRootPane(), KEY_PRESSED, System.currentTimeMillis(), 0, VK_ESCAPE, CHAR_UNDEFINED);
        SwingUtilities.processKeyBindings(event);
        SwingUtilities.invokeAndWait(() -> SwingUtilities.processKeyBindings(event));
        AsyncTest.timeout(SWING_TIMEOUT, () -> ! dialog.isVisible());
    }
}