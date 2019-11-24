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

import io.github.jonestimd.swing.window.StatusFrame;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DeferredStatusIndicatorTest {
    @Mock
    private Component component;
    @Mock
    private StatusFrame frame;
    @Captor
    private ArgumentCaptor<HierarchyListener> listenerCaptor;

    private HierarchyEvent newEvent(boolean isShowing, StatusFrame parent) {
        when(component.isShowing()).thenReturn(isShowing);
        when(component.getParent()).thenReturn(parent);
        return new HierarchyEvent(component, HierarchyEvent.SHOWING_CHANGED, component, frame);
    }

    @Test
    public void addsVisibilityHandler() throws Exception {
        new DeferredStatusIndicator(component);

        verify(component).addHierarchyListener(listenerCaptor.capture());
        assertThat(listenerCaptor.getValue()).isInstanceOf(VisibilityHandler.class);
    }

    @Test
    public void displaysStatusMessageWhenAlreadyShowing() throws Exception {
        String message = "message";
        DeferredStatusIndicator indicator = new DeferredStatusIndicator(component);
        verify(component).addHierarchyListener(listenerCaptor.capture());
        listenerCaptor.getValue().hierarchyChanged(newEvent(true, frame));

        indicator.setStatusMessage(message);

        verify(frame).setStatusMessage(message);
    }

    @Test
    public void savesStatusMessageUntilShowing() throws Exception {
        String message = "message";
        DeferredStatusIndicator indicator = new DeferredStatusIndicator(component);
        verify(component).addHierarchyListener(listenerCaptor.capture());

        indicator.setStatusMessage(message);

        listenerCaptor.getValue().hierarchyChanged(newEvent(true, frame));
        verify(frame).setStatusMessage(message);
    }

    @Test
    public void disablesUIWhenAlreadyShowing() throws Exception {
        String message = "message";
        DeferredStatusIndicator indicator = new DeferredStatusIndicator(component);
        verify(component).addHierarchyListener(listenerCaptor.capture());
        listenerCaptor.getValue().hierarchyChanged(newEvent(true, frame));

        indicator.disableUI(message);

        verify(frame).disableUI(message);
    }

    @Test
    public void savesDisabledStateUntilShowing() throws Exception {
        String message = "message";
        DeferredStatusIndicator indicator = new DeferredStatusIndicator(component);
        verify(component).addHierarchyListener(listenerCaptor.capture());

        indicator.disableUI(message);

        listenerCaptor.getValue().hierarchyChanged(newEvent(true, frame));
        verify(frame).disableUI(message);
    }

    @Test
    public void enablesUIWhenAlreadyShowing() throws Exception {
        DeferredStatusIndicator indicator = new DeferredStatusIndicator(component);
        verify(component).addHierarchyListener(listenerCaptor.capture());
        listenerCaptor.getValue().hierarchyChanged(newEvent(true, frame));

        indicator.enableUI();

        verify(frame).enableUI();
    }

    @Test
    public void savesEnabledStateUntilShowing() throws Exception {
        String message = "message";
        DeferredStatusIndicator indicator = new DeferredStatusIndicator(component);
        verify(component).addHierarchyListener(listenerCaptor.capture());
        indicator.disableUI(message);

        indicator.enableUI();

        listenerCaptor.getValue().hierarchyChanged(newEvent(true, frame));
        verify(frame).setStatusMessage(message);
        verifyNoMoreInteractions(frame);
    }
}