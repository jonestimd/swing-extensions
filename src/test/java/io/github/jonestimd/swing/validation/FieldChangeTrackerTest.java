package io.github.jonestimd.swing.validation;

import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.google.common.collect.Lists;
import io.github.jonestimd.swing.validation.FieldChangeTracker.FieldChangeHandler;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class FieldChangeTrackerTest {
    private final JPanel panel = new JPanel();
    private int changes = 0;
    private boolean changed;

    @Test
    public void trackChangesToTextField() throws Exception {
        JTextField field = new JTextField();
        panel.add(field);
        FieldChangeTracker.install(new Handler(), panel);

        field.setText("text");
        assertThat(changed).isTrue();

        field.setText("");
        assertThat(changed).isFalse();

        panel.remove(field);
        field.setText("text");

        assertThat(changes).isEqualTo(2);
    }

    @Test
    public void trackChangesToComboBox() throws Exception {
        JComboBox<String> comboBox = new JComboBox<>(new String[] {null, "one", "two"});
        panel.add(comboBox);
        FieldChangeTracker.install(new Handler(), panel);

        comboBox.setSelectedIndex(1);
        assertThat(changed).isTrue();

        comboBox.setSelectedIndex(0);
        assertThat(changed).isFalse();

        panel.remove(comboBox);
        comboBox.setSelectedIndex(1);

        assertThat(changes).isEqualTo(2);
    }

    @Test
    public void trackChangesToCheckBox() throws Exception {
        JCheckBox checkBox = new JCheckBox();
        panel.add(checkBox);
        FieldChangeTracker.install(new Handler(), panel);

        checkBox.setSelected(!checkBox.isSelected());
        assertThat(changed).isTrue();

        checkBox.setSelected(!checkBox.isSelected());
        assertThat(changed).isFalse();

        panel.remove(checkBox);
        checkBox.setSelected(!checkBox.isSelected());

        assertThat(changes).isEqualTo(2);
    }

    @Test
    public void trackListSelection() throws Exception {
        JList<String> list = new JList<>(new Vector<>(Lists.newArrayList("one", "two")));
        panel.add(list);
        FieldChangeTracker.install(new Handler(), panel);

        list.setSelectedIndex(1);
        assertThat(changed).isTrue();

        list.clearSelection();
        assertThat(changed).isFalse();

        panel.remove(list);
        list.setSelectedIndex(1);

        assertThat(changes).isEqualTo(2);
    }

    @Test
    public void trackAddedComponents() throws Exception {
        JTextField field = new JTextField();
        FieldChangeTracker.install(new Handler(), panel);
        panel.add(field);

        field.setText("text");
        assertThat(changed).isTrue();

        field.setText("");
        assertThat(changed).isFalse();

        assertThat(changes).isEqualTo(2);
    }

    @Test
    public void trackNestedAddedComponents() throws Exception {
        JTextField field = new JTextField();
        FieldChangeTracker.install(new Handler(), panel);
        JPanel nested = new JPanel();
        nested.add(field);
        panel.add(nested);

        field.setText("text");
        assertThat(changed).isTrue();

        field.setText("");
        assertThat(changed).isFalse();

        panel.remove(nested);
        field.setText("text");

        assertThat(changes).isEqualTo(2);
    }

    private class Handler implements FieldChangeHandler {
        @Override
        public void fieldsChanged(boolean changed) {
            changes++;
            FieldChangeTrackerTest.this.changed = changed;
        }
    }
}