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
package io.github.jonestimd.swing;

import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import io.github.jonestimd.swing.dialog.ExceptionDialog;
import io.github.jonestimd.swing.window.StatusFrame;
import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.Robot;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BackgroundTaskTest {
    private static final String THE_STRING = "the String";
    private static final String STATUS_MESSAGE = "working";
    @Mock
    private Supplier<String> supplier;
    @Mock
    private Consumer<String> consumer;
    @Mock
    private BackgroundTask<String> task;
    private CompletableFuture<String> future;
    private String threadName;

    private StatusFrame window;
    private Robot robot;

    @Before
    public void createRobot() throws Exception {
        robot = BasicRobot.robotWithNewAwtHierarchy();
    }

    @After
    public void cleanUp() throws Exception {
        robot.cleanUp();
        if (window != null && window.isVisible()) SwingUtilities.invokeAndWait(window::dispose);
    }

    @Test
    public void handleExceptionDefaultsToFalse() throws Exception {
        assertThat(new BackgroundTask<String>() {
            @Override
            public String getStatusMessage() {
                return null;
            }

            @Override
            public String performTask() {
                return null;
            }

            @Override
            public void updateUI(String result) {
            }
        }.handleException(null)).isFalse();
    }

    @Test
    public void taskStatusMessageDefaultsToNull() throws Exception {
        BackgroundTask<String> task = BackgroundTask.task(supplier, consumer);

        assertThat(task.getStatusMessage()).isNull();
    }

    @Test
    public void taskReturnsStatusMessage() throws Exception {
        BackgroundTask<String> task = BackgroundTask.task(STATUS_MESSAGE, supplier, consumer);

        assertThat(task.getStatusMessage()).isEqualTo(STATUS_MESSAGE);
    }

    @Test
    public void taskCallsSupplier() throws Exception {
        BackgroundTask<String> task = BackgroundTask.task(STATUS_MESSAGE, supplier, consumer);

        task.performTask();

        verify(supplier).get();
    }

    @Test
    public void taskCallsConsumer() throws Exception {
        BackgroundTask<String> task = BackgroundTask.task(STATUS_MESSAGE, supplier, consumer);

        task.updateUI(THE_STRING);

        verify(consumer).accept(THE_STRING);
    }

    @Test
    public void runStartsTaskOnNewThread() throws Exception {
        TestStatusIndicator indicator = mock(TestStatusIndicator.class);
        when(task.getStatusMessage()).thenReturn(STATUS_MESSAGE);
        when(task.performTask()).thenAnswer(invocation -> {
            threadName = Thread.currentThread().getName();
            return THE_STRING;
        });

        SwingUtilities.invokeAndWait(() -> future = BackgroundTask.run(task, indicator, new JPanel()));

        assertThat(future.get()).isEqualTo(THE_STRING);
        verify(indicator, timeout(1000)).disableUI(STATUS_MESSAGE);
        verify(indicator, timeout(1000)).enableUI();
        verify(task, timeout(1000)).updateUI(THE_STRING);
        assertThat(threadName).startsWith("ForkJoinPool.");
    }

    @Test
    public void runPassesExceptionToTask() throws Exception {
        TestStatusIndicator indicator = mock(TestStatusIndicator.class);
        when(task.getStatusMessage()).thenReturn(STATUS_MESSAGE);
        RuntimeException exception = new RuntimeException("task failed");
        when(task.performTask()).thenThrow(exception);
        when(task.handleException(any())).thenReturn(true);

        SwingUtilities.invokeAndWait(() -> future = BackgroundTask.run(task, indicator, new JPanel()));

        try {
            future.get();
            Assert.fail("expected an exception");
        } catch (ExecutionException ex) {
            assertThat(ex.getCause()).isSameAs(exception);
            verify(indicator, timeout(1000)).disableUI(STATUS_MESSAGE);
            verify(indicator, timeout(1000)).enableUI();
            verify(task, timeout(1000)).handleException(same(exception));
        }
    }

    @Test
    public void runUsesParentStatusIndicator() throws Exception {
        TestStatusIndicator indicator = mock(TestStatusIndicator.class);
        JComponent owner = mock(JComponent.class);
        when(owner.getParent()).thenReturn(indicator);
        when(task.getStatusMessage()).thenReturn(STATUS_MESSAGE);
        when(task.performTask()).thenAnswer(invocation -> THE_STRING);

        SwingUtilities.invokeAndWait(() -> future = BackgroundTask.run(task, owner));

        assertThat(future.get()).isEqualTo(THE_STRING);
        verify(indicator, timeout(1000)).disableUI(STATUS_MESSAGE);
        verify(indicator, timeout(1000)).enableUI();
        verify(task, timeout(1000)).updateUI(THE_STRING);
    }

    @Test
    public void displaysErrorFromSupplier() throws Exception {
        String message = "error loading data";
        when(supplier.get()).thenThrow(new RuntimeException(message));
        BackgroundTask<String> task = BackgroundTask.task("Loading ...", supplier, consumer);
        showWindow();

        SwingUtilities.invokeAndWait(() -> task.run(window));

        waitForEnableUI();
        verify(supplier, timeout(1000)).get();
        ExceptionDialog dialog = robot.finder().findByType(ExceptionDialog.class);
        JTextArea textArea = robot.finder().findByType(dialog.getContentPane(), JTextArea.class);
        assertThat(textArea.getText()).contains(message);
        SwingUtilities.invokeAndWait(dialog::dispose);
        verifyZeroInteractions(consumer);
    }

    @Test
    public void displaysErrorFromConsumer() throws Exception {
        String message = "error showing data";
        when(supplier.get()).thenReturn("result");
        doThrow(new RuntimeException(message)).when(consumer).accept(any());
        BackgroundTask<String> task = BackgroundTask.task("Loading ...", supplier, consumer);
        showWindow();

        SwingUtilities.invokeAndWait(() -> task.run(window));

        verify(supplier, timeout(1000)).get();
        verify(consumer, timeout(1000)).accept(any());
        ExceptionDialog dialog = robot.finder().findByType(ExceptionDialog.class);
        JTextArea textArea = robot.finder().findByType(dialog.getContentPane(), JTextArea.class);
        assertThat(textArea.getText()).contains(message);
        SwingUtilities.invokeAndWait(dialog::dispose);
        waitForEnableUI();
    }

    private void showWindow() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            window = new StatusFrame(ResourceBundle.getBundle("test-resources"), "StatusFrameTest");
            window.pack();
            window.setVisible(true);
        });
    }

    private void waitForEnableUI() {
        long deadline = System.currentTimeMillis() + 1000;
        while (window.getGlassPane().isVisible() && System.currentTimeMillis() < deadline) Thread.yield();
        if (window.getGlassPane().isVisible()) fail("timed out waiting for window to be enabled");
    }

    private static abstract class TestStatusIndicator extends JComponent implements StatusIndicator {}
}