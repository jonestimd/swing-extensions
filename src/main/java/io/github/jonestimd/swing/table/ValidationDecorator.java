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

import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

import io.github.jonestimd.swing.table.model.BeanTableModel;
import io.github.jonestimd.swing.table.model.ValidatedTableModel;
import io.github.jonestimd.swing.validation.ValidationBorder;

public class ValidationDecorator implements TableDecorator {
    private ValidationBorder validationBorder = new ValidationBorder();

    public <B, M extends BeanTableModel<B>> void prepareRenderer(DecoratedTable<B, M> table, JComponent renderer, int row, int column) {
        String errors = ((ValidatedTableModel) table.getModel()).validateAt(row, column);
        validationBorder.setValid(errors == null);
        addValidationBorder(renderer);
        renderer.setToolTipText(errors == null ? null : errors);
    }

    private void addValidationBorder(JComponent component) {
        Border border = component.getBorder();
        if (border == null) {
            component.setBorder(validationBorder);
        }
        else {
            component.setBorder(new CompoundBorder(border, validationBorder));
        }
    }
}