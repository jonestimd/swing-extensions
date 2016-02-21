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

import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BackgroundRunnerTest {
    private static final String THE_STRING = "the String";
    public static final String STATUS_MESSAGE = "working";
    @Mock
    private Consumer<String> updateUi;
    @Mock
    private BackgroundTask<String> task;

    private String threadName;

    @Test
    public void doTaskStartsNewThreadIfOnEventThread() throws Exception {
        TestStatusIndicator indicator = mock(TestStatusIndicator.class);
        BackgroundRunner<String> runner = new BackgroundRunner<>(BackgroundTask.task(STATUS_MESSAGE, this::getThreadName, updateUi), indicator);

        SwingUtilities.invokeAndWait(runner::doTask);

        verify(indicator, timeout(1000)).disableUI(STATUS_MESSAGE);
        verify(indicator, timeout(1000)).enableUI();
        verify(updateUi, timeout(1000)).accept(THE_STRING);
        assertThat(threadName).isEqualTo("BackgroundTask");
    }

    @Test
    public void usesParentStatusIndicator() throws Exception {
        TestStatusIndicator indicator = mock(TestStatusIndicator.class);
        JComponent owner = mock(JComponent.class);
        when(owner.getParent()).thenReturn(indicator);
        BackgroundRunner<String> runner = new BackgroundRunner<>(BackgroundTask.task(STATUS_MESSAGE, this::getThreadName, updateUi), owner);

        SwingUtilities.invokeAndWait(runner::doTask);

        verify(indicator, timeout(1000)).disableUI(STATUS_MESSAGE);
        verify(indicator, timeout(1000)).enableUI();
        verify(updateUi, timeout(1000)).accept(THE_STRING);
        assertThat(threadName).isEqualTo("BackgroundTask");
    }

    @Test
    public void usesExplicitStatusIndicator() throws Exception {
        TestStatusIndicator indicator = mock(TestStatusIndicator.class);
        JComponent owner = mock(JComponent.class);
        BackgroundRunner<String> runner = new BackgroundRunner<>(BackgroundTask.task(STATUS_MESSAGE, this::getThreadName, updateUi), owner, indicator);

        SwingUtilities.invokeAndWait(runner::doTask);

        verify(indicator, timeout(1000)).disableUI(STATUS_MESSAGE);
        verify(indicator, timeout(1000)).enableUI();
        verify(updateUi, timeout(1000)).accept(THE_STRING);
        assertThat(threadName).isEqualTo("BackgroundTask");
    }

    @Test
    public void displaysStatusMessageWhenAddedToParentBeforeStartingTheTask() throws Exception {
        TestStatusIndicator indicator = mock(TestStatusIndicator.class);
        JComponent owner = mock(JComponent.class);
        when(owner.isShowing()).thenReturn(true);
        when(owner.getParent()).thenReturn(null, indicator);
        ArgumentCaptor<HierarchyListener> listenerCaptor = ArgumentCaptor.forClass(HierarchyListener.class);
        BackgroundRunner<String> runner = new BackgroundRunner<>(BackgroundTask.task(STATUS_MESSAGE, this::getThreadName, updateUi), owner);
        verify(owner).addHierarchyListener(listenerCaptor.capture());
        listenerCaptor.getValue().hierarchyChanged(new HierarchyEvent(owner, -1, owner, indicator));

        SwingUtilities.invokeAndWait(runner::doTask);

        verify(owner, times(2)).getParent();
        verify(indicator, timeout(1000)).disableUI(STATUS_MESSAGE);
        verify(indicator, timeout(1000)).enableUI();
        verify(updateUi, timeout(1000)).accept(THE_STRING);
        assertThat(threadName).isEqualTo("BackgroundTask");
    }

    @Test
    public void displaysStatusMessageWhenAddedToParentAfterStartingTheTask() throws Exception {
        TestStatusIndicator indicator = mock(TestStatusIndicator.class);
        JComponent owner = mock(JComponent.class);
        when(owner.isShowing()).thenReturn(true);
        when(owner.getParent()).thenReturn(null, indicator);
        ArgumentCaptor<HierarchyListener> listenerCaptor = ArgumentCaptor.forClass(HierarchyListener.class);
        BackgroundRunner<String> runner = new BackgroundRunner<>(BackgroundTask.task(STATUS_MESSAGE, createSupplier(100L), updateUi), owner);
        verify(owner).addHierarchyListener(listenerCaptor.capture());

        SwingUtilities.invokeLater(runner::doTask);
        listenerCaptor.getValue().hierarchyChanged(new HierarchyEvent(owner, -1, owner, indicator));

        verify(owner, times(2)).getParent();
        verify(indicator, timeout(1000)).disableUI(STATUS_MESSAGE);
        verify(indicator, timeout(1000)).enableUI();
        verify(updateUi, timeout(1000)).accept(THE_STRING);
        assertThat(threadName).isEqualTo("BackgroundTask");
    }

    @Test
    public void doTaskUsesCurrentThreadIfNotOnEventThread() throws Exception {
        BackgroundRunner<String> runner = new BackgroundRunner<>(BackgroundTask.task(this::getThreadName, updateUi));

        runner.doTask();

        verify(updateUi, timeout(1000)).accept(THE_STRING);
        assertThat(threadName).isEqualTo(Thread.currentThread().getName());
    }

    @Test
    public void doTaskCallsHandleException() throws Exception {
        final RuntimeException exception = new RuntimeException();
        when(task.getStatusMessage()).thenReturn(STATUS_MESSAGE);
        when(task.performTask()).thenThrow(exception);
        when(task.handleException(any())).thenReturn(true);
        BackgroundRunner<String> runner = new BackgroundRunner<>(task);

        runner.doTask();

        verify(task, timeout(1000)).performTask();
        verify(task, timeout(1000)).handleException(exception);
    }

    private Supplier<String> createSupplier(long delay) {
        return () -> {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return getThreadName();
        };
    }

    private String getThreadName() {
        threadName = Thread.currentThread().getName();
        return THE_STRING;
    }

    private static abstract class TestStatusIndicator extends JComponent implements StatusIndicator {}
}