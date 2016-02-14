package io.github.jonestimd.mockito;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.event.TableModelEvent;

import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

public class Matchers {
    public static <T> Collection<T> containsOnly(final Collection<T> items) {
        final Set<T> uniqueItems = new HashSet<>(items);
        return Mockito.argThat(new ArgumentMatcher<Collection<T>>() {
            @Override
            @SuppressWarnings("unchecked")
            public boolean matches(Object obj) {
                Set<T> arg = new HashSet<>((Collection<T>) obj);
                return arg.size() == uniqueItems.size() && arg.containsAll(uniqueItems);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(items.toString());
            }
        });
    }

    public static TableModelEvent matches(final TableModelEvent example) {
        return Mockito.argThat(new ArgumentMatcher<TableModelEvent>() {
            @Override
            public boolean matches(Object argument) {
                TableModelEvent actual = (TableModelEvent) argument;
                return actual.getSource() == example.getSource() &&
                        actual.getType() == example.getType() &&
                        actual.getColumn() == example.getColumn() &&
                        actual.getFirstRow() == example.getFirstRow() &&
                        actual.getLastRow() == example.getLastRow();
            }
        });
    }
}
