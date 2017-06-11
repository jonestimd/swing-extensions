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

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.function.Predicate;

import javax.swing.ComboBoxEditor;

import io.github.jonestimd.swing.validation.RequiredValidator;
import io.github.jonestimd.swing.validation.ValidatedTextField;
import io.github.jonestimd.swing.validation.Validator;
import io.github.jonestimd.util.JavaPredicates;

/**
 * File selection field that displays files and sub-directories in a popup menu.
 */
public class FileSuggestField extends SuggestField<File> {
    public static final int DEFAULT_PARENT_DEPTH = 2;
    private static final int MODIFIER_MASK = KeyEvent.SHIFT_DOWN_MASK | KeyEvent.ALT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK;
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

    private final RelativePathFileFormat listFormat;
    private final int parentDepth;

    /**
     * Create a FileSuggestField starting at a specific directory and using the default parent depth.
     * @param onlyDirectories {@code true} to only list directories
     * @param startDirectory the starting directory
     */
    public FileSuggestField(boolean onlyDirectories, File startDirectory) throws IOException {
        this(DEFAULT_PARENT_DEPTH, onlyDirectories, startDirectory);
    }

    /**
     * Create a FileSuggestField starting at a specific directory.
     * @param parentDepth the number of parent directories to display in the drop down menu (if less than 1, displays the full path)
     * @param onlyDirectories {@code true} to only list directories
     * @param startDirectory the starting directory
     */
    public FileSuggestField(int parentDepth, boolean onlyDirectories, File startDirectory) throws IOException {
        this(parentDepth, onlyDirectories ? File::isDirectory : JavaPredicates.alwaysTrue(), startDirectory, Validator.empty());
    }

    /**
     * Create a FileSuggestField starting at a specific directory and using the default parent depth.
     * @param onlyDirectories {@code true} to only list directories
     * @param startDirectory the starting directory
     * @param requiredMessage the message to display when the editor is empty
     */
    public FileSuggestField(boolean onlyDirectories, File startDirectory, String requiredMessage) throws IOException {
        this(DEFAULT_PARENT_DEPTH, onlyDirectories, startDirectory, requiredMessage);
    }

    /**
     * Create a FileSuggestField starting at a specific directory.
     * @param parentDepth the number of parent directories to display in the drop down menu (if less than 1, displays the full path)
     * @param onlyDirectories {@code true} to only list directories
     * @param startDirectory the starting directory
     * @param requiredMessage the message to display when the editor is empty
     */
    public FileSuggestField(int parentDepth, boolean onlyDirectories, File startDirectory, String requiredMessage) throws IOException {
        this(parentDepth, onlyDirectories ? File::isDirectory : JavaPredicates.alwaysTrue(), startDirectory, new RequiredValidator(requiredMessage));
    }

    /**
     * Create a filtered FileSuggestField using the default parent depth.
     * @param filePredicate only display files and directories matching this predicate
     * @param startDirectory the starting directory
     * @param validator the validator for the editor component
     */
    public FileSuggestField(Predicate<File> filePredicate, File startDirectory, Validator<String> validator) throws IOException {
        this(DEFAULT_PARENT_DEPTH, filePredicate, startDirectory, validator);
    }

    /**
     * Create a filtered FileSuggestField.
     * @param parentDepth the number of parent directories to display in the drop down menu (if less than 1, displays the full path)
     * @param filePredicate only display files and directories matching this predicate
     * @param startDirectory the starting directory
     * @param validator the validator for the editor component
     */
    public FileSuggestField(int parentDepth, Predicate<File> filePredicate, File startDirectory, Validator<String> validator) throws IOException {
        this(new RelativePathFileFormat(null), parentDepth, filePredicate, startDirectory.getCanonicalFile(), validator);
    }

    private FileSuggestField(RelativePathFileFormat listFormat, int parentDepth, Predicate<File> filePredicate, File startDirectory, Validator<String> validator) {
        super(listFormat, FORMAT, validator, new FileSuggestModel(startDirectory, filePredicate));
        this.listFormat = listFormat;
        this.parentDepth = parentDepth;
        setSelectedItem(startDirectory);
        getEditorComponent().setCaretPosition(getEditorText().length());
    }

    private void setBasePath(File file) {
        if (parentDepth > 0 && file != null) {
            File basePath = file.isDirectory() && getEditorText().endsWith(File.separator) ? file : file.getParentFile();
            for (int i = 0; i < parentDepth && basePath != null; i++) {
                basePath = basePath.getParentFile();
            }
            listFormat.setBasePath(basePath);
        }
    }

    @Override
    public void configureEditor(ComboBoxEditor anEditor, Object anItem) {
        setBasePath((File) anItem);
        // keep trailing separator when drop down list changes
        String text = getEditorText();
        if (anItem == null || !text.equals(anItem.toString() + File.separator)) {
            super.configureEditor(anEditor, anItem);
        }
    }

    @Override
    protected void processEditorKeyEvent(KeyEvent event) {
        ValidatedTextField editor = getEditorComponent();
        int position = editor.getCaretPosition();
        super.processEditorKeyEvent(event);
        if (event.getKeyChar() == File.separatorChar && (event.getModifiersEx() & MODIFIER_MASK) == 0) {
            String text = editor.getText();
            if (position >= text.length() && ! text.endsWith(File.separator)) {
                editor.setText(text + File.separator);
            }
        }
    }
}
