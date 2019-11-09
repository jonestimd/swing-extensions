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
package io.github.jonestimd.swing.component;

import java.awt.Component;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class MultiSelectListCellRenderer<T> extends Box implements ListCellRenderer<T> {
    private static final Border SAFE_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
    private static final Border DEFAULT_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
    protected static Border noFocusBorder = DEFAULT_NO_FOCUS_BORDER;

    private final boolean opaque;
    private final Function<T, List<String>> getter;

    public MultiSelectListCellRenderer(boolean opaque, Function<T, List<String>> getter) {
        super(BoxLayout.X_AXIS);
        this.opaque = opaque;
        this.getter = getter;
        setOpaque(true);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends T> list, T value, int index, boolean isSelected, boolean cellHasFocus) {
        setComponentOrientation(list.getComponentOrientation());

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        }
        else {
            setForeground(list.getForeground());
            setBackground(list.getBackground());
        }

        removeAll();
        List<String> items = value == null ? Collections.emptyList() : getter.apply(value);
        if (items == null || items.isEmpty()) add(new JLabel("\u2023"));
        else {
            for (String item : items) {
                add(getMultiSelectItem(list, item));
            }
        }
        setEnabled(list.isEnabled());

        Border border = null;
        if (cellHasFocus) {
            if (isSelected) border = UIManager.getBorder("List.focusSelectedCellHighlightBorder", getLocale());
            if (border == null) border = UIManager.getBorder("List.focusCellHighlightBorder", getLocale());
        }
        else border = getNoFocusBorder();
        setBorder(border);

        return this;
    }

    protected MultiSelectItem getMultiSelectItem(JList<? extends T> list, String item) {
        MultiSelectItem comp = new MultiSelectItem(item, false, opaque);
        comp.setFont(list.getFont());
        return comp;
    }

    private Border getNoFocusBorder() {
        Border border = (Border) UIManager.get("cellNoFocusBorder", getLocale());
        if (System.getSecurityManager() != null) {
            return border != null ? border : SAFE_NO_FOCUS_BORDER;
        }
        if (border != null && (noFocusBorder == null || noFocusBorder == DEFAULT_NO_FOCUS_BORDER)) {
            return border;
        }
        return noFocusBorder;
    }
}
