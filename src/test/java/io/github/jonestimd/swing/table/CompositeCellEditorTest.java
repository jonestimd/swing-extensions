package io.github.jonestimd.swing.table;

import java.awt.Component;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import io.github.jonestimd.swing.FocusContainer;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class CompositeCellEditorTest {
    private TestCellEditor editor = new TestCellEditor();

    @Test
    public void editorComponentImplementsFocusContainer() throws Exception {
        Component component = editor.getTableCellEditorComponent(new JTable(), new TestBean(), true, 0, 0);

        assertThat(component).isInstanceOf(FocusContainer.class);
        assertThat(((FocusContainer) component).getFocusField()).isSameAs(editor.field1);
    }

    @Test
    public void navigateToPreviousFieldUpdatesInputMap() throws Exception {
        editor.initialFocus = 1;
        JComponent component = (JComponent) editor.getTableCellEditorComponent(new JTable(), new TestBean(), true, 0, 0);
        performAction(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK, editor.field1);

        InputMap inputMap = component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        assertThat(inputMap.get(KeyStroke.getKeyStroke("TAB"))).isNotNull();
        assertThat(inputMap.get(KeyStroke.getKeyStroke("RIGHT"))).isNotNull();
        assertThat(inputMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_KP_RIGHT, 0))).isNotNull();
        // navigate to previous from 1st field ends editing
        assertThat(inputMap.get(KeyStroke.getKeyStroke("shift TAB"))).isNull();
        assertThat(inputMap.get(KeyStroke.getKeyStroke("LEFT"))).isNull();
        assertThat(inputMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_KP_LEFT, 0))).isNull();
    }

    @Test
    public void navigateToNextFieldUpdatesInputMap() throws Exception {
        editor.initialFocus = 0;
        JComponent component = (JComponent) editor.getTableCellEditorComponent(new JTable(), new TestBean(), true, 0, 0);
        performAction(KeyEvent.VK_TAB, 0, editor.field1);

        InputMap inputMap = component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        assertThat(inputMap.get(KeyStroke.getKeyStroke("TAB"))).isNull();
        assertThat(inputMap.get(KeyStroke.getKeyStroke("RIGHT"))).isNull();
        assertThat(inputMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_KP_RIGHT, 0))).isNull();
        // navigate to next from last field ends editing
        assertThat(inputMap.get(KeyStroke.getKeyStroke("shift TAB"))).isNotNull();
        assertThat(inputMap.get(KeyStroke.getKeyStroke("LEFT"))).isNotNull();
        assertThat(inputMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_KP_LEFT, 0))).isNotNull();
    }

    private void performAction(int keyCode, int modifiers, JTextField field) {
        JComponent component = (JComponent) editor.getTableCellEditorComponent(new JTable(), new TestBean(), true, modifiers, modifiers);
        Object key = component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).get(KeyStroke.getKeyStroke(keyCode, modifiers));
        Action action = component.getActionMap().get(key);
        KeyEvent event = new KeyEvent(field, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), modifiers, keyCode, KeyEvent.CHAR_UNDEFINED);
        SwingUtilities.notifyAction(action, KeyStroke.getKeyStroke(keyCode, modifiers), event, field, modifiers);
    }

    private static class TestBean implements Cloneable {
        private String field1;
        private String field2;

        @Override
        public TestBean clone() {
            try {
                return (TestBean) super.clone();
            } catch (CloneNotSupportedException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private static class TestCellEditor extends CompositeCellEditor<TestBean> {
        private int initialFocus = 0;
        private JTextField field1 = new JTextField();
        private JTextField field2 = new JTextField();

        public TestCellEditor() {
            super(TestBean::clone);
            addFields(field1, field2);
        }

        @Override
        protected int getInitialFocus() {
            return 0;
        }

        @Override
        protected void prepareEditor(JTable table, TestBean value, boolean isSelected, int row, int column) {
        }

        @Override
        protected void updateCellEditorValue(TestBean bean) {
        }
    }
}