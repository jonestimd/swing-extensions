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
package io.github.jonestimd.swing.component;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;

import javax.swing.JFormattedTextField;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CalendarButtonBorderTest {
    private static final int WIDTH = 50;
    @Mock
    private Graphics g;
    @Mock
    private Graphics2D g2d;
    @Mock
    private JFormattedTextField field;

    private final Dimension fieldPreferredSize = new Dimension(4, 19);
    private final Insets fieldInsets = new Insets(2, 2, 2, 2);
    private final Dimension screenSize = new Dimension(150, 150);
    private final Dimension popupSize = new Dimension(60, 60);

    @Before
    public void setupMocks() throws Exception {
        when(g.create(anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(g2d);
        when(field.getPreferredSize()).thenReturn(fieldPreferredSize);
        when(field.getInsets()).thenReturn(fieldInsets);
    }

    @Test
    public void paintButton() throws Exception {
        CalendarButtonBorder border = new CalendarButtonBorder(field);

        border.paintBorder(null, g, 0, 0, WIDTH, fieldPreferredSize.height);

        final int size = fieldPreferredSize.height - fieldInsets.top - fieldInsets.bottom;
        verify(g).create(WIDTH - size, (fieldPreferredSize.height-size)/2, size, size);
        verify(g2d).dispose();
    }

    @Test
    public void getPopupLocationForComponentInTopLeftOfScreen() throws Exception {
        when(field.getLocationOnScreen()).thenReturn(new Point());
        when(field.getWidth()).thenReturn(WIDTH);
        when(field.getHeight()).thenReturn(fieldPreferredSize.height);
        final CalendarButtonBorder border = new CalendarButtonBorder(field);

        Point location = border.getPopupLocation(screenSize, popupSize, field);

        assertThat(location.x).isEqualTo(WIDTH);
        assertThat(location.y).isEqualTo(0);
    }

    @Test
    public void getPopupLocationForComponentInBottomLeftOfScreen() throws Exception {
        when(field.getLocationOnScreen()).thenReturn(new Point(0, screenSize.height - fieldPreferredSize.height));
        when(field.getWidth()).thenReturn(WIDTH);
        when(field.getHeight()).thenReturn(fieldPreferredSize.height);
        final CalendarButtonBorder border = new CalendarButtonBorder(field);

        Point location = border.getPopupLocation(screenSize, popupSize, field);

        assertThat(location.x).isEqualTo(WIDTH);
        assertThat(location.y).isEqualTo(screenSize.height - popupSize.height);
    }

    @Test
    public void getPopupLocationForComponentInBottomRightOfScreen() throws Exception {
        when(field.getLocationOnScreen()).thenReturn(new Point(screenSize.width - WIDTH, screenSize.height - fieldPreferredSize.height));
        when(field.getWidth()).thenReturn(WIDTH);
        when(field.getHeight()).thenReturn(fieldPreferredSize.height);
        final CalendarButtonBorder border = new CalendarButtonBorder(field);

        Point location = border.getPopupLocation(screenSize, popupSize, field);

        assertThat(location.x).isEqualTo(screenSize.width - popupSize.width);
        assertThat(location.y).isEqualTo(screenSize.height - popupSize.height - fieldPreferredSize.height);
    }

    @Test
    public void getPopupLocationForComponentInTopRightOfScreen() throws Exception {
        when(field.getLocationOnScreen()).thenReturn(new Point(screenSize.width - WIDTH, 0));
        when(field.getWidth()).thenReturn(WIDTH);
        when(field.getHeight()).thenReturn(fieldPreferredSize.height);
        final CalendarButtonBorder border = new CalendarButtonBorder(field);

        Point location = border.getPopupLocation(screenSize, popupSize, field);

        assertThat(location.x).isEqualTo(screenSize.width - popupSize.width);
        assertThat(location.y).isEqualTo(fieldPreferredSize.height);
    }
}