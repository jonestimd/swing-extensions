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
package io.github.jonestimd.swing.component;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JTextField;

import com.google.common.base.Strings;
import io.github.jonestimd.swing.ComponentTreeUtils;
import io.github.jonestimd.swing.validation.ValidatedComponent;
import io.github.jonestimd.swing.validation.ValidationBorder;
import io.github.jonestimd.swing.validation.ValidationSupport;
import io.github.jonestimd.swing.validation.Validator;

import static io.github.jonestimd.swing.component.ComponentBinder.*;

public class ValidatedFileChooser extends JFileChooser implements ValidatedComponent {
    private final ValidationSupport<String> validationSupport;
    private final ValidationBorder validationBorder = new ValidationBorder();
    private final JTextField selectionField;

    public ValidatedFileChooser(File path, Validator<String> validator) {
        this(path, validator, FILES_ONLY);
    }

    public ValidatedFileChooser(File path, Validator<String> validator, int mode) {
        File file = getCanonicalFile(path);
        setFileHidingEnabled(false);
        setFileSelectionMode(mode);
        setControlButtonsAreShown(false);
        selectionField = ComponentTreeUtils.findComponent(this, JTextField.class);
        selectionField.setBorder(BorderFactory.createCompoundBorder(selectionField.getBorder(), validationBorder));
        if (mode == DIRECTORIES_ONLY) {
            setCurrentDirectory(file.getParentFile());
            setSelectedFile(file);
            selectionField.setText(file.getAbsolutePath());
        }
        else if (file.isFile() || ! file.exists()) {
            setCurrentDirectory(file.getParentFile());
            setSelectedFile(file);
        }
        else {
            setCurrentDirectory(file);
        }
        validationSupport = new ValidationSupport<>(this, validator);
        onChange(selectionField, this::validateValue);
        validateValue();
    }

    private static File getCanonicalFile(File file) {
        try {
            return file.getCanonicalFile();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public JTextField getSelectionField() {
        return selectionField;
    }

    @Override
    public void validateValue() {
        validationSupport.validateValue(selectionField.getText());
        validationBorder.setValid(Strings.isNullOrEmpty(validationSupport.getMessages()));
        if (selectionField.getText().isEmpty()) {
            setSelectedFile(null);
        }
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
}
