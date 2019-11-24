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
package io.github.jonestimd.swing.table;

import java.text.Format;
import java.text.NumberFormat;

import io.github.jonestimd.swing.component.IconSource;

/**
 * A table cell renderer that uses a {@link Format} to convert the value to a string.  If an {@link IconSource} is
 * provided then an icon will also be displayed in the cell.
 */
public class FormatTableCellRenderer extends HighlightTableCellRenderer {
    private final Format format;
    private final IconSource iconSource;

    /**
     * Create a table cell renderer without highlighting. Will display an icon if the format implements {@link IconSource}.
     * @param format the value format
     */
    public FormatTableCellRenderer(Format format) {
        this(format, Highlighter.NOOP_HIGHLIGHTER);
    }

    /**
     * Create a table cell renderer. Will display an icon if the format implements {@link IconSource}.
     * @param format the value format
     * @param highlighter the value highlighter
     */
    public FormatTableCellRenderer(Format format, Highlighter highlighter) {
        this(format, format instanceof IconSource ? (IconSource) format : IconSource.DEFAULT_ICON_SOURCE, highlighter);
    }

    /**
     * Create a table cell renderer.
     * @param format the value format
     * @param iconSource provides an icon to display in the cell
     * @param highlighter the value highlighter
     */
    public FormatTableCellRenderer(Format format, IconSource iconSource, Highlighter highlighter) {
        super(highlighter);
        this.format = format;
        this.iconSource = iconSource != null ? iconSource : IconSource.DEFAULT_ICON_SOURCE;
        if (format instanceof NumberFormat) {
            setHorizontalAlignment(RIGHT);
        }
    }

    protected void setValue(Object value) {
        super.setValue(value == null ? null : format.format(value));
        setIcon(iconSource.getIcon(value));
    }
}