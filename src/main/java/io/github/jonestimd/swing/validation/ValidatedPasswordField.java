// The MIT License (MIT)
//
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
package io.github.jonestimd.swing.validation;

import java.awt.Cursor;
import java.beans.PropertyChangeListener;

import javax.swing.JPasswordField;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import io.github.jonestimd.swing.DocumentChangeHandler;

public class ValidatedPasswordField extends JPasswordField implements ValidatedComponent {
    private ValidationTooltipBorder validationBorder;
    private final ValidationSupport<char[]> validationSupport;
    private DocumentListener validationHandler = new DocumentChangeHandler(this::validateValue);

    public ValidatedPasswordField(String requiredMessage) {
        this(value -> value.length == 0 ? requiredMessage : null);
    }

    public ValidatedPasswordField(Validator<char[]> validator) {
        this.validationSupport = new ValidationSupport<>(this, validator);
        this.validationBorder = new ValidationTooltipBorder(this);
        super.setBorder(new CompoundBorder(super.getBorder(), validationBorder));
        validateValue();
        getDocument().addDocumentListener(validationHandler);
    }

    @Override
    public void setBorder(Border border) {
        if (border == null) {
            super.setBorder(validationBorder);
        }
        else {
            super.setBorder(new CompoundBorder(border, validationBorder));
        }
    }

    @Override
    public void validateValue() {
        validationBorder.setValid(validationSupport.validateValue(getPassword()) == null);
    }

    @Override
    public String getValidationMessages() {
        return validationSupport.getMessages();
    }

    @Override
    public void addValidationListener(PropertyChangeListener listener) {
        validationSupport.addValidationListener(listener);
    }

    @Override
    public void removeValidationListener(PropertyChangeListener listener) {
        validationSupport.removeValidationListener(listener);
    }

    @Override
    public void setDocument(Document document) {
        if (getDocument() != null) {
            getDocument().removeDocumentListener(validationHandler);
        }
        super.setDocument(document);
        document.addDocumentListener(validationHandler);
    }

    @Override
    public Cursor getCursor() {
        return validationBorder.isMouseOverIndicator() ? Cursor.getDefaultCursor() : super.getCursor();
    }

    @Override
    public String getToolTipText() {
        return validationBorder.isMouseOverIndicator() ? validationSupport.getMessages() : super.getToolTipText();
    }
}