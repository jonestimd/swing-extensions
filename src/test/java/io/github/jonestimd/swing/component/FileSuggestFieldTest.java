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
import java.awt.event.KeyListener;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.swing.plaf.basic.BasicComboBoxUI;

import io.github.jonestimd.swing.SwingEdtRule;
import io.github.jonestimd.util.Streams;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FileSuggestFieldTest {
    @Rule
    public final SwingEdtRule swingEdtRule = new SwingEdtRule();

    @Mock
    private BasicComboBoxUI comboBoxUI;
    private File startDir = new File(".");
    private String requiredMessage = "File is required";

    private KeyEvent newKeyEvent(FileSuggestField field, int keyCode, char keyChar) {
        return new KeyEvent(field.getEditorComponent(), KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, keyCode, keyChar);
    }

    private KeyListener getKeyListener(FileSuggestField field) {
        KeyListener[] listeners = field.getEditorComponent().getKeyListeners();
        return listeners[listeners.length-1];
    }

    @Test
    public void createFieldForFilesWithoutValidation() throws Exception {
        List<File> children = Arrays.asList(startDir.listFiles());

        FileSuggestField field = new FileSuggestField(false, startDir);

        assertThat(field.getEditorComponent().getText()).isEqualTo(".");
        assertThat(field.getEditorComponent().getSelectionStart()).isEqualTo(1);
        assertThat(field.getEditorComponent().getSelectionEnd()).isEqualTo(1);
        assertThat(field.getModel().getSize()).isEqualTo(children.size() + 1);
        assertThat(field.getModel().getElementAt(0)).isEqualTo(startDir);
        assertThat(field.getModel()).contains(children.toArray());

        field.getEditorComponent().setText("");

        assertThat(field.getValidationMessages()).isNull();
    }

    @Test
    public void createFieldForFilesWithValidation() throws Exception {
        List<File> children = Arrays.asList(startDir.listFiles());

        FileSuggestField field = new FileSuggestField(false, startDir, requiredMessage);

        assertThat(field.getEditorComponent().getText()).isEqualTo(".");
        assertThat(field.getEditorComponent().getSelectionStart()).isEqualTo(1);
        assertThat(field.getEditorComponent().getSelectionEnd()).isEqualTo(1);
        assertThat(field.getModel().getSize()).isEqualTo(children.size() + 1);
        assertThat(field.getModel().getElementAt(0)).isEqualTo(startDir);
        assertThat(field.getModel()).contains(children.toArray());

        field.getEditorComponent().setText("");

        assertThat(field.getValidationMessages()).isEqualTo(requiredMessage);
    }

    @Test
    public void createFieldForDirectoriesWithoutValidation() throws Exception {
        List<File> children = Streams.filter(Arrays.asList(startDir.listFiles()), File::isDirectory);

        FileSuggestField field = new FileSuggestField(true, startDir);

        assertThat(field.getEditorComponent().getText()).isEqualTo(".");
        assertThat(field.getEditorComponent().getSelectionStart()).isEqualTo(1);
        assertThat(field.getEditorComponent().getSelectionEnd()).isEqualTo(1);
        assertThat(field.getModel().getSize()).isEqualTo(children.size() + 1);
        assertThat(field.getModel().getElementAt(0)).isEqualTo(startDir);
        assertThat(field.getModel()).contains(children.toArray());

        field.getEditorComponent().setText("");

        assertThat(field.getValidationMessages()).isNull();
    }

    @Test
    public void createFieldForDirectoriesWithValidation() throws Exception {
        List<File> children = Streams.filter(Arrays.asList(startDir.listFiles()), File::isDirectory);

        FileSuggestField field = new FileSuggestField(true, startDir, requiredMessage);

        assertThat(field.getEditorComponent().getText()).isEqualTo(".");
        assertThat(field.getEditorComponent().getSelectionStart()).isEqualTo(1);
        assertThat(field.getEditorComponent().getSelectionEnd()).isEqualTo(1);
        assertThat(field.getModel().getSize()).isEqualTo(children.size() + 1);
        assertThat(field.getModel().getElementAt(0)).isEqualTo(startDir);
        assertThat(field.getModel()).contains(children.toArray());

        field.getEditorComponent().setText("");

        assertThat(field.getValidationMessages()).isEqualTo(requiredMessage);
    }

    @Test
    public void getEditorItemParsesText() throws Exception {
        FileSuggestField field = new FileSuggestField(false, startDir);

        field.getEditorComponent().setText("." + File.separator + "file.txt");

        assertThat(field.getEditor().getItem()).isEqualTo(new File("." + File.separator + "file.txt"));
    }

    @Test
    public void includeCurrentDirectoryInSuggestions() throws Exception {
        File directory = new File("unknown");

        FileSuggestField field = new FileSuggestField(false, directory);

        assertThat(field.getModel()).contains(directory);
    }

    @Test
    public void updatesItemsOnTypingFileSeparator() throws Exception {
        FileSuggestField field = new FileSuggestField(true, startDir);
        field.setUI(comboBoxUI);
        File child = field.getModel().getElementAt(1);
        field.getEditorComponent().setText(child.toString() + File.separator);

        getKeyListener(field).keyReleased(newKeyEvent(field, 0, File.separatorChar));

        assertThat(field.getModel()).contains(Streams.filter(Arrays.asList(child.listFiles()), File::isDirectory).toArray());
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
    public void doesNotUpdateSuggestionsForEmptyInput() throws Exception {
        FileSuggestField field = new FileSuggestField(false, startDir);
        field.getEditorComponent().setText("");
        field.setUI(comboBoxUI);

        getKeyListener(field).keyReleased(newKeyEvent(field, KeyEvent.VK_DELETE, (char)0));

        assertThat(field.getModel()).hasSize(startDir.list().length + 1);
    }

    @Test
    public void setSelectedItemUpdatesSuggestions() throws Exception {
        FileSuggestField field = new FileSuggestField(false, startDir);
        File child = startDir.listFiles(File::isDirectory)[0];
        File selectedItem = child.listFiles(File::isDirectory)[0];

        field.setSelectedItem(selectedItem);

        assertThat(field.getModel()).contains((Object[]) child.listFiles());
    }
}