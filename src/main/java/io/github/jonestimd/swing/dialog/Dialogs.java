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

import java.awt.Container;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import io.github.jonestimd.swing.ComponentResources;

/**
 * Factory class for common dialogs.
 */
public class Dialogs {
    /**
     * Display a dialog to confirm closing a window with unsaved data.
     * @param owner the owner for the dialog (e.g. the window being closed)
     * @return true if the user chose to discard unsaved data
     */
    public static boolean confirmDiscardChanges(Container owner) {
        return confirmDiscardChanges(ComponentResources.BUNDLE, owner);
    }

    /**
     * Display a dialog to confirm closing a window with unsaved data.
     * @param bundle provides title and button text
     * @param owner the owner for the dialog (e.g. the window being closed)
     * @return true if the user chose to discard unsaved data
     */
    public static boolean confirmDiscardChanges(ResourceBundle bundle, Container owner) {
        String[] options = {
                bundle.getString("confirm.unsavedChanges.option.confirm"),
                bundle.getString("confirm.unsavedChanges.option.cancel")
        };
        return JOptionPane.showOptionDialog(owner, bundle.getString("confirm.unsavedChanges.message"),
                bundle.getString("confirm.unsavedChanges.title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE,
                null, options, options[1]) == JOptionPane.OK_OPTION;
    }
}
