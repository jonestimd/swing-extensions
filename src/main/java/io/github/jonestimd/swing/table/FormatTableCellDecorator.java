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

import java.text.Format;
import java.text.NumberFormat;

import javax.swing.JTable;
import javax.swing.SwingConstants;

import io.github.jonestimd.swing.component.IconSource;

/**
 * A table cell decorator that uses a {@link Format} to convert the value to a string.  If an {@link IconSource} is
 * provided then an icon will also be displayed in the cell.
 */
public class FormatTableCellDecorator implements TableCellDecorator {
    private final Format format;
    private final IconSource iconSource;
    private final Integer horizontalAlignment;

    /**
     * Create a table cell renderer without highlighting. Will display an icon if the format implements {@link IconSource}.
     * @param format the value format
     */
    public FormatTableCellDecorator(Format format) {
        this(format, format instanceof IconSource ? (IconSource) format : null);
    }

    /**
     * Create a table cell renderer.
     * @param format the value format
     * @param iconSource provides an icon to display in the cell
     */
    public FormatTableCellDecorator(Format format, IconSource iconSource) {
        this.format = format;
        this.iconSource = iconSource;
        horizontalAlignment = format instanceof NumberFormat ? SwingConstants.RIGHT : null;
    }

    @Override
    public Object configure(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column, CompositeTableCellRenderer renderer) {
        if (iconSource != null) renderer.setIcon(iconSource.getIcon(value));
        if (horizontalAlignment != null) renderer.setHorizontalAlignment(horizontalAlignment);
        return value == null ? null : format.format(value);
    }
}