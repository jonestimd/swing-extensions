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
package io.github.jonestimd.swing.component;

import java.io.File;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.function.Predicate;

import javax.swing.ComboBoxEditor;

import io.github.jonestimd.swing.validation.RequiredValidator;
import io.github.jonestimd.swing.validation.Validator;
import io.github.jonestimd.util.JavaPredicates;

/**
 * File selection field that displays files and sub-directories in a popup menu.
 */
public class FileSuggestField extends SuggestField<File> {
    private static final Format FORMAT = new Format() {
        @Override
        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            if (obj instanceof File) toAppendTo.append(obj.toString());
            return toAppendTo;
        }

        @Override
        public Object parseObject(String source, ParsePosition pos) {
            pos.setIndex(pos.getIndex() + 1);
            return new File(source);
        }
    };

    public FileSuggestField(boolean onlyDirectories, File startDirectory) {
        this(onlyDirectories ? File::isDirectory : JavaPredicates.alwaysTrue(), startDirectory, Validator.empty());
    }

    /**
     * Create a FileSuggestField starting at a specific directory.
     * @param onlyDirectories {@code true} to only list directories
     * @param startDirectory the starting directory
     * @param requiredMessage the message to display when the editor is empty
     */
    public FileSuggestField(boolean onlyDirectories, File startDirectory, String requiredMessage) {
        this(onlyDirectories ? File::isDirectory : JavaPredicates.alwaysTrue(), startDirectory, new RequiredValidator(requiredMessage));
    }

    /**
     * Create a filtered FileSuggestField.
     * @param filePredicate only display files and directories matching this predicate
     * @param startDirectory the starting directory
     * @param validator the validator for the editor component
     */
    public FileSuggestField(Predicate<File> filePredicate, File startDirectory, Validator<String> validator) {
        super(FORMAT, validator, new FileSuggestModel(startDirectory, filePredicate));
        setSelectedItem(startDirectory);
        getEditorComponent().setCaretPosition(getEditorText().length());
    }

    @Override
    public void configureEditor(ComboBoxEditor anEditor, Object anItem) {
        String text = getEditorText();
        if (anItem == null || !text.equals(anItem.toString() + File.separator)) {
            super.configureEditor(anEditor, anItem);
        }
    }
}