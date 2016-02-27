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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;

import io.github.jonestimd.swing.LabelBuilder;
import io.github.jonestimd.swing.component.AutosizeTextField;
import io.github.jonestimd.swing.component.ComponentBinder;

public class TableSummaryPanel extends Box {
    private static final int LABEL_GAP = 5;
    private static final int SUMMARY_GAP = 10;

    public TableSummaryPanel(TableSummary tableSummary) {
        this();
        addSummaries(tableSummary);
    }

    public TableSummaryPanel() {
        super(BoxLayout.X_AXIS);
        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        setVisible(false);
    }

    public void addSummaries(TableSummary tableSummary) {
        for (PropertyAdapter property : tableSummary.getSummaryProperties()) {
            add(new LabelBuilder().name(property.getLabel()).bold().get());
            add(Box.createHorizontalStrut(LABEL_GAP));
            add(createSummaryField(tableSummary, property));
            add(Box.createHorizontalStrut(SUMMARY_GAP));
        }
        setVisible(getComponentCount() > 0);
    }

    private AutosizeTextField createSummaryField(TableSummary tableSummary, PropertyAdapter property) {
        return ComponentBinder.bind(tableSummary, property.getName(), property.getValue(),
                new AutosizeTextField(false), property.getFormat());
    }
}
