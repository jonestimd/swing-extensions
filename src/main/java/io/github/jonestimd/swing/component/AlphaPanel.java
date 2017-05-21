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
package io.github.jonestimd.swing.component;

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;

import javax.swing.JComponent;
import javax.swing.KeyStroke;

/**
 * Extends {@link JComponent} to fill the bounds using the background color at a specified transparency.
 */
public class AlphaPanel extends JComponent {
    public static final float GLASS_PANE_ALPHA = 0.5f;

    private float alpha;

    /**
     * Construct a new panel.
     * @param layout the layout manager for child components
     * @param alpha the transparency to use when drawing the background
     */
    public AlphaPanel(LayoutManager layout, float alpha) {
        this.alpha = alpha;
        setLayout(layout);
    }

    /**
     * Fill the component using the background color at the specified transparency.
     */
    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Composite composite = g2d.getComposite();
        g2d.setComposite(AlphaComposite.SrcOver.derive(alpha));
        g2d.setColor(getBackground());
        g2d.fill(g2d.getClipBounds());
        g2d.setComposite(composite);
        super.paint(g);
    }

    /**
     * Overridden to set the component to be opaque.
     */
    @Override
    protected void addImpl(Component comp, Object constraints, int index) {
        if (comp instanceof JComponent) {
            ((JComponent)comp).setOpaque(true);
        }
        super.addImpl(comp, constraints, index);
    }

    /**
     * Create an {@link AlphaPanel} to use as a glass pane on a window.  When visible, the panel will display the
     * status message component and block input on the window.
     * @param statusMessage the component for displaying messages
     */
    public static AlphaPanel createStatusPane(JComponent statusMessage) {
        AlphaPanel statusPane = new AlphaPanel(new GridBagLayout(), GLASS_PANE_ALPHA) {
            @Override
            protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
                super.processKeyBinding(ks, e, condition, pressed);
                // block keyboard events when glass pane is visible
                return true;
            }
        };
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        statusPane.add(statusMessage, gbc);
        // block mouse events when glass pane is visible
        MouseAdapter mouseAdapter = new MouseAdapter() {};
        statusPane.addMouseListener(mouseAdapter);
        statusPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        return statusPane;
    }
}