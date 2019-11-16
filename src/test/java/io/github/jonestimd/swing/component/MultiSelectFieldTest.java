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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.text.StyleConstants;

import io.github.jonestimd.swing.ComponentResources;
import io.github.jonestimd.swing.JFrameRobotTest;
import org.junit.Test;
import org.mockito.exceptions.verification.VerificationInOrderFailure;
import org.mockito.internal.verification.AtLeast;
import org.mockito.internal.verification.api.VerificationData;
import org.mockito.invocation.Invocation;
import org.mockito.invocation.MatchableInvocation;
import org.mockito.verification.VerificationMode;

import static io.github.jonestimd.mockito.Matchers.matches;
import static io.github.jonestimd.swing.component.MultiSelectField.*;
import static java.awt.KeyboardFocusManager.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MultiSelectFieldTest extends JFrameRobotTest {
    private MultiSelectField multiSelectField;
    private PropertyChangeListener listener = mock(PropertyChangeListener.class);

    @Override
    protected JPanel createContentPane() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(multiSelectField, BorderLayout.CENTER);
        panel.add(new JTextField(), BorderLayout.SOUTH);
        panel.setPreferredSize(new Dimension(400, 50));
        return panel;
    }

    @Test
    public void setItems_replacesItems() throws Exception {
        multiSelectField = new MultiSelectField(true, true);
        multiSelectField.setItems(Arrays.asList("one", "two", "three"));
        multiSelectField.addPropertyChangeListener(ITEMS_PROPERTY, listener);

        List<String> newItems = Arrays.asList("apple", "banana", "cherry");
        multiSelectField.setItems(newItems);

        assertThat(multiSelectField.getItems()).isEqualTo(newItems);
        verify(listener).propertyChange(matches(new PropertyChangeEvent(multiSelectField, ITEMS_PROPERTY, null, Collections.emptyList())));
        verify(listener).propertyChange(matches(new PropertyChangeEvent(multiSelectField, ITEMS_PROPERTY, null, newItems)));
    }

    @Test
    public void enterAddsAnItem() throws Exception {
        multiSelectField = new MultiSelectField(true, true);
        multiSelectField.addPropertyChangeListener(ITEMS_PROPERTY, listener);
        showWindow();
        robot.focus(multiSelectField);

        robot.enterText("apple\nbanana\ncherry\n");
        robot.waitForIdle();

        List<String> newItems = Arrays.asList("apple", "banana", "cherry");
        assertThat(multiSelectField.getItems()).isEqualTo(newItems);
        verify(listener, last()).propertyChange(matches(new PropertyChangeEvent(multiSelectField, ITEMS_PROPERTY, null, newItems)));
    }

    @Test
    public void enterIgnoresEmptyValue() throws Exception {
        multiSelectField = new MultiSelectField(true, true);
        showWindow();
        robot.focus(multiSelectField);

        robot.enterText("apple\nbanana\n  \ncherry\n");
        robot.waitForIdle();

        assertThat(multiSelectField.getItems()).containsExactly("apple", "banana", "  cherry");
    }

    @Test
    public void jumpsToEndOnTextKeystrokesBetweenItems() throws Exception {
        multiSelectField = MultiSelectField.builder(true, true).items(Arrays.asList("apple", "banana", "cherry")).get();
        showWindow();
        robot.focus(multiSelectField);
        robot.enterText("pea");
        robot.pressAndReleaseKeys(KeyEvent.VK_HOME, KeyEvent.VK_RIGHT);

        robot.enterText("ch\n");
        robot.waitForIdle();

        assertThat(multiSelectField.getItems()).containsExactly("apple", "banana", "cherry", "peach");
    }

    @Test
    public void deleteRemovesAnItem() throws Exception {
        List<String> items = Arrays.asList("apple", "banana", "cherry");
        multiSelectField = MultiSelectField.builder(true, true).items(items).get();
        multiSelectField.addPropertyChangeListener(ITEMS_PROPERTY, listener);
        showWindow();
        robot.focus(multiSelectField);

        robot.pressAndReleaseKeys(KeyEvent.VK_HOME, KeyEvent.VK_DELETE);
        robot.waitForIdle();

        assertThat(multiSelectField.getItems()).containsExactly("banana", "cherry");
        verify(listener).propertyChange(matches(new PropertyChangeEvent(multiSelectField, ITEMS_PROPERTY, null, items.subList(1, 3))));
    }

    @Test
    public void deleteRemovesACharacter() throws Exception {
        multiSelectField = MultiSelectField.builder(true, true).items(Arrays.asList("apple", "banana", "cherry")).get();
        multiSelectField.addPropertyChangeListener(ITEMS_PROPERTY, listener);
        showWindow();
        robot.focus(multiSelectField);
        robot.enterText("peacch");

        robot.pressAndReleaseKeys(KeyEvent.VK_LEFT, KeyEvent.VK_LEFT, KeyEvent.VK_DELETE);
        robot.waitForIdle();

        assertThat(multiSelectField.getItems()).containsExactly("apple", "banana", "cherry", "peach");
        assertThat(multiSelectField.getText()).endsWith("peach");
        verify(listener, last()).propertyChange(matches(new PropertyChangeEvent(multiSelectField, ITEMS_PROPERTY, null,
                Arrays.asList("apple", "banana", "cherry", "peach"))));
    }

    @Test
    public void ignoresBackspaceAtBeginning() throws Exception {
        multiSelectField = new MultiSelectField(true, true);
        multiSelectField.setItems(Arrays.asList("apple", "banana", "cherry"));
        showWindow();
        robot.focus(multiSelectField);

        robot.pressAndReleaseKeys(KeyEvent.VK_HOME, KeyEvent.VK_BACK_SPACE);
        robot.waitForIdle();

        assertThat(multiSelectField.getItems()).containsExactly("apple", "banana", "cherry");
    }

    @Test
    public void backspaceRemovesAnItem() throws Exception {
        multiSelectField = new MultiSelectField(true, true);
        multiSelectField.setItems(Arrays.asList("apple", "banana", "cherry"));
        showWindow();
        robot.focus(multiSelectField);

        robot.pressAndReleaseKeys(KeyEvent.VK_END, KeyEvent.VK_BACK_SPACE);
        robot.waitForIdle();

        assertThat(multiSelectField.getItems()).containsExactly("apple", "banana");
    }

    @Test
    public void backspaceRemovesACharacter() throws Exception {
        multiSelectField = MultiSelectField.builder(true, true).items(Arrays.asList("apple", "banana", "cherry")).get();
        showWindow();
        robot.focus(multiSelectField);
        robot.enterText("peacch");

        robot.pressAndReleaseKeys(KeyEvent.VK_LEFT, KeyEvent.VK_BACK_SPACE);
        robot.waitForIdle();

        assertThat(multiSelectField.getItems()).containsExactly("apple", "banana", "cherry", "peach");
        assertThat(multiSelectField.getText()).endsWith("peach");
    }

    @Test
    public void deleteButtonRemovesItem() throws Exception {
        List<String> items = Arrays.asList("apple", "banana", "cherry");
        multiSelectField = MultiSelectField.builder(true, true).items(items).get();
        multiSelectField.addPropertyChangeListener(ITEMS_PROPERTY, listener);
        showWindow();

        robot.click(multiSelectField.getComponent(0), new Point(5, 5));
        robot.waitForIdle();

        assertThat(multiSelectField.getItems()).isEqualTo(items.subList(1, 3));
        verify(listener).propertyChange(matches(new PropertyChangeEvent(multiSelectField, ITEMS_PROPERTY, null, items.subList(1, 3))));
    }

    @Test
    public void removeItemIgnoresInvalidValue() throws Exception {
        multiSelectField = MultiSelectField.builder(true, true).items(Arrays.asList("apple", "banana", "cherry")).get();

        multiSelectField.removeItem(new MultiSelectItem("mango", true, true));

        assertThat(multiSelectField.getItems()).containsExactly("apple", "banana", "cherry");
    }

    @Test
    public void highlightsInvalidItemText() throws Exception {
        multiSelectField = new MultiSelectField(false, false);

        assertThat(StyleConstants.getBackground(multiSelectField.getInvalidItemStyle()))
                .isEqualTo(ComponentResources.lookupColor("multiSelectField.invalidItem.background"));
    }

    @Test
    public void addsItemOnFocusLost() throws Exception {
        multiSelectField = MultiSelectField.builder(false, true).disableTab().get();
        showWindow();
        robot.focus(multiSelectField);
        robot.enterText("peach");

        robot.pressAndReleaseKeys(KeyEvent.VK_TAB);
        robot.waitForIdle();

        assertThat(multiSelectField.getItems()).containsExactly("peach");
        assertThat(multiSelectField.getText().substring(1)).isEmpty();
    }

    @Test
    public void retainsFocusWithInvalidText() throws Exception {
        multiSelectField = MultiSelectField.builder(false, true).disableTab().setYieldFocusOnError(false).get();
        showWindow();
        robot.focus(multiSelectField);
        robot.enterText("  ");

        robot.pressAndReleaseKeys(KeyEvent.VK_TAB);
        robot.waitForIdle();

        assertThat(multiSelectField.isFocusOwner()).isTrue();
    }

    @Test
    public void keepsTextOnFocusLost() throws Exception {
        multiSelectField = MultiSelectField.builder(false, true).disableTab().setKeepTextOnFocusLost(true).get();
        showWindow();
        robot.focus(multiSelectField);
        robot.enterText("peach");

        robot.pressAndReleaseKeys(KeyEvent.VK_TAB);
        robot.waitForIdle();

        assertThat(multiSelectField.isFocusOwner()).isFalse();
        assertThat(multiSelectField.getItems()).containsExactly("peach");
        assertThat(multiSelectField.getText()).isEqualTo("peach");
    }

    @Test
    public void clearsInvalidTextOnFocusLost() throws Exception {
        multiSelectField = MultiSelectField.builder(false, true).disableTab().get();
        showWindow();
        robot.focus(multiSelectField);
        robot.enterText("  ");

        robot.pressAndReleaseKeys(KeyEvent.VK_TAB);
        robot.waitForIdle();

        assertThat(multiSelectField.getItems()).isEmpty();
        assertThat(multiSelectField.getText()).isEmpty();
    }

    @Test
    public void builder_disableTab_setsFocusTraversalKeys() throws Exception {
        multiSelectField = MultiSelectField.builder(false, true).disableTab().get();

        assertThat(multiSelectField.getInputMap().get(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0))).isNotEqualTo("insert-tab");
        assertThat(multiSelectField.getFocusTraversalKeys(FORWARD_TRAVERSAL_KEYS)).containsExactly(KeyStroke.getKeyStroke("pressed TAB"));
        assertThat(multiSelectField.getFocusTraversalKeys(BACKWARD_TRAVERSAL_KEYS)).containsExactly(KeyStroke.getKeyStroke("shift pressed TAB"));
    }

    @Test
    public void builder_setsIsValidPredicate() throws Exception {
        BiPredicate<MultiSelectField, String> isValid = (field, text) -> !field.getItems().contains(text);
        multiSelectField = MultiSelectField.builder(false, true).pendingItemValidator(isValid).items(Arrays.asList("one", "two")).get();
        showWindow();

        robot.focus(multiSelectField);
        robot.enterText("one");
        robot.pressAndReleaseKeys(KeyEvent.VK_ENTER);
        robot.waitForIdle();

        assertThat(multiSelectField.isValidItem()).isFalse();
        assertThat(multiSelectField.getText().substring(2)).isEqualTo("one");
    }

    private static VerificationMode last() {
        return new AtLeast(1) {
            @Override
            public void verify(VerificationData data) {
                super.verify(data);
                MatchableInvocation wanted = data.getTarget();
                List<Invocation> invocations = data.getAllInvocations();
                if (!wanted.matches(invocations.get(invocations.size()-1))) {
                    throw new VerificationInOrderFailure("Last invocation does not match " + wanted.toString());
                }
            }
        };
    }
}