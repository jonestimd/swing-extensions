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
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.google.common.collect.Lists;
import org.junit.Test;

import static io.github.jonestimd.mockito.Matchers.matches;
import static io.github.jonestimd.swing.component.FilterComboBox.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FilterComboBoxTest {
    private static final List<String> items = Lists.newArrayList(
        "Apple", "Banana", "Blueberry", "Cherry", "Grape", "Peach", "Pineapple", "Raspberry"
    );
    private PropertyChangeListener listener = mock(PropertyChangeListener.class);

    @Test
    public void setAutoSelectTextFiresPropertyChange() throws Exception {
        FilterComboBox<String> comboBox = new FilterComboBox<>(new ContainsFilterComboBoxModel<>(Collections.emptyList(), Function.identity()));
        comboBox.addPropertyChangeListener(AUTO_SELECT_TEXT, listener);

        comboBox.setAutoSelectText(false);

        verify(listener).propertyChange(matches(new PropertyChangeEvent(comboBox, AUTO_SELECT_TEXT, true, false)));
        assertThat(comboBox.isAutoSelectText()).isFalse();
    }

    @Test
    public void setAutoSelectItemFiresPropertyChange() throws Exception {
        FilterComboBox<String> comboBox = new FilterComboBox<>(new ContainsFilterComboBoxModel<>(Collections.emptyList(), Function.identity()));
        comboBox.addPropertyChangeListener(AUTO_SELECT_ITEM, listener);

        comboBox.setAutoSelectItem(false);

        verify(listener).propertyChange(matches(new PropertyChangeEvent(comboBox, AUTO_SELECT_ITEM, true, false)));
        assertThat(comboBox.isAutoSelectItem()).isFalse();
    }

    @Test
    public void autoSelectSingleMatch() throws Exception {
        FilterComboBox<String> comboBox = new FilterComboBox<>(new ContainsFilterComboBoxModel<>(items, Function.identity()));

        comboBox.setText("bl");

        assertThat(comboBox.getSelectedItem()).isEqualTo("Blueberry");
        assertThat(comboBox.getPopupList().getSelectedIndex()).isEqualTo(0);
    }

    @Test
    public void updatesListSelectedIndexOnModelChange() throws Exception {
        FilterComboBox<String> comboBox = new FilterComboBox<>(new ContainsFilterComboBoxModel<>(items, Function.identity()));

        comboBox.getModel().setSelectedItem("Blueberry");

        assertThat(comboBox.getPopupList().getSelectedIndex()).isEqualTo(items.indexOf("Blueberry"));
    }

    @Test
    public void updatesListSelectedIndexOnFilterChange() throws Exception {
        FilterComboBox<String> comboBox = new FilterComboBox<>(new ContainsFilterComboBoxModel<>(items, Function.identity()));
        comboBox.getModel().setSelectedItem("Blueberry");

        comboBox.setText("b");

        assertThat(comboBox.getPopupList().getSelectedIndex()).isEqualTo(1);
    }
}