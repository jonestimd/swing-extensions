// The MIT License (MIT)
//
// Copyright (c) 2020 Timothy D. Jones
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
import io.github.jonestimd.swing.JFrameRobotTest;
import io.github.jonestimd.swing.dialog.ExceptionDialog;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class DialogActionTest extends JFrameRobotTest {
    private final ResourceBundle bundle = ResourceBundle.getBundle("test-resources");
    private boolean loaded = false;
    private boolean saved = false;
    private boolean setResultOnUI = false;

    protected JPanel createContentPane() {
        return new JPanel();
    }

    @Test
    public void actionPerformedNoDialog() throws Exception {
        loaded = false;
        TestAction action = new TestAction(false);

        SwingUtilities.invokeAndWait(() -> action.actionPerformed(new ActionEvent(new JPanel(), -1, null)));

        AsyncTest.timeout(5000L, () -> loaded);
        assertThat(saved).isFalse();
        assertThat(setResultOnUI).isFalse();
    }

    @Test
    public void actionPerformedWithDialog() throws Exception {
        loaded = false;
        TestAction action = new TestAction(true);

        SwingUtilities.invokeAndWait(() -> action.actionPerformed(new ActionEvent(new JPanel(), -1, null)));

        AsyncTest.timeout(5000L, () -> loaded);
        AsyncTest.timeout(5000L, () -> saved && setResultOnUI);
    }

    @Test
    public void errorOnLoadDisplaysExceptionDialog() throws Exception {
        TestAction action = new TestAction(true) {
            @Override
            protected void loadDialogData() {
                super.loadDialogData();
                throw new RuntimeException("load failed");
            }
        };

        SwingUtilities.invokeAndWait(() -> action.actionPerformed(new ActionEvent(new JPanel(), -1, null)));

        AsyncTest.timeout(5000L, () -> loaded);
        robot.finder().findByType(ExceptionDialog.class).dispose();
    }

    @Test
    public void errorOnSaveDisplaysExceptionDialog() throws Exception {
        TestAction action = new TestAction(true) {
            @Override
            protected void saveDialogData() {
                super.saveDialogData();
                throw new RuntimeException("load failed");
            }
        };

        SwingUtilities.invokeAndWait(() -> action.actionPerformed(new ActionEvent(new JPanel(), -1, null)));

        AsyncTest.timeout(5000L, () -> loaded);
        AsyncTest.timeout(5000L, () -> saved);
        assertThat(setResultOnUI).isFalse();
        robot.finder().findByType(ExceptionDialog.class).dispose();
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