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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WindowEventPublisherTest {
    private enum TestWindowInfo implements WindowInfo {
        MultiFrame;

        @Override
        public boolean isSingleton() {
            return false;
        }

        @Override
        public String getResourcePrefix() {
            return null;
        }
    }
    @Mock
    private WindowEventListener<TestWindowInfo> listener1;
    @Mock
    private WindowEventListener<TestWindowInfo> listener2;

    private WindowEventPublisher<TestWindowInfo> publisher = new WindowEventPublisher<>();

    @Test
    public void publishesEventsToRegisteredListeners() throws Exception {
        final ApplicationWindowEvent<TestWindowInfo> event = new ApplicationWindowEvent<>(this, TestWindowInfo.MultiFrame);
        publisher.register(listener1);
        publisher.register(listener2);
        publisher.unregister(listener2);

        publisher.publishEvent(event);

        verify(listener1).onWindowEvent(same(event));
        verify(listener2, never()).onWindowEvent(same(event));
    }
}