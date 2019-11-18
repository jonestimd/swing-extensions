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
package io.github.jonestimd.swing.table;

import java.math.BigDecimal;
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;

import io.github.jonestimd.swing.component.IconSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.*;

@RunWith(MockitoJUnitRunner.class)
public class FormatTableCellRendererTest {
    @Mock
    private Icon icon;

    @Test
    public void testDateTableCellRenderer() throws Exception {
        DateTableCellRenderer renderer = new DateTableCellRenderer("MM/dd/yyyy");
        Date value = new Date();

        renderer.getTableCellRendererComponent(new JTable(), value, false, false, 0, 0);

        assertThat(renderer.getText()).isEqualTo(new SimpleDateFormat("MM/dd/yyyy").format(value));
        assertThat(renderer.getHorizontalAlignment()).isEqualTo(JLabel.LEADING);
    }

    @Test
    public void testFormatTableCellRenderer_NumberFormat() throws Exception {
        NumberFormat numberInstance = NumberFormat.getNumberInstance();
        numberInstance.setMaximumFractionDigits(9);
        FormatTableCellRenderer renderer = new FormatTableCellRenderer(numberInstance);
        BigDecimal value = new BigDecimal("98765.4321");

        renderer.getTableCellRendererComponent(new JTable(), value, false, false, 0, 0);

        assertThat(numberInstance.getMaximumFractionDigits()).isEqualTo(9);
        assertThat(renderer.getText()).isEqualTo("98,765.4321");
        assertThat(renderer.getHorizontalAlignment()).isEqualTo(JLabel.RIGHT);
    }

    @Test
    public void testFormatTableCellRenderer_NumberFormatWithNull() throws Exception {
        FormatTableCellRenderer renderer = new FormatTableCellRenderer(NumberFormat.getNumberInstance());

        renderer.getTableCellRendererComponent(new JTable(), null, false, false, 0, 0);

        assertThat(renderer.getText()).isEmpty();
        assertThat(renderer.getHorizontalAlignment()).isEqualTo(JLabel.RIGHT);
    }

    @Test
    public void testFormatTableCellRenderer_NullIconSource() throws Exception {
        FormatTableCellRenderer renderer = new FormatTableCellRenderer(NumberFormat.getInstance(), null, Highlighter.NOOP_HIGHLIGHTER);

        renderer.getTableCellRendererComponent(new JTable(), null, false, false, 0, 0);

        assertThat(renderer.getIcon()).isNull();
    }

    @Test
    public void testFormatTableCellRenderer_IconSourceFormat() throws Exception {
        FormatTableCellRenderer renderer = new FormatTableCellRenderer(new IconSourceFormat());

        renderer.getTableCellRendererComponent(new JTable(), null, false, false, 0, 0);

        assertThat(renderer.getIcon()).isSameAs(icon);
    }

    private class IconSourceFormat extends Format implements IconSource {
        @Override
        public Icon getIcon(Object bean) {
            return icon;
        }

        @Override
        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            return null;
        }

        @Override
        public Object parseObject(String source, ParsePosition pos) {
            return null;
        }
    }
}