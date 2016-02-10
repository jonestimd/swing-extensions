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
import java.awt.Component;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;

import javax.swing.JComponent;

public class AlphaPanel extends JComponent {

    private float alpha;

    public AlphaPanel(LayoutManager layout, float alpha) {
        this.alpha = alpha;
        setLayout(layout);
    }

    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Composite composite = g2d.getComposite();
        g2d.setComposite(AlphaComposite.SrcOver.derive(alpha));
        g2d.setColor(getBackground());
        g2d.fill(g2d.getClipBounds());
        g2d.setComposite(composite);
        super.paint(g);
    }

    protected void addImpl(Component comp, Object constraints, int index) {
        if (comp instanceof JComponent) {
            ((JComponent)comp).setOpaque(true);
        }
        super.addImpl(comp, constraints, index);
    }
}