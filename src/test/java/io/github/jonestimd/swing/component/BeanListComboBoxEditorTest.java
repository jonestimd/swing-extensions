package io.github.jonestimd.swing.component;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;

import io.github.jonestimd.swing.validation.Validator;
import io.github.jonestimd.text.StringFormat;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class BeanListComboBoxEditorTest {
    private BeanListModel<String> model = new BeanListModel<>(Arrays.asList("Apple", "Banana", "Cherry"));
    private JComboBox<String> comboBox = new JComboBox<>(model);
    private BeanListComboBoxEditor<String> editor = new BeanListComboBoxEditor<>(comboBox, new StringFormat(), Validator.empty());

    @Test
    public void updatesTextToMatchSelectedItemOnFocusLost() throws Exception {
        comboBox.setEditor(editor);
        comboBox.setEditable(true);

        SwingUtilities.invokeAndWait(() -> {
            editor.getEditorComponent().setText("apple");
            FocusEvent event = new FocusEvent(new JButton(), FocusEvent.FOCUS_LOST);
            for (FocusListener listener : editor.getEditorComponent().getFocusListeners()) {
                listener.focusLost(event);
            }
        });

        SwingUtilities.invokeAndWait(() -> {}); // wait for auto-complete to get processed
        assertThat(editor.getEditorComponent().getText()).isEqualTo("Apple");
        assertThat(comboBox.getSelectedItem()).isEqualTo("Apple");
    }

    @Test
    public void doesNotupdateTextOnFocusLostIfNoSelectedItem() throws Exception {
        comboBox.setEditor(editor);
        comboBox.setEditable(true);

        SwingUtilities.invokeAndWait(() -> {
            editor.getEditorComponent().setText("x");
            FocusEvent event = new FocusEvent(new JButton(), FocusEvent.FOCUS_LOST);
            for (FocusListener listener : editor.getEditorComponent().getFocusListeners()) {
                listener.focusLost(event);
            }
        });

        assertThat(editor.getEditorComponent().getText()).isEqualTo("x");
        assertThat(comboBox.getSelectedItem()).isNull();
    }
}
