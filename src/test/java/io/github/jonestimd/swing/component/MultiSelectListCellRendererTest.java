// The MIT License (MIT)
//
// Copyright (c) 2019 Timothy D. Jones
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
import java.util.List;
import java.util.function.Function;

import javax.swing.JList;
import javax.swing.UIManager;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class MultiSelectListCellRendererTest {
    private static final Function<String, List<String>> GETTER = (value) -> Arrays.asList(value.split(","));

    private JList<String> listComponent = new JList<>();

    @Test
    public void defaultsToOpaque() throws Exception {
        MultiSelectListCellRenderer<String> renderer = new MultiSelectListCellRenderer<>(false, GETTER);

        assertThat(renderer.isOpaque()).isTrue();
        assertThat(renderer.isEnabled()).isEqualTo(listComponent.isEnabled());
        assertThat(renderer.getListCellRendererComponent(listComponent, "abc", 0, false, false)).isSameAs(renderer);
    }

    @Test
    public void usesListColors() throws Exception {
        MultiSelectListCellRenderer<String> renderer = new MultiSelectListCellRenderer<>(true, GETTER);

        renderer.getListCellRendererComponent(listComponent, "abc,xyz", 0, false, false);

        assertThat(renderer.getForeground()).isEqualTo(listComponent.getForeground());
        assertThat(renderer.getBackground()).isEqualTo(listComponent.getBackground());
    }

    @Test
    public void usesListSelectionColors() throws Exception {
        MultiSelectListCellRenderer<String> renderer = new MultiSelectListCellRenderer<>(true, GETTER);

        renderer.getListCellRendererComponent(listComponent, "abc,xyz", 0, true, false);

        assertThat(renderer.getForeground()).isEqualTo(listComponent.getSelectionForeground());
        assertThat(renderer.getBackground()).isEqualTo(listComponent.getSelectionBackground());
    }

    @Test
    public void displaysNothingForNullValue() throws Exception {
        MultiSelectListCellRenderer<String> renderer = new MultiSelectListCellRenderer<>(true, GETTER);

        renderer.getListCellRendererComponent(listComponent, null, 0, false, false);

        assertThat(renderer.getComponentCount()).isEqualTo(0);
    }

    @Test
    public void displaysMultiSelectItemsForValue() throws Exception {
        MultiSelectListCellRenderer<String> renderer = new MultiSelectListCellRenderer<>(true, GETTER);

        renderer.getListCellRendererComponent(listComponent, "abc,def", 0, false, false);

        assertThat(renderer.getComponentCount()).isEqualTo(2);
        assertThat(((MultiSelectItem) renderer.getComponent(0)).getText()).isEqualTo("abc");
        assertThat(((MultiSelectItem) renderer.getComponent(1)).getText()).isEqualTo("def");
    }

    @Test
    public void displaysFocusBorder() throws Exception {
        MultiSelectListCellRenderer<String> renderer = new MultiSelectListCellRenderer<>(true, GETTER);

        renderer.getListCellRendererComponent(listComponent, "abc", 0, false, true);

        assertThat(renderer.getBorder()).isEqualTo(UIManager.getBorder("List.focusCellHighlightBorder", renderer.getLocale()));
    }

    @Test
    public void displaysSelectedFocusBorder() throws Exception {
        MultiSelectListCellRenderer<String> renderer = new MultiSelectListCellRenderer<>(true, GETTER);

        renderer.getListCellRendererComponent(listComponent, "abc", 0, true, true);

        assertThat(renderer.getBorder()).isEqualTo(UIManager.getBorder("List.focusCellHighlightBorder", renderer.getLocale()));
    }
}