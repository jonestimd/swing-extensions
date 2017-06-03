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
package io.github.jonestimd.swing.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import io.github.jonestimd.swing.SwingResource;

/**
 * A {@link FilterParser} that generates a compound predicate from the filter text.  The filter text may contain terms
 * separated by logical operators.  The following logical operators are supported.  The operators are listed in the
 * order of precedence (highest first).
 * <ul>
 *   <li>grouping: ()</li>
 *   <li>not: !</li>
 *   <li>and: &amp;</li>
 *   <li>or: &#x2502;</li>
 * </ul>
 * <strong>Note:</strong>  This class is not thread safe.  It is intended for use on the Swing event thread.
 */
public class BasicFilterParser<T> implements FilterParser<T> {
    private static final char AND = SwingResource.FILTER_OPERATOR_SYMBOL_AND.getChar();
    private static final char OR = SwingResource.FILTER_OPERATOR_SYMBOL_OR.getChar();
    private static final char NOT = SwingResource.FILTER_OPERATOR_SYMBOL_NOT.getChar();
    private static final char GROUP_START = SwingResource.FILTER_OPERATOR_SYMBOL_GROUP_START.getChar();
    private static final char GROUP_END = SwingResource.FILTER_OPERATOR_SYMBOL_GROUP_END.getChar();
    private final Function<String, Predicate<T>> predicateFactory;
    private final StringBuilder buffer = new StringBuilder();
    private List<Operation<T>> stack = new LinkedList<>();
    private Predicate<T> groupTerm;
    private final List<String> terms = new ArrayList<>();

    /**
     * Construct a parser.
     * @param predicateFactory a function for creating a predicate for a single term
     */
    public BasicFilterParser(Function<String, Predicate<T>> predicateFactory) {
        this.predicateFactory = predicateFactory;
    }

    public Predicate<T> parse(FilterSource source) {
        terms.clear();
        buffer.setLength(0);
        stack.clear();
        groupTerm = null;
        String text = source.getText();
        for (int i = 0; i < text.length(); i++) {
            if (source.isOperator(i)) {
                if (text.charAt(i) == AND) squashStack(Operator.And);
                else if (text.charAt(i) == OR) squashStack(Operator.Or);
                else if (text.charAt(i) == NOT) stack.add(0, Operation.not());
                else if (text.charAt(i) == GROUP_START) stack.add(0, Operation.group());
                else if (text.charAt(i) == GROUP_END) closeParenthesis();
                else throw new IllegalArgumentException("unknown operator: " + text.charAt(i));
            }
            else {
                buffer.append(text.charAt(i));
            }
        }
        if (buffer.toString().trim().isEmpty() && groupTerm == null && stack.isEmpty()) return null;
        Predicate<T> predicate = getTerm();
        while (! stack.isEmpty()) predicate = pop(predicate);
        return predicate;
    }

    private void closeParenthesis() {
        Predicate<T> term = getTerm();
        while (! stack.isEmpty()) {
            Operation<T> operation = stack.remove(0);
            if (operation.operator == Operator.Group) {
                groupTerm = term;
                return;
            }
            term = operation.apply(term);
        }
        throw newParseException("unexpected ')'");
    }

    private Predicate<T> getTerm() {
        String value = buffer.toString().trim();
        if (! value.isEmpty()) {
            if (groupTerm != null) throw newParseException("missing operator");
            buffer.setLength(0);
            terms.add(value);
            return predicateFactory.apply(value);
        }
        if (groupTerm != null) {
            Predicate<T> term = groupTerm;
            groupTerm = null;
            return term;
        }
        throw newParseException("missing term");
    }

    private IllegalStateException newParseException(String s) {
        terms.clear();
        return new IllegalStateException(s);
    }

    private void squashStack(Operator operator) {
        Predicate<T> leftTerm = getTerm();
        while (! stack.isEmpty() && stack.get(0).operator.preceeds(operator)) leftTerm = pop(leftTerm);
        stack.add(0, operator.operateOn(leftTerm));
    }

    private Predicate<T> pop(Predicate<T> term) {
        return stack.remove(0).apply(term);
    }

    @Override
    public List<String> getTerms() {
        return Collections.unmodifiableList(terms);
    }
}
