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
package io.github.jonestimd.swing;

import java.util.Arrays;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import io.github.jonestimd.swing.component.TextField;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class EditableComponentFocusTraversalPolicyTest {
    private final EditableComponentFocusTraversalPolicy policy = new EditableComponentFocusTraversalPolicy();

    @Test
    public void getComponentAfterSkipsReadOnlyJTextFields() throws Exception {
        List<JComponent> fields = Arrays.asList(
                new MockTextField(),
                new MockComboBox(),
                new TextField<>(new MockTextField()).readOnly().get(),
                new TextField<>(new MockTextField()).readOnly().get(),
                new MockTextField());
        JPanel panel = new MockPanel(fields);
        new JFrame().getContentPane().add(panel);

        assertThat(policy.getComponentAfter(panel, fields.get(0))).isSameAs(fields.get(1));
        assertThat(policy.getComponentAfter(panel, fields.get(1))).isSameAs(fields.get(4));
        assertThat(policy.getComponentAfter(panel, fields.get(2))).isSameAs(fields.get(4));
        assertThat(policy.getComponentAfter(panel, fields.get(3))).isSameAs(fields.get(4));
        assertThat(policy.getComponentAfter(panel, fields.get(4))).isSameAs(fields.get(0));
    }

    @Test
    public void getComponentBeforeSkipsReadOnlyJTextFields() throws Exception {
        List<JComponent> fields = Arrays.asList(
                new MockTextField(),
                new MockComboBox(),
                new TextField<>(new MockTextField()).readOnly().get(),
                new TextField<>(new MockTextField()).readOnly().get(),
                new MockTextField());
        JPanel panel = new MockPanel(fields);
        new JFrame().getContentPane().add(panel);

        assertThat(policy.getComponentBefore(panel, fields.get(4))).isSameAs(fields.get(1));
        assertThat(policy.getComponentBefore(panel, fields.get(3))).isSameAs(fields.get(1));
        assertThat(policy.getComponentBefore(panel, fields.get(2))).isSameAs(fields.get(1));
        assertThat(policy.getComponentBefore(panel, fields.get(1))).isSameAs(fields.get(0));
        assertThat(policy.getComponentBefore(panel, fields.get(0))).isSameAs(fields.get(4));
    }

    private static class MockPanel extends JPanel {
        public MockPanel(List<? extends JComponent> components) {
            setFocusCycleRoot(true);
            components.forEach(this::add);
        }

        @Override
        public boolean isShowing() {
            return true;
        }

        @Override
        public boolean isDisplayable() {
            return true;
        }
    }

    private static class MockTextField extends JTextField {
        @Override
        public boolean isDisplayable() {
            return true;
        }
    }

    private static class MockComboBox extends JComboBox {
        @Override
        public boolean isDisplayable() {
            return true;
        }
    }
}