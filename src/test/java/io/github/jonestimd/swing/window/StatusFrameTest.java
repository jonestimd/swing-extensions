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
package io.github.jonestimd.swing.window;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.ImageProducer;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import io.github.jonestimd.AsyncTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static java.lang.System.*;
import static org.assertj.core.api.Assertions.*;

public class StatusFrameTest {
    private static final String RESOURCE_PREFIX = "StatusFrameTest";
    private static final String HEIGHT_RESOURCE = RESOURCE_PREFIX + ".height";
    private static final String WIDTH_RESOURCE = RESOURCE_PREFIX + ".width";
    private static final String STATE_RESOURCE = RESOURCE_PREFIX + ".state";
    private static final Integer RESTORE_WIDTH = 300;
    private static final Integer RESTORE_HEIGHT = 250;
    private static final long SWING_TIMEOUT = 500L;
    private final ResourceBundle bundle = ResourceBundle.getBundle("test-resources");

    private StatusFrame frame;

    @Before
    public void clearProperties() throws Exception {
        System.getProperties().remove(STATE_RESOURCE);
        System.getProperties().remove(WIDTH_RESOURCE);
        System.getProperties().remove(HEIGHT_RESOURCE);
    }

    @After
    public void disposeFrame() {
        frame.setVisible(false);
        frame.dispose();
    }

    @Test
    public void savesSizeToSystemProperties() throws Exception {
        createStatusFrame(RESOURCE_PREFIX);
        SwingUtilities.invokeAndWait(() -> frame.setVisible(true));
        Dimension size = frame.getSize();
        int state = frame.getExtendedState();

        SwingUtilities.invokeAndWait(() -> {
            frame.setVisible(false);
            frame.dispose();
        });

        assertThat(System.getProperty(STATE_RESOURCE)).isEqualTo(Integer.toString(state));
        assertThat(System.getProperty(WIDTH_RESOURCE)).isEqualTo(Integer.toString(size.width));
        assertThat(System.getProperty(HEIGHT_RESOURCE)).isEqualTo(Integer.toString(size.height));
    }

    @Test
    public void usesDefaultSize() throws Exception {
        createStatusFrame(RESOURCE_PREFIX + ".defaultSize");

        SwingUtilities.invokeAndWait(() -> frame.setVisible(true));

        assertThat(frame.getExtendedState()).isEqualTo(0);
        assertThat(frame.getWidth()).isEqualTo(StatusFrame.DEFAULT_WIDTH);
        assertThat(frame.getHeight()).isEqualTo(StatusFrame.DEFAULT_HEIGHT);
    }

    @Test
    public void restoresSizeFromResourceBundle() throws Exception {
        createStatusFrame(RESOURCE_PREFIX);

        SwingUtilities.invokeAndWait(() -> frame.setVisible(true));

        assertThat(frame.getExtendedState()).isEqualTo(0);
        assertThat(frame.getWidth()).isEqualTo(getInt(WIDTH_RESOURCE));
        assertThat(frame.getHeight()).isEqualTo(getInt(HEIGHT_RESOURCE));
    }

    private int getInt(String key) {
        return Integer.parseInt(bundle.getString(key));
    }

    @Test
    public void restoresSizeFromSystemProperties() throws Exception {
        System.setProperty(STATE_RESOURCE, "0");
        System.setProperty(WIDTH_RESOURCE, RESTORE_WIDTH.toString());
        System.setProperty(HEIGHT_RESOURCE, RESTORE_HEIGHT.toString());
        createStatusFrame(RESOURCE_PREFIX);

        SwingUtilities.invokeAndWait(() -> frame.setVisible(true));

        assertThat(frame.getExtendedState()).isEqualTo(0);
        assertThat(frame.getWidth()).isEqualTo(RESTORE_WIDTH);
        assertThat(frame.getHeight()).isEqualTo(RESTORE_HEIGHT);
    }

    @Test
    public void ignoresInvalidWidthFromSystemProperties() throws Exception {
        System.setProperty(STATE_RESOURCE, "0");
        System.setProperty(WIDTH_RESOURCE, "-300");
        System.setProperty(HEIGHT_RESOURCE, RESTORE_HEIGHT.toString());
        createStatusFrame(RESOURCE_PREFIX);

        SwingUtilities.invokeAndWait(() -> frame.setVisible(true));

        assertThat(frame.getExtendedState()).isEqualTo(0);
        assertThat(frame.getWidth()).isEqualTo(getInt(WIDTH_RESOURCE));
        assertThat(frame.getHeight()).isEqualTo(RESTORE_HEIGHT);
    }

