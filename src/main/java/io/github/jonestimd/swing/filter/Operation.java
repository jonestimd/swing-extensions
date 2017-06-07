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

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A logical operation to apply to one or more predicates.
 * @param <T> the parameter class of the predicate(s)
 * @see BasicFilterParser
 */
public abstract class Operation<T> {
    public final Operator operator;

    protected Operation(Operator operator) {
        this.operator = operator;
    }

    public abstract Predicate<T> apply(Predicate<T> operand);

    /**
     * A logical operation that combines 2 predicates.
     * @param <T> the parameter class of the predicates
     */
    public static class BinaryOperation<T> extends Operation<T> {
        public final Function<Predicate<T>, Predicate<T>> leftOperand;

        protected BinaryOperation(Operator operator, Function<Predicate<T>, Predicate<T>> leftOperand) {
            super(operator);
            this.leftOperand = leftOperand;
        }

        @Override
        public Predicate<T> apply(Predicate<T> rightOperand) {
            return leftOperand.apply(rightOperand);
        }
    }

    public static <T> Operation<T> and(Predicate<T> leftOperand) {
        return new BinaryOperation<>(Operator.And, leftOperand::and);
    }

    public static <T> Operation<T> or(Predicate<T> leftOperand) {
        return new BinaryOperation<>(Operator.Or, leftOperand::or);
    }

    public static <T> Operation<T> not() {
        return new Operation<T>(Operator.Not) {
            @Override
            public Predicate<T> apply(Predicate<T> rightOperand) {
                return rightOperand.negate();
            }
        };
    }

    public static <T> Operation<T> group() {
        return new Operation<T>(Operator.Group) {
            @Override
            public Predicate<T> apply(Predicate<T> rightOperand) {
                throw new IllegalStateException("missing ')'");
            }
        };
    }
}
