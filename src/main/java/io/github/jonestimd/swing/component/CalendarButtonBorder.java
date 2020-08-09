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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.Date;
import java.util.ResourceBundle;

import javax.swing.JFormattedTextField;

import io.github.jonestimd.swing.ComponentResources;

/**
 * The border for a {@link DateField}.  This border contains a calendar "button" that allows the user to select a
 * date in a {@link CalendarPanel} when the "button" is clicked.
 */
public class CalendarButtonBorder extends AbstractButtonBorder<JFormattedTextField, CalendarPanel> {
    private static final float STROKE_WIDTH = 1.8f;
    private static final float CURL1 = 0.8f;
    private static final float CURL2 = 0.6f;

    public CalendarButtonBorder(JFormattedTextField textField) {
        this(textField, ComponentResources.BUNDLE);
    }

    public CalendarButtonBorder(JFormattedTextField textField, ResourceBundle bundle) {
        this(textField, new CalendarPanel((Date) textField.getValue()), bundle);
    }

    private CalendarButtonBorder(final JFormattedTextField textField, final CalendarPanel calendarPanel, ResourceBundle bundle) {
        super(textField, calendarPanel, Side.RIGHT);
        calendarPanel.addPropertyChangeListener(CalendarPanel.DATE_PROPERTY, event -> {
            textField.setValue(event.getNewValue());
            hidePopup();
        });
        setTooltip(bundle.getString("calendar.button.tooltip"));
    }

    @Override
    protected void initializePopup(JFormattedTextField borderComponent, CalendarPanel popupComponent) {
        popupComponent.setDate((Date) borderComponent.getValue());
    }

    @Override
    protected void paintBorder(Component c, Graphics2D g2d, int size) {
        g2d.setColor(Color.YELLOW);
        g2d.fillRect(1, 1, size-2, size-1);
        // draw shadow
        g2d.setColor(Color.GRAY);
        g2d.drawLine(1, size, size, size);
        g2d.drawLine(size, 1, size, size-1);
        // draw page
        g2d.setColor(Color.BLACK);
        g2d.drawLine(0, 0, 0, size-1);
        g2d.drawLine(0, 0, size-1, 0);
        int c1 = Math.round(size * CURL1);
        int c2 = Math.round(size * CURL2);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawLine(size-1, 1, size-1, c1);
        g2d.drawLine(size-1, c1, size/2, size-1);
        g2d.drawLine(1, size-1, size/2, size-1);
        g2d.drawLine(size/2, size-1, c1, c2);
        g2d.drawLine(c1, c2, size-1, c1);
        // draw "1"
        g2d.setStroke(new BasicStroke(STROKE_WIDTH));
        c2 = size - c1;
        g2d.drawLine(size/2, c2, size/2, c1-1);
        g2d.drawLine(size/2 - 2, c2 + 2, size / 2, c2);
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