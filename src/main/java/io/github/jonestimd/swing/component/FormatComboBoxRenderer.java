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
package io.github.jonestimd.swing.component;

import java.awt.Component;
import java.text.Format;

import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

/**
 * A combo box renderer that uses a {@link Format} to convert the value to a string.  If an {@link IconSource} is
 * provided then an icon will also be displayed for the combo box values.
 */
public class FormatComboBoxRenderer extends BasicComboBoxRenderer {
    private final Format format;
    private final IconSource iconSource;

    /**
     * Create a combo box renderer. Will display an icon if the format implements {@link IconSource}.
     * @param format the value format
     */
    public FormatComboBoxRenderer(Format format) {
        this(format, format instanceof IconSource ? (IconSource) format : IconSource.DEFAULT_ICON_SOURCE);
    }

    /**
     * Create a combo box renderer.
     * @param format the value format
     * @param iconSource provides an icon to display in the combo box
     */
    public FormatComboBoxRenderer(Format format, IconSource iconSource) {
        this.format = format;
        this.iconSource = iconSource != null ? iconSource : IconSource.DEFAULT_ICON_SOURCE;
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        setIcon(iconSource.getIcon(value));
        String text = value == null ? null : format.format(value);
        return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
    }
}