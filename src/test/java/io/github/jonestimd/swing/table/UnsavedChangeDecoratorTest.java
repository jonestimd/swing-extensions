// The MIT License (MIT)
//
// Copyright (c) 2016 Timothy D. Jones
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
import java.awt.Font;
import java.awt.font.TextAttribute;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import io.github.jonestimd.swing.table.model.ChangeBufferTableModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UnsavedChangeDecoratorTest {
    @Mock
    private DecoratedTable<Object, ChangeBufferTableModel<Object>> table;
    @Mock
    private ChangeBufferTableModel<Object> model;

    private JLabel renderer = new JLabel();
    private Font font = renderer.getFont();
    private UnsavedChangeDecorator decorator = new UnsavedChangeDecorator();

    @Test
    public void setsStrikeoutFontForPendingDelete() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            when(table.getModel()).thenReturn(model);
            when(model.isPendingDelete(0)).thenReturn(true);
            renderer.setBackground(Color.lightGray);

            decorator.prepareRenderer(table, renderer, 0, 0);

            assertThat(renderer.getBackground()).isEqualTo(new Color(191, 130, 130));
            assertThat(renderer.getFont().getAttributes().get(TextAttribute.STRIKETHROUGH)).isEqualTo(TextAttribute.STRIKETHROUGH_ON);
        });
    }

    @Test
    public void setsBackgroundForPendingChange() throws Exception {
        when(table.getModel()).thenReturn(model);
        when(model.isPendingDelete(0)).thenReturn(false);
        when(model.isChangedAt(0, 0)).thenReturn(true);
        renderer.setBackground(Color.lightGray);

        decorator.prepareRenderer(table, renderer, 0, 0);

        assertThat(renderer.getBackground()).isEqualTo(new Color(0, 192, 192));
        assertThat(renderer.getFont()).isSameAs(font);
    }
}