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

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.ComboPopup;

import io.github.jonestimd.swing.SwingEdtRule;
import io.github.jonestimd.swing.validation.Validator;
import io.github.jonestimd.util.JavaPredicates;
import io.github.jonestimd.util.Streams;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FileSuggestFieldTest {
    private final int SEPARATOR_KEY_CODE = KeyStroke.getKeyStroke(File.separatorChar).getKeyCode();

    @Rule
    public final SwingEdtRule swingEdtRule = new SwingEdtRule();

    @Mock
    private BasicComboBoxUI comboBoxUI;
    private File startDir = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getFile());
    private String requiredMessage = "File is required";

    private KeyEvent newKeyEvent(FileSuggestField field, int keyCode, char keyChar) {
        return newKeyEvent(field, keyCode, keyChar, 0);
    }

    private KeyEvent newKeyEvent(FileSuggestField field, int keyCode, char keyChar, int modifiers) {
        return new KeyEvent(field.getEditorComponent(), KeyEvent.KEY_RELEASED, System.currentTimeMillis(), modifiers, keyCode, keyChar);
    }

    private KeyListener getKeyListener(FileSuggestField field) {
        KeyListener[] listeners = field.getEditorComponent().getKeyListeners();
        return listeners[listeners.length-1];
    }

    private void verifyField(FileSuggestField field) {
        List<File> children = Arrays.asList(startDir.getParentFile().listFiles());
        String text = startDir.toString();
        assertThat(field.getEditorComponent().getText()).isEqualTo(text);
        assertThat(field.getEditorComponent().getSelectionStart()).isEqualTo(text.length());
        assertThat(field.getEditorComponent().getSelectionEnd()).isEqualTo(text.length());
        assertThat(field.getModel().getSize()).isEqualTo(children.size() + 1);
        assertThat(field.getModel().getElementAt(0)).isEqualTo(startDir.getParentFile());
        assertThat(field.getModel()).containsAll(children);
    }

    @Test
    public void createFieldForFilesWithoutValidation() throws Exception {
        FileSuggestField field = new FileSuggestField(false, startDir);
        verifyField(field);

        field.getEditorComponent().setText("");

        assertThat(field.getValidationMessages()).isNull();
    }

    @Test
    public void createFieldForFilesWithoutValidationShowingThreeParents() throws Exception {
        FileSuggestField field = new FileSuggestField(3, JavaPredicates.alwaysTrue(), startDir, Validator.empty());

        verifyField(field);
        verifyListFormat(field, 3);
    }

    @SuppressWarnings("unchecked")
    private void verifyListFormat(FileSuggestField field, int parentDepth) {
        ComboPopup popup = (ComboPopup) field.getUI().getAccessibleChild(field, 0);
        JList list = popup.getList();
        File basePath = startDir.getParentFile();
        for (int i = 0; i < parentDepth; i++) {
            basePath = basePath.getParentFile();
        }
        JLabel renderer = (JLabel) list.getCellRenderer().getListCellRendererComponent(list, startDir, 0, false, false);
        assertThat(renderer.getText()).isEqualTo(new RelativePathFileFormat(basePath).format(startDir));
    }

    @Test
    public void createFieldForFilesWithValidation() throws Exception {
        FileSuggestField field = new FileSuggestField(false, startDir, requiredMessage);
        verifyField(field);

        field.getEditorComponent().setText("");

        assertThat(field.getValidationMessages()).isEqualTo(requiredMessage);
    }

    @Test
    public void createFieldForDirectoriesWithoutValidation() throws Exception {
        FileSuggestField field = new FileSuggestField(true, startDir);
        verifyField(field);

        field.getEditorComponent().setText("");

        assertThat(field.getValidationMessages()).isNull();
    }

    @Test
    public void createFieldForDirectoriesWithValidation() throws Exception {
        FileSuggestField field = new FileSuggestField(true, startDir, requiredMessage);
        verifyField(field);

        field.getEditorComponent().setText("");

        assertThat(field.getValidationMessages()).isEqualTo(requiredMessage);
    }

    @Test
    public void createFieldWithFilterAndValidator() throws Exception {
        FileSuggestField field = new FileSuggestField(JavaPredicates.alwaysTrue(), startDir, Validator.empty());
        verifyField(field);

        field.getEditorComponent().setText("");

        assertThat(field.getValidationMessages()).isNull();
    }

    @Test
    public void getEditorItemParsesText() throws Exception {
        FileSuggestField field = new FileSuggestField(false, startDir);

        field.getEditorComponent().setText("." + File.separator + "file.txt");

        assertThat(field.getEditor().getItem()).isEqualTo(new File("." + File.separator + "file.txt"));
    }

    @Test
    public void setsSelectedItemToStartingDirectory() throws Exception {
        File directory = new File("unknown");

        FileSuggestField field = new FileSuggestField(false, directory);

        assertThat(field.getSelectedItem()).isEqualTo(directory.getCanonicalFile());
    }

    @Test
    public void updatesItemsOnTypingFileSeparator() throws Exception {
        FileSuggestField field = new FileSuggestField(true, startDir);
        field.setUI(comboBoxUI);
        File child = field.getModel().getElementAt(1);
        field.getEditorComponent().setText(child.toString() + File.separator);

        getKeyListener(field).keyReleased(newKeyEvent(field, 0, File.separatorChar));

        assertThat(field.getModel()).containsAll(Streams.filter(Arrays.asList(child.listFiles()), File::isDirectory));
        verify(comboBoxUI).setPopupVisible(field, true);
    }

    @Test
    public void doesNotRemoveEndingFileSeparatorWhenSuggestionsChange() throws Exception {
        FileSuggestField field = new FileSuggestField(false, startDir);
        field.getEditorComponent().setText(startDir.toString() + File.separator);

        field.configureEditor(field.getEditor(), startDir);

        assertThat(field.getEditorText()).isEqualTo(startDir.toString() + File.separator);
    }

    @Test
    public void usesFilesInCurrentDirectoryForSuggestions() throws Exception {
        FileSuggestField field = new FileSuggestField(false, startDir);
        field.getEditorComponent().setText(startDir.toString() + File.separator + "x");
        field.setUI(comboBoxUI);

        getKeyListener(field).keyReleased(newKeyEvent(field, KeyEvent.VK_X, 'x'));

        assertThat(field.getModel()).hasSize(startDir.list().length + 1);
    }

    @Test
    public void keyListenerRetainsTrailingFileSeparator() throws Exception {
        FileSuggestField field = new FileSuggestField(false, startDir);
        field.getEditorComponent().setText(startDir.toString());
        field.setUI(comboBoxUI);

        getKeyListener(field).keyReleased(newKeyEvent(field, SEPARATOR_KEY_CODE, File.separatorChar));

        assertThat(field.getEditorText()).endsWith(File.separator);
    }

    @Test
    public void ctrlPlusFileSeparatorDoesNotAppendText() throws Exception {
        FileSuggestField field = new FileSuggestField(false, startDir);
        field.getEditorComponent().setText(startDir.toString());
        field.setUI(comboBoxUI);

        getKeyListener(field).keyReleased(newKeyEvent(field, SEPARATOR_KEY_CODE, File.separatorChar, KeyEvent.CTRL_DOWN_MASK));

        assertThat(field.getEditorText()).isEqualTo(startDir.toString());
    }

    @Test
    public void typingFileSeparatorRetainsCursorPosition() throws Exception {
        String text = "/#xyz123";
        FileSuggestField field = new FileSuggestField(false, new File(text));
        field.getEditorComponent().setText(text);
        field.setUI(comboBoxUI);
        field.getEditorComponent().setCaretPosition(4);

        getKeyListener(field).keyReleased(newKeyEvent(field, SEPARATOR_KEY_CODE, File.separatorChar));

        assertThat(field.getEditorComponent().getCaretPosition()).isEqualTo(4);
        assertThat(field.getEditorText()).isEqualTo(text);
    }

    @Test
    public void usesRootFileForEmptyInput() throws Exception {
        FileSuggestField field = new FileSuggestField(false, startDir);
        field.getEditorComponent().setText("");
        field.setUI(comboBoxUI);

        getKeyListener(field).keyReleased(newKeyEvent(field, KeyEvent.VK_DELETE, (char)0));

        int count = File.listRoots()[0].listFiles().length;
        assertThat(field.getModel()).hasSize(count + 1);
    }

    @Test
    public void setSelectedItemUpdatesSuggestions() throws Exception {
        FileSuggestField field = new FileSuggestField(false, startDir);
        File child = startDir.listFiles(File::isDirectory)[0];
        File selectedItem = child.listFiles(File::isDirectory)[0];

        field.setSelectedItem(selectedItem);

        assertThat(field.getModel()).contains(child.listFiles());
    }

    @Test
    public void setSelectedItemRetainsSelection() throws Exception {
        FileSuggestField field = new FileSuggestField(false, startDir);
        File child = startDir.listFiles(File::isDirectory)[0];
        File selectedItem = new File(child, "unknown");

        field.setSelectedItem(selectedItem);

        assertThat(field.getSelectedItem()).isEqualTo(selectedItem);
    }

    public static void main(String ...args) {
        try {
            FileSuggestField field = new FileSuggestField(false, new File("."));
            JFrame frame = new JFrame("test");
            frame.getContentPane().setLayout(new BorderLayout());
            frame.getContentPane().add(field, BorderLayout.SOUTH);
            frame.pack();
            frame.setSize(400, 100);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}