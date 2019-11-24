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

import java.util.Arrays;
import java.util.function.Function;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static io.github.jonestimd.mockito.Matchers.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PredicateSuggestModelTest {
    @Mock
    private ListDataListener listener;

    @Test
    public void newModelIncludesAllItems() throws Exception {
        PredicateSuggestModel<String> model = new PredicateSuggestModel<>((x, y) -> false, Arrays.asList("Abc", "Def", "Xyz"), String::compareToIgnoreCase);

        assertThat(model).hasSize(3);
    }

    @Test
    public void contains() throws Exception {
        PredicateSuggestModel<String> model = new PredicateSuggestModel<>(Function.identity(), String::compareToIgnoreCase);
        model.setElements(Arrays.asList("Abc", "Bcd", "Cde"), false);
        model.addListDataListener(listener);

        assertThat(model.updateSuggestions("Bc")).isEqualTo("Bcd");

        assertThat(model).containsExactly("Bcd");
        verify(listener).intervalRemoved(listDataEvent(model, ListDataEvent.INTERVAL_REMOVED, 0, 2));
        verify(listener).intervalAdded(listDataEvent(model, ListDataEvent.INTERVAL_ADDED, 0, 0));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void containsIgnoreCase() throws Exception {
        PredicateSuggestModel<String> model = PredicateSuggestModel.ignoreCase(Function.identity(), String::compareToIgnoreCase);
        model.setElements(Arrays.asList("Abc", "Bcd", "Cde"), false);
        model.addListDataListener(listener);

        assertThat(model.updateSuggestions("Bc")).isEqualTo("Abc");

        assertThat(model).containsExactly("Abc", "Bcd");
        verify(listener).intervalRemoved(listDataEvent(model, ListDataEvent.INTERVAL_REMOVED, 0, 2));
        verify(listener).intervalAdded(listDataEvent(model, ListDataEvent.INTERVAL_ADDED, 0, 1));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void addItemUpdatesSuggestions() throws Exception {
        PredicateSuggestModel<String> model = PredicateSuggestModel.ignoreCase(Function.identity(), String::compareToIgnoreCase);
        model.setElements(Arrays.asList("Abc", "Bcd", "Cde"), false);
        model.updateSuggestions("Bc");
        model.addListDataListener(listener);

        model.addElement("a bcd");
        model.addElement("Def");

        assertThat(model).containsExactly("a bcd", "Abc", "Bcd");
        verify(listener).intervalRemoved(listDataEvent(model, ListDataEvent.INTERVAL_REMOVED, 0, 1));
        verify(listener).intervalAdded(listDataEvent(model, ListDataEvent.INTERVAL_ADDED, 0, 2));
        model.updateSuggestions("");
        assertThat(model).containsExactly("a bcd", "Abc", "Bcd", "Cde", "Def");
        verify(listener).intervalRemoved(listDataEvent(model, ListDataEvent.INTERVAL_REMOVED, 0, 2));
        verify(listener).intervalAdded(listDataEvent(model, ListDataEvent.INTERVAL_ADDED, 0, 4));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void indexOfReturnsIndexInUnfilteredList() throws Exception {
        PredicateSuggestModel<String> model = PredicateSuggestModel.ignoreCase(Function.identity(), String::compareToIgnoreCase);
        model.setElements(Arrays.asList("Abc", "Bcd", "Cde"), false);
        model.updateSuggestions("x");

        assertThat(model.indexOf("Abc")).isEqualTo(0);
        assertThat(model.indexOf("Bcd")).isEqualTo(1);
        assertThat(model.indexOf("Cde")).isEqualTo(2);
    }
}