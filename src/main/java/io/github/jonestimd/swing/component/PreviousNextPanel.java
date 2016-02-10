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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

public class PreviousNextPanel extends JPanel {
    private static final ImageIcon previousIcon = new ImageIcon(PreviousNextPanel.class.getResource("previous.png"));
    private static final ImageIcon nextIcon = new ImageIcon(PreviousNextPanel.class.getResource("next.png"));

    public PreviousNextPanel(Component valueComponent, String previousTooltip, String nextTooltip) {
        super(new BorderLayout());
        add(createButton(previousIcon, previousTooltip, new PreviousHandler()), BorderLayout.WEST);
        add(valueComponent, BorderLayout.CENTER);
        add(createButton(nextIcon, nextTooltip, new NextHandler()), BorderLayout.EAST);
    }

    private JButton createButton(ImageIcon icon, String tooltip, ActionListener actionListener) {
        JButton button = new JButton(icon);
        button.setPreferredSize(new Dimension(icon.getIconWidth() + 6, icon.getIconHeight()));
        button.setToolTipText(tooltip);
        button.addActionListener(actionListener);
        return button;
    }

    public PreviousNextListener[] getPreviousNextListeners() {
        return getListeners(PreviousNextListener.class);
    }

    public void addPreviousNextListener(PreviousNextListener listener) {
        listenerList.add(PreviousNextListener.class, listener);
    }

    public void removePreviousNextListener(PreviousNextListener listener) {
        listenerList.remove(PreviousNextListener.class, listener);
    }

    private class PreviousHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            for (PreviousNextListener listener : getPreviousNextListeners()) {
                listener.selectPrevious(PreviousNextPanel.this);
            }
        }
    }

    private class NextHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            for (PreviousNextListener listener : getPreviousNextListeners()) {
                listener.selectNext(PreviousNextPanel.this);
            }
        }
    }
}