    @Test
    public void ignoresInvalidHeightFromSystemProperties() throws Exception {
        System.setProperty(STATE_RESOURCE, "0");
        System.setProperty(WIDTH_RESOURCE, RESTORE_WIDTH.toString());
        System.setProperty(HEIGHT_RESOURCE, "-250");
        createStatusFrame(RESOURCE_PREFIX);

        SwingUtilities.invokeAndWait(() -> frame.setVisible(true));

        assertThat(frame.getExtendedState()).isEqualTo(0);
        assertThat(frame.getWidth()).isEqualTo(RESTORE_WIDTH);
        assertThat(frame.getHeight()).isEqualTo(getInt(HEIGHT_RESOURCE));
    }

    @Test
    public void savesMaximizedStateToSystemProperties() throws Exception {
        createStatusFrame(RESOURCE_PREFIX);
        SwingUtilities.invokeAndWait(() -> frame.setVisible(true));
        Dimension size = frame.getSize();
        SwingUtilities.invokeAndWait(() -> frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH));
        int state = frame.getExtendedState();

        SwingUtilities.invokeAndWait(() -> {
            frame.setVisible(false);
            frame.dispose();
        });

        assertThat(System.getProperty(STATE_RESOURCE)).isEqualTo(Integer.toString(state));
        assertThat(System.getProperty(WIDTH_RESOURCE)).isEqualTo(Integer.toString(size.width));
        assertThat(System.getProperty(HEIGHT_RESOURCE)).isEqualTo(Integer.toString(size.height));
    }

    @Test
    @Ignore
    public void restoresMaximizedStateFromSystemProperties() throws Exception {
        System.setProperty(STATE_RESOURCE, Integer.toString(JFrame.MAXIMIZED_BOTH));
        System.setProperty(WIDTH_RESOURCE, RESTORE_WIDTH.toString());
        System.setProperty(HEIGHT_RESOURCE, RESTORE_HEIGHT.toString());
        createStatusFrame(RESOURCE_PREFIX);

        SwingUtilities.invokeAndWait(() -> frame.setVisible(true));
        AsyncTest.timeout(SWING_TIMEOUT, () -> (frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH);

        SwingUtilities.invokeAndWait(() -> frame.setExtendedState(frame.getExtendedState() & ~JFrame.MAXIMIZED_BOTH));
        AsyncTest.timeout(SWING_TIMEOUT, () ->
                Math.abs(frame.getWidth() - RESTORE_WIDTH) < 50 && Math.abs(frame.getHeight() - RESTORE_HEIGHT) < 50);
    }

    @Test
    public void noFocusAfterDisableUI() throws Exception {
        createStatusFrame(RESOURCE_PREFIX);
        SwingUtilities.invokeAndWait(() -> frame.setVisible(true));

        SwingUtilities.invokeAndWait(() -> frame.disableUI("wait..."));
        AsyncTest.timeout(SWING_TIMEOUT, frame.getGlassPane()::isFocusOwner);
        SwingUtilities.invokeAndWait(frame::enableUI);

        AsyncTest.timeout(SWING_TIMEOUT, () -> !frame.getGlassPane().isFocusOwner());
    }

    @Test
    public void setsWaitCursorOnGlassPane() throws Exception {
        createStatusFrame(RESOURCE_PREFIX);

        assertThat(frame.getGlassPane().getCursor()).isEqualTo(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    @Test
    public void noApplicationIcons() throws Exception {
        frame = new StatusFrame(ResourceBundle.getBundle("test-resources2"), RESOURCE_PREFIX);

        assertThat(frame.getIconImages()).hasSize(0);
    }

    @Test
    public void setsApplicationIcons() throws Exception {
        createStatusFrame(RESOURCE_PREFIX);

        assertThat(frame.getIconImages()).hasSize(2);
        assertThat(getUrl(frame.getIconImages().get(0).getSource()).getFile()).endsWith("/app-small-icon.png");
        assertThat(getUrl(frame.getIconImages().get(1).getSource()).getFile()).endsWith("/app-large-icon.png");
    }

    @Test
    public void setsFrameIcons() throws Exception {
        createStatusFrame(RESOURCE_PREFIX + ".icons");

        assertThat(frame.getIconImages()).hasSize(2);
        assertThat(getUrl(frame.getIconImages().get(0).getSource()).getFile()).endsWith("/small-icon.png");
        assertThat(getUrl(frame.getIconImages().get(1).getSource()).getFile()).endsWith("/large-icon.png");
    }

    private URL getUrl(ImageProducer source) throws Exception {
        Field field = source.getClass().getDeclaredField("url");
        field.setAccessible(true);
        return (URL) field.get(source);
    }

    @Test
    public void disableUIBlocksKeyboardInput() throws Exception {
        TestAction action = createFrameWithMenuBar();
        SwingUtilities.invokeAndWait(() -> frame.setVisible(true));
        SwingUtilities.invokeAndWait(() -> frame.disableUI("wait..."));
        AsyncTest.timeout(SWING_TIMEOUT, frame.getGlassPane()::isFocusOwner);

        SwingUtilities.invokeAndWait(() -> {
            KeyEvent event = new KeyEvent(frame.getGlassPane(), KeyEvent.KEY_PRESSED, currentTimeMillis(), KeyEvent.CTRL_DOWN_MASK, KeyEvent.VK_A, KeyEvent.CHAR_UNDEFINED);
            SwingUtilities.processKeyBindings(event);
        });

        assertThat(action.actionPerformed).isFalse();
    }

    @Test
    public void disableUIWithNullMessageBlocksKeyboardInput() throws Exception {
        TestAction action = createFrameWithMenuBar();
        SwingUtilities.invokeAndWait(() -> frame.setVisible(true));
        SwingUtilities.invokeAndWait(() -> frame.disableUI(null));
        AsyncTest.timeout(SWING_TIMEOUT, frame.getGlassPane()::isFocusOwner);

        SwingUtilities.invokeAndWait(() -> {
            KeyEvent event = new KeyEvent(frame.getGlassPane(), KeyEvent.KEY_PRESSED, currentTimeMillis(), KeyEvent.CTRL_DOWN_MASK, KeyEvent.VK_A, KeyEvent.CHAR_UNDEFINED);
            SwingUtilities.processKeyBindings(event);
        });

        assertThat(action.actionPerformed).isFalse();
    }

    @Test
    public void disableUIBlocksMouseInput() throws Exception {
        createFrameWithMenuBar();
        SwingUtilities.invokeAndWait(() -> frame.setVisible(true));
        SwingUtilities.invokeAndWait(() -> frame.disableUI("wait..."));
        AsyncTest.timeout(SWING_TIMEOUT, frame.getGlassPane()::isFocusOwner);

        SwingUtilities.invokeAndWait(() -> {
            Point frameLoc = frame.getLocationOnScreen();
            Point menuLoc = frame.getJMenuBar().getLocationOnScreen();
            Point location = new Point(menuLoc.x - frameLoc.x, menuLoc.y - frameLoc.y);
            MouseEvent event = new MouseEvent(frame, MouseEvent.MOUSE_PRESSED, currentTimeMillis(), MouseEvent.BUTTON1_DOWN_MASK, location.x, location.y, 1, false, MouseEvent.BUTTON1);
            frame.dispatchEvent(event);
        });

        assertThat(frame.getJMenuBar().getMenu(0).isPopupMenuVisible()).isFalse();
    }

    private TestAction createFrameWithMenuBar() {
        createStatusFrame(RESOURCE_PREFIX);
        TestAction action = new TestAction();
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Menu");
        menu.add(action);
        menuBar.add(menu);
        frame.setJMenuBar(menuBar);
        return action;
    }

    @Test
    public void enableUIRestoresFocusOwner() throws Exception {
        JTextField field = new JTextField();
        createStatusFrame(RESOURCE_PREFIX, new JTextField(), field);
        SwingUtilities.invokeAndWait(() -> {
            frame.setVisible(true);
            field.requestFocusInWindow();
        });
        AsyncTest.timeout(SWING_TIMEOUT, field::isFocusOwner);

        SwingUtilities.invokeAndWait(() -> frame.disableUI("wait..."));
        AsyncTest.timeout(SWING_TIMEOUT, frame.getGlassPane()::isFocusOwner);
        SwingUtilities.invokeAndWait(frame::enableUI);

        AsyncTest.timeout(SWING_TIMEOUT, field::isFocusOwner);
    }

    @Test
    public void setUnsavedChangesAddsIndicatorToTitle() throws Exception {
        createStatusFrame(RESOURCE_PREFIX);
        frame.setTitle("Frame title");

        frame.setUnsavedChanges(true);
        assertThat(frame.getTitle()).matches(".* \\*$");

        frame.setUnsavedChanges(false);
        assertThat(frame.getTitle()).doesNotMatch(".* \\*$");
    }

    private void createStatusFrame(String resourcePrefix, Component... fields) {
        JPanel panel = new JPanel();
        for (Component field : fields) {
            panel.add(field);
        }
        frame = new StatusFrame(bundle, resourcePrefix);
        frame.getContentPane().add(panel);
    }

    private class TestAction extends AbstractAction {
        private boolean actionPerformed = false;

        public TestAction() {
            super("Action");
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('A', KeyEvent.CTRL_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            actionPerformed = true;
        }
    }
}