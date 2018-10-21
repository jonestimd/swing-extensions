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
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;

import javax.swing.Action;
import javax.swing.KeyStroke;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ActionAdapterTest {
    private final ResourceBundle bundle = ResourceBundle.getBundle("test-resources");

    @Mock
    private ActionListener listener;

    @Test
    public void actionPerformedCallsActionListener() throws Exception {
        final ActionEvent event = new ActionEvent(this, 0, "command");
        Action action = ActionAdapter.forMnemonicAndName(listener, "AAction");

        action.actionPerformed(event);

        verify(listener).actionPerformed(same(event));
        assertThat(action.getValue(Action.MNEMONIC_KEY)).isEqualTo((int) 'A');
        assertThat(action.getValue(Action.NAME)).isEqualTo("Action");
    }

    @Test
    public void initializeFromResourceBundle() throws Exception {
        ActionAdapter action = new ActionAdapter(listener, bundle, "testAction");

        assertThat(action.getValue(Action.MNEMONIC_KEY)).isEqualTo((int) 'T');
        assertThat(action.getValue(Action.NAME)).isEqualTo("Test Action");
        assertThat(action.getValue(Action.ACCELERATOR_KEY)).isEqualTo(KeyStroke.getKeyStroke('T', KeyEvent.CTRL_DOWN_MASK));
        assertThat(action.getValue(Action.SMALL_ICON)).isNull();
    }

    @Test
    public void iconOnlyAction() throws Exception {
        ActionAdapter action = new ActionAdapter(listener, bundle, "iconAction");

        assertThat(action.getValue(Action.MNEMONIC_KEY)).isNull();
        assertThat(action.getValue(Action.NAME)).isNull();
        assertThat(action.getValue(Action.ACCELERATOR_KEY)).isNull();
        assertThat(action.getValue(Action.SMALL_ICON).toString()).endsWith("/small-icon.png");
    }
}