package io.github.jonestimd.swing.component;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import io.github.jonestimd.swing.validation.RequiredValidator;
import org.junit.Test;

import static io.github.jonestimd.mockito.Matchers.matches;
import static io.github.jonestimd.swing.validation.ValidatedComponent.*;
import static org.fest.assertions.Assertions.*;
import static org.mockito.Mockito.*;

public class ValidatedFileChooserTest {
    public static final String REQUIRED = "required";

    @Test
    public void defaultSettings() throws Exception {
        ValidatedFileChooser fileChooser = new ValidatedFileChooser(new File("."), new RequiredValidator(REQUIRED));

        assertThat(fileChooser.isFileHidingEnabled()).isFalse();
        assertThat(fileChooser.getFileSelectionMode()).isEqualTo(ValidatedFileChooser.FILES_ONLY);
        assertThat(fileChooser.getCurrentDirectory()).isEqualTo(new File(".").getCanonicalFile());
        assertThat(fileChooser.getSelectedFile()).isNull();
        assertThat(fileChooser.getSelectionField().getText()).isEmpty();
        assertThat(fileChooser.getValidationMessages()).isEqualTo(REQUIRED);
    }

    @Test
    public void defaultSettingsWithExistingFile() throws Exception {
        File file = new File("./LICENSE").getCanonicalFile();

        ValidatedFileChooser fileChooser = new ValidatedFileChooser(file, new RequiredValidator(REQUIRED));

        assertThat(fileChooser.isFileHidingEnabled()).isFalse();
        assertThat(fileChooser.getFileSelectionMode()).isEqualTo(ValidatedFileChooser.FILES_ONLY);
        assertThat(fileChooser.getCurrentDirectory()).isEqualTo(file.getParentFile());
        assertThat(fileChooser.getSelectedFile()).isEqualTo(file);
        assertThat(fileChooser.getSelectionField().getText()).isEqualTo(file.getName());
        assertThat(fileChooser.getValidationMessages()).isNull();
    }

    @Test
    public void defaultSettingsWithFileName() throws Exception {
        File file = new File("./noSuchFile").getCanonicalFile();

        ValidatedFileChooser fileChooser = new ValidatedFileChooser(file, new RequiredValidator(REQUIRED));

        assertThat(fileChooser.isFileHidingEnabled()).isFalse();
        assertThat(fileChooser.getFileSelectionMode()).isEqualTo(ValidatedFileChooser.FILES_ONLY);
        assertThat(fileChooser.getCurrentDirectory()).isEqualTo(file.getParentFile());
        assertThat(fileChooser.getSelectedFile()).isEqualTo(file);
        assertThat(fileChooser.getSelectionField().getText()).isEqualTo(file.getName());
        assertThat(fileChooser.getValidationMessages()).isNull();
    }

    @Test
    public void filesAndDirectoriesSettings() throws Exception {
        ValidatedFileChooser fileChooser = new ValidatedFileChooser(new File("."), new RequiredValidator(REQUIRED), ValidatedFileChooser.FILES_AND_DIRECTORIES);

        assertThat(fileChooser.isFileHidingEnabled()).isFalse();
        assertThat(fileChooser.getFileSelectionMode()).isEqualTo(ValidatedFileChooser.FILES_AND_DIRECTORIES);
        assertThat(fileChooser.getSelectedFile()).isNull();
        assertThat(fileChooser.getSelectionField().getText()).isEmpty();
        assertThat(fileChooser.getValidationMessages()).isEqualTo(REQUIRED);
    }

    @Test
    public void directoriesOnlySettings() throws Exception {
        ValidatedFileChooser fileChooser = new ValidatedFileChooser(new File("."), new RequiredValidator(REQUIRED), ValidatedFileChooser.DIRECTORIES_ONLY);

        assertThat(fileChooser.isFileHidingEnabled()).isFalse();
        assertThat(fileChooser.getFileSelectionMode()).isEqualTo(ValidatedFileChooser.DIRECTORIES_ONLY);
        assertThat(fileChooser.getSelectedFile()).isEqualTo(new File(".").getCanonicalFile());
        assertThat(fileChooser.getSelectionField().getText()).isEqualTo(new File(".").getCanonicalPath());
        assertThat(fileChooser.getValidationMessages()).isNull();
    }

    @Test
    public void notifiesListenersWhenValidationChanges() throws Exception {
        PropertyChangeListener listener = mock(PropertyChangeListener.class);
        ValidatedFileChooser fileChooser = new ValidatedFileChooser(new File("."), new RequiredValidator(REQUIRED));
        fileChooser.addValidationListener(listener);

        fileChooser.setSelectedFile(new File("./LICENSE"));
        fileChooser.removeValidationListener(listener);
        fileChooser.getSelectionField().setText("");

        verify(listener).propertyChange(matches(new PropertyChangeEvent(fileChooser, VALIDATION_MESSAGES, REQUIRED, null)));
        verifyNoMoreInteractions(listener);
        assertThat(fileChooser.getValidationMessages()).isEqualTo(REQUIRED);
    }

    @Test
    public void setValidator() throws Exception {
        ValidatedFileChooser fileChooser = new ValidatedFileChooser(new File("."), ValidatedFileChooser.FILES_ONLY);

        fileChooser.setValidator(new RequiredValidator(REQUIRED));

        assertThat(fileChooser.isFileHidingEnabled()).isFalse();
        assertThat(fileChooser.getFileSelectionMode()).isEqualTo(ValidatedFileChooser.FILES_ONLY);
        assertThat(fileChooser.getCurrentDirectory()).isEqualTo(new File(".").getCanonicalFile());
        assertThat(fileChooser.getSelectedFile()).isNull();
        assertThat(fileChooser.getSelectionField().getText()).isEmpty();
        assertThat(fileChooser.getValidationMessages()).isEqualTo(REQUIRED);
    }
}