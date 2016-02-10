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
package io.github.jonestimd.swing.dialog;

import java.awt.BorderLayout;
import java.awt.Window;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import io.github.jonestimd.swing.action.CancelAction;

import static io.github.jonestimd.swing.ComponentFactory.*;

public class ExceptionDialog extends MessageDialog {
    private static final String RESOURCE_PREFIX = "exceptionDialog.";

    public ExceptionDialog(Window owner, Throwable exception) {
        this(owner, null, exception);
    }

    public ExceptionDialog(Window owner, String messageLabel, Throwable exception) {
        super(owner, ModalityType.APPLICATION_MODAL);
        initialize(messageLabel == null ? DEFAULT_BUNDLE.getString("default.exception.label") : messageLabel, exception);
        CancelAction.install(this);
    }

    private void initialize(String messageLabel, Throwable exception) {
        setTitle(DEFAULT_BUNDLE.getString(RESOURCE_PREFIX + "title"));
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new JLabel(messageLabel), BorderLayout.NORTH);

        JTextArea detailArea = new JTextArea(20, 50);
        detailArea.setEditable(false);
        detailArea.setTabSize(4);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        exception.printStackTrace(new PrintStream(buffer));
        try {
            detailArea.setText(buffer.toString("UTF-8"));
        }
        catch (UnsupportedEncodingException ex) {
            detailArea.setText(DEFAULT_BUNDLE.getString(RESOURCE_PREFIX + "noStackTrace"));
        }
        getContentPane().add(new JScrollPane(detailArea), BorderLayout.CENTER);
        pack();
    }

    public static void main(String args[]) {
        JFrame frame = new JFrame();
        frame.setBounds(50, 50, 100, 100);
        frame.setVisible(true);

        ExceptionDialog dialog = new ExceptionDialog(frame, new Exception("Test message"));
        dialog.setVisible(true);
        System.exit(0);
    }
}