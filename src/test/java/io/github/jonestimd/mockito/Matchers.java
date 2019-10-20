package io.github.jonestimd.mockito;

import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.swing.event.ListDataEvent;
import javax.swing.event.TableModelEvent;

import org.mockito.Mockito;

public class Matchers {
    @SafeVarargs
    public static <T> Collection<T> containsOnly(T... items) {
        return containsOnly(Arrays.asList(items));
    }

    public static <T> Collection<T> isEmpty() {
        return containsOnly(Collections.emptyList());
    }

    public static <T> Collection<T> containsOnly(final Collection<T> items) {
        final Set<T> uniqueItems = new HashSet<>(items);
        return Mockito.argThat(arg -> arg.size() == uniqueItems.size() && arg.containsAll(uniqueItems));
    }

    public static TableModelEvent matches(final TableModelEvent example) {
        return Mockito.argThat(actual -> actual.getSource() == example.getSource() &&
                actual.getType() == example.getType() &&
                actual.getColumn() == example.getColumn() &&
                actual.getFirstRow() == example.getFirstRow() &&
                actual.getLastRow() == example.getLastRow());
    }

    public static PropertyChangeEvent matches(final PropertyChangeEvent example) {
        return Mockito.argThat(actual -> actual.getSource() == example.getSource() &&
                actual.getPropertyName().equals(example.getPropertyName()) &&
                Objects.equals(actual.getOldValue(), example.getOldValue()) &&
                Objects.equals(actual.getNewValue(), example.getNewValue()));
    }

    public static ListDataEvent listDataEvent(Object source, int type, int index0, int index1) {
        return Mockito.argThat(actual -> actual.getSource() == source &&
                actual.getType() == type &&
                actual.getIndex0() == index0 &&
                actual.getIndex1() == index1);
    }
}
