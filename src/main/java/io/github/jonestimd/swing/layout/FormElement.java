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
package io.github.jonestimd.swing.layout;

import java.awt.GridBagConstraints;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * The default {@link GridBagFormula}s for {@link GridBagBuilder}.
 */
public enum FormElement implements GridBagFormula {
    /** Label constraints for {@link #TABLE}, {@link #LIST}, {@link #TEXT_AREA} and {@link #BUTTON_GROUP}.
     * The label is placed on the row above the component. */
    TOP_LABEL(null, GridBagConstraints.WEST, 2, 1, 0d, 0d, 0),
    /** Label constraints for {@link #TEXT_FIELD}.  The label is placed in the column to the left of the field. */
    LEFT_LABEL(null, GridBagConstraints.EAST, 1, 1, 0d, 0d, 5),
    /** Constraints for a button group (a {@link Box} or a {@link JPanel}).  Two columns wide and no vertical growth. */
    BUTTON_GROUP(TOP_LABEL, GridBagConstraints.WEST, 2, 1, 1d, 0d, 0),
    /** Constraints for a {@link JCheckBox}. */
    CHECK_BOX(null, GridBagConstraints.WEST, 1, 1, 1d, 0d, 0),
    /** Constraints for a {@link JTextField}. */
    TEXT_FIELD(LEFT_LABEL, GridBagConstraints.WEST, 1, 1, 1d, 0d, 0),
    /** Constraints for a {@link JTextArea}. */
    TEXT_AREA(TOP_LABEL, GridBagConstraints.WEST, 2, 1, 1d, 0.3d, 0),
    /** Constraints for a {@link JList}. */
    LIST(TOP_LABEL, GridBagConstraints.WEST, 2, 1, 1d, 1d, 0),
    /** Constraints for a {@link JTable}. */
    TABLE(TOP_LABEL, GridBagConstraints.WEST, 2, 1, 1d, 1d, 0),
    /** Constraints for a {@link JPanel}. */
    PANEL(null, GridBagConstraints.WEST, 2, 1, 1d, 1d, 0);

    private final GridBagFormula delegate;

    FormElement(FormElement labelConstraints, int anchor, int width, int height, double weightx, double weighty, int indent) {
        this.delegate = GridBagFormula.get(labelConstraints, anchor, width, height, weightx, weighty, indent);
    }

    public GridBagFormula getLabelConstraints() {
        return delegate.getLabelConstraints();
    }

    public GridBagConstraints setConstraints(GridBagConstraints gbc) {
        return delegate.setConstraints(gbc);
    }
}