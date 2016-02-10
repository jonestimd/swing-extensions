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

import java.awt.Window;
import java.util.Collection;
import java.util.ResourceBundle;

import io.github.jonestimd.swing.validation.FieldChangeTracker;
import io.github.jonestimd.swing.validation.FieldChangeTracker.FieldChangeHandler;

public abstract class InputDialog extends FormDialog {
    /**
     * Create a document modal dialog.
     */
    protected InputDialog(Window owner, ResourceBundle bundle) {
        this(owner, null, ModalityType.DOCUMENT_MODAL, bundle);
    }

    /**
     * Create a document modal dialog.
     */
    protected InputDialog(Window owner, String title, ResourceBundle bundle) {
        this(owner, title, ModalityType.DOCUMENT_MODAL, bundle);
    }

    protected InputDialog(Window owner, String title, ModalityType modalityType, ResourceBundle bundle) {
        super(owner, title, modalityType, bundle);
        FieldChangeTracker.install(new ValidationHandler(), getFormPanel());
    }

    private class ValidationHandler implements FieldChangeHandler {
        public void fieldsChanged(boolean changed, Collection<String> validationMessages) {
            setSaveEnabled(changed && validationMessages.isEmpty());
            setStatusText(validationMessages);
        }
    }
}