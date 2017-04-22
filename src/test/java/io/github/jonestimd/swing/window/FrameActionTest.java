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

import java.util.ResourceBundle;

import javax.swing.Action;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FrameActionTest {
    private enum TestWindowInfo implements WindowInfo {
        Singleton, MultiFrame;

        @Override
        public boolean isSingleton() {
            return this == Singleton;
        }

        @Override
        public String getResourcePrefix() {
            return "window." + name().toLowerCase();
        }
    }
    @Mock
    private WindowEventPublisher<TestWindowInfo> eventPublisher;

    @Test
    public void initializeFromBundle() throws Exception {
        ApplicationWindowEvent<TestWindowInfo> event = new ApplicationWindowEvent<>(this, TestWindowInfo.MultiFrame);
        ResourceBundle bundle = ResourceBundle.getBundle("test-resources");

        FrameAction<TestWindowInfo> action = new FrameAction<>(bundle, "FrameActionTest", eventPublisher, event);

        assertThat(action.getValue(Action.MNEMONIC_KEY)).isEqualTo((int) 'N');
        assertThat(action.getValue(Action.NAME)).isEqualTo("New Frame");
    }

    @Test
    public void actionPerformedPublishesEvent() throws Exception {
        ApplicationWindowEvent<TestWindowInfo> event = new ApplicationWindowEvent<>(this, TestWindowInfo.MultiFrame);
        FrameAction<TestWindowInfo> action = new FrameAction<>("NNew Window", null, eventPublisher, event);

        action.actionPerformed(null);

        verify(eventPublisher).publishEvent(same(event));
    }
}