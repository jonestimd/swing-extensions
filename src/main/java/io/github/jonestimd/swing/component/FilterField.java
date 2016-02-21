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
package io.github.jonestimd.swing.component;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.PrimitiveIterator.OfInt;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.CaretEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

import io.github.jonestimd.swing.DocumentChangeHandler;
import io.github.jonestimd.swing.filter.BasicFilterParser;
import io.github.jonestimd.swing.filter.FilterSource;
import io.github.jonestimd.util.Streams;

public class FilterField<T> extends JTextPane implements FilterSource {
    public static final String PREDICATE_PROPERTY = "FilterField.predicate";
    private static final String OPERATORS = "()&|!";
    private static final String SYMBOLS = "()&\u2502!";
    private final Style plainStyle = getStyle("default");
    private final Style boldStyle = addStyle("bold", plainStyle);
    private final HighlightPainter highlightPainter = new DefaultHighlightPainter(Color.CYAN);
    private Object highlightKey;

    private final BasicFilterParser<T> filterParser;
    private final Color normalBackground;
    private final Color errorBackground;
    private final List<String> terms = new ArrayList<>();
    private Predicate<T> predicate;

    public FilterField(Function<String, Predicate<T>> predicateFactory, Color errorBackground) {
        this.filterParser = new BasicFilterParser<>(predicateFactory, terms::add);
        this.normalBackground = getBackground();
        this.errorBackground = errorBackground;
        StyleConstants.setBold(boldStyle, true);
        StyleConstants.setForeground(boldStyle, Color.BLUE);
        addCaretListener(this::caretUpdate);
        getDocument().addDocumentListener(new DocumentChangeHandler(this::updateFilter));
    }

    private void caretUpdate(CaretEvent event) {
        if (highlightKey != null) {
            getHighlighter().removeHighlight(highlightKey);
            highlightKey = null;
        }
        int dot = event.getDot();
        if (dot > 0) {
            try {
                if (getText(dot - 1, 1).equals("(") && isOperator(dot - 1)) {
                    highlight(IntStream.range(dot, getText().length()).iterator(), ')', '(');
                }
                else if (getText(dot - 1, 1).equals(")") && isOperator(dot - 1)) {
                    highlight(Streams.reverseRange(dot - 1, 0).iterator(), '(', ')');
                }
            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void updateFilter() {
        terms.clear();
        setToolTipText(null);
        try {
            predicate = getText().trim().isEmpty() ? null : filterParser.parse(FilterField.this);
            firePropertyChange(PREDICATE_PROPERTY, null, predicate);
            setBackground(normalBackground);
        } catch (Exception ex) {
            predicate = null;
            terms.clear();
            setToolTipText(ex.getMessage());
            setBackground(errorBackground);
            firePropertyChange(PREDICATE_PROPERTY, null, null);
        }
    }

    /**
     * Highlight the matching parenthesis.
     * @param iterator character indexes to search for the match
     * @param highlight parenthesis to find and highlight
     * @param pair opposing parenthesis
     * @throws BadLocationException
     */
    private void highlight(OfInt iterator, char highlight, char pair) throws BadLocationException {
        int depth = 0;
        String text = getText();
        while (iterator.hasNext()) {
            int i = iterator.nextInt();
            if (text.charAt(i) == pair && isOperator(i)) depth++;
            else if (text.charAt(i) == highlight && isOperator(i) && depth-- == 0) {
                highlightKey = getHighlighter().addHighlight(i, i + 1, highlightPainter);
                break;
            }
        }
    }

    public boolean isOperator(int index) {
        return StyleConstants.isBold(getStyledDocument().getCharacterElement(index).getAttributes());
    }

    @Override
    protected void processComponentKeyEvent(KeyEvent e) {
        if (e.getID() == KeyEvent.KEY_TYPED) {
            int index = OPERATORS.indexOf(e.getKeyChar());
            if (index >= 0 && e.isControlDown()) {
                setInputAttributes(boldStyle);
                replaceSelection(SYMBOLS.substring(index, index + 1));
                setInputAttributes(plainStyle);
                e.consume();
            }
        }
        else {
            setInputAttributes(plainStyle);
            super.processComponentKeyEvent(e);
        }
    }

    private void setInputAttributes(Style style) {
        MutableAttributeSet attributes = getInputAttributes();
        attributes.removeAttributes(attributes);
        attributes.addAttributes(style);
    }

    public Predicate<T> getFilter() {
        return predicate;
    }

    public List<String> getTerms() {
        return terms;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
            SwingUtilities.invokeAndWait(() -> {
                FilterField<?> filterField = new FilterField<String>(term -> term::contains, Color.PINK);
                JFrame frame = new JFrame("Filter FIeld");
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frame.setPreferredSize(new Dimension(400, 100));
                frame.getContentPane().add(filterField);
                frame.pack();
                frame.setVisible(true);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
