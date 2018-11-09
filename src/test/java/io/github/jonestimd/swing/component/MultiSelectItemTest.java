// The MIT License (MIT)
//
// Copyright (c) 2018 Timothy D. Jones
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
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Consumer;

import javax.swing.JPanel;

import io.github.jonestimd.swing.JFrameRobotTest;
import java.awt.Font;
import org.assertj.swing.core.MouseButton;
import org.junit.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

public class MultiSelectItemTest extends JFrameRobotTest {
    @SuppressWarnings("unchecked")
    private Consumer<MultiSelectItem> deleteListener = mock(Consumer.class);

    private MultiSelectItem multiSelectItem = new MultiSelectItem("item text", true, false);

    @Override
    protected JPanel createContentPane() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.add(multiSelectItem);
        panel.setPreferredSize(new Dimension(400, 50));
        return panel;
    }

    @Test
    public void fillsOutlineWhenOpaque() throws Exception {
        multiSelectItem.setOpaque(true);
        Graphics2D g2d = mock(Graphics2D.class);
        when(g2d.create()).thenReturn(g2d);
        when(g2d.create(anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(g2d);
        when(g2d.getFont()).thenReturn(new Font(Font.DIALOG, Font.PLAIN, 12));

        multiSelectItem.paintComponent(g2d);

        verify(g2d, atLeastOnce()).setColor(multiSelectItem.getBackground());
        verify(g2d).fill(isA(RoundRectangle2D.Double.class));
    }

    @Test
    public void notifiesListenersOnDeleteButtonClick() throws Exception {
        multiSelectItem.addDeleteListener(deleteListener);
        showWindow();

        robot.moveMouse(0, 0);
        robot.click(multiSelectItem, new Point(12, 10));

        robot.waitForIdle();
        verify(deleteListener, timeout(1500)).accept(multiSelectItem);
    }

    @Test
    public void doesNotNotifyRemovedListener() throws Exception {
        multiSelectItem.addDeleteListener(deleteListener);
        multiSelectItem.removeDeleteListener(deleteListener);
        showWindow();

        robot.moveMouse(0, 0);
        robot.click(multiSelectItem, new Point(12, 10));

        robot.waitForIdle();
        verify(deleteListener, never()).accept(multiSelectItem);
    }

    @Test
    public void ignoresClickWhenNotOverDeleteButton() throws Exception {
        multiSelectItem.addDeleteListener(deleteListener);
        showWindow();

        robot.moveMouse(0, 0);
        robot.click(multiSelectItem, new Point(multiSelectItem.getWidth()-15, 10));

        robot.waitForIdle();
        verify(deleteListener, never()).accept(multiSelectItem);
        assertThat(multiSelectItem.isShowDelete()).isTrue();
    }

    @Test
    public void ignoresClickWhenDeleteButtonNotDisplayed() throws Exception {
        multiSelectItem = new MultiSelectItem("item text", false, false);
        multiSelectItem.addDeleteListener(deleteListener);
        showWindow();

        robot.moveMouse(0, 0);
        robot.click(multiSelectItem, new Point(12, 10));

        robot.waitForIdle();
        verify(deleteListener, never()).accept(multiSelectItem);
        assertThat(multiSelectItem.isShowDelete()).isFalse();
    }

    @Test
    public void ignoresRightClickOnDeleteButton() throws Exception {
        multiSelectItem.addDeleteListener(deleteListener);
        showWindow();

        robot.moveMouse(0, 0);
        robot.click(multiSelectItem, new Point(12, 10), MouseButton.RIGHT_BUTTON, 1);

        robot.waitForIdle();
        verify(deleteListener, never()).accept(multiSelectItem);
    }
}