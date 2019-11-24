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
package io.github.jonestimd.swing.validation;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ValidationBorderTest {
    private List<PropertyChangeListener> validationListeners = new ArrayList<>();
    private String validationMessages;

    @Test
    public void addToViewport_wrapsViewportBorder() throws Exception {
        ValidatedPanel panel = new ValidatedPanel();
        Border viewportBorder = new EmptyBorder(1, 1, 1, 1);
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setViewportBorder(viewportBorder);

        ValidationBorder.addToViewport(panel);

        assertThat(((CompoundBorder) scrollPane.getViewportBorder()).getInsideBorder()).isSameAs(viewportBorder);
        Border outsideBorder = ((CompoundBorder) scrollPane.getViewportBorder()).getOutsideBorder();
        assertThat(outsideBorder).isInstanceOf(ValidationBorder.class);
        assertThat(validationListeners).isNotEmpty();
        assertThat(((ValidationBorder) outsideBorder).isValid()).isTrue();
    }

    @Test
    public void addToViewport_initializesBorder() throws Exception {
        validationMessages = "error";
        ValidatedPanel panel = new ValidatedPanel();
        Border viewportBorder = new EmptyBorder(1, 1, 1, 1);
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setViewportBorder(viewportBorder);

        ValidationBorder.addToViewport(panel);

        assertThat(((CompoundBorder) scrollPane.getViewportBorder()).getInsideBorder()).isSameAs(viewportBorder);
        Border outsideBorder = ((CompoundBorder) scrollPane.getViewportBorder()).getOutsideBorder();
        assertThat(outsideBorder).isInstanceOf(ValidationBorder.class);
        assertThat(((ValidationBorder) outsideBorder).isValid()).isFalse();
    }

    @Test
    public void addToViewport_updatesBorder() throws Exception {
        ValidatedPanel panel = new ValidatedPanel();
        JScrollPane scrollPane = new JScrollPane(panel);
        ValidationBorder.addToViewport(panel);

        validationListeners.forEach(listener -> listener.propertyChange(new PropertyChangeEvent(this, "messages", null, "error")));

        Border outsideBorder = ((CompoundBorder) scrollPane.getViewportBorder()).getOutsideBorder();
        ValidationBorder validationBorder = (ValidationBorder) outsideBorder;
        assertThat(validationBorder.isValid()).isFalse();

        validationListeners.forEach(listener -> listener.propertyChange(new PropertyChangeEvent(this, "messages", "error", null)));

        assertThat(validationBorder.isValid()).isTrue();
    }

    @Test
    public void isBorderOpaqueReturnsFalse() throws Exception {
        assertThat(new ValidationBorder().isBorderOpaque()).isFalse();
    }

    @Test
    public void getBorderInsetsReturnsEmptyInsetsWhenValid() throws Exception {
        JPanel panel = mock(JPanel.class);
        ValidationBorder border = new ValidationBorder();

        Insets insets = border.getBorderInsets(panel);

        assertThat(insets.right).isEqualTo(0);
        verify(panel, never()).getGraphics();
    }

    @Test
    public void getBorderInsetsReturnsRightInsetWhenNotValid() throws Exception {
        JPanel panel = mock(JPanel.class);
        Graphics2D graphics2D = mock(Graphics2D.class);
        when(panel.getGraphics()).thenReturn(graphics2D);
        FontMetrics fontMetrics = mock(FontMetrics.class);
        when(graphics2D.getFontMetrics(any())).thenReturn(fontMetrics);
        when(fontMetrics.getMaxAscent()).thenReturn(10);
        when(fontMetrics.getMaxDescent()).thenReturn(3);
        ValidationBorder border = new ValidationBorder();
        border.setValid(false);

        Insets insets = border.getBorderInsets(panel);

        assertThat(insets.right).isEqualTo(13);
        verify(panel).getGraphics();
        verify(graphics2D).getFontMetrics(null);
    }

    @Test
    public void paintBorderDoesNothingWhenValid() throws Exception {
        new ValidationBorder().paintBorder(null, null, 0, 0, 0, 0);
    }

    @Test
    public void paintBorderDrawsMarkerWhenInvalid() throws Exception {
        JPanel panel = mock(JPanel.class);
        Graphics2D graphics2D = mock(Graphics2D.class);
        Graphics2D scratch = mock(Graphics2D.class);
        when(graphics2D.create(anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(scratch);
        FontMetrics fontMetrics = mock(FontMetrics.class);
        when(graphics2D.getFontMetrics(any())).thenReturn(fontMetrics);
        when(fontMetrics.getMaxAscent()).thenReturn(10);
        when(fontMetrics.getMaxDescent()).thenReturn(3);
        ValidationBorder border = new ValidationBorder();
        border.setValid(false);

        border.paintBorder(panel, graphics2D, 0, 0, 50, 15);

        verify(graphics2D).create(50-13, 1, 13, 13);
        verify(graphics2D).getFontMetrics(any());
        verifyNoMoreInteractions(graphics2D);
        verify(scratch).dispose();
    }

    @Test
    public void paintBorderFillsBackgroundWhenNotNull() throws Exception {
        JPanel panel = mock(JPanel.class);
        Graphics2D graphics2D = mock(Graphics2D.class);
        Graphics2D scratch = mock(Graphics2D.class);
        when(graphics2D.create(anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(scratch);
        FontMetrics fontMetrics = mock(FontMetrics.class);
        when(graphics2D.getFontMetrics(any())).thenReturn(fontMetrics);
        when(fontMetrics.getMaxAscent()).thenReturn(10);
        when(fontMetrics.getMaxDescent()).thenReturn(3);
        ValidationBorder border = new ValidationBorder(Color.LIGHT_GRAY);
        border.setValid(false);

        border.paintBorder(panel, graphics2D, 0, 0, 50, 15);

        verify(graphics2D).create(50-13, 0, 13, 15);
        verify(graphics2D).create(50-13, 1, 13, 13);
        verify(graphics2D).getFontMetrics(any());
        verifyNoMoreInteractions(graphics2D);
        verify(scratch).setColor(Color.LIGHT_GRAY);
        verify(scratch).fillRect(0, 0, 13, 15);
        verify(scratch, times(2)).dispose();
    }

    @Test
    public void paintInvalidMarker() throws Exception {
        JPanel panel = mock(JPanel.class);
        Graphics2D graphics2D = mock(Graphics2D.class);
        Graphics2D scratch = mock(Graphics2D.class);
        when(graphics2D.create(anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(scratch);
        FontMetrics fontMetrics = mock(FontMetrics.class);
        when(graphics2D.getFontMetrics(any())).thenReturn(fontMetrics);
        when(fontMetrics.getMaxAscent()).thenReturn(10);
        when(fontMetrics.getMaxDescent()).thenReturn(3);

        ValidationBorder.paintInvalidMarker(panel, graphics2D, 0, 0, 50, 15);

        verify(graphics2D).create(50-13, 1, 13, 13);
        verify(graphics2D).getFontMetrics(any());
        verifyNoMoreInteractions(graphics2D);
        verify(scratch).dispose();
    }

    public class ValidatedPanel extends JPanel implements ValidatedComponent {
        @Override
        public void validateValue() {
        }

        @Override
        public String getValidationMessages() {
            return validationMessages;
        }

        @Override
        public void addValidationListener(PropertyChangeListener listener) {
            validationListeners.add(listener);
        }

        @Override
        public void removeValidationListener(PropertyChangeListener listener) {

        }
    }
}