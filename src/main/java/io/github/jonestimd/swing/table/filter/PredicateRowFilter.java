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
package io.github.jonestimd.swing.table.filter;

import java.util.function.BiPredicate;

import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;

import io.github.jonestimd.swing.DocumentChangeHandler;
import io.github.jonestimd.swing.table.model.BeanTableModel;

public class PredicateRowFilter<T> extends RowFilter<BeanTableModel<T>, Integer> {
    private final BiPredicate<T, String> predicate;
    private final JTextField filterField;

    public static <T> void install(TableRowSorter<? extends BeanTableModel<T>> rowSorter, JTextField filterField, BiPredicate<T, String> predicate) {
        PredicateRowFilter<T> rowFilter = new PredicateRowFilter<>(filterField, predicate);
        rowSorter.setRowFilter(rowFilter);
        filterField.getDocument().addDocumentListener(new DocumentChangeHandler(rowSorter::allRowsChanged));
    }

    public PredicateRowFilter(JTextField filterField, BiPredicate<T, String> predicate) {
        this.filterField = filterField;
        this.predicate = predicate;
    }

    @Override
    public boolean include(Entry<? extends BeanTableModel<T>, ? extends Integer> entry) {
        return predicate.test(getBean(entry), filterField.getText());
    }

    protected T getBean(Entry<? extends BeanTableModel<T>, ? extends Integer> entry) {
        return entry.getModel().getBean(entry.getIdentifier());
    }
}
