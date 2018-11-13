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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.text.Highlighter.Highlight;
import javax.swing.text.Highlighter.HighlightPainter;

import io.github.jonestimd.swing.JFrameRobotTest;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PopupListFieldTest extends JFrameRobotTest {
    public static final int WINDOW_WIDTH = 400;
    public static final int WINDOW_HEIGHT = 100;
    private PopupListField popupListField;

    private JTextField field = new JTextField();
    private ArgumentCaptor<PropertyChangeEvent> eventCaptor = ArgumentCaptor.forClass(PropertyChangeEvent.class);

    @Override
    protected JPanel createContentPane() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(field, BorderLayout.NORTH);
        panel.add(popupListField, BorderLayout.SOUTH);
        panel.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        return panel;
    }

    @Test
    public void showsItemDeleteButtons() throws Exception {
        popupListField = PopupListField.builder(true, true).build();

        popupListField.addItem("Apple");

        assertThat(((MultiSelectItem) popupListField.getComponent(0)).isShowDelete()).isTrue();
    }

    @Test
    public void hidesItemDeleteButtons() throws Exception {
        popupListField = PopupListField.builder(false, true).build();

        popupListField.addItem("Apple");

        assertThat(((MultiSelectItem) popupListField.getComponent(0)).isShowDelete()).isFalse();
    }

    @Test
    public void drawsOpaqueItems() throws Exception {
        popupListField = PopupListField.builder(false, true).build();

        popupListField.addItem("Apple");

        assertThat(popupListField.getComponent(0).isOpaque()).isTrue();
    }

    @Test
    public void drawsTransparentItems() throws Exception {
        popupListField = PopupListField.builder(false, false).build();

        popupListField.addItem("Apple");

        assertThat(popupListField.getComponent(0).isOpaque()).isFalse();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void setItemsFiresPropertyChange() throws Exception {
        final JLabel focusedLabel = new JLabel("focused");
        popupListField = PopupListField.builder(true, true).build();
        popupListField.setItems("Cucumber", "Potato");
        popupListField.add(focusedLabel);
        PropertyChangeListener listener = mock(PropertyChangeListener.class);
        popupListField.addPropertyChangeListener(PopupListField.ITEMS_PROPERTY, listener);

        popupListField.setItems("Apple");

        verify(listener).propertyChange(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getPropertyName()).isEqualTo(PopupListField.ITEMS_PROPERTY);
        assertThat(eventCaptor.getValue().getNewValue().getClass().getName()).contains("Unmodifiable");
        assertThat((List) eventCaptor.getValue().getNewValue()).containsExactly("Apple");
        assertThat(popupListField.getComponentCount()).isEqualTo(2);
        assertThat(popupListField.getComponent(1)).isSameAs(focusedLabel);
        assertThat(popupListField.getItems()).containsExactly("Apple");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void addItemFiresPropertyChange() throws Exception {
        popupListField = PopupListField.builder(true, true).build();
        popupListField.setItems("Pineapple");
        PropertyChangeListener listener = mock(PropertyChangeListener.class);
        popupListField.addPropertyChangeListener(PopupListField.ITEMS_PROPERTY, listener);

        popupListField.addItem("Apple");

        verify(listener).propertyChange(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getPropertyName()).isEqualTo(PopupListField.ITEMS_PROPERTY);
        assertThat(eventCaptor.getValue().getNewValue().getClass().getName()).contains("Unmodifiable");
        assertThat((List) eventCaptor.getValue().getNewValue()).containsExactly("Pineapple", "Apple");
        assertThat(popupListField.getComponentCount()).isEqualTo(2);
        assertThat(popupListField.getItems()).containsExactly("Pineapple", "Apple");
    }

    @Test
    public void getPreferredSize_returnsMinHeightForEmptyList() throws Exception {
        popupListField = PopupListField.builder(true, true).build();

        Dimension size = popupListField.getPreferredSize();

        assertThat(size.height).isEqualTo(PopupListField.MIN_HEIGHT);
    }

    @Test
    public void getPreferredSize_returnsHeightBasedOnChildren() throws Exception {
        FlowLayout layout = spy(new FlowLayout());
        popupListField = PopupListField.builder(true, true).build();
        popupListField.setLayout(layout);
        popupListField.setItems("Apple", "Banana", "Cherry", "Peach");
        popupListField.setSize(200, 50);

        Dimension size = popupListField.getPreferredSize();

        Insets insets = popupListField.getInsets();
        Component item = popupListField.getComponent(3);
        assertThat(size.height).isEqualTo(item.getY()+item.getHeight()+PopupListField.VGAP+insets.bottom);
        verify(layout).layoutContainer(popupListField);
    }

    @Test
    public void itemDeleteButtonRemovesItem() throws Exception {
        popupListField = PopupListField.builder(true, true).build();
        popupListField.setItems("Cucumber", "Potato");
        MultiSelectItem item = (MultiSelectItem) popupListField.getComponent(0);

        item.fireDelete();

        assertThat(popupListField.getItems()).containsExactly("Potato");
        assertThat(popupListField.getComponentCount()).isEqualTo(1);
        assertThat(((MultiSelectItem) popupListField.getComponent(0)).getText()).isEqualTo("Potato");
    }

    @Test
    public void removeItemIgnoresUnknownValue() throws Exception {
        popupListField = PopupListField.builder(true, true).build();
        popupListField.setItems("Cucumber", "Potato");

        popupListField.removeItem("other");

        assertThat(popupListField.getItems()).containsExactly("Cucumber", "Potato");
        assertThat(popupListField.getComponentCount()).isEqualTo(2);
    }

    @Test
    public void editorHighlightsInvalidRow() throws Exception {
        popupListField = PopupListField.builder(true, true).build();
        showWindow();
        robot.click(popupListField);

        robot.enterText("Apple\n\n \n");

        JTextArea textArea = robot.finder().findByType(JTextArea.class);
        Highlight[] highlights = textArea.getHighlighter().getHighlights();
        assertThat(highlights).hasSize(2);
        assertThat(highlights[0].getStartOffset()).isEqualTo(6);
        assertThat(highlights[0].getEndOffset()).isEqualTo(6);
        assertThat(highlights[1].getStartOffset()).isEqualTo(7);
        assertThat(highlights[1].getEndOffset()).isEqualTo(8);

        robot.pressAndReleaseKeys(KeyEvent.VK_BACK_SPACE, KeyEvent.VK_BACK_SPACE);

        highlights = textArea.getHighlighter().getHighlights();
        assertThat(highlights).hasSize(1);
        assertThat(highlights[0].getStartOffset()).isEqualTo(6);
        assertThat(highlights[0].getEndOffset()).isEqualTo(6);
    }

    @Test
    public void setValidatorAndHighlighter() throws Exception {
        HighlightPainter highlighter = (g, p0, p1, bounds, c) -> {};
        popupListField = PopupListField.builder(true, true)
                .validator(text -> !text.contains("x"))
                .errorHighlighter(highlighter)
                .build();
        showWindow();
        robot.click(popupListField);

        robot.enterText("Apple\nx\n\n");

        JTextArea textArea = robot.finder().findByType(JTextArea.class);
        Highlight[] highlights = textArea.getHighlighter().getHighlights();
        assertThat(highlights).hasSize(1);
        assertThat(highlights[0].getStartOffset()).isEqualTo(6);
        assertThat(highlights[0].getEndOffset()).isEqualTo(7);
        assertThat(highlights[0].getPainter()).isSameAs(highlighter);
    }

    @Test
    public void setErrorHighligher() throws Exception {
        popupListField = PopupListField.builder(true, true).validator(text -> !text.contains("x")).build();
        showWindow();
        robot.click(popupListField);

        robot.enterText("Apple\nx\n\n");

        JTextArea textArea = robot.finder().findByType(JTextArea.class);
        Highlight[] highlights = textArea.getHighlighter().getHighlights();
        assertThat(highlights).hasSize(1);
        assertThat(highlights[0].getStartOffset()).isEqualTo(6);
        assertThat(highlights[0].getEndOffset()).isEqualTo(7);
    }

    @Test
    public void editorCommitsOnCtrlEnter() throws Exception {
        popupListField = PopupListField.builder(true, true).build();
        showWindow();
        robot.click(popupListField);
        Window popupWindow = (Window) robot.finder().find(c -> c.getClass() == Window.class);
        robot.enterText("Apple\nBanana");

        robot.pressAndReleaseKey(KeyEvent.VK_ENTER, KeyEvent.CTRL_MASK);

        assertThat(popupWindow.isVisible()).isFalse();
        assertThat(popupListField.getItems()).containsExactly("Apple", "Banana");
    }

    @Test
    public void editorIgnoresCtrlEnterWhenInvalid() throws Exception {
        popupListField = PopupListField.builder(true, true).build();
        showWindow();
        robot.click(popupListField);
        Window popupWindow = (Window) robot.finder().find(c -> c.getClass() == Window.class);
        robot.enterText("Apple\n\n");

        robot.pressAndReleaseKey(KeyEvent.VK_ENTER, KeyEvent.CTRL_MASK);

        assertThat(popupWindow.isVisible()).isTrue();
        assertThat(popupListField.getItems()).isEmpty();
    }

    @Test
    public void editorCancelsEditOnEscape() throws Exception {
        popupListField = PopupListField.builder(true, true).build();
        showWindow();
        robot.click(popupListField);
        Window popupWindow = (Window) robot.finder().find(c -> c.getClass() == Window.class);
        robot.enterText("Apple\nBanana");

        robot.pressAndReleaseKey(KeyEvent.VK_ESCAPE);

        assertThat(popupWindow.isVisible()).isFalse();
        assertThat(popupListField.getItems()).isEmpty();
    }

    @Test
    public void focusGainShowsPopup() throws Exception {
        popupListField = PopupListField.builder(true, true).build();
        showWindow();
        robot.focus(field);

        robot.pressAndReleaseKey(KeyEvent.VK_TAB);

        Window popupWindow = (Window) robot.finder().find(c -> c.getClass() == Window.class);
        assertThat(popupWindow.isVisible()).isTrue();
    }

    @Test
    public void focusLossHidesPopup() throws Exception {
        popupListField = PopupListField.builder(true, true).build();
        showWindow();
        robot.focus(field);
        robot.pressAndReleaseKey(KeyEvent.VK_TAB);
        Window popupWindow = (Window) robot.finder().find(c -> c.getClass() == Window.class);
        assertThat(popupWindow.isVisible()).isTrue();

        robot.click(field);

        assertThat(popupWindow.isVisible()).isFalse();
    }

    @Test
    public void spaceKeyOpensPopup() throws Exception {
        popupListField = PopupListField.builder(true, true).build();
        showWindow();
        robot.click(popupListField);
        Window popupWindow = (Window) robot.finder().find(c -> c.getClass() == Window.class);
        assertThat(popupWindow.isVisible()).isTrue();
        robot.pressAndReleaseKey(KeyEvent.VK_ESCAPE);
        assertThat(popupWindow.isVisible()).isFalse();

        robot.pressAndReleaseKey(KeyEvent.VK_SPACE);

        assertThat(popupWindow.isVisible()).isTrue();
    }

    @Test
    public void setPopupBorder() throws Exception {
        final LineBorder border = new LineBorder(Color.RED, 1);
        popupListField = PopupListField.builder(true, true).popupBorder(border).build();
        showWindow();
        robot.click(popupListField);

        JScrollPane scrollPane = robot.finder().findByType(JScrollPane.class);

        assertThat(scrollPane.getBorder()).isSameAs(border);
    }

    @Test
    public void setPopupRows() throws Exception {
        popupListField = PopupListField.builder(true, true).popupRows(10).build();

        Field field = PopupListField.class.getDeclaredField("textArea");
        field.setAccessible(true);
        JTextArea textArea = (JTextArea) field.get(popupListField);
        assertThat(textArea.getRows()).isEqualTo(10);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(new JTextField(), BorderLayout.SOUTH);
        PopupListField selectField = PopupListField.builder(true, true).build();
        selectField.setItems(Arrays.asList("Apple", "Orange", "Banana"));
        frame.getContentPane().add(selectField, BorderLayout.NORTH);
        frame.getContentPane().setBackground(Color.CYAN);
        frame.pack();
        frame.setSize(new Dimension(500, 80));
        frame.setLocation(100, 500);
        frame.setVisible(true);
    }
}