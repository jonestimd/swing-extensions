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

import java.awt.Component;
import java.text.ParseException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import io.github.jonestimd.swing.component.DateField;

/**
 * A table cell editor that provides a calendar popup for selecting a date.
 * @see DateField
 */
public class DateCellEditor extends AbstractCellEditor implements TableCellEditor {
    private static final Logger logger = Logger.getLogger(DateCellEditor.class.getName());
    private DateField editorComponent;

    public DateCellEditor(String pattern) {
        editorComponent = new DateField(pattern);
        editorComponent.setBorder(null);
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        editorComponent.setValue(value);
        return editorComponent;
    }

    public Date getCellEditorValue() {
        return editorComponent.getValue();
    }

    @Override
    public boolean stopCellEditing() {
        try {
            editorComponent.commitEdit();
        }
        catch (ParseException ex) {
            logger.log(Level.FINE, "parse exception on commit: {0}", ex.getMessage());
        }
        return super.stopCellEditing();
    }
}