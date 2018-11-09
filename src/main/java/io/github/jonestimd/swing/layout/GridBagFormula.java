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
import java.awt.Insets;

import static java.awt.GridBagConstraints.*;

/**
 * An interface for generating {@link GridBagConstraints}.
 * @see GridBagBuilder
 */
public interface GridBagFormula {
    /**
     * Get the constraints to use for the label associated with the current component.
     */
    GridBagFormula getLabelConstraints();

    /**
     * Apply the constraints for the current component.
     * @param gbc the constraints to be updated
     * @return the updated {@code gbc} parameter
     */
    GridBagConstraints setConstraints(GridBagConstraints gbc);

    /**
     * Create an instance of {@code GridBagFormula}.
     * @param labelConstraints the {@code GridBagFormula} to use for the label
     * @param anchor the anchor position for the field
     * @param width the grid width for the field
     * @param height the grid height for the field
     * @param weightx the width size allocation for the field
     * @param weighty the height size allocation for the field
     * @param indent the left inset for the field
     */
    static GridBagFormula get(GridBagFormula labelConstraints, int anchor, int width, int height, double weightx, double weighty, int indent) {
        return get(labelConstraints, anchor, width, height, weightx, weighty, new Insets(0, indent, 0, 0));
    }

    /**
     * Create an instance of {@code GridBagFormula}.
     * @param labelConstraints the {@code GridBagFormula} to use for the label
     * @param anchor the anchor position for the field
     * @param width the grid width for the field
     * @param height the grid height for the field
     * @param weightx the width size allocation for the field
     * @param weighty the height size allocation for the field
     * @param insets the insets for the field
     */
    static GridBagFormula get(GridBagFormula labelConstraints, int anchor, int width, int height, double weightx, double weighty, Insets insets) {
        return new GridBagFormula() {
            @Override
            public GridBagFormula getLabelConstraints() {
                return labelConstraints;
            }

            @Override
            public GridBagConstraints setConstraints(GridBagConstraints gbc) {
                if (labelConstraints == null && anchor == GridBagConstraints.WEST && width == 1) {
                    // unlabeled component, e.g. checkbox
                    gbc.gridx = Math.max(gbc.gridx, 0) + 1;
                }
                gbc.anchor = anchor;
                gbc.gridwidth = width;
                gbc.gridheight = height;
                gbc.weightx = weightx;
                gbc.weighty = weighty;
                gbc.insets.top += insets.top;
                gbc.insets.bottom += insets.bottom;
                gbc.insets.left += insets.left;
                gbc.insets.right += insets.right;
                if (weightx > 0) {
                    gbc.fill = weighty > 0 ? BOTH : HORIZONTAL;
                }
                else {
                    gbc.fill = NONE;
                }
                return gbc;
            }
        };
    }
}
