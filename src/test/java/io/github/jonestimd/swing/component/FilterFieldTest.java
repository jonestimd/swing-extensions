// The MIT License (MIT)
//
// Copyright (c) 2017 Timothy D. Jones
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;

import io.github.jonestimd.swing.ComponentFactory;
import io.github.jonestimd.swing.ComponentResources;
import io.github.jonestimd.swing.filter.FilterParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FilterFieldTest {
    @Mock
    private FilterParser<String> parser;
    @Mock
    private PropertyChangeListener listener;

    private Function<String, Predicate<String>> predicateFactory = string -> value -> value.equals(string);

    @Test
    public void getFilterReturnsNullPredicateForEmptyText() throws Exception {
        FilterField<String> field = FilterField.builder(predicateFactory).bundle(ComponentResources.BUNDLE).build();
        field.addPropertyChangeListener(FilterField.PREDICATE_PROPERTY, listener);

        field.setText("");
        assertThat(field.getFilter()).isNull();
        assertThat(field.getToolTipText()).isNull();

        field.setText(" ");
        assertThat(field.getFilter()).isNull();
        assertThat(field.getToolTipText()).isNull();
        verify(listener).propertyChange(event(null, null));
    }

    @Test
    public void getFilterReturnsNullForInvalidText() throws Exception {
        String error = "parse exception";
        when(parser.parse(any())).thenThrow(new IllegalStateException(error));
        FilterField<String> field = FilterField.builder(parser).errorBackground(Color.red).build();
        field.addPropertyChangeListener(FilterField.PREDICATE_PROPERTY, listener);

        field.setText("abc");

        assertThat(field.getFilter()).isNull();
        assertThat(field.getToolTipText()).isEqualTo(error);
        assertThat(field.getBackground()).isEqualTo(Color.red);
        verify(listener).propertyChange(event(null, field.getFilter()));
    }

    @Test
    public void clearsTermsWhenTextIsEmpty() throws Exception {
        FilterField<String> field = FilterField.builder(predicateFactory).build();
        field.addPropertyChangeListener(FilterField.PREDICATE_PROPERTY, listener);
        field.setText("abc");

        field.setText("");

        assertThat(field.getTerms()).isEmpty();
        assertThat(field.getFilter()).isNull();
        assertThat(field.getToolTipText()).isNull();
        verify(listener).propertyChange(event(null, null));
    }

    @Test
    public void parseText() throws Exception {
        FilterField<String> field = FilterField.builder(predicateFactory).build();

        field.setText("abc");

        assertThat(field.getFilter().test("abc")).isTrue();
        assertThat(field.getFilter().test("def")).isFalse();
    }

    @Test
    public void highlightsMatchingParenthesisOperator() throws Exception {
        FilterField<String> field = FilterField.builder(predicateFactory).build();
        inputText(field, "abc & ( def | (xyz) )");

        field.setCaretPosition(field.getText().length());
        checkHighlight(field, 6, 7);

        field.setCaretPosition(0);
        assertThat(field.getHighlighter().getHighlights().length).isEqualTo(0);

        field.setCaretPosition(7);
        checkHighlight(field, field.getText().length()-1, field.getText().length());
    }

    @Test
    public void ignoresNonParenthesisOperator() throws Exception {
        FilterField<String> field = FilterField.builder(predicateFactory).build();
        inputText(field, "abc & ( def )");
        field.getDocument().insertString(12, "(xyz)", null);

        field.setCaretPosition(7);
        checkHighlight(field, field.getText().length()-1, field.getText().length());

        field.setCaretPosition(field.getText().length());
        checkHighlight(field, 6, 7);

        field.setCaretPosition(field.getText().length()-1);
        assertThat(field.getHighlighter().getHighlights().length).isEqualTo(0);

        field.setCaretPosition(field.getText().lastIndexOf('(')+1);
        assertThat(field.getHighlighter().getHighlights().length).isEqualTo(0);
    }

    private void checkHighlight(FilterField<String> field, int start, int end) {
        Highlighter highlighter = field.getHighlighter();
        assertThat(highlighter.getHighlights().length).isEqualTo(1);
        assertThat(highlighter.getHighlights()[0].getStartOffset()).isEqualTo(start);
        assertThat(highlighter.getHighlights()[0].getEndOffset()).isEqualTo(end);
    }

    @Test
    public void getTerms() throws Exception {
        FilterField<String> field = FilterField.builder(predicateFactory).build();

        inputText(field, "abc & !(def | ghi)");

        assertThat(field.getTerms()).containsExactly("abc", "def", "ghi");
    }

    private void inputText(FilterField<?> field, String text) throws BadLocationException {
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if ("&!|()".indexOf(ch) >= 0) {
                field.processComponentKeyEvent(new KeyEvent(field, KeyEvent.KEY_TYPED, 0L, KeyEvent.CTRL_DOWN_MASK, KeyEvent.VK_UNDEFINED, ch));
            }
            else {
                field.processComponentKeyEvent(new KeyEvent(field, KeyEvent.KEY_PRESSED, 0L, 0, KeyEvent.VK_UNDEFINED, ch));
                field.getDocument().insertString(field.getText().length(), text.substring(i, i+1), null);
            }
            field.setCaretPosition(field.getText().length());
        }
    }

    private PropertyChangeEvent event(Object oldValue, Object newValue) {
        return argThat(event -> event.getPropertyName().equals(FilterField.PREDICATE_PROPERTY) &&
                Objects.equals(oldValue, event.getOldValue()) && Objects.equals(newValue, event.getNewValue()));
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
//            UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
//            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
//            UIManager.setLookAndFeel("javax.swing.plaf.synth.SynthLookAndFeel");
            SwingUtilities.invokeAndWait(() -> {
                FilterField<?> filterField = new ComponentFactory().newFilterField(term -> term::contains, 3, 1);
//                JTextField filterField = new ComponentFactory().newFilterField();
                JFrame frame = new JFrame("Filter FIeld");
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frame.setPreferredSize(new Dimension(400, 100));
                JToolBar toolbar = ComponentFactory.newMenuToolBar();
                toolbar.add(ComponentFactory.newToolbarButton(
                    new AbstractAction("Action", new ImageIcon(FilterFieldTest.class.getResource("/io/github/jonestimd/swing/component/filter.png"))) {
                        @Override
                        public void actionPerformed(ActionEvent e) {}
                    }
                ));
                toolbar.add(ComponentFactory.newMenuBarSeparator());
                toolbar.add(filterField);
                toolbar.add(Box.createHorizontalStrut(5));
                frame.setJMenuBar(new JMenuBar());
                frame.getJMenuBar().add(new JMenu("Actions"));
                frame.getJMenuBar().add(ComponentFactory.newMenuBarSeparator());
                frame.getJMenuBar().add(toolbar);
                frame.pack();
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
                filterField.requestFocus();
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}