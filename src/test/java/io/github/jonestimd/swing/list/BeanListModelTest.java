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
package io.github.jonestimd.swing.list;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static io.github.jonestimd.mockito.Matchers.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class BeanListModelTest {
    @Mock
    private ListDataListener listDataListener;

    @Test
    public void getSizeReturnsSize() throws Exception {
        assertThat(new BeanListModel<>().getSize()).isEqualTo(0);
        assertThat(new BeanListModel<>(Arrays.asList("one", "two")).getSize()).isEqualTo(2);
    }

    @Test
    public void addElementAllowsNull() throws Exception {
        BeanListModel<String> model = new BeanListModel<>();
        model.addListDataListener(listDataListener);

        model.addElement(null);

        assertThat(model).hasSize(1);
        assertThat(model.getElementAt(0)).isNull();
        verify(listDataListener).intervalAdded(listDataEvent(model, ListDataEvent.INTERVAL_ADDED, 0, 0));
        verifyNoMoreInteractions(listDataListener);
    }

    @Test
    public void addElementAppendsToTheList() throws Exception {
        BeanListModel<String> model = new BeanListModel<>();
        model.addListDataListener(listDataListener);

        model.addElement("a");
        model.addElement("b");

        assertThat(model.getSize()).isEqualTo(2);
        assertThat(model.getElementAt(0)).isEqualTo("a");
        assertThat(model.getElementAt(1)).isEqualTo("b");
        verify(listDataListener).intervalAdded(listDataEvent(model, ListDataEvent.INTERVAL_ADDED, 0, 0));
        verify(listDataListener).intervalAdded(listDataEvent(model, ListDataEvent.INTERVAL_ADDED, 1, 1));
        verifyNoMoreInteractions(listDataListener);
    }

    @Test
    public void addMissingElementsAddsAllItemsWhenEmpty() throws Exception {
        BeanListModel<String> model = new BeanListModel<>();
        model.addListDataListener(listDataListener);

        model.addMissingElements(Arrays.asList("one", "two"));

        assertThat(model).containsExactly("one", "two");
        verify(listDataListener).intervalAdded(listDataEvent(model, ListDataEvent.INTERVAL_ADDED, 0, 0));
        verify(listDataListener).intervalAdded(listDataEvent(model, ListDataEvent.INTERVAL_ADDED, 1, 1));
        verifyNoMoreInteractions(listDataListener);
    }

    @Test
    public void addMissingElementsSkipsExistingItems() throws Exception {
        BeanListModel<String> model = new BeanListModel<>(Collections.singletonList("one"));
        model.addListDataListener(listDataListener);

        model.addMissingElements(Arrays.asList("one", "two"));

        assertThat(model).containsExactly("one", "two");
        verify(listDataListener).intervalAdded(listDataEvent(model, ListDataEvent.INTERVAL_ADDED, 1, 1));
        verifyNoMoreInteractions(listDataListener);
    }

    @Test
    public void addMissingElementsSkipsNullItem() throws Exception {
        BeanListModel<String> model = new BeanListModel<>(Arrays.asList(null, "two"));
        model.addListDataListener(listDataListener);

        model.addMissingElements(Arrays.asList("one", "two"));

        assertThat(model).containsExactly(null, "one", "two");
        verify(listDataListener).intervalAdded(listDataEvent(model, ListDataEvent.INTERVAL_ADDED, 1, 1));
        verifyNoMoreInteractions(listDataListener);
    }

    @Test
    public void indexOfReturnsIndex() throws Exception {
        BeanListModel<String> model = new BeanListModel<>(Arrays.asList("one", "two"));

        assertThat(model.indexOf("one")).isEqualTo(0);
        assertThat(model.indexOf("two")).isEqualTo(1);
    }

    @Test
    public void insertElementAtAllowsNull() throws Exception {
        BeanListModel<String> model = new BeanListModel<>();
        model.addListDataListener(listDataListener);

        model.insertElementAt(null, 0);

        assertThat(model).hasSize(1);
        assertThat(model.getElementAt(0)).isNull();
        verify(listDataListener).intervalAdded(listDataEvent(model, ListDataEvent.INTERVAL_ADDED, 0, 0));
    }

    @Test
    public void removeElementAt() throws Exception {
        BeanListModel<String> model = new BeanListModel<>();
        model.setElements(Arrays.asList("a", "b"));
        model.addListDataListener(listDataListener);

        model.removeElementAt(0);

        assertThat(model.getSize()).isEqualTo(1);
        assertThat(model.getElementAt(0)).isEqualTo("b");
        verify(listDataListener).intervalRemoved(listDataEvent(model, ListDataEvent.INTERVAL_REMOVED, 0, 0));
        verifyNoMoreInteractions(listDataListener);
    }

    @Test
    public void removeElement() throws Exception {
        BeanListModel<String> model = new BeanListModel<>(Arrays.asList("one", "two", "three"));
        model.addListDataListener(listDataListener);

        model.removeElement("three");

        assertThat(model).hasSize(2);
        verify(listDataListener).intervalRemoved(listDataEvent(model, ListDataEvent.INTERVAL_REMOVED, 2, 2));
        verifyNoMoreInteractions(listDataListener);
    }

    @Test
    public void removeElementIgnoresUnknownElement() throws Exception {
        BeanListModel<String> model = new BeanListModel<>(Arrays.asList("one", "two"));
        model.addListDataListener(listDataListener);

        model.removeElement("three");

        assertThat(model).hasSize(2);
        verifyNoInteractions(listDataListener);
    }

    @Test
    public void removeAllDoesNothingIfEmpty() throws Exception {
        BeanListModel<String> model = new BeanListModel<>();
        model.addListDataListener(listDataListener);

        model.removeAll();

        verifyNoInteractions(listDataListener);
    }

    @Test
    public void removeAllClearsElements() throws Exception {
        BeanListModel<String> model = new BeanListModel<>(Arrays.asList("one", "two"));
        model.addListDataListener(listDataListener);

        model.removeAll();

        verify(listDataListener).intervalRemoved(listDataEvent(model, ListDataEvent.INTERVAL_REMOVED, 0, 1));
        verifyNoMoreInteractions(listDataListener);
    }

    @Test
    public void setElementsReplacesTheListItems() throws Exception {
        BeanListModel<String> model = new BeanListModel<>();

        model.setElements(Arrays.asList("a", "b"));

        assertThat(model.getSize()).isEqualTo(2);
        assertThat(model.getElementAt(0)).isEqualTo("a");
        assertThat(model.getElementAt(1)).isEqualTo("b");
    }

    @Test
    public void contains() throws Exception {
        BeanListModel<String> model = new BeanListModel<>(Arrays.asList("one", "two"));

        assertThat(model.contains("one")).isTrue();
        assertThat(model.contains("One")).isFalse();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void iteratorIsReadOnly() throws Exception {
        BeanListModel<String> model = new BeanListModel<>();
        model.addElement("one");
        model.addElement("two");

        Iterator<String> iterator = model.iterator();
        iterator.remove();
    }
}