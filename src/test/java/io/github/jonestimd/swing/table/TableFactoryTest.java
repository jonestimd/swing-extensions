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
package io.github.jonestimd.swing.table;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.function.Consumer;

import javax.swing.JTable;

import io.github.jonestimd.mockito.ArgumentCaptorFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TableFactoryTest {
    @Mock
    private Consumer<MouseEvent> mouseEventConsumer;

    @Test
    public void addDoubleClickHanlder() throws Exception {
        JTable table = mock(JTable.class);
        when(table.getLocationOnScreen()).thenReturn(new Point());

        TableFactory.addDoubleClickHandler(table, mouseEventConsumer);

        ArgumentCaptor<MouseListener> captor = ArgumentCaptorFactory.create();
        verify(table).addMouseListener(captor.capture());
        MouseEvent event = new MouseEvent(table, -1, 0L, 0, 0, 0, 2, false, MouseEvent.BUTTON1);
        captor.getValue().mouseClicked(event);
        captor.getValue().mouseClicked(new MouseEvent(table, -1, 0L, 0, 0, 0, 2, false, MouseEvent.BUTTON2));
        captor.getValue().mouseClicked(new MouseEvent(table, -1, 0L, 0, 0, 0, 1, false, MouseEvent.BUTTON1));
        verify(mouseEventConsumer).accept(same(event));
        verifyNoMoreInteractions(mouseEventConsumer);
    }
}