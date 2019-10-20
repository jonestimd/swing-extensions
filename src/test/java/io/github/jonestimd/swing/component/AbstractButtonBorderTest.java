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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import io.github.jonestimd.swing.component.AbstractButtonBorder.Side;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AbstractButtonBorderTest {
    private final Dimension fieldSize = new Dimension(150, 20);
    private final Insets fieldInsets = new Insets(2, 2, 2, 2);
    private JTextField field = mock(JTextField.class);
    private JPanel popupPanel = mock(JPanel.class);

    private Boolean initializedPopup;
    private Point popupLocation;

    @Before
    public void setupMocks() throws Exception {
        when(field.getPreferredSize()).thenReturn(fieldSize);
        when(field.getInsets()).thenReturn(fieldInsets);
        when(field.getHeight()).thenReturn(fieldSize.height);
        when(field.getWidth()).thenReturn(fieldSize.width);
        when(field.getLocationOnScreen()).thenReturn(new Point());
    }

    @Test
    public void preparePopupPanel() throws Exception {
        TestButtonBorder border = new TestButtonBorder(field, popupPanel, Side.RIGHT);

        assertThat(border.preparePopupPanel()).isSameAs(popupPanel);
    }

    @Test
    public void isBorderOpaque() throws Exception {
        TestButtonBorder border = new TestButtonBorder(field, popupPanel, Side.RIGHT);

        assertThat(border.isBorderOpaque()).isTrue();
    }

    @Test
    public void getBorderInsetsRightSide() throws Exception {
        int buttonSize = fieldSize.height - fieldInsets.top - fieldInsets.bottom;
        TestButtonBorder border = new TestButtonBorder(field, popupPanel, Side.RIGHT);

        assertThat(border.getBorderInsets(field)).isEqualTo(new Insets(0, 0, 0, buttonSize+2));
    }

    @Test
    public void getBorderInsetsLeftSide() throws Exception {
        int buttonSize = fieldSize.height - fieldInsets.top - fieldInsets.bottom;
        TestButtonBorder border = new TestButtonBorder(field, popupPanel, Side.LEFT);

        assertThat(border.getBorderInsets(field)).isEqualTo(new Insets(0, buttonSize+2, 0, 0));
    }

    @Test
    public void paintBorder() throws Exception {
        TestButtonBorder border = new TestButtonBorder(field, popupPanel, Side.LEFT);

        border.paintBorder(field, null, 0, 0, 0, 0);
    }

    @Test
    public void displaysTooltipWhenMouseOverButton() throws Exception {
        TestButtonBorder border = new TestButtonBorder(field, new JPanel(), Side.RIGHT);
        border.setTooltip("tooltip");
        triggerCreatePopupWindow();
        ArgumentCaptor<MouseMotionListener> listener = ArgumentCaptor.forClass(MouseMotionListener.class);
        verify(field).addMouseMotionListener(listener.capture());

        listener.getValue().mouseMoved(new MouseEvent(field, -1, 0L, 0, fieldSize.width, 0, 0, false));

        verify(field).setToolTipText("tooltip");
    }

    @Test
    public void hidesTooltipWhenMouseNotOverButton() throws Exception {
        TestButtonBorder border = new TestButtonBorder(field, new JPanel(), Side.RIGHT);
        border.setTooltip("tooltip");
        triggerCreatePopupWindow();
        ArgumentCaptor<MouseMotionListener> listener = ArgumentCaptor.forClass(MouseMotionListener.class);
        verify(field).addMouseMotionListener(listener.capture());

        listener.getValue().mouseMoved(newMouseEvent(field, fieldSize.width - fieldSize.height));

        verify(field).setToolTipText(null);
    }

    private void triggerCreatePopupWindow() {
        when(field.isShowing()).thenReturn(true);
        ArgumentCaptor<HierarchyListener> listener = ArgumentCaptor.forClass(HierarchyListener.class);
        verify(field).addHierarchyListener(listener.capture());
        listener.getValue().hierarchyChanged(new HierarchyEvent(field, -1, field, new JPanel(), HierarchyEvent.SHOWING_CHANGED));
    }

    @Test
    public void isOverButtonRightSide() throws Exception {
        int buttonSize = fieldSize.height - fieldInsets.top - fieldInsets.bottom;
        TestButtonBorder border = new TestButtonBorder(field, popupPanel, Side.RIGHT);

        assertThat(border.isOverButton(new Point(fieldSize.width - buttonSize + 1, 0))).isTrue();
        assertThat(border.isOverButton(new Point(fieldSize.width - buttonSize, 0))).isFalse();
    }

    @Test
    public void isOverButtonLeftSide() throws Exception {
        int buttonSize = fieldSize.height - fieldInsets.top - fieldInsets.bottom;
        TestButtonBorder border = new TestButtonBorder(field, popupPanel, Side.LEFT);

        assertThat(border.isOverButton(new Point(buttonSize - 1, 0))).isTrue();
        assertThat(border.isOverButton(new Point(buttonSize, 0))).isFalse();
    }

    @Test
    public void showPopup() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JTextField field = new JTextField("some text");
            TestButtonBorder border = new TestButtonBorder(field, new JPanel(), Side.RIGHT);
            JFrame frame = new JFrame("test frame");
            frame.getContentPane().add(field);
            frame.pack();
            frame.setVisible(true);

            MouseListener[] listeners = field.getListeners(MouseListener.class);
            listeners[listeners.length-1].mouseClicked(newMouseEvent(field, field.getWidth()));
            border.hidePopup();

            assertThat(popupLocation).isNotNull();
            assertThat(initializedPopup).isTrue();
            frame.dispose();
        });
    }

    private MouseEvent newMouseEvent(JTextField source, int x) {
        return new MouseEvent(source, -1, System.currentTimeMillis(), 0, x, 0, 1, false, MouseEvent.BUTTON1);
    }

    private class TestButtonBorder extends AbstractButtonBorder<JTextField, JPanel> {
        private final JPanel popupPanel;

        public TestButtonBorder(JTextField component, JPanel popupPanel, Side side) {
            super(component, popupPanel, side);
            this.popupPanel = popupPanel;
        }

        @Override
        protected void paintBorder(Component c, Graphics g, int x, int y, int width, int height, int inset) {
            assertThat(inset).isEqualTo(fieldSize.height - fieldInsets.top - fieldInsets.bottom);
        }

        @Override
        protected Point getPopupLocation(Dimension screenSize, Dimension popupSize, JTextField borderComponent) {
            Dimension toolkitScreenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(borderComponent.getGraphicsConfiguration());
            assertThat(screenSize.width).isEqualTo(toolkitScreenSize.width - insets.left - insets.right);
            assertThat(screenSize.height).isEqualTo(toolkitScreenSize.height - insets.top - insets.bottom);
            assertThat(popupSize).isEqualTo(popupPanel.getSize());

            popupLocation = borderComponent.getLocationOnScreen();
            popupLocation.y += borderComponent.getHeight();
            return popupLocation;
        }

        @Override
        protected void initializePopup(JTextField borderComponent, JPanel popupComponent) {
            initializedPopup = true;
        }
    }
}