package io.github.jonestimd.mockito;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.event.TableModelEvent;

import com.google.common.base.Joiner;
import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

public class Matchers {
    public static <T> Collection<T> containsOnly(final Collection<T> items) {
        final Set<T> uniqueItems = new HashSet<T>(items);
        return Mockito.argThat(new ArgumentMatcher<Collection<T>>() {
            @Override
            @SuppressWarnings("unchecked")
            public boolean matches(Object obj) {
                Set<T> arg = new HashSet<T>((Collection<T>) obj);
                return arg.size() == uniqueItems.size() && arg.containsAll(uniqueItems);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(items.toString());
            }
        });
    }

    public static <T> Stream<T> emptyStream() {
        return streamOf(Collections.emptyList());
    }

    public static <T> Stream<T> streamOf(List<T> items) {
        return Mockito.argThat(new ArgumentMatcher<Stream<T>>() {
            private final Map<Stream<T>, List<T>> streamItems = new HashMap<>();

            @Override
            @SuppressWarnings("unchecked")
            public boolean matches(Object obj) {
                List<T> arg = streamItems.computeIfAbsent((Stream<T>) obj, Matchers::collect);
                return items.equals(arg);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Stream{" + Joiner.on(',').join(items) + "}");
            }
        });
    }

    private static <T> List<T> collect(Stream<T> stream) {
        return stream.collect(Collectors.toList());
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
