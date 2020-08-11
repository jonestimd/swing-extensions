// The MIT License (MIT)
//
// Copyright (c) 2020 Timothy D. Jones
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
package io.github.jonestimd.swing.component;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Insets;

import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import io.github.jonestimd.swing.component.AbstractButtonBorder.Side;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ClearButtonBorderTest {
    private static final int LONG_SIDE = 75;
    private static final int SHORT_SIDE = 16;

    @Test
    public void setHighlightBorder() throws Exception {
        ClearButtonBorder border = new ClearButtonBorder();

        assertThat(border.isHighlightButton()).isFalse();
        border.setHighlightButton(true);
        assertThat(border.isHighlightButton()).isTrue();
    }

    @Test
    public void isBorderOpaque() throws Exception {
        assertThat(new ClearButtonBorder().isBorderOpaque()).isFalse();
    }

    @Test
    public void getBorderInsets_handlesComponent() throws Exception {
        Component field = mock(Component.class);
        when(field.getWidth()).thenReturn(LONG_SIDE);
        when(field.getHeight()).thenReturn(SHORT_SIDE);

        Insets insets = new ClearButtonBorder().getBorderInsets(field);

        assertThat(insets).isEqualTo(new Insets(0, 0, 0, SHORT_SIDE + 2));
    }

    @Test
    public void getBorderInsets_left_landscape_usesHeight() throws Exception {
        JTextField field = mock(JTextField.class);
        when(field.getWidth()).thenReturn(LONG_SIDE);
        when(field.getHeight()).thenReturn(SHORT_SIDE);

        Insets insets = new ClearButtonBorder(Side.LEFT).getBorderInsets(field);

        assertThat(insets).isEqualTo(new Insets(0, SHORT_SIDE + 2, 0, 0));
    }

    @Test
    public void getBorderInsets_right_landscape_usesHeight() throws Exception {
        JTextField field = mock(JTextField.class);
        when(field.getWidth()).thenReturn(LONG_SIDE);
        when(field.getHeight()).thenReturn(SHORT_SIDE);

        Insets insets = new ClearButtonBorder().getBorderInsets(field);

        assertThat(insets).isEqualTo(new Insets(0, 0, 0, SHORT_SIDE + 2));
    }

    @Test
    public void getBorderInsets_right_portrait_usesWidth() throws Exception {
        JTextField field = mock(JTextField.class);
        when(field.getWidth()).thenReturn(SHORT_SIDE);
        when(field.getHeight()).thenReturn(LONG_SIDE);

        Insets insets = new ClearButtonBorder().getBorderInsets(field);

        assertThat(insets).isEqualTo(new Insets(0, 0, 0, SHORT_SIDE));
    }

    @Test
    public void getBorderInsets_landscape_usesInsetsFromOuterBorder() throws Exception {
        ClearButtonBorder border = new ClearButtonBorder();
        CompoundBorder compoundBorder = new CompoundBorder(new EmptyBorder(1, 0, 1, 0), border);
        JTextField field = mock(JTextField.class);
        when(field.getWidth()).thenReturn(LONG_SIDE);
        when(field.getHeight()).thenReturn(SHORT_SIDE);
        when(field.getBorder()).thenReturn(compoundBorder);

        Insets insets = border.getBorderInsets(field);

        assertThat(insets).isEqualTo(new Insets(0, 0, 0, SHORT_SIDE));
    }

    @Test
    public void getBorderInsets_portrait_usesInsetsFromOuterBorder() throws Exception {
        ClearButtonBorder border = new ClearButtonBorder();
        CompoundBorder compoundBorder = new CompoundBorder(new EmptyBorder(0, 1, 0, 1), border);
        JTextField field = mock(JTextField.class);
        when(field.getWidth()).thenReturn(SHORT_SIDE);
        when(field.getHeight()).thenReturn(LONG_SIDE);
        when(field.getBorder()).thenReturn(compoundBorder);

        Insets insets = border.getBorderInsets(field);

        assertThat(insets).isEqualTo(new Insets(0, 0, 0, SHORT_SIDE - 2));
    }

    @Test
    public void getBorderInsets_ignoresSelfAsOuterBorder() throws Exception {
        ClearButtonBorder border = new ClearButtonBorder();
        CompoundBorder compoundBorder = new CompoundBorder(border, new EmptyBorder(1, 0, 1, 0));
        JTextField field = mock(JTextField.class);
        when(field.getWidth()).thenReturn(LONG_SIDE);
        when(field.getHeight()).thenReturn(SHORT_SIDE);
        when(field.getBorder()).thenReturn(compoundBorder);

        Insets insets = border.getBorderInsets(field);

        assertThat(insets).isEqualTo(new Insets(0, 0, 0, SHORT_SIDE + 2));
    }

    @Test
    public void paint_right() throws Exception {
        ClearButtonBorder border = new ClearButtonBorder();
        JTextField field = mock(JTextField.class);
        when(field.getWidth()).thenReturn(LONG_SIDE);
        when(field.getHeight()).thenReturn(SHORT_SIDE);
        Graphics2D g1 = mock(Graphics2D.class);
        Graphics2D g2 = mock(Graphics2D.class);
        when(g1.create(anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(g2);

        border.paintBorder(field, g1, 0, 0, LONG_SIDE, SHORT_SIDE);

        int size = SHORT_SIDE - 2;
        verify(g1).create(LONG_SIDE - SHORT_SIDE, 0, SHORT_SIDE, SHORT_SIDE);
        verify(g2).fillOval(1, 1, size, size);
        verify(g2).drawLine(4, 4, 11, 11);
        verify(g2).drawLine(11, 4, 4, 11);
        verify(g2).dispose();
    }

    @Test
    public void paint_left() throws Exception {
        ClearButtonBorder border = new ClearButtonBorder(Side.LEFT);
        JTextField field = mock(JTextField.class);
        when(field.getWidth()).thenReturn(LONG_SIDE);
        when(field.getHeight()).thenReturn(SHORT_SIDE);
        Graphics2D g1 = mock(Graphics2D.class);
        Graphics2D g2 = mock(Graphics2D.class);
        when(g1.create(anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(g2);

        border.paintBorder(field, g1, 0, 0, LONG_SIDE, SHORT_SIDE);

        int size = SHORT_SIDE - 2;
        verify(g1).create(0, 0, SHORT_SIDE, SHORT_SIDE);
        verify(g2).fillOval(1, 1, size, size);
        verify(g2).drawLine(4, 4, 11, 11);
        verify(g2).drawLine(11, 4, 4, 11);
        verify(g2).dispose();
    }
}