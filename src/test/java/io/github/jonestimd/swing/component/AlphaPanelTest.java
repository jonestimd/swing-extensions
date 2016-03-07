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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import org.junit.Test;
import org.mockito.InOrder;

import static org.mockito.Mockito.*;

public class AlphaPanelTest {
    private static final float ALPHA = 0.5f;

    @Test
    public void paintFillsBackground() throws Exception {
        final Rectangle bounds = new Rectangle();
        final Graphics2D g2d = mock(Graphics2D.class);
        final Composite composite = mock(Composite.class);
        when(g2d.getComposite()).thenReturn(composite);
        when(g2d.getClipBounds()).thenReturn(bounds);
        AlphaPanel panel = new AlphaPanel(new FlowLayout(), ALPHA);
        panel.setBackground(Color.blue);

        panel.paint(g2d);

        InOrder inOrder = inOrder(g2d);
        inOrder.verify(g2d).getComposite();
        inOrder.verify(g2d).setComposite(AlphaComposite.SrcOver.derive(ALPHA));
        inOrder.verify(g2d).setColor(panel.getBackground());
        inOrder.verify(g2d).fill(same(bounds));
        inOrder.verify(g2d).setComposite(same(composite));
    }
}