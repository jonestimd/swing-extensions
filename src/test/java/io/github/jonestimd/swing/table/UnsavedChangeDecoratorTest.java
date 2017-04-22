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

import java.awt.Color;
import java.awt.Font;
import java.awt.font.TextAttribute;

import javax.swing.JComponent;

import io.github.jonestimd.swing.table.model.ChangeBufferTableModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UnsavedChangeDecoratorTest {
    @Mock
    private DecoratedTable<Object, ChangeBufferTableModel<Object>> table;
    @Mock
    private ChangeBufferTableModel<Object> model;
    @Mock
    private JComponent renderer;
    private UnsavedChangeDecorator decorator = new UnsavedChangeDecorator();

    @Test
    public void setsStrikeoutFontForPendingDelete() throws Exception {
        final Font font = Font.decode(Font.MONOSPACED);
        when(table.getModel()).thenReturn(model);
        when(model.isPendingDelete(0)).thenReturn(true);
        when(renderer.getBackground()).thenReturn(Color.lightGray);
        when(renderer.getFont()).thenReturn(font);

        decorator.prepareRenderer(table, renderer, 0, 0);

        verify(renderer).setBackground(new Color(187, 129, 129));
        ArgumentCaptor<Font> captor = ArgumentCaptor.forClass(Font.class);
        verify(renderer).setFont(captor.capture());
        assertThat(captor.getValue().getAttributes().get(TextAttribute.STRIKETHROUGH)).isEqualTo(TextAttribute.STRIKETHROUGH_ON);
    }

    @Test
    public void setsBackgroundForPendingChange() throws Exception {
        when(table.getModel()).thenReturn(model);
        when(model.isPendingDelete(0)).thenReturn(false);
        when(model.isChangedAt(0, 0)).thenReturn(true);
        when(renderer.getBackground()).thenReturn(Color.lightGray);

        decorator.prepareRenderer(table, renderer, 0, 0);

        verify(renderer).setBackground(new Color(24, 190, 190));
        verify(renderer, never()).setFont(any(Font.class));
    }
}