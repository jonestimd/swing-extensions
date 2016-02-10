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
import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.plaf.TableUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import io.github.jonestimd.swing.ComponentDefaults;
import io.github.jonestimd.swing.table.model.SectionTableModel;

/**
 * A table with rows grouped into sections with a header row displayed at the beginning of each section.
 */
public class SectionTable<Bean, Model extends SectionTableModel<Bean>> extends DecoratedTable<Bean, Model> {
    private static final String uiClassID = "SectionTableUI";
    private Color sectionRowBackground;
    private TableCellRenderer sectionRowRenderer = new DefaultSectionRowRenderer();

    public SectionTable(Model dm) {
        super(dm);
    }

    /**
     * Overridden to set the section header background.
     */
    @Override
    public void setUI(TableUI ui) {
        sectionRowBackground = ComponentDefaults.getColor("SectionTable.sectionRowBackground");
        super.setUI(ui);
    }

    /**
     * Overridden to prevent editing of section headers.
     */
    @Override
    public boolean isCellEditable(int row, int column) {
        return ! getModel().isSectionRow(convertRowIndexToModel(row)) && super.isCellEditable(row, column);
    }

    public Color getSectionRowBackground() {
        return sectionRowBackground;
    }

    public void setSectionRowBackground(Color sectionRowBackground) {
        this.sectionRowBackground = sectionRowBackground;
    }

    /**
     * Set the renderer to use for section header rows.
     */
    public void setSectionRowRenderer(TableCellRenderer sectionRowRenderer) {
        this.sectionRowRenderer = sectionRowRenderer;
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        return getModel().isSectionRow(convertRowIndexToModel(row)) ? sectionRowRenderer : super.getCellRenderer(row, column);
    }

    @Override
    public Object getValueAt(int row, int column) {
        int modelRow = convertRowIndexToModel(row);
        if (getModel().isSectionRow(modelRow)) {
            return column == 0 ? getModel().getSectionName(modelRow) : null;
        }
        return super.getValueAt(row, column);
    }

    /**
     * @return true if the specified row is a section header.
     */
    public boolean isSectionRow(int row) {
        return getModel().isSectionRow(convertRowIndexToModel(row));
    }

    @Override
    public String getUIClassID() {
        return uiClassID;
    }

    @Override
    public void updateUI() {
        if (UIManager.get(getUIClassID()) == null) {
            UIManager.put(getUIClassID(), SectionTableUI.class.getName());
        }
        super.updateUI();
    }

    private class DefaultSectionRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(getFont().deriveFont(Font.BOLD));
            setBackground(sectionRowBackground);
            return this;
        }
    }
}
