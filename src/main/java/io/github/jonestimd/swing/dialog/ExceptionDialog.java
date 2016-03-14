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
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import io.github.jonestimd.swing.ComponentFactory;
import io.github.jonestimd.swing.action.CancelAction;

import static io.github.jonestimd.swing.ComponentFactory.*;

/**
 * Display an exception with its stack trace.
 */
public class ExceptionDialog extends MessageDialog {
    private static final String RESOURCE_PREFIX = "exceptionDialog.";

    /**
     * Construct an exception dialog using {@link ComponentFactory#DEFAULT_BUNDLE}.
     */
    public ExceptionDialog(Window owner, Throwable exception) {
        this(owner, DEFAULT_BUNDLE, exception);
    }

    /**
     * Construct an exception dialog using the specified resource bundle with fallback to
     * {@link ComponentFactory#DEFAULT_BUNDLE} for missing values.
     */
    public ExceptionDialog(Window owner, ResourceBundle bundle, Throwable exception) {
        super(owner, getString(bundle, "title"), ModalityType.APPLICATION_MODAL);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new JLabel(getString(bundle, "exception.label")), BorderLayout.NORTH);
        JTextArea detailArea = new JTextArea(getInt(bundle, "exception.rows"), getInt(bundle, "exception.columns"));
        detailArea.setEditable(false);
        detailArea.setTabSize(4);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        exception.printStackTrace(new PrintStream(buffer));
        try {
            detailArea.setText(buffer.toString("UTF-8"));
        }
        catch (UnsupportedEncodingException ex) {
            detailArea.setText(getString(bundle, "noStackTrace"));
        }
        getContentPane().add(new JScrollPane(detailArea), BorderLayout.CENTER);
        pack();
        CancelAction.install(this);
    }

    private static int getInt(ResourceBundle bundle, String key) {
        try {
            return Integer.parseInt(bundle.getObject(RESOURCE_PREFIX + key).toString());
        } catch (Exception ex) {
            return (int) DEFAULT_BUNDLE.getObject(RESOURCE_PREFIX + key);
        }
    }

    private static String getString(ResourceBundle bundle, String key) {
        try {
            return bundle.getString(RESOURCE_PREFIX + key);
        } catch (Exception ex) {
            return DEFAULT_BUNDLE.getString(RESOURCE_PREFIX + key);
        }
    }
}