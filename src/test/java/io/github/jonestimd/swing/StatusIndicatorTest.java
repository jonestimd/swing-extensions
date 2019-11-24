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
package io.github.jonestimd.swing;

import java.awt.Component;

import io.github.jonestimd.swing.window.StatusFrame;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StatusIndicatorTest {
    @Mock
    private Component component;
    @Mock
    private StatusFrame frame;

    @Test
    public void returnsLoggerStatusIndicatorForNullComponent() throws Exception {
        assertThat(StatusIndicator.forComponent(null)).isSameAs(LoggerStatusIndicator.INSTANCE);
    }

    @Test
    public void returnsStatusFrameAncestor() throws Exception {
        when(component.getParent()).thenReturn(frame);

        assertThat(StatusIndicator.forComponent(component)).isSameAs(frame);
    }

    @Test
    public void returnsLoggerStatusIndicatorWhenComponentIsShowing() throws Exception {
        when(component.getParent()).thenReturn(null);
        when(component.isShowing()).thenReturn(true);

        assertThat(StatusIndicator.forComponent(component)).isSameAs(LoggerStatusIndicator.INSTANCE);
    }

    @Test
    public void returnsDeferredStatusIndicatorWhenComponentIsNotShowing() throws Exception {
        when(component.getParent()).thenReturn(null);
        when(component.isShowing()).thenReturn(false);

        assertThat(StatusIndicator.forComponent(component)).isInstanceOf(DeferredStatusIndicator.class);
    }
}