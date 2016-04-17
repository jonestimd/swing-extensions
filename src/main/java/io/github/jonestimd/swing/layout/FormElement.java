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
package io.github.jonestimd.swing.layout;

import java.awt.GridBagConstraints;

import static java.awt.GridBagConstraints.*;

public enum FormElement implements GridBagFormula {
    TOP_LABEL(null, GridBagConstraints.WEST, 2, 1, 0d, 0d, 0),
    LEFT_LABEL(null, GridBagConstraints.EAST, 1, 1, 0d, 0d, 5),
    BUTTON_GROUP(TOP_LABEL, GridBagConstraints.WEST, 2, 1, 1d, 0d, 0),
    TEXT_FIELD(LEFT_LABEL, GridBagConstraints.WEST, 1, 1, 1d, 0d, 0),
    TEXT_AREA(TOP_LABEL, GridBagConstraints.WEST, 2, 1, 1d, 0.3d, 0),
    LIST(TOP_LABEL, GridBagConstraints.WEST, 2, 1, 1d, 1d, 0),
    TABLE(TOP_LABEL, GridBagConstraints.WEST, 2, 1, 1d, 1d, 0),
    PANEL(null, GridBagConstraints.WEST, 2, 1, 1d, 1d, 0);

    private final FormElement labelConstraints;
    private final int anchor;
    private final int width;
    private final int height;
    private final double weightx;
    private final double weighty;
    private final int fill;
    private final int indent;

    private FormElement(FormElement labelConstraints, int anchor, int width, int height, double weightx, double weighty, int indent) {
        this.labelConstraints = labelConstraints;
        this.anchor = anchor;
        this.width = width;
        this.height = height;
        this.weightx = weightx;
        this.weighty = weighty;
        this.indent = indent;
        if (weightx > 0) {
            fill = weighty > 0 ? BOTH : HORIZONTAL;
        }
        else {
            fill = NONE;
        }
    }

    public GridBagFormula getLabelConstraints() {
        return labelConstraints;
    }

    public GridBagConstraints setConstraints(GridBagConstraints gbc) {
        gbc.anchor = anchor;
        gbc.gridwidth = width;
        gbc.gridheight = height;
        gbc.weightx = weightx;
        gbc.weighty = weighty;
        gbc.insets.left = indent;
        gbc.fill = fill;
        return gbc;
    }
}