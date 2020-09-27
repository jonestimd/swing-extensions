// The MIT License (MIT)
//
// Copyright (c) 2020 Timothy D. Jones
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
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;

import javax.swing.JPanel;

import io.github.jonestimd.swing.JFrameRobotTest;
import io.github.jonestimd.swing.validation.Validator;
import io.github.jonestimd.text.StringFormat;
import io.github.jonestimd.util.Streams;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class SuggestFieldRobotTest extends JFrameRobotTest {
    private SuggestField<String> suggestField;
    private List<String> items = Arrays.asList("Apple", "Banana", "Cherry", "Mango", "Peach");
    private Model model = new Model();

    private void showWindow() throws Exception {
        showWindow(this::createContentPane);
    }

    @Test
    public void retainsSelectedTextWhenSelectingNewItem() throws Exception {
        String item = items.get(0);
        showWindow();
        robot.focus(suggestField.getEditorComponent());
        robot.type(item.charAt(0));
        robot.type(item.charAt(1));
        robot.waitForIdle();

        assertThat(suggestField.getEditorComponent().getCaretPosition()).isEqualTo(item.length());
        assertThat(suggestField.getEditorComponent().getSelectionStart()).isEqualTo(2);
        assertThat(suggestField.getEditorComponent().getSelectionEnd()).isEqualTo(item.length());
    }

    @Test
    public void extendsSelectionUsingArrowKeys() throws Exception {
        showWindow();
        robot.focus(suggestField.getEditorComponent());
        robot.pressAndReleaseKey(KeyEvent.VK_DOWN);
        robot.pressAndReleaseKey(KeyEvent.VK_LEFT, KeyEvent.SHIFT_MASK);
        robot.pressAndReleaseKey(KeyEvent.VK_LEFT, KeyEvent.SHIFT_MASK);
        robot.waitForIdle();

        int position = items.get(0).length() - 2;
        assertThat(suggestField.getEditorComponent().getCaretPosition()).isEqualTo(position);
        assertThat(suggestField.getEditorComponent().getSelectionStart()).isEqualTo(position);
        assertThat(suggestField.getEditorComponent().getSelectionEnd()).isEqualTo(position+2);
    }

    protected JPanel createContentPane() {
        suggestField = new SuggestField<>(new StringFormat(), Validator.empty(), model);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(suggestField, BorderLayout.SOUTH);
        panel.setPreferredSize(new Dimension(400, 50));
        return panel;
    }

    private class Model extends BeanListComboBoxModel<String> implements SuggestModel<String> {
        public Model() {
            setElements(items, false);
        }

        @Override
        public String updateSuggestions(String editorText) {
            setElements(Streams.filter(items, item -> item.toLowerCase().contains(editorText.toLowerCase())), false);
            return getSize() > 0 ? getElementAt(0) : null;
        }
    }
}