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

import java.awt.Component;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JTable;

import io.github.jonestimd.swing.component.CalendarButtonBorder;
import io.github.jonestimd.swing.component.DateField;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.Assertions.*;

@RunWith(MockitoJUnitRunner.class)
public class DateCellEditorTest {
    public static final String DATE_FORMAT = "MM/dd/yyyy";
    @Mock
    private JTable table;
    private final DateCellEditor editor = new DateCellEditor(DATE_FORMAT);

    @Test
    public void setsBorderToNull() throws Exception {
        Component component = editor.getTableCellEditorComponent(table, null, false, 0, 0);

        assertThat(component).isInstanceOf(DateField.class);
        assertThat(((DateField) component).getBorder()).isInstanceOf(CalendarButtonBorder.class);
    }

    @Test
    public void validEdit() throws Exception {
        final String dateString = "01/13/2015";
        DateField component = (DateField) editor.getTableCellEditorComponent(table, new Date(), false, 0, 0);
        component.setText(dateString);

        editor.stopCellEditing();

        assertThat(editor.getCellEditorValue()).isEqualTo(new SimpleDateFormat(DATE_FORMAT).parse(dateString));
    }

    @Test
    public void invalidEdit() throws Exception {
        final Date date = new Date();
        DateField component = (DateField) editor.getTableCellEditorComponent(table, date, false, 0, 0);
        component.setText("x");

        editor.stopCellEditing();

        assertThat(editor.getCellEditorValue()).isSameAs(date);
    }
}