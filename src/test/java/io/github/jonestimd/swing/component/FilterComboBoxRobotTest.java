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

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import io.github.jonestimd.swing.JFrameRobotTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class FilterComboBoxRobotTest extends JFrameRobotTest {
    private static final String[] items = {
        "Apple", "Banana", "Blueberry", "Cherry", "Grape", "Peach", "Pineapple", "Raspberry"
    };
    private FilterComboBox<String> field;

    protected JComponent createContentPane(BiFunction<FilterComboBox<String>, String, String> parser) {
        field = new FilterComboBox<>(new ContainsFilterComboBoxModel<>(Arrays.asList(items), Function.identity()), parser);
        Box panel = Box.createVerticalBox();
        panel.add(new JTextField("just to take up space"));
        panel.add(Box.createVerticalGlue());
        panel.add(field);
        panel.setPreferredSize(new Dimension(400, panel.getPreferredSize().height));
        return panel;
    }

    private void showWindow(BiFunction<FilterComboBox<String>, String, String> parser) throws Exception {
        showWindow(() -> createContentPane(parser));
    }

    private void showWindow() throws Exception {
        showWindow((c, t) -> t);
    }

    @Test
    public void focusGainedShowsPopup() throws Exception {
        showWindow();

        robot.focusAndWaitForFocusGain(field);
        robot.waitForIdle();

        assertThat(field.getPopupList().isShowing()).isTrue();
    }

    @Test
    public void focusLostHidesPopupAndSetsText() throws Exception {
        showWindow();
        field.setSetTextOnFocusLost(true);

        robot.focusAndWaitForFocusGain(field);
        robot.enterText("ap");
        robot.pressAndReleaseKey(KeyEvent.VK_TAB);
        robot.waitForIdle();

        assertThat(field.getPopupList().isShowing()).isFalse();
        assertThat(field.getText()).isEqualTo("");
    }

    @Test
    public void focusLostHidesPopupAndKeepsText() throws Exception {
        showWindow();
        field.setSetTextOnFocusLost(false);

        robot.focusAndWaitForFocusGain(field);
        robot.enterText("ap");
        robot.pressAndReleaseKey(KeyEvent.VK_TAB);
        robot.waitForIdle();

        assertThat(field.getPopupList().isShowing()).isFalse();
        assertThat(field.getText()).isEqualTo("ap");
    }

    @Test
    public void escapeHidesPopup() throws Exception {
        showWindow();

        robot.focusAndWaitForFocusGain(field);
        robot.pressAndReleaseKey(KeyEvent.VK_ESCAPE);
        robot.waitForIdle();

        assertThat(field.getPopupList().isShowing()).isFalse();
    }

    @Test
    public void escapeReShowsPopup() throws Exception {
        showWindow();

        robot.focusAndWaitForFocusGain(field);
        robot.pressAndReleaseKey(KeyEvent.VK_ESCAPE);
        robot.pressAndReleaseKey(KeyEvent.VK_ESCAPE);
        robot.waitForIdle();

        assertThat(field.getPopupList().isShowing()).isTrue();
    }

    @Test
    public void escapeRestoresInitialSelection() throws Exception {
        showWindow();
        field.getPopupList().setSelectedIndex(0);

        robot.focusAndWaitForFocusGain(field);
        robot.enterText(items[1].substring(0, 3));
        robot.pressAndReleaseKey(KeyEvent.VK_ESCAPE);
        robot.waitForIdle();

        assertThat(field.getText()).isEqualTo(items[0]);
        assertThat(field.getSelectedItem()).isEqualTo(items[0]);
    }

    @Test
    public void downSelectsFirstItem() throws Exception {
        showWindow();

        robot.focusAndWaitForFocusGain(field);
        robot.pressAndReleaseKey(KeyEvent.VK_DOWN);
        robot.waitForIdle();

        assertThat(field.getSelectedItem()).isEqualTo(items[0]);
    }

    @Test
    public void downOnLastItemSelectsFirstItem() throws Exception {
        showWindow();

        robot.focusAndWaitForFocusGain(field);
        field.getPopupList().setSelectedIndex(items.length - 1);
        robot.pressAndReleaseKey(KeyEvent.VK_DOWN);
        robot.waitForIdle();

        assertThat(field.getSelectedItem()).isEqualTo(items[0]);
    }

    @Test
    public void upSelectsLastItem() throws Exception {
        showWindow();

        robot.focusAndWaitForFocusGain(field);
        robot.pressAndReleaseKey(KeyEvent.VK_UP);
        robot.waitForIdle();

        assertThat(field.getSelectedItem()).isEqualTo(items[items.length - 1]);
    }

    @Test
    public void upOnFirstItemSelectsLastItem() throws Exception {
        showWindow();

        robot.focusAndWaitForFocusGain(field);
        field.getPopupList().setSelectedIndex(0);
        robot.pressAndReleaseKey(KeyEvent.VK_UP);
        robot.waitForIdle();

        assertThat(field.getSelectedItem()).isEqualTo(items[items.length - 1]);
    }

    @Test
    public void enterHidesPopupAndSetsTextOfSelectedItem() throws Exception {
        showWindow();
        field.getPopupList().setSelectedIndex(2);
        field.getModel().setSelectedItem(null);

        robot.focusAndWaitForFocusGain(field);
        robot.pressAndReleaseKey(KeyEvent.VK_DOWN);
        robot.pressAndReleaseKey(KeyEvent.VK_ENTER);
        robot.waitForIdle();

        assertThat(field.getPopupList().isShowing()).isFalse();
        assertThat(field.getText()).isEqualTo(items[0]);
        assertThat(field.getSelectedItem()).isEqualTo(items[0]);
    }

    @Test
    public void ctrlEnterAddsNewItemToFilteredList() throws Exception {
        final String newItem = "Pear";
        showWindow();

        robot.focusAndWaitForFocusGain(field);
        robot.enterText(newItem);
        robot.pressAndReleaseKey(KeyEvent.VK_ENTER, KeyEvent.CTRL_MASK);
        robot.waitForIdle();

        assertThat(field.getPopupList().isShowing()).isFalse();
        assertThat(field.getText()).isEqualTo(newItem);
        assertThat(field.getSelectedItem()).isEqualTo(newItem);
        assertThat(field.getModel().getMatches()).contains(newItem);
    }

    @Test
    public void ctrlEnterSelectsExistingItemInFilteredList() throws Exception {
        showWindow();

        robot.focusAndWaitForFocusGain(field);
        robot.enterText("apple");
        robot.pressAndReleaseKey(KeyEvent.VK_ENTER, KeyEvent.CTRL_MASK);
        robot.waitForIdle();

        assertThat(field.getPopupList().isShowing()).isFalse();
        assertThat(field.getText()).isEqualTo(items[0]);
        assertThat(field.getSelectedItem()).isEqualTo(items[0]);
    }

    @Test
    public void ctrlEnterClearsSelectionIfParserReturnsNull() throws Exception {
        final String newItem = "Pear";
        showWindow((c, t) -> null);

        robot.focusAndWaitForFocusGain(field);
        robot.enterText(newItem);
        robot.pressAndReleaseKey(KeyEvent.VK_ENTER, KeyEvent.CTRL_MASK);
        robot.waitForIdle();

        assertThat(field.getPopupList().isShowing()).isFalse();
        assertThat(field.getText()).isEmpty();
        assertThat(field.getSelectedItem()).isNull();
        assertThat(field.getModel().getMatches()).doesNotContain(newItem);
    }

    @Test
    public void ctrlEnterClearsSelectionIfParserIsNull() throws Exception {
        final String newItem = "Pear";
        showWindow((BiFunction<FilterComboBox<String>, String, String>) null);

        robot.focusAndWaitForFocusGain(field);
        robot.enterText(newItem);
        robot.pressAndReleaseKey(KeyEvent.VK_ENTER, KeyEvent.CTRL_MASK);
        robot.waitForIdle();

        assertThat(field.getPopupList().isShowing()).isFalse();
        assertThat(field.getText()).isEmpty();
        assertThat(field.getSelectedItem()).isNull();
        assertThat(field.getModel().getMatches()).doesNotContain(newItem);
    }

    @Test
    public void ctrlEnterDoesNotAddDuplicateItem() throws Exception {
        String newItem = "Pear";
        showWindow();

        robot.focusAndWaitForFocusGain(field);
        robot.enterText(newItem);
        robot.pressAndReleaseKey(KeyEvent.VK_ENTER, KeyEvent.CTRL_MASK);
        robot.pressAndReleaseKey(KeyEvent.VK_ENTER, KeyEvent.CTRL_MASK);
        robot.waitForIdle();

        assertThat(field.getPopupList().isShowing()).isFalse();
        assertThat(field.getText()).isEqualTo(newItem);
        assertThat(field.getSelectedItem()).isEqualTo(newItem);
        long count = field.getModel().getMatches().stream().filter((t) -> t.equals(newItem)).count();
        assertThat(count).isEqualTo(1);
    }

    public static void main(String... args) {
        System.setProperty("swing.defaultlaf", "com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
        FilterComboBoxModel<String> model = new ContainsFilterComboBoxModel<>(Arrays.asList(items), Function.identity());
        FilterComboBox<String> field = new FilterComboBox<>(model, (c, text) -> text);
        // field.setMaximumSize(new Dimension(Integer.MAX_VALUE, field.getPreferredSize().height));
        JFrame frame = new JFrame("test");
        frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        frame.getContentPane().add(Box.createVerticalStrut(150));
        frame.getContentPane().add(field);
        frame.getContentPane().add(Box.createVerticalGlue());
        frame.getContentPane().add(new JComboBox<>(items));
        frame.getContentPane().add(Box.createVerticalStrut(20));
        frame.pack();
        frame.setSize(400, frame.getHeight());
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
