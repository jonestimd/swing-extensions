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
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.PrimitiveIterator.OfInt;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import javax.swing.JTextPane;
import javax.swing.event.CaretEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

import io.github.jonestimd.swing.ComponentResources;
import io.github.jonestimd.swing.DocumentConsumerAdapter;
import io.github.jonestimd.swing.SwingResource;
import io.github.jonestimd.swing.filter.BasicFilterParser;
import io.github.jonestimd.swing.filter.FilterParser;
import io.github.jonestimd.swing.filter.FilterSource;
import io.github.jonestimd.swing.table.sort.BeanModelRowSorter;
import io.github.jonestimd.util.Resources;
import io.github.jonestimd.util.Streams;

/**
 * A text pane that allows the user to enter a filter expression.  When the text changes it is parsed by a
 * {@link FilterParser}.  When the text is successfully parsed a property change event is fired for the parsed
 * predicate.  When parsing fails the fields background color is changed to the specified {@code errorBackground}.
 * Operators are typed using the {@code Ctrl} key and are displayed with a bold font. When the caret is at a parenthesis
 * the matching parenthesis is highlighted.  This component supports the following operators.
 * <ul>
 *   <li>grouping: ()</li>
 *   <li>not: !</li>
 *   <li>and: &amp;</li>
 *   <li>or: | (displayed as &#x2502;)</li>
 * </ul>
 * <p>This component can be used with a {@link BeanModelRowSorter} to filter the rows displayed in a table.</p>
 * @param <T> the parameter type of the parsed predicate
 */
public class FilterField<T> extends JTextPane implements FilterSource {
    /** Property name for predicate property change events. */
    public static final String PREDICATE_PROPERTY = "FilterField.predicate";
    private final Style plainStyle = getStyle("default");
    private final Style boldStyle = addStyle("bold", plainStyle);
    private final HighlightPainter highlightPainter = new DefaultHighlightPainter(Color.CYAN);
    private Object highlightKey;

    private final FilterParser<T> filterParser;
    private final Color normalBackground;
    private final Color errorBackground;
    private Predicate<T> predicate;
    private final String operatorKeys;
    private final String operatorSymbols;
    private final char groupStart;
    private final char groupEnd;

    /**
     * Construct a filter field using {@link BasicFilterParser}.
     * @param predicateFactory a function for creating a predicate for a single term
     * @param errorBackground the background color to use when parsing fails
     */
    public FilterField(Function<String, Predicate<T>> predicateFactory, Color errorBackground) {
        this(ComponentResources.BUNDLE, predicateFactory, errorBackground);
    }

    /**
     * Construct a filter field using {@link BasicFilterParser}.
     * @param bundle provides filter operator keys and symbols
     * @param predicateFactory a function for creating a predicate for a single term
     * @param errorBackground the background color to use when parsing fails
     */
    public FilterField(ResourceBundle bundle, Function<String, Predicate<T>> predicateFactory, Color errorBackground) {
        this(bundle, new BasicFilterParser<>(predicateFactory), errorBackground);
    }

    /**
     * Construct a filter field using the specified {@code filterParser}.
     * @param filterParser the filter parser
     * @param errorBackground the background color to use when parsing fails
     */
    public FilterField(FilterParser<T> filterParser, Color errorBackground) {
        this(ComponentResources.BUNDLE, filterParser, errorBackground);
    }

    /**
     * Construct a filter field using the specified {@code filterParser}.
     * @param bundle provides filter operator keys and symbols
     * @param filterParser the filter parser
     * @param errorBackground the background color to use when parsing fails
     */
    public FilterField(ResourceBundle bundle, FilterParser<T> filterParser, Color errorBackground) {
        this.filterParser = filterParser;
        this.normalBackground = getBackground();
        this.errorBackground = errorBackground;
        this.operatorKeys = Resources.join(ComponentResources.BUNDLE, "filter.operator.key.");
        this.operatorSymbols = Resources.join(ComponentResources.BUNDLE, "filter.operator.symbol.");
        this.groupStart = SwingResource.FILTER_OPERATOR_SYMBOL_GROUP_START.getChar();
        this.groupEnd = SwingResource.FILTER_OPERATOR_SYMBOL_GROUP_END.getChar();
        StyleConstants.setBold(boldStyle, true);
        StyleConstants.setForeground(boldStyle, Color.BLUE);
        addCaretListener(this::caretUpdate);
        getDocument().addDocumentListener(new DocumentConsumerAdapter(this::updateFilter));
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
                    highlight(IntStream.range(dot, getText().length()).iterator(), groupEnd, groupStart);
                }
                else if (getText(dot - 1, 1).equals(")") && isOperator(dot - 1)) {
                    highlight(Streams.reverseRange(dot - 1, 0).iterator(), groupStart, groupEnd);
                }
            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void updateFilter(String text) {
        setToolTipText(null);
        try {
            predicate = filterParser.parse(this);
            firePropertyChange(PREDICATE_PROPERTY, null, predicate);
            setBackground(normalBackground);
        } catch (Exception ex) {
            predicate = null;
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

    @Override
    public boolean isOperator(int index) {
        return StyleConstants.isBold(getStyledDocument().getCharacterElement(index).getAttributes());
    }

    @Override
    protected void processComponentKeyEvent(KeyEvent e) {
        if (e.getID() == KeyEvent.KEY_TYPED) {
            int index = operatorKeys.indexOf(e.getKeyChar());
            if (index >= 0 && e.isControlDown()) {
                setInputAttributes(boldStyle);
                replaceSelection(operatorSymbols.substring(index, index + 1));
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

    /**
     * Get the predicate for the current filter text.
     */
    public Predicate<T> getFilter() {
        return predicate;
    }

    /**
     * Get the list of filter terms.  Useful for highlighting matches.
     */
    public List<String> getTerms() {
        return filterParser.getTerms();
    }
}
