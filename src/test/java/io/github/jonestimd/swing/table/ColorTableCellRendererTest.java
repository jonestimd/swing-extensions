// The MIT License (MIT)
//
// Copyright (c) 2019 Timothy D. Jones
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
import java.awt.Graphics2D;
import java.awt.Insets;

import javax.swing.plaf.LabelUI;

import org.junit.Before;
import org.junit.Test;

import static io.github.jonestimd.swing.table.ColorTableCellRenderer.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ColorTableCellRendererTest {
    private static final int SIZE = 20;

    private Graphics2D graphics = mock(Graphics2D.class);
    private Graphics2D scratchGraphics = mock(Graphics2D.class);
    private LabelUI ui = mock(LabelUI.class);
    private ColorTableCellRenderer renderer = new ColorTableCellRenderer();

    @Before
    public void setUp() throws Exception {
        when(graphics.create()).thenReturn(scratchGraphics);
        renderer.setSize(SIZE, SIZE);
        renderer.setUI(ui);
    }

    @Test
    public void doesNothingIfGraphicsIsNull() throws Exception {
        renderer.setValue(Color.MAGENTA);

        renderer.paintComponent(null);

        verify(ui, never()).update(null, renderer);
    }

    @Test
    public void paintsColorSwatch() throws Exception {
        renderer.setValue(Color.MAGENTA);

        renderer.paintComponent(graphics);

        verify(graphics, times(2)).create();
        verify(scratchGraphics).setColor(Color.MAGENTA);
        verify(scratchGraphics).fillRect(DEFAULT_INSET, DEFAULT_INSET, SIZE-2*DEFAULT_INSET, SIZE-2*DEFAULT_INSET);
        verify(scratchGraphics, times(2)).dispose();
        verify(ui).update(scratchGraphics, renderer);
    }

    @Test
    public void setValueClearsLabelText() throws Exception {
        renderer.setValue(Color.RED);

        assertThat(renderer.getText()).isEqualTo("");
    }

    @Test
    public void ignoresNullValue() throws Exception {
        renderer.setValue(null);

        renderer.paintComponent(graphics);

        verify(graphics).create();
        verify(scratchGraphics, never()).setColor(any());
        verify(scratchGraphics).dispose();
        verify(ui).update(scratchGraphics, renderer);
    }

    @Test
    public void ignoresNonColorValue() throws Exception {
        renderer.setValue("red");

        renderer.paintComponent(graphics);

        verify(graphics).create();
        verify(scratchGraphics, never()).setColor(any());
        verify(scratchGraphics).dispose();
        verify(ui).update(scratchGraphics, renderer);
    }

    @Test
    public void usesInsets() throws Exception {
        renderer.setValue(Color.MAGENTA);

        renderer.setInsets(new Insets(1, 2, 3, 4));
        renderer.paintComponent(graphics);

        verify(scratchGraphics).fillRect(2, 1, SIZE-2-4, SIZE-1-3);
    }
}