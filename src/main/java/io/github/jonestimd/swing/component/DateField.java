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

import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JFormattedTextField;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.text.DateFormatter;

import io.github.jonestimd.swing.ComponentTreeUtils;

public class DateField extends JFormattedTextField {
    private static final int FORMATTED_LENGTH = 10;
    private static final String SEPARATOR_STRING = "/";
    private static final char SEPARATOR = SEPARATOR_STRING.charAt(0);
    private static final String FIELD_PATTERN = "([0-9]+/?)*";
    private CalendarButtonBorder calendarButtonBorder;

    public DateField(String dateFormat) {
        super(new DateFormatter(new SimpleDateFormat(dateFormat)));
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "disable-reset-field-edit");
        setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
        calendarButtonBorder = new CalendarButtonBorder(this);
        setBorder(getBorder());
        setValue(new Date());
    }

    public void setBorder(Border border) {
        super.setBorder(border == null ? calendarButtonBorder : new CompoundBorder(border, calendarButtonBorder));
    }

    @Override
    public void setValue(Object value) {
        super.setValue(value);
        selectField(0);
    }

    @Override
    public Date getValue() {
        return (Date) super.getValue();
    }

    private void selectField(int dot) {
        int from = getText().lastIndexOf('/', dot) + 1;
        int to = getText().indexOf('/', from);
        setCaretPosition(from);
        moveCaretPosition(to > 0 ? to : getText().length());
    }

    private void selectNextField() {
        String text = getText();
        int slash = text.indexOf(SEPARATOR, getCaretPosition());
        if (slash >= 0) {
            setCaretPosition(slash + 1);
            slash = text.indexOf(SEPARATOR, slash+1);
            moveCaretPosition(slash > 0 ? slash : text.length());
        }
        else {
            selectField(0);
        }
    }

    @Override
    public void replaceSelection(String content) {
        if (content.matches(FIELD_PATTERN)) {
            super.replaceSelection(content);
            removeExtraFields();
        }
    }

    private void removeExtraFields() {
        String text = getText();
        String[] fields = text.split(SEPARATOR_STRING);
        if (fields.length > 3) {
            setText(Stream.of(fields).limit(3).collect(Collectors.joining(SEPARATOR_STRING)));
            selectField(0);
        }
    }

    @Override
    protected void processComponentKeyEvent(KeyEvent e) {
        if (e.getID() == KeyEvent.KEY_TYPED && e.getKeyChar() == SEPARATOR) {
            selectNextField();
            e.consume();
        }
        else if (e.getID() == KeyEvent.KEY_PRESSED && e.getModifiers() == 0) {
            if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                if (!deleteSelectedDigits()) {
                    int caret = getCaretPosition();
                    if (caret > 0) {
                        if (getText().charAt(caret - 1) == SEPARATOR) {
                            setCaretPosition(caret - 1);
                        }
                        else {
                            deleteCharAt(caret - 1, caret - 1);
                        }
                    }
                }
                e.consume();
            }
            else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                if (!deleteSelectedDigits()) {
                    int caret = getCaretPosition();
                    if (caret < getDocument().getLength()) {
                        int toDelete = skipSeparators();
                        if (toDelete < getDocument().getLength()) {
                            deleteCharAt(toDelete, caret);
                        }
                    }
                }
                e.consume();
            }
            else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_KP_UP || e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_KP_DOWN) {
                try {
                    if (getText().length() < FORMATTED_LENGTH) {
                        Date value = (Date) getFormatter().stringToValue(getText());
                        int dot = getCaretPosition();
                        int field = getText().substring(0, dot).replaceAll("[^/]", "").length();
                        setText(getFormatter().valueToString(value));
                        setCaretPosition(field * 3);
                    }
                } catch (ParseException e1) {
                    // ignore
                }
            }
        }
        super.processComponentKeyEvent(e);
    }

    private int skipSeparators() {
        String text = getText();
        int caret = getCaretPosition();
        while (caret < text.length() && text.charAt(caret) == SEPARATOR) caret++;
        return caret;
    }

    private void deleteCharAt(int pos, int newCaret) {
        StringBuilder text = new StringBuilder(getText());
        setText(text.deleteCharAt(pos).toString());
        setCaretPosition(newCaret);
    }

    /**
     * Delete the digit characters from the selection but leave the separators.
     * @return true if there was a selection and the text was updated.
     */
    private boolean deleteSelectedDigits() {
        int start = Math.min(getCaret().getDot(), getCaret().getMark());
        int end = Math.max(getCaret().getDot(), getCaret().getMark());
        if (start != end) {
            StringBuilder text = new StringBuilder(getText());
            for (int i = end-1; i >= start; i--) {
                if (text.charAt(i) != SEPARATOR) {
                    text.deleteCharAt(i);
                }
            }
            setText(text.toString());
            setCaretPosition(start);
            return true;
        }
        return false;
    }

    /**
     * Override to select a field when the focus is gained.  If the opposite component is not an ancestor then the
     * field containing the caret position is selected.  If the opposite component is an ancestor and the caret
     * position is not in the first field then the current field is selected (special case for a table
     * cell editor and the edit was started by typing {@code '/'}).
     */
    @Override
    protected void processFocusEvent(FocusEvent event) {
        int position = getCaretPosition();
        super.processFocusEvent(event);
        if (! event.isTemporary() && event.getID() == FocusEvent.FOCUS_GAINED) {
            if (ComponentTreeUtils.isAncestor(this, event.getOppositeComponent())) {
                if (position >= 2) selectField(position);
            }
            else {
                selectField(position-1);
            }
        }
    }
}