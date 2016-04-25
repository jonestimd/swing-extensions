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

import java.util.function.Consumer;

import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DocumentConsumerAdapterTest {
    public static final String TEXT = "text";
    @Mock
    private DocumentEvent event;
    @Mock
    private Document document;
    @Mock
    private Consumer<String> handler;
    @InjectMocks
    private DocumentConsumerAdapter adapter;

    @Test
    public void insertUpdateCallsHandler() throws Exception {
        when(event.getDocument()).thenReturn(document);
        when(document.getText(eq(0), anyInt())).thenReturn(TEXT);

        adapter.insertUpdate(event);

        verify(handler).accept(TEXT);
        verify(document).getLength();
    }

    @Test
    public void changedUpdateCallsHandler() throws Exception {
        when(event.getDocument()).thenReturn(document);
        when(document.getText(eq(0), anyInt())).thenReturn(TEXT);

        adapter.changedUpdate(event);

        verify(handler).accept(TEXT);
        verify(document).getLength();
    }

    @Test
    public void removeUpdateCallsHandler() throws Exception {
        when(event.getDocument()).thenReturn(document);
        when(document.getText(eq(0), anyInt())).thenReturn(TEXT);

        adapter.removeUpdate(event);

        verify(handler).accept(TEXT);
        verify(document).getLength();
    }

    @Test(expected = RuntimeException.class)
    public void rethrowsException() throws Exception {
        when(event.getDocument()).thenReturn(document);
        when(document.getText(eq(0), anyInt())).thenThrow(new BadLocationException("test", -1));

        adapter.removeUpdate(event);
    }
}