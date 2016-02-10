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
package io.github.jonestimd.swing;

import java.awt.Color;
import java.util.ListResourceBundle;

import javax.swing.ImageIcon;

public class ComponentResources extends ListResourceBundle {
    @Override
    protected Object[][] getContents() {
        return new Object[][] {
            { "filter.iconImage", new ImageIcon(getClass().getResource("/io/github/jonestimd/swing/component/filter.png")) },
            { "filter.invalid.background", new Color(255, 210, 210) },

            { "exceptionDialog.title", "Unexpected Exception" },
            { "exceptionDialog.noStackTrace", "Stack trace is unavailable" },
            { "default.exception.label", "Unexpectd exception:" },

            { "action.save.mnemonicAndName", "SSave" },
            { "action.cancel.name", "Cancel" },

            { "confirm.unsavedChanges.title", "Confirm Discard Changes" },
            { "confirm.unsavedChanges.message", "Your changes have not been saved." },
            { "confirm.unsavedChanges.option.confirm", "Discard Changes" },
            { "confirm.unsavedChanges.option.cancel", "Cancel" },

            { "calendar.button.tooltip", "Select a date from the calendar" },
            { "calendar.tooltip", "Select a date" },
            { "calendar.tooltip.month.next", "Next month" },
            { "calendar.tooltip.month.previous", "Previous month" },
            { "calendar.tooltip.year.next", "Next year" },
            { "calendar.tooltip.year.previous", "Previous year" },
        };
    }
}
