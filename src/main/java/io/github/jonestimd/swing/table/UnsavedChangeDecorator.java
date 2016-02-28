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

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.awt.font.TextAttribute;
import java.util.Collections;
import java.util.Map;

import javax.swing.JComponent;

import io.github.jonestimd.swing.table.model.BeanTableModel;
import io.github.jonestimd.swing.table.model.ChangeBufferTableModel;

public class UnsavedChangeDecorator implements TableDecorator {
    private static final Map<TextAttribute, ?> STRIKETHRU_ATTRS =
            Collections.singletonMap(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
    private final Color pendingChangeBackground;
    private final Color pendingDeleteBackground;

    public UnsavedChangeDecorator() {
        this(Color.CYAN, Color.PINK);
    }

    public UnsavedChangeDecorator(Color pendingChangeBackground, Color pendingDeleteBackground) {
        this.pendingChangeBackground = pendingChangeBackground;
        this.pendingDeleteBackground = pendingDeleteBackground;
    }

    public <B, M extends BeanTableModel<B>> void prepareRenderer(DecoratedTable<B, M> table, JComponent renderer, int modelRow, int modelColumn) {
        if (((ChangeBufferTableModel<?>) table.getModel()).isPendingDelete(modelRow)) {
            renderer.setBackground(mixColor(renderer.getBackground(), pendingDeleteBackground));
            renderer.setFont(renderer.getFont().deriveFont(STRIKETHRU_ATTRS));
        }
        else if (((ChangeBufferTableModel<?>) table.getModel()).isChangedAt(modelRow, modelColumn)) {
            renderer.setBackground(mixColor(renderer.getBackground(), pendingChangeBackground));
        }
    }

    private Color mixColor(Color color1, Color color2) {
        ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
        float[] components1 = color1.getColorComponents(colorSpace, null);
        float[] components2 = color2.getColorComponents(colorSpace, null);
        for (int i = 0; i < components1.length; i++) {
            components1[i] *= components2[i];
        }
        return new Color(colorSpace, components1, 1f);
    }
}