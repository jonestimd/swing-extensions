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
import java.util.Collection;
import java.util.Collections;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import io.github.jonestimd.swing.ComponentTreeUtils;
import io.github.jonestimd.swing.HighlightText;

public class HighlightTableCellRenderer extends DefaultTableCellRenderer {
    private final Highlighter highlighter;
    private Collection<String> highlightText;

    public HighlightTableCellRenderer(Highlighter highlighter) {
        this.highlighter = highlighter;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        HighlightText highlightSource = ComponentTreeUtils.findAncestor(table, HighlightText.class);
        this.highlightText = highlightSource != null ? highlightSource.getHighlightText() : Collections.emptyList();
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }

    protected void setValue(Object value) {
        super.setValue(value == null ? null : highlighter.highlight(value.toString(), highlightText));
    }
}
