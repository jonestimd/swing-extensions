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

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

import com.google.common.collect.Lists;
import io.github.jonestimd.swing.validation.RequiredValidator;
import io.github.jonestimd.swing.validation.ValidationBorder;
import org.junit.Test;

import static io.github.jonestimd.mockito.Matchers.matches;
import static io.github.jonestimd.swing.component.FilterComboBox.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FilterComboBoxTest {
    private static final List<String> items = Lists.newArrayList(
        "Apple", "Banana", "Blueberry", "Cherry", "Grape", "Peach", "Pineapple", "Raspberry"
    );
    private final PropertyChangeListener listener = mock(PropertyChangeListener.class);
    private final String message = "selection is required";

    @Test
    public void setAutoSelectTextFiresPropertyChange() throws Exception {
        FilterComboBox<String> comboBox = new FilterComboBox<>(newModel(Collections.emptyList()));
        comboBox.addPropertyChangeListener(AUTO_SELECT_TEXT, listener);

        comboBox.setAutoSelectText(false);

        verify(listener).propertyChange(matches(new PropertyChangeEvent(comboBox, AUTO_SELECT_TEXT, true, false)));
        assertThat(comboBox.isAutoSelectText()).isFalse();
    }

    @Test
    public void setAutoSelectItemFiresPropertyChange() throws Exception {
        FilterComboBox<String> comboBox = new FilterComboBox<>(newModel(Collections.emptyList()));
        comboBox.addPropertyChangeListener(AUTO_SELECT_ITEM, listener);

        comboBox.setAutoSelectItem(false);

        verify(listener).propertyChange(matches(new PropertyChangeEvent(comboBox, AUTO_SELECT_ITEM, true, false)));
        assertThat(comboBox.isAutoSelectItem()).isFalse();
    }

    @Test
    public void setSetTextOnFocusLostFiresPropertyChange() throws Exception {
        FilterComboBox<String> comboBox = new FilterComboBox<>(newModel(Collections.emptyList()));
        comboBox.addPropertyChangeListener(SET_TEXT_ON_FOCUS_LOST, listener);

        comboBox.setSetTextOnFocusLost(false);

        verify(listener).propertyChange(matches(new PropertyChangeEvent(comboBox, SET_TEXT_ON_FOCUS_LOST, true, false)));
        assertThat(comboBox.isSetTextOnFocusLost()).isFalse();
    }

    @Test
    public void autoSelectSingleMatch() throws Exception {
        FilterComboBox<String> comboBox = new FilterComboBox<>(newModel(items));

        comboBox.setText("bl");

        assertThat(comboBox.getSelectedItem()).isEqualTo("Blueberry");
        assertThat(comboBox.getPopupList().getSelectedIndex()).isEqualTo(0);
    }

    @Test
    public void unselectsAutoSelectedItem() throws Exception {
        FilterComboBox<String> comboBox = new FilterComboBox<>(newModel(items));
        comboBox.setText("bl");

        comboBox.setText("b");

        assertThat(comboBox.getSelectedItem()).isNull();
    }
    @Test
    public void clearsManualSelectionWhenFilteredFromList() throws Exception {
        FilterComboBox<String> comboBox = new FilterComboBox<>(newModel(items));
        comboBox.getModel().setSelectedItem(items.get(0));

        comboBox.setText("b");

        assertThat(comboBox.getSelectedItem()).isNull();
    }

    @Test
    public void retainsManualSelectionAfterSingleMatch() throws Exception {
        FilterComboBox<String> comboBox = new FilterComboBox<>(newModel(items));
        comboBox.getModel().setSelectedItem(items.get(2));

        comboBox.setText("bl");
        comboBox.setText("b");

        assertThat(comboBox.getSelectedItem()).isEqualTo(items.get(2));
    }

    @Test
    public void updatesListSelectedIndexOnModelChange() throws Exception {
        FilterComboBox<String> comboBox = new FilterComboBox<>(newModel(items));

        comboBox.getModel().setSelectedItem("Blueberry");

        assertThat(comboBox.getPopupList().getSelectedIndex()).isEqualTo(items.indexOf("Blueberry"));
    }

    @Test
    public void updatesListSelectedIndexOnFilterChange() throws Exception {
        FilterComboBox<String> comboBox = new FilterComboBox<>(newModel(items));
        comboBox.getModel().setSelectedItem("Blueberry");

        comboBox.setText("b");

        assertThat(comboBox.getPopupList().getSelectedIndex()).isEqualTo(1);
    }

    @Test
    public void validatesSelectedValue() throws Exception {
        FilterComboBox<String> comboBox = new FilterComboBox<>(newModel(items), new RequiredValidator(message), null);
        comboBox.addValidationListener(listener);

        assertThat(comboBox.getValidationMessages()).isEqualTo(message);
        comboBox.getModel().setSelectedItem(items.get(0));

        assertThat(comboBox.getValidationMessages()).isNull();
        verify(listener).propertyChange(matches(new PropertyChangeEvent(comboBox, VALIDATION_MESSAGES, message, null)));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void setNonNullBorderWrappsValidationBorder() throws Exception {
        FilterComboBox<String> comboBox = new FilterComboBox<>(newModel(items));
        Border border = BorderFactory.createEmptyBorder();

        comboBox.setBorder(border);

        assertThat(comboBox.getBorder()).isInstanceOf(CompoundBorder.class);
        CompoundBorder compoundBorder = (CompoundBorder) comboBox.getBorder();
        assertThat(compoundBorder.getInsideBorder()).isInstanceOf(ValidationBorder.class);
        assertThat(compoundBorder.getOutsideBorder()).isSameAs(border);
    }

    @Test
    public void setNullBorderWrapsValidationBorder() throws Exception {
        FilterComboBox<String> comboBox = new FilterComboBox<>(newModel(items));

        comboBox.setBorder(null);

        assertThat(comboBox.getBorder()).isInstanceOf(ValidationBorder.class);
    }

    @Test
    public void getCursor() throws Exception {
        FilterComboBox<String> comboBox = new FilterComboBox<>(newModel(items), new RequiredValidator(message), null);
        comboBox.validateValue();
        assertThat(comboBox.getCursor()).isNotEqualTo(Cursor.getDefaultCursor());
        for (MouseMotionListener listener : comboBox.getMouseMotionListeners()) {
            listener.mouseMoved(new MouseEvent(comboBox, MouseEvent.MOUSE_MOVED, 0L, 0, comboBox.getWidth(), 0, 0, false));
        }

        assertThat(comboBox.getCursor()).isEqualTo(Cursor.getDefaultCursor());
    }

    @Test
    public void getTooltip() throws Exception {
        FilterComboBox<String> comboBox = new FilterComboBox<>(newModel(items), new RequiredValidator(message), null);
        comboBox.validateValue();
        assertThat(comboBox.getToolTipText()).isNull();
        for (MouseMotionListener listener : comboBox.getMouseMotionListeners()) {
            listener.mouseMoved(new MouseEvent(comboBox, MouseEvent.MOUSE_MOVED, 0L, 0, comboBox.getWidth(), 0, 0, false));
        }

        assertThat(comboBox.getToolTipText()).isEqualTo(message);
    }

    private ContainsFilterComboBoxModel<String> newModel(List<String> items) {
        return new ContainsFilterComboBoxModel<>(items, Function.identity());
    }
}