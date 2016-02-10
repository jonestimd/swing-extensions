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
package io.github.jonestimd.swing.filter;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Not thread safe.
 */
public class BasicFilterParser<T> {
    private final Function<String, Predicate<T>> predicateFactory;
    private final StringBuilder buffer = new StringBuilder();
    private final Consumer<String> termSink;
    private List<Operation<T>> stack = new LinkedList<>();
    private Predicate<T> groupTerm;

    public BasicFilterParser(Function<String, Predicate<T>> predicateFactory, Consumer<String> termSink) {
        this.predicateFactory = predicateFactory;
        this.termSink = termSink;
    }

    public Predicate<T> parse(FilterSource source) {
        buffer.setLength(0);
        stack.clear();
        groupTerm = null;
        String text = source.getText();
        for (int i = 0; i < text.length(); i++) {
            if (source.isOperator(i)) {
                switch (text.charAt(i)) {
                    case '&':
                        squashStack(getTerm(), Operator.And);
                        break;
                    case '\u2502':
                    case '|':
                        squashStack(getTerm(), Operator.Or);
                        break;
                    case '!':
                        stack.add(0, Operation.not());
                        break;
                    case '(':
                        stack.add(0, Operation.group());
                        break;
                    case ')':
                        closeParenthesis();
                        break;
                    default:
                        throw new IllegalArgumentException("unknown operator: " + text.charAt(i));
                }
            }
            else {
                buffer.append(text.charAt(i));
            }
        }
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
        throw new IllegalStateException("unexpected ')'");
    }

    private Predicate<T> getTerm() {
        String value = buffer.toString().trim();
        if (! value.isEmpty()) {
            if (groupTerm != null) throw new IllegalStateException("missing operator");
            buffer.setLength(0);
            termSink.accept(value);
            return predicateFactory.apply(value);
        }
        if (groupTerm != null) {
            Predicate<T> term = groupTerm;
            groupTerm = null;
            return term;
        }
        throw new IllegalStateException("missing term");
    }

    private void squashStack(Predicate<T> leftTerm, Operator operator) {
        while (! stack.isEmpty() && stack.get(0).operator.preceeds(operator)) leftTerm = pop(leftTerm);
        stack.add(0, operator.operateOn(leftTerm));
    }

    private Predicate<T> pop(Predicate<T> term) {
        return stack.remove(0).apply(term);
    }
}
