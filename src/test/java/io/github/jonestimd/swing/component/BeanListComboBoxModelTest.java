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
public class BeanListComboBoxModelTest {
    @Mock
    private ListDataListener listDataListener;

    @Test
    public void removeAllClearsSelectedElement() throws Exception {
        BeanListComboBoxModel<String> model = new BeanListComboBoxModel<>(Arrays.asList("one", "two"));
        model.setSelectedItem("two");
        model.addListDataListener(listDataListener);

        model.removeAll();

        assertThat(model.getSelectedItem()).isNull();
        verify(listDataListener).intervalRemoved(listDataEvent(model, ListDataEvent.INTERVAL_REMOVED, 0, 1));
        verify(listDataListener).contentsChanged(listDataEvent(model, ListDataEvent.CONTENTS_CHANGED, -1, -1));
        verifyNoMoreInteractions(listDataListener);
    }

    @Test
    public void setSelectedElementAllowsNull() throws Exception {
        BeanListComboBoxModel<String> model = new BeanListComboBoxModel<>(Arrays.asList("one", "two"));
        model.setSelectedItem("one");
        model.addListDataListener(listDataListener);

        model.setSelectedItem(null);

        assertThat(model.getSelectedItem()).isNull();
        verify(listDataListener).contentsChanged(listDataEvent(model, ListDataEvent.CONTENTS_CHANGED, -1, -1));
        verifyNoMoreInteractions(listDataListener);
    }

    @Test
    public void setSelectedElement() throws Exception {
        BeanListComboBoxModel<String> model = new BeanListComboBoxModel<>();
        model.addListDataListener(listDataListener);

        model.setSelectedItem("one");

        assertThat(model.getSelectedItem()).isEqualTo("one");
        verify(listDataListener).contentsChanged(listDataEvent(model, ListDataEvent.CONTENTS_CHANGED, -1, -1));
        verifyNoMoreInteractions(listDataListener);
    }

    @Test
    public void setElementsReplacesElements() throws Exception {
        BeanListComboBoxModel<String> model = new BeanListComboBoxModel<>(Arrays.asList("one", "two"));
        model.addListDataListener(listDataListener);

        model.setElements(Arrays.asList("ONE", "TWO"), false);

        assertThat(model).hasSize(2);
        assertThat(model.getElementAt(0)).isEqualTo("ONE");
        assertThat(model.getElementAt(1)).isEqualTo("TWO");
        assertThat(model.getSelectedItem()).isNull();
        verify(listDataListener).contentsChanged(listDataEvent(model, ListDataEvent.CONTENTS_CHANGED, 0, Integer.MAX_VALUE));
        verifyNoMoreInteractions(listDataListener);
    }

    @Test
    public void setElementsClearsSelection() throws Exception {
        BeanListComboBoxModel<String> model = new BeanListComboBoxModel<>(Arrays.asList("one", "two"));
        model.setSelectedItem("one");
        model.addListDataListener(listDataListener);

        model.setElements(Arrays.asList("ONE", "TWO"), false);

        assertThat(model.getSelectedItem()).isNull();
        verify(listDataListener).contentsChanged(listDataEvent(model, ListDataEvent.CONTENTS_CHANGED, 0, Integer.MAX_VALUE));
        verify(listDataListener).contentsChanged(listDataEvent(model, ListDataEvent.CONTENTS_CHANGED, -1, -1));
        verifyNoMoreInteractions(listDataListener);
    }

    @Test
    public void setElementsRetainsSelectionOfExistingItem() throws Exception {
        BeanListComboBoxModel<String> model = new BeanListComboBoxModel<>(Arrays.asList("one", "two"));
        model.setSelectedItem("one");
        model.addListDataListener(listDataListener);

        model.setElements(Arrays.asList("zero", "one", "two"), false);

        assertThat(model.getSelectedItem()).isEqualTo("one");
        verify(listDataListener).contentsChanged(listDataEvent(model, ListDataEvent.CONTENTS_CHANGED, 0, Integer.MAX_VALUE));
        verifyNoMoreInteractions(listDataListener);
    }

    @Test
    public void setElementsRetainsSelectionOfNonExistentItem() throws Exception {
        BeanListComboBoxModel<String> model = new BeanListComboBoxModel<>(Arrays.asList("one", "two"));
        model.setSelectedItem("three");
        model.addListDataListener(listDataListener);

        model.setElements(Arrays.asList("zero", "one", "two"), true);

        assertThat(model.getSelectedItem()).isEqualTo("three");
        verify(listDataListener).contentsChanged(listDataEvent(model, ListDataEvent.CONTENTS_CHANGED, 0, Integer.MAX_VALUE));
        verifyNoMoreInteractions(listDataListener);
    }
}