// The MIT License (MIT)
//
// Copyright (c) 2018 Timothy D. Jones
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
package io.github.jonestimd.swing.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.Arrays;

import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.ColorUIResource;

import io.github.jonestimd.swing.component.MultiSelectItem;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MultiSelectTableCellRendererTest {
    @Mock
    private JTable table;

    private Font font = new Font(Font.DIALOG, Font.PLAIN, 12);

    @Before
    public void setupTable() throws Exception {
        when(table.getFont()).thenReturn(font);
    }

    @Test
    public void addsMultiSelectItemPerListItem() throws Exception {
        when(table.getBackground()).thenReturn(Color.PINK);
        when(table.getForeground()).thenReturn(Color.BLUE);
        MultiSelectTableCellRenderer<String> renderer = new MultiSelectTableCellRenderer<>(true);

        renderer.getTableCellRendererComponent(table, Arrays.asList("one", "two"), false, false, 0, 0);

        assertThat(renderer.getComponentCount()).isEqualTo(2);
        verifyComponent(renderer.getComponent(0), "one");
        verifyComponent(renderer.getComponent(1), "two");
        assertThat(renderer.getBackground()).isEqualTo(Color.PINK);
        assertThat(renderer.getForeground()).isEqualTo(Color.BLUE);
        assertThat(renderer.getBorder()).isInstanceOf(EmptyBorder.class);
    }

    protected void verifyComponent(Component component, String text) {
        assertThat(component).isInstanceOf(MultiSelectItem.class);
        assertThat(component.getFont()).isSameAs(font);
        assertThat(((MultiSelectItem) component).isFill()).isTrue();
        assertThat(((MultiSelectItem) component).getText()).isEqualTo(text);
        assertThat(((MultiSelectItem) component).isShowDelete()).isFalse();
    }

    @Test
    public void usesRendererColors() throws Exception {
        MultiSelectTableCellRenderer<String> renderer = new MultiSelectTableCellRenderer<>(true);
        renderer.setBackground(Color.YELLOW);
        renderer.setForeground(Color.GREEN);

        renderer.getTableCellRendererComponent(table, null, false, false, 0, 0);

        assertThat(renderer.getBackground()).isEqualTo(Color.YELLOW);
        assertThat(renderer.getForeground()).isEqualTo(Color.GREEN);
    }

    @Test
    public void highlightsSelectedRow() throws Exception {
        when(table.getSelectionBackground()).thenReturn(Color.PINK);
        when(table.getSelectionForeground()).thenReturn(Color.BLUE);
        MultiSelectTableCellRenderer<String> renderer = new MultiSelectTableCellRenderer<>(true);

        renderer.getTableCellRendererComponent(table, null, true, false, 0, 0);

        assertThat(renderer.getBackground()).isEqualTo(Color.PINK);
        assertThat(renderer.getForeground()).isEqualTo(Color.BLUE);
    }

    @Test
    public void drawsFocusBorder() throws Exception {
        MultiSelectTableCellRenderer<String> renderer = new MultiSelectTableCellRenderer<>(true);

        renderer.getTableCellRendererComponent(table, Arrays.asList("one", "two"), false, true, 0, 0);

        assertThat(renderer.getBorder()).isInstanceOf(LineBorder.class);
    }

    @Test
    public void drawsFocusSelectedBorder() throws Exception {
        MultiSelectTableCellRenderer<String> renderer = new MultiSelectTableCellRenderer<>(true);

        renderer.getTableCellRendererComponent(table, null, true, true, 0, 0);

        assertThat(renderer.getBorder()).isInstanceOf(LineBorder.class);
    }

    @Test
    public void changesColorWhenFocusedAndEditable() throws Exception {
        when(table.isCellEditable(anyInt(), anyInt())).thenReturn(true);
        MultiSelectTableCellRenderer<String> renderer = new MultiSelectTableCellRenderer<>(true);

        renderer.getTableCellRendererComponent(table, null, false, true, 0, 0);

        assertThat(renderer.getBackground()).isInstanceOf(ColorUIResource.class);
        assertThat(renderer.getBackground()).isEqualTo(Color.WHITE);
        assertThat(renderer.getForeground()).isInstanceOf(ColorUIResource.class);
        // assertThat(renderer.getForeground()).isEqualTo(new Color(51, 51, 51));
    }
}