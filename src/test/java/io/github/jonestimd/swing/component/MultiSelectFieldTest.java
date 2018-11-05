// The MIT License (MIT)
//
// Copyright (c) 2018 Timothy D. Jones
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
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.security.Key;
import java.util.Arrays;

import javax.swing.JPanel;

import io.github.jonestimd.swing.JFrameRobotTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class MultiSelectFieldTest extends JFrameRobotTest {
    private MultiSelectField multiSelectField;

    @Override
    protected JPanel createContentPane() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(multiSelectField);
        panel.setPreferredSize(new Dimension(400, 50));
        return panel;
    }

    @Test
    public void enterAddsAnItem() throws Exception {
        multiSelectField = new MultiSelectField(true, true);
        showWindow();
        robot.focus(multiSelectField);

        robot.enterText("apple\nbanana\ncherry\n");

        assertThat(multiSelectField.getItems()).containsExactly("apple", "banana", "cherry");
    }

    @Test
    public void enterIgnoresEmptyValue() throws Exception {
        multiSelectField = new MultiSelectField(true, true);
        showWindow();
        robot.focus(multiSelectField);

        robot.enterText("apple\nbanana\n  \ncherry\n");

        assertThat(multiSelectField.getItems()).containsExactly("apple", "banana", "  cherry");
    }

    @Test
    public void jumpsToEndOnTextKeystrokesBetweenItems() throws Exception {
        multiSelectField = new MultiSelectField(Arrays.asList("apple", "banana", "cherry"), true, true, MultiSelectField.DEFAULT_IS_VALID_ITEM);
        showWindow();
        robot.focus(multiSelectField);
        robot.enterText("pea");
        robot.pressAndReleaseKeys(KeyEvent.VK_HOME, KeyEvent.VK_RIGHT);

        robot.enterText("ch\n");

        assertThat(multiSelectField.getItems()).containsExactly("apple", "banana", "cherry", "peach");
    }

    @Test
    public void deleteRemovesAnItem() throws Exception {
        multiSelectField = new MultiSelectField(Arrays.asList("apple", "banana", "cherry"), true, true, MultiSelectField.DEFAULT_IS_VALID_ITEM);
        showWindow();
        robot.focus(multiSelectField);

        robot.pressAndReleaseKeys(KeyEvent.VK_HOME, KeyEvent.VK_DELETE);

        assertThat(multiSelectField.getItems()).containsExactly("banana", "cherry");
    }

    @Test
    public void deleteRemovesACharacter() throws Exception {
        multiSelectField = new MultiSelectField(Arrays.asList("apple", "banana", "cherry"), true, true, MultiSelectField.DEFAULT_IS_VALID_ITEM);
        showWindow();
        robot.focus(multiSelectField);
        robot.enterText("peacch");

        robot.pressAndReleaseKeys(KeyEvent.VK_LEFT, KeyEvent.VK_LEFT, KeyEvent.VK_DELETE);

        assertThat(multiSelectField.getItems()).containsExactly("apple", "banana", "cherry");
        assertThat(multiSelectField.getText()).endsWith("peach");
    }

    @Test
    public void ignoresBackspaceAtBeginning() throws Exception {
        multiSelectField = new MultiSelectField(true, true);
        multiSelectField.setItems(Arrays.asList("apple", "banana", "cherry"));
        showWindow();
        robot.focus(multiSelectField);

        robot.pressAndReleaseKeys(KeyEvent.VK_HOME, KeyEvent.VK_BACK_SPACE);

        assertThat(multiSelectField.getItems()).containsExactly("apple", "banana", "cherry");
    }

    @Test
    public void backspaceRemovesAnItem() throws Exception {
        multiSelectField = new MultiSelectField(true, true);
        multiSelectField.setItems(Arrays.asList("apple", "banana", "cherry"));
        showWindow();
        robot.focus(multiSelectField);

        robot.pressAndReleaseKeys(KeyEvent.VK_END, KeyEvent.VK_BACK_SPACE);

        assertThat(multiSelectField.getItems()).containsExactly("apple", "banana");
    }

    @Test
    public void backspaceRemovesACharacter() throws Exception {
        multiSelectField = new MultiSelectField(Arrays.asList("apple", "banana", "cherry"), true, true, MultiSelectField.DEFAULT_IS_VALID_ITEM);
        showWindow();
        robot.focus(multiSelectField);
        robot.enterText("peacch");

        robot.pressAndReleaseKeys(KeyEvent.VK_LEFT, KeyEvent.VK_BACK_SPACE);

        assertThat(multiSelectField.getItems()).containsExactly("apple", "banana", "cherry");
        assertThat(multiSelectField.getText()).endsWith("peach");
    }

    @Test
    public void deleteButtonRemovesItem() throws Exception {
        multiSelectField = new MultiSelectField(Arrays.asList("apple", "banana", "cherry"), true, true, MultiSelectField.DEFAULT_IS_VALID_ITEM);
        showWindow();

        robot.click(multiSelectField, new Point(15, 12));

        assertThat(multiSelectField.getItems()).containsExactly("banana", "cherry");
    }

    @Test
    public void removeItemIgnoresInvalidValue() throws Exception {
        multiSelectField = new MultiSelectField(Arrays.asList("apple", "banana", "cherry"), true, true, MultiSelectField.DEFAULT_IS_VALID_ITEM);

        multiSelectField.removeItem("mango");

        assertThat(multiSelectField.getItems()).containsExactly("apple", "banana", "cherry");
    }
}