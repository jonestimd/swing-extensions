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
package io.github.jonestimd.swing.component;

import java.awt.event.ActionEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.text.MaskFormatter;

public class DateMaskFormatter extends MaskFormatter {
    private SimpleDateFormat format;

    public DateMaskFormatter(String pattern)  {
        try {
            setMask(pattern.replaceAll("[Mdy]", "#"));
        }
        catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format", e);
        }
        this.format = new SimpleDateFormat(pattern);
        this.format.setLenient(false);
    }

    public Object stringToValue(String text) throws ParseException {
        if (text == null) {
            throw new ParseException("null value", -1);
        }
        return format.parse(text);
    }

    public String valueToString(Object value) throws ParseException {
        if (value == null) {
            String pattern = format.toPattern();
            return pattern.replaceAll("[Mdy]", " ");
        }
        return format.format(value);
    }

    protected Action[] getActions() {
        Action[] actions = super.getActions();
        int index = 0;
        if (actions == null) {
            actions = new Action[2];
        }
        else {
            index = actions.length;
            actions = Arrays.copyOf(actions, index + 2);
        }
        actions[index++] = new IncrementAction("increment", 1);
        actions[index++] = new IncrementAction("decrement", -1);
        return actions;
    }

    private class IncrementAction extends AbstractAction {
        private int amount;

        private IncrementAction(String name, int amount) {
            super(name);
            this.amount = amount;
        }

        public void actionPerformed(ActionEvent e) {
            if (getFormattedTextField().isEditable() && getFormattedTextField().isEditValid()) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime((Date) getFormattedTextField().getValue());
                calendar.add(Calendar.DATE, amount);
                getFormattedTextField().setValue(calendar.getTime());
            }
        }
    }
}