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
package io.github.jonestimd.swing.window;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Predicate;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

/**
 * This class handles window closing events.  It checks a {@link Predicate} and if the result is {@code true} then
 * a confirmation dialog is displayed before the window closes.
 */
public class ConfirmCloseAdapter extends WindowAdapter {
    private final Predicate<WindowEvent> confirmClose;

    /**
     * Install a new {@link ConfirmCloseAdapter} on a window.
     * @param window the window
     * @param confirmClose the {@link Predicate} to check to determine if confirmation is required before
     * closing the window
     */
    public static void install(Window window, Predicate<WindowEvent> confirmClose) {
        if (window instanceof JFrame) {
            ((JFrame) window).setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        }
        else if (window instanceof JDialog) {
            ((JDialog) window).setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        }
        window.addWindowListener(new ConfirmCloseAdapter(confirmClose));
    }

    /**
     * @param confirmClose the {@link Predicate} to check to determine if confirmation is required before
     * closing the window
     */
    public ConfirmCloseAdapter(Predicate<WindowEvent> confirmClose) {
        this.confirmClose = confirmClose;
    }

    @Override
    public void windowClosing(WindowEvent e) {
        if (confirmClose.test(e)) {
            e.getWindow().dispose();
        }
    }
}
