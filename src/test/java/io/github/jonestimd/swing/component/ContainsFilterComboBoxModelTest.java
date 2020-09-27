// The MIT License (MIT)
//
// Copyright (c) 2020 Timothy D. Jones
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import io.github.jonestimd.mockito.Matchers;
import org.junit.Test;
import org.mockito.internal.stubbing.answers.ReturnsArgumentAt;

import static com.google.common.collect.Lists.*;
import static io.github.jonestimd.swing.component.ContainsFilterComboBoxModel.*;
import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ContainsFilterComboBoxModelTest {
    private static final List<String> items = Arrays.asList(
        "Apple","Banana","Blueberry","Cherry","Grape","Peach","Pineapple","Raspberry"
    );

    private final ListDataListener listener = mock(ListDataListener.class);

    private ContainsFilterComboBoxModel<String> newModel(List<String> items, Function<String, String> format) {
        return new ContainsFilterComboBoxModel<>(items, format);
    }

    @Test
    public void getFormat() throws Exception {
        ContainsFilterComboBoxModel<String> model = newModel(items, Function.identity());

        assertThat(model.getFormat()).isSameAs(Function.identity());
    }

    @Test
    public void getElementsReturnsUnmodifiableUnfilteredList() throws Exception {
        ContainsFilterComboBoxModel<String> model = newModel(items, Function.identity());
        model.setFilter("xyz");

        assertThat(model.getElements()).isEqualTo(items);
        assertThat(model.getElements().getClass().getSimpleName()).startsWith("Unmodifiable");
    }

    @Test
    public void setElementsUpdatesFilteredList() throws Exception {
        ContainsFilterComboBoxModel<String> model = newModel(singletonList("xyz"), Function.identity());
        model.setFilter("berry");

        model.setElements(items);

        assertThat(model.getElements()).isEqualTo(items);
        assertThat(model.getMatches()).isEqualTo(newArrayList("Blueberry", "Raspberry"));
    }

    @Test
    public void setElementsKeepsSelection() throws Exception {
        ContainsFilterComboBoxModel<String> model = newModel(singletonList("xyz"), Function.identity());
        model.setSelectedItem("not a fruit");
        PropertyChangeListener listener = mock(PropertyChangeListener.class);
        model.addPropertyChangeListener(SELECTED_ITEM, listener);

        model.setElements(items, true);

        assertThat(model.getSelectedItem()).isEqualTo("not a fruit");
        verifyNoInteractions(listener);
    }

    @Test
    public void setElementsKeepsSelectedItemIfInList() throws Exception {
        ContainsFilterComboBoxModel<String> model = newModel(singletonList("xyz"), Function.identity());
        model.setSelectedItem("Cherry");

        model.setElements(items, false);

        assertThat(model.getSelectedItem()).isEqualTo("Cherry");
    }

    @Test
    public void setElementsClearsSelectedItemIfNotInList() throws Exception {
        ContainsFilterComboBoxModel<String> model = newModel(singletonList("xyz"), Function.identity());
        model.setSelectedItem("not a fruit");
        PropertyChangeListener listener = mock(PropertyChangeListener.class);
        model.addPropertyChangeListener(SELECTED_ITEM, listener);

        model.setElements(items, false);

        assertThat(model.getSelectedItem()).isNull();
        verify(listener).propertyChange(Matchers.matches(new PropertyChangeEvent(model, SELECTED_ITEM, "not a fruit", null)));
    }

    @Test
    public void containsReturnsTrueForItemInUnfilteredList() throws Exception {
        ContainsFilterComboBoxModel<String> model = newModel(items, Function.identity());
        model.setFilter("berry");

        assertThat(model.contains("Blueberry")).isTrue();
        assertThat(model.contains("Apple")).isTrue();
    }

    @Test
    public void setSelectedItem() throws Exception {
        ContainsFilterComboBoxModel<String> model = new ContainsFilterComboBoxModel<>(String::toUpperCase);
        String unformatted = "unformatted";
        PropertyChangeListener listener = mock(PropertyChangeListener.class);
        model.addPropertyChangeListener(SELECTED_ITEM, listener);

        model.setSelectedItem(unformatted);

        assertThat(model.getSelectedItem()).isEqualTo(unformatted);
        assertThat(model.getSelectedItemText()).isEqualTo(unformatted.toUpperCase());
        assertThat(model.getSelectedItemIndex()).isEqualTo(-1);
        verify(listener).propertyChange(Matchers.matches(new PropertyChangeEvent(model, SELECTED_ITEM, null, unformatted)));
    }

    @Test
    public void getSelectedItemIndex() throws Exception {
        ContainsFilterComboBoxModel<String> model = newModel(items, Function.identity());
        model.setFilter("berry");
        model.setSelectedItem("Blueberry");

        assertThat(model.getSelectedItemIndex()).isEqualTo(0);
    }

    @Test
    public void getSelectedItemTextReturnsEmptyStringForNull() throws Exception {
        ContainsFilterComboBoxModel<String> model = newModel(items, Function.identity());

        assertThat(model.getSelectedItemText()).isEqualTo("");
    }

    @Test
    public void getSelectedItemTextReturnsFormattedItem() throws Exception {
        ContainsFilterComboBoxModel<String> model = newModel(items, String::toUpperCase);
        model.setSelectedItem("Pawpaw");

        assertThat(model.getSelectedItemText()).isEqualTo("PAWPAW");
    }

    @Test
    public void addElementAddsToUnfilteredList() throws Exception {
        ContainsFilterComboBoxModel<String> model = newModel(items, String::toUpperCase);
        model.setFilter("apple");

        model.addElement("Blackberry");

        assertThat(model.getElements()).endsWith("Blackberry");
        assertThat(model.getMatches().contains("Blackberry")).isFalse();
    }

    @Test
    public void addElementAddsMatchToFilteredList() throws Exception {
        ContainsFilterComboBoxModel<String> model = newModel(items, String::toUpperCase);
        model.setFilter("berry");

        model.addElement("Blackberry");

        assertThat(model.getElements()).endsWith("Blackberry");
        assertThat(model.getMatches()).endsWith("Blackberry");
    }

    @Test
    public void addMissingElementAddsToUnfilteredList() throws Exception {
        ContainsFilterComboBoxModel<String> model = newModel(items, String::toUpperCase);
        model.setFilter("apple");

        List<String> newList = new ArrayList<>(items);
        newList.addAll(Arrays.asList("Blackberry", "Crab apple"));
        Collections.sort(newList);
        model.addMissingElements(newList);

        assertThat(model.getElements()).isEqualTo(newList);
        assertThat(model.getMatches().contains("Crab apple")).isTrue();
        assertThat(model.getMatches().contains("Blackberry")).isFalse();
    }

    @Test
    public void removeElementAtFiresListChangeForMatchingItem() throws Exception {
        ContainsFilterComboBoxModel<String> model = newModel(items, String::toUpperCase);
        model.setFilter("apple");
        model.addListDataListener(listener);

        model.removeElementAt(0);

        verify(listener).intervalRemoved(Matchers.listDataEvent(model, ListDataEvent.INTERVAL_REMOVED, 0, 0));
    }

    @Test
    public void removeElementAtDoesNotFireEventForFilteredItem() throws Exception {
        ContainsFilterComboBoxModel<String> model = newModel(items, String::toUpperCase);
        model.setFilter("apple");
        model.addListDataListener(listener);

        model.removeElementAt(1);

        verifyNoInteractions(listener);
    }

    @Test
    public void indexOfReturnsIndexInUnfilteredList() throws Exception {
        ContainsFilterComboBoxModel<String> model = newModel(items, String::toUpperCase);
        model.setFilter("apple");

        assertThat(model.indexOf("Cherry")).isEqualTo(3);
    }

    @Test
    public void iteratorUsesUnfilteredList() throws Exception {
        ContainsFilterComboBoxModel<String> model = newModel(items, String::toUpperCase);
        model.setFilter("apple");

        Iterator<String> iterator = model.iterator();

        int i;
        for (i = 0; iterator.hasNext(); i++) {
            assertThat(iterator.next()).isEqualTo(items.get(i));
        }
        assertThat(i).isEqualTo(items.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void setFilterIgnoresCase() throws Exception {
        Function<String, String> format = mock(Function.class, new ReturnsArgumentAt(0));
        ContainsFilterComboBoxModel<String> model = newModel(items, format);
        PropertyChangeListener listener = mock(PropertyChangeListener.class);
        model.addPropertyChangeListener(FILTER, listener);

        model.setFilter("berry");

        items.forEach(item -> verify(format).apply(item));
        assertThat(model.getSize()).isEqualTo(2);
        assertThat(model.getElementAt(0)).isEqualTo("Blueberry");
        assertThat(model.getElementAt(1)).isEqualTo("Raspberry");
        assertThat(model.getMatches()).isEqualTo(newArrayList("Blueberry", "Raspberry"));
        verify(listener).propertyChange(Matchers.matches(new PropertyChangeEvent(model, FILTER, "", "berry")));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void findElementSearchesFilteredItems() throws Exception {
        ContainsFilterComboBoxModel<String> model = newModel(items, String::toUpperCase);
        model.setFilter("apple");

        assertThat(model.findElement("apple").get()).isEqualTo("Apple");
        assertThat(model.findElement("pineapple").get()).isEqualTo("Pineapple");
        assertThat(model.findElement("cherry").isPresent()).isFalse();
    }
}