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

import java.awt.Color;
import java.math.BigDecimal;
import java.text.Format;

/**
 * A table cell renderer that uses different colors for positive and negative numbers.
 */
public class CurrencyTableCellRenderer extends FormatTableCellRenderer {
    private final Color positiveColor;
    private final Color negativeColor;

    public CurrencyTableCellRenderer(Format format) {
        this(format, Highlighter.NOOP_HIGHLIGHTER);
    }

    public CurrencyTableCellRenderer(Format format, Highlighter highlighter) {
        this(format, highlighter, Color.red, Color.black);
        setHorizontalAlignment(RIGHT);
    }

    public CurrencyTableCellRenderer(Format format, Highlighter highlighter, Color negativeColor, Color positiveColor) {
        super(format, highlighter);
        this.negativeColor = negativeColor;
        this.positiveColor = positiveColor;
        setHorizontalAlignment(RIGHT);
    }

    protected void setValue(Object value) {
        super.setValue(value);
        if (value != null && ((BigDecimal) value).signum() < 0) {
            setForeground(negativeColor);
        }
        else {
            setForeground(positiveColor);
        }
    }
}