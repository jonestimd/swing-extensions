package io.github.jonestimd.swing.component;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static io.github.jonestimd.mockito.Matchers.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BeanListModelTest {
    @Mock
    private ListDataListener listDataListener;

    @Test
    public void getSizeReturnsSize() throws Exception {
        assertThat(new BeanListModel<String>().getSize()).isEqualTo(0);
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
    public void addAllElementAppendsElements() throws Exception {
        BeanListModel<String> model = new BeanListModel<>(Arrays.asList("one", "two"));
        model.addListDataListener(listDataListener);

        model.addAllElements(Arrays.asList("three", "four"));

        assertThat(model).hasSize(4);
        assertThat(model.getElementAt(2)).isEqualTo("three");
        assertThat(model.getElementAt(3)).isEqualTo("four");
        verify(listDataListener).intervalAdded(listDataEvent(model, ListDataEvent.INTERVAL_ADDED, 2, 3));
        verifyNoMoreInteractions(listDataListener);
    }

    @Test
    public void addAllElementAllowsEmptyList() throws Exception {
        BeanListModel<String> model = new BeanListModel<>(Arrays.asList("one", "two"));
        model.addListDataListener(listDataListener);

        model.addAllElements(Collections.emptyList());

        assertThat(model).hasSize(2);
        verifyZeroInteractions(listDataListener);
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
    public void removeElement() throws Exception {
        BeanListModel<String> model = new BeanListModel<>(Arrays.asList("one", "two"));
        model.addListDataListener(listDataListener);

        model.removeElement("one");

        assertThat(model).hasSize(1);
        assertThat(model.getElementAt(0)).isEqualTo("two");
        verify(listDataListener).intervalRemoved(listDataEvent(model, ListDataEvent.INTERVAL_REMOVED, 0, 0));
    }

    @Test
    public void removeElementIgnoresUnknownElement() throws Exception {
        BeanListModel<String> model = new BeanListModel<>(Arrays.asList("one", "two"));
        model.addListDataListener(listDataListener);

        model.removeElement("three");

        assertThat(model).hasSize(2);
        verifyZeroInteractions(listDataListener);
    }

    @Test
    public void removeElementAt() throws Exception {
        BeanListModel<String> model = new BeanListModel<>();
        model.addElement("one");
        model.addElement("two");
        model.addListDataListener(listDataListener);

        model.removeElementAt(1);

        assertThat(model).hasSize(1);
        assertThat(model.getElementAt(0)).isEqualTo("one");
        verify(listDataListener).intervalRemoved(listDataEvent(model, ListDataEvent.INTERVAL_REMOVED, 1, 1));
        verifyNoMoreInteractions(listDataListener);
    }

    @Test
    public void removeAllElementsDoesNothingIfEmpty() throws Exception {
        BeanListModel<String> model = new BeanListModel<>();
        model.addListDataListener(listDataListener);

        model.removeAllElements();

        verifyZeroInteractions(listDataListener);
    }

    @Test
    public void removeAllElementsClearsElements() throws Exception {
        BeanListModel<String> model = new BeanListModel<>(Arrays.asList("one", "two"));
        model.addListDataListener(listDataListener);

        model.removeAllElements();

        verify(listDataListener).intervalRemoved(listDataEvent(model, ListDataEvent.INTERVAL_REMOVED, 0, 1));
        verifyNoMoreInteractions(listDataListener);
    }

    @Test
    public void removeAllElementsClearsSelectedElement() throws Exception {
        BeanListModel<String> model = new BeanListModel<>(Arrays.asList("one", "two"));
        model.setSelectedItem("two");
        model.addListDataListener(listDataListener);

        model.removeAllElements();

        assertThat(model.getSelectedItem()).isNull();
        verify(listDataListener).intervalRemoved(listDataEvent(model, ListDataEvent.INTERVAL_REMOVED, 0, 1));
        verify(listDataListener).contentsChanged(listDataEvent(model, ListDataEvent.CONTENTS_CHANGED, -1, -1));
        verifyNoMoreInteractions(listDataListener);
    }

    @Test
    public void setSelectedElementAllowsNull() throws Exception {
        BeanListModel<String> model = new BeanListModel<>(Arrays.asList("one", "two"));
        model.setSelectedItem("one");
        model.addListDataListener(listDataListener);

        model.setSelectedItem(null);

        assertThat(model.getSelectedItem()).isNull();
        verify(listDataListener).contentsChanged(listDataEvent(model, ListDataEvent.CONTENTS_CHANGED, -1, -1));
        verifyNoMoreInteractions(listDataListener);
    }

    @Test
    public void setSelectedElement() throws Exception {
        BeanListModel<String> model = new BeanListModel<>();
        model.addListDataListener(listDataListener);

        model.setSelectedItem("one");

        assertThat(model.getSelectedItem()).isEqualTo("one");
        verify(listDataListener).contentsChanged(listDataEvent(model, ListDataEvent.CONTENTS_CHANGED, -1, -1));
        verifyNoMoreInteractions(listDataListener);
    }

    @Test
    public void setElementsReplacesElements() throws Exception {
        BeanListModel<String> model = new BeanListModel<>(Arrays.asList("one", "two"));
        model.addListDataListener(listDataListener);

        model.setElements(Arrays.asList("ONE", "TWO"), false);

        assertThat(model).hasSize(2);
        assertThat(model.getElementAt(0)).isEqualTo("ONE");
        assertThat(model.getElementAt(1)).isEqualTo("TWO");
        assertThat(model.getSelectedItem()).isNull();
        verify(listDataListener).intervalRemoved(listDataEvent(model, ListDataEvent.INTERVAL_REMOVED, 0, 1));
        verify(listDataListener).intervalAdded(listDataEvent(model, ListDataEvent.INTERVAL_ADDED, 0, 1));
        verifyNoMoreInteractions(listDataListener);
    }

    @Test
    public void setElementsClearsSelection() throws Exception {
        BeanListModel<String> model = new BeanListModel<>(Arrays.asList("one", "two"));
        model.setSelectedItem("one");
        model.addListDataListener(listDataListener);

        model.setElements(Arrays.asList("ONE", "TWO"), false);

        assertThat(model.getSelectedItem()).isNull();
        verify(listDataListener).intervalRemoved(listDataEvent(model, ListDataEvent.INTERVAL_REMOVED, 0, 1));
        verify(listDataListener).intervalAdded(listDataEvent(model, ListDataEvent.INTERVAL_ADDED, 0, 1));
        verify(listDataListener).contentsChanged(listDataEvent(model, ListDataEvent.CONTENTS_CHANGED, -1, -1));
        verifyNoMoreInteractions(listDataListener);
    }

    @Test
    public void setElementsRetainsSelectionOfExistingItem() throws Exception {
        BeanListModel<String> model = new BeanListModel<>(Arrays.asList("one", "two"));
        model.setSelectedItem("one");
        model.addListDataListener(listDataListener);

        model.setElements(Arrays.asList("zero", "one", "two"), false);

        assertThat(model.getSelectedItem()).isEqualTo("one");
        verify(listDataListener).intervalRemoved(listDataEvent(model, ListDataEvent.INTERVAL_REMOVED, 0, 1));
        verify(listDataListener).intervalAdded(listDataEvent(model, ListDataEvent.INTERVAL_ADDED, 0, 2));
        verifyNoMoreInteractions(listDataListener);
    }

    @Test
    public void setElementsRetainsSelectionOfNonExistentItem() throws Exception {
        BeanListModel<String> model = new BeanListModel<>(Arrays.asList("one", "two"));
        model.setSelectedItem("three");
        model.addListDataListener(listDataListener);

        model.setElements(Arrays.asList("zero", "one", "two"), true);

        assertThat(model.getSelectedItem()).isEqualTo("three");
        verify(listDataListener).intervalRemoved(listDataEvent(model, ListDataEvent.INTERVAL_REMOVED, 0, 1));
        verify(listDataListener).intervalAdded(listDataEvent(model, ListDataEvent.INTERVAL_ADDED, 0, 2));
        verifyNoMoreInteractions(listDataListener);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void iteratorIsReadOnly() throws Exception {
        BeanListModel<String> model = new BeanListModel<>(Lists.newArrayList("one", "two"));

        Iterator<String> iterator = model.iterator();
        iterator.remove();
    }
}