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
package io.github.jonestimd.swing.dialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Window;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import io.github.jonestimd.swing.ComponentTreeUtils;
import io.github.jonestimd.swing.StatusIndicator;
import io.github.jonestimd.swing.component.AlphaPanel;

import static java.awt.KeyboardFocusManager.*;

/**
 * Extends {@link JDialog} to implement {@link StatusIndicator}.
 */
public class MessageDialog extends JDialog implements StatusIndicator {
    private JLabel statusMessageLabel = new JLabel();
    private Component lastFocusOwner;
    private final PropertyChangeListener focusListener = (event) -> {
        Component newValue = (Component) event.getNewValue();
        if (ComponentTreeUtils.findAncestor(newValue, this.getContentPane()::equals).isPresent()) {
            lastFocusOwner = newValue;
        }
    };

    public MessageDialog(Window owner, ModalityType modalityType) {
        this(owner, null, modalityType);
    }

    public MessageDialog(Window owner, String title, ModalityType modalityType) {
        super(owner, title, modalityType);
        statusMessageLabel.setBorder(new CompoundBorder(new LineBorder(Color.BLACK), new EmptyBorder(5, 5, 5, 5)));
        setGlassPane(AlphaPanel.createStatusPane(statusMessageLabel));
    }

    /**
     * Overridden to center the dialog on the owner and capture the focus owner.
     */
    public void setVisible(boolean visible) {
        if (visible) {
            setLocationRelativeTo(getOwner());
            getCurrentKeyboardFocusManager().addPropertyChangeListener("focusOwner", focusListener);
        }
        else getCurrentKeyboardFocusManager().removePropertyChangeListener("focusOwner", focusListener);
        super.setVisible(visible);
    }

    @Override
    public void setStatusMessage(String message) {
        statusMessageLabel.setText(message);
    }

    @Override
    public void disableUI(String message) {
        if (message != null) setStatusMessage(message);
        getGlassPane().setVisible(true);
        getGlassPane().requestFocus();
    }

    @Override
    public void enableUI() {
        getGlassPane().setVisible(false);
        if (lastFocusOwner != null) {
            lastFocusOwner.requestFocusInWindow();
        }
    }
}