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
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.function.Consumer;

import io.github.jonestimd.swing.window.StatusFrame;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static java.awt.event.HierarchyEvent.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class VisibilityHandlerTest {
    @Mock
    private Component component;
    @Mock
    private StatusFrame frame;
    @Mock
    private Consumer<Component> callback;
    @Captor
    private ArgumentCaptor<HierarchyListener> listenerCaptor;

    @Test
    public void addCallbackDoesNothingIfComponentIsShowing() throws Exception {
        when(component.isShowing()).thenReturn(true);

        VisibilityHandler.addCallback(component, callback);

        verify(component, never()).addHierarchyListener(any());
    }

    @Test
    public void addCallbackAddsTheCallbackIfComponentIsNotShowing() throws Exception {
        when(component.isShowing()).thenReturn(false, false, true);

        VisibilityHandler.addCallback(component, callback);

        verify(component).addHierarchyListener(listenerCaptor.capture());
        HierarchyEvent event = new HierarchyEvent(component, SHOWING_CHANGED, component, frame);
        listenerCaptor.getValue().hierarchyChanged(event);
        listenerCaptor.getValue().hierarchyChanged(event);
        verify(callback).accept(component);
    }
}