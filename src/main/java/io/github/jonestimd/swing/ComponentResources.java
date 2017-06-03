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
package io.github.jonestimd.swing;

import java.awt.Color;
import java.util.ListResourceBundle;

import javax.swing.ImageIcon;

import static io.github.jonestimd.swing.SwingResource.*;

@SuppressWarnings({"MagicCharacter", "MagicNumber"})
public class ComponentResources extends ListResourceBundle {
    @Override
    protected Object[][] getContents() {
        return new Object[][] {
            { BUTTON_TOOLTIP_ACCELERATOR_FORMAT.key, " (%s)" },
            { "filter.iconImage", new ImageIcon(getClass().getResource("/io/github/jonestimd/swing/component/filter.png")) },
            { "filter.invalid.background", new Color(255, 210, 210) },
            { FILTER_OPERATOR_SYMBOL_AND.key, '&' },
            { FILTER_OPERATOR_SYMBOL_OR.key, '\u2502' },
            { FILTER_OPERATOR_SYMBOL_NOT.key, '!' },
            { FILTER_OPERATOR_SYMBOL_GROUP_START.key, '(' },
            { FILTER_OPERATOR_SYMBOL_GROUP_END.key, ')' },
            { "filter.operator.key.and", '&' },
            { "filter.operator.key.or", '|' },
            { "filter.operator.key.not", '!' },
            { "filter.operator.key.group.start", '(' },
            { "filter.operator.key.group.end", ')' },

            { "exceptionDialog.title", "Unexpected Exception" },
            { "exceptionDialog.noStackTrace", "Stack trace is unavailable" },
            { "exceptionDialog.exception.label", "Unexpected exception:" },
            { "exceptionDialog.exception.columns", 50 },
            { "exceptionDialog.exception.rows", 20 },

            { "action.save.mnemonicAndName", "SSave" },
            { "action.cancel.name", "Cancel" },

            { "confirm.unsavedChanges.title", "Confirm Discard Changes" },
            { "confirm.unsavedChanges.message", "Your changes have not been saved." },
            { "confirm.unsavedChanges.option.confirm", "Discard Changes" },
            { "confirm.unsavedChanges.option.cancel", "Cancel" },

            { VALIDATION_MESSAGE_BACKGROUND.key, new Color(255,255,200) },

            { "calendar.button.tooltip", "Select a date from the calendar" },
            { "calendar.tooltip", "Select a date" },
            { "calendar.tooltip.month.next", "Next month" },
            { "calendar.tooltip.month.previous", "Previous month" },
            { "calendar.tooltip.year.next", "Next year" },
            { "calendar.tooltip.year.previous", "Previous year" },
            { "calendarPanel.selected.border", Color.red },
            { "calendarPanel.month.background", new Color(255,255,224) },
            { "calendarPanel.month.foreground", Color.black },
            { "calendarPanel.month.adjacent.background", new Color(223,223,192) },
            { "calendarPanel.month.adjacent.foreground", Color.darkGray }
        };
    }
}
