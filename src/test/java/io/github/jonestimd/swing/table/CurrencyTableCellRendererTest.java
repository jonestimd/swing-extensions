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
package io.github.jonestimd.swing.table;

import java.awt.Color;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Collections;

import javax.swing.JTable;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CurrencyTableCellRendererTest {
    @Test
    public void setsColorToRedForNegativeNumber() throws Exception {
        CurrencyTableCellRenderer renderer = new CurrencyTableCellRenderer(NumberFormat.getCurrencyInstance());

        renderer.getTableCellRendererComponent(new JTable(), BigDecimal.ONE.negate(), false, false, 0, 0);

        assertThat(renderer.getForeground()).isEqualTo(Color.RED);
    }

    @Test
    public void setsColorToBlackForNull() throws Exception {
        CurrencyTableCellRenderer renderer = new CurrencyTableCellRenderer(NumberFormat.getCurrencyInstance());

        renderer.getTableCellRendererComponent(new JTable(), null, false, false, 0, 0);

        assertThat(renderer.getForeground()).isEqualTo(Color.BLACK);
    }

    @Test
    public void setsColorToBlackForNonNegativeNumber() throws Exception {
        CurrencyTableCellRenderer renderer = new CurrencyTableCellRenderer(NumberFormat.getCurrencyInstance());

        renderer.getTableCellRendererComponent(new JTable(), BigDecimal.ZERO, false, false, 0, 0);

        assertThat(renderer.getForeground()).isEqualTo(Color.BLACK);
    }

    @Test
    public void highlightsTextInAmount() throws Exception {
        NumberFormat format = NumberFormat.getCurrencyInstance();
        Highlighter highlighter = mock(Highlighter.class);
        String highlighted = "highlighted";
        when(highlighter.highlight(anyString(), anyCollection())).thenReturn(highlighted);
        CurrencyTableCellRenderer renderer = new CurrencyTableCellRenderer(format, highlighter);

        renderer.getTableCellRendererComponent(new JTable(), BigDecimal.ZERO, false, false, 0, 0);

        assertThat(renderer.getText()).isEqualTo(highlighted);
        verify(highlighter).highlight(format.format(BigDecimal.ZERO), Collections.emptyList());
    }
}