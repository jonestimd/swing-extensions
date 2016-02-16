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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.Date;

import javax.swing.JFormattedTextField;

import io.github.jonestimd.swing.ComponentFactory;

/**
 * The border for a {@link DateField}.  This border contains a calendar "button" that allows the user to select a
 * date in a {@link CalendarPanel} when the "button" is clicked.
 */
public class CalendarButtonBorder extends AbstractButtonBorder<JFormattedTextField, CalendarPanel> {
    public CalendarButtonBorder(JFormattedTextField textField) {
        this(textField, new CalendarPanel((Date) textField.getValue()));
        setTooltip(ComponentFactory.DEFAULT_BUNDLE.getString("calendar.button.tooltip"));
    }

    private CalendarButtonBorder(final JFormattedTextField textField, final CalendarPanel calendarPanel) {
        super(textField, calendarPanel, Side.RIGHT);
        calendarPanel.addPropertyChangeListener(CalendarPanel.DATE_PROPERTY, event -> {
            textField.setValue(event.getNewValue());
            hidePopup();
        });
    }

    @Override
    protected void initializePopup(JFormattedTextField borderComponent, CalendarPanel popupComponent) {
        popupComponent.setDate((Date) borderComponent.getValue());
    }

    @Override
    protected void paintBorder(Component c, Graphics g, int x, int y, int width, int height, int inset) {
        int size = Math.min(inset, height);
        Graphics2D g2d = (Graphics2D) g.create(x + width - size, y + (height - size)/2, size, size--);
        g2d.setColor(Color.YELLOW);
        g2d.fillRect(1, 1, size-2, size-1);
        g2d.setColor(Color.GRAY);
        g2d.drawLine(1, size, size, size);
        g2d.drawLine(size, 1, size, size-1);
        g2d.setColor(Color.BLACK);
        g2d.drawLine(0, 0, 0, size-1);
        g2d.drawLine(0, 0, size-1, 0);
        int c1 = Math.round(size * 0.8f);
        int c2 = Math.round(size * 0.6f);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawLine(size-1, 1, size-1, c1);
        g2d.drawLine(size-1, c1, size/2, size-1);
        g2d.drawLine(1, size-1, size/2, size-1);
        g2d.drawLine(size/2, size-1, c1, c2);
        g2d.drawLine(c1, c2, size-1, c1);
        g2d.setStroke(new BasicStroke(1.8f));
        c2 = size - c1;
        g2d.drawLine(size/2, c2, size/2, c1-1);
        g2d.drawLine(size/2 - 2, c2 + 2, size / 2, c2);
        g2d.dispose();
    }

    @Override
    protected Point getPopupLocation(Dimension screenSize, Dimension popupSize, JFormattedTextField borderComponent) {
        Point location = borderComponent.getLocationOnScreen();
        location.x += borderComponent.getWidth();
        if (location.x + popupSize.width > screenSize.width) {
            location.x -= popupSize.width;
            if (location.y + borderComponent.getHeight() + popupSize.height > screenSize.height) {
                location.y -= popupSize.height;
            }
            else {
                location.y += borderComponent.getHeight();
            }
        }
        else if (location.y + popupSize.height > screenSize.height) {
            location.y -= popupSize.height - borderComponent.getHeight();
        }
        return location;
    }
}