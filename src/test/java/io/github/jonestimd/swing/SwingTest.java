package io.github.jonestimd.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public abstract class SwingTest {
    private static JFrame frame = new JFrame("test");

    @BeforeClass
    public static void showFrame() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            frame.setContentPane(new JPanel());
            frame.pack();
            frame.setVisible(true);
            frame.toFront();
        });
    }

    @AfterClass
    public static void disposeFrame() {
        frame.dispose();
    }

    @After
    public void removeComponents() throws Exception {
        SwingUtilities.invokeAndWait(() -> frame.getContentPane().removeAll());
    }

    protected void showComponent(final Component component) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            frame.getContentPane().add(component);
            frame.pack();
            component.requestFocus();
        });
    }

    protected void invokeAction(final JComponent source, String actionKey) throws Exception {
        invokeAction(source, source.getActionMap().get(actionKey));
    }

    protected void invokeAction(final Component source, final Action action) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            action.actionPerformed(new ActionEvent(source, -1, (String) action.getValue(Action.ACTION_COMMAND_KEY)));
        });
    }
}