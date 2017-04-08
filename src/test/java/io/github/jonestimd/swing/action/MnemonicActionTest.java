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
package io.github.jonestimd.swing.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;

import javax.swing.Action;
import javax.swing.KeyStroke;

import org.junit.Test;

import static org.fest.assertions.Assertions.*;
import static org.mockito.Mockito.*;

public class MnemonicActionTest {
    private final ResourceBundle bundle = ResourceBundle.getBundle("test-resources");

    @Test
    public void forListenerUsesActionListener() throws Exception {
        ActionEvent event = new ActionEvent(this, -1, "command");
        ActionListener listener = mock(ActionListener.class);
        MnemonicAction action = MnemonicAction.forListener(listener, bundle, "testAction");

        action.actionPerformed(event);

        assertThat(action.getValue(Action.MNEMONIC_KEY)).isEqualTo((int) 'T');
        assertThat(action.getValue(Action.NAME)).isEqualTo("Test Action");
        assertThat(action.getValue(Action.ACCELERATOR_KEY)).isEqualTo(KeyStroke.getKeyStroke('T', KeyEvent.CTRL_DOWN_MASK));
        verify(listener).actionPerformed(event);
    }
}