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
package io.github.jonestimd.swing.border;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthStyle;
import javax.swing.plaf.synth.SynthTextFieldUI;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OblongBorderTest {
    private static final int X = 0;
    private static final int Y = 1;
    private static final int WIDTH = 20;
    private static final int HEIGHT = 10;
    private static final int LINE_WIDTH = 2;

    @Mock
    private Graphics g;
    @Mock
    private Graphics2D g2d;
    @Mock
    private Component component;
    @Mock
    private SynthTextFieldUI synthUI;
    @Mock
    private SynthContext context;
    @Mock
    private SynthStyle style;
    @Captor
    private ArgumentCaptor<Shape> shapeCaptor;

    @Test
    public void paint() throws Exception {
        when(g.create(anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(g2d);
        when(component.getBackground()).thenReturn(Color.gray);

        new OblongBorder(LINE_WIDTH, Color.black).paintBorder(component, g, X, Y, WIDTH, HEIGHT);

        verify(g).create(X, Y, WIDTH, HEIGHT);
        verify(g2d).setStroke(new BasicStroke(LINE_WIDTH));
        verify(g2d).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        verify(g2d).setColor(Color.black);
        verify(g2d).draw(isA(RoundRectangle2D.Double.class));
        verify(g2d).dispose();
    }

    @Test
    public void paintUsesParentBackgroundForTextField() throws Exception {
        final JPanel parent = new JPanel();
        parent.setBackground(Color.RED);
        final JTextField textField = new JTextField();
        parent.add(textField);
        when(g.create(anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(g2d);

        new OblongBorder(LINE_WIDTH, Color.black).paintBorder(textField, g, X, Y, WIDTH, HEIGHT);

        verify(g).create(X, Y, WIDTH, HEIGHT);
        verify(g2d).setStroke(new BasicStroke(LINE_WIDTH));
        verify(g2d).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        verify(g2d).setColor(parent.getBackground());
        verify(g2d, times(2)).setClip(shapeCaptor.capture());
        assertThat(shapeCaptor.getAllValues().get(0)).isInstanceOfAny(Area.class);
        assertThat(shapeCaptor.getAllValues().get(1)).isNull();
        verify(g2d).fillRect(X, Y, WIDTH, HEIGHT);
        verify(g2d).setColor(Color.black);
        verify(g2d).draw(isA(RoundRectangle2D.Double.class));
        verify(g2d).dispose();
    }

    @Test
    public void insetsUsesLineWidth() throws Exception {
        OblongBorder border = new OblongBorder(LINE_WIDTH, Color.black);

        assertThat(border.getBorderInsets(null)).isEqualTo(new Insets(LINE_WIDTH+1, LINE_WIDTH, LINE_WIDTH+1, LINE_WIDTH));
    }

    @Test
    public void isBorderOpaqueReturnsTrue() throws Exception {
        assertThat(new OblongBorder(2, Color.black).isBorderOpaque()).isFalse();
    }
}