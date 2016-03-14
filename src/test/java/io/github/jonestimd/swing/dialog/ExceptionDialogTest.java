package io.github.jonestimd.swing.dialog;

import java.awt.event.KeyEvent;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import io.github.jonestimd.AsyncTest;
import org.junit.Test;

import static io.github.jonestimd.swing.ComponentFactory.*;
import static java.awt.event.KeyEvent.*;
import static org.fest.assertions.Assertions.*;

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
        assertThat(textArea.getRows()).isEqualTo((Integer) DEFAULT_BUNDLE.getObject("exceptionDialog.exception.rows"));
        assertThat(textArea.getColumns()).isEqualTo((Integer) DEFAULT_BUNDLE.getObject("exceptionDialog.exception.columns"));
    }

    @Test
    public void nullBundle() throws Exception {
        ExceptionDialog dialog = new ExceptionDialog(JOptionPane.getRootFrame(), null, new RuntimeException());

        assertThat(dialog.getTitle()).isEqualTo("Unexpected Exception");
        JLabel label = (JLabel) dialog.getContentPane().getComponent(0);
        assertThat(label.getText()).isEqualTo("Unexpected exception:");
        JScrollPane scrollPane = (JScrollPane) dialog.getContentPane().getComponent(1);
        JTextArea textArea = (JTextArea) scrollPane.getViewport().getView();
        assertThat(textArea.getRows()).isEqualTo((Integer) DEFAULT_BUNDLE.getObject("exceptionDialog.exception.rows"));
        assertThat(textArea.getColumns()).isEqualTo((Integer) DEFAULT_BUNDLE.getObject("exceptionDialog.exception.columns"));
    }

    @Test
    public void escapeClosesDialog() throws Exception {
        ExceptionDialog dialog = new ExceptionDialog(JOptionPane.getRootFrame(), null, new RuntimeException());

        SwingUtilities.invokeLater(() -> dialog.setVisible(true));

        AsyncTest.timeout(SWING_TIMEOUT, dialog::isVisible);
        KeyEvent event = new KeyEvent(dialog.getRootPane(), KEY_PRESSED, System.currentTimeMillis(), 0, VK_ESCAPE, CHAR_UNDEFINED);
        SwingUtilities.processKeyBindings(event);
        SwingUtilities.invokeAndWait(() -> SwingUtilities.processKeyBindings(event));
        AsyncTest.timeout(SWING_TIMEOUT, () -> ! dialog.isVisible());
    }
}