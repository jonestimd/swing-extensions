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
package io.github.jonestimd.swing.action;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import io.github.jonestimd.AsyncTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class DialogActionTest {
    private final ResourceBundle bundle = ResourceBundle.getBundle("test-resources");
    private boolean loaded = false;
    private boolean saved = false;
    private boolean setResultOnUI = false;

    @Test
    public void actionPerformedNoDialog() throws Exception {
        final int threads = Thread.currentThread().getThreadGroup().activeCount();
        TestAction action = new TestAction(false);

        SwingUtilities.invokeAndWait(() -> action.actionPerformed(new ActionEvent(new JPanel(), -1, null)));

        AsyncTest.waitForThreadCount(threads);
        assertThat(loaded).isTrue();
        assertThat(saved).isFalse();
        assertThat(setResultOnUI).isFalse();
    }

    @Test
    public void actionPerformedWithDialog() throws Exception {
        final int threads = Thread.currentThread().getThreadGroup().activeCount();
        TestAction action = new TestAction(true);

        SwingUtilities.invokeAndWait(() -> action.actionPerformed(new ActionEvent(new JPanel(), -1, null)));

        AsyncTest.waitForThreadCount(threads);
        assertThat(loaded).isTrue();
        AsyncTest.timeout(10000L, () -> saved && setResultOnUI);
        assertThat(saved).isTrue();
        assertThat(setResultOnUI).isTrue();
    }

    private class TestAction extends DialogAction {
        private final boolean displayDialog;

        public TestAction(boolean displayDialog) {
            super(bundle, "dialogActionTest");
            this.displayDialog = displayDialog;
        }
        @Override
        protected void loadDialogData() {
            loaded = true;
        }

        @Override
        protected boolean displayDialog(JComponent owner) {
            return displayDialog;
        }

        @Override
        protected void saveDialogData() {
            saved = true;
        }

        @Override
        protected void setSaveResultOnUI() {
            setResultOnUI = true;
        }
    }
}