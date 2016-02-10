package io.github.jonestimd.swing.component;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JFormattedTextField;
import javax.swing.SwingUtilities;

import io.github.jonestimd.swing.SwingTest;
import org.junit.Test;

import static org.junit.Assert.*;

public class DateMaskFormatterTest extends SwingTest {
    private static final long MILLIS_PER_DAY = 3600*24*1000L;

    @Test
    public void testCursorNavigation() throws Exception {
        String pattern = "MM/dd/yyyy";
        JFormattedTextField textField = new JFormattedTextField(new DateMaskFormatter(pattern));
        showComponent(textField);

        assertEquals(0, getCaretPosition(textField));
        for (int i = 0; i < pattern.length(); i++) {
            if (pattern.charAt(i) == '/') i++;
            assertEquals(i, getCaretPosition(textField));
            invokeAction(textField, "caret-forward");
        }
        assertEquals(pattern.length(), getCaretPosition(textField));

        invokeAction(textField, "caret-forward");
        assertEquals(10, getCaretPosition(textField));

        for (int i = pattern.length(); i >= 0; i--) {
            if (i < pattern.length() && pattern.charAt(i) == '/') i--;
            assertEquals(i, getCaretPosition(textField));
            invokeAction(textField, "caret-backward");
        }
        assertEquals(0, getCaretPosition(textField));

        invokeAction(textField, "caret-backward");
        assertEquals(0, getCaretPosition(textField));
    }

    @Test
    public void testCursorNavigation2() throws Exception {
        String pattern = "/MM/dd/yyyy";
        JFormattedTextField textField = new JFormattedTextField(new DateMaskFormatter(pattern));
        showComponent(textField);

        for (int i = 0; i < pattern.length(); i++) {
            if (pattern.charAt(i) == '/') i++;
            assertEquals(i, getCaretPosition(textField));
            invokeAction(textField, "caret-forward");
        }
        assertEquals(pattern.length(), getCaretPosition(textField));

        invokeAction(textField, "caret-forward");
        assertEquals(11, getCaretPosition(textField));

        for (int i = pattern.length(); i >= 1; i--) {
            if (i < pattern.length() && pattern.charAt(i) == '/') i--;
            assertEquals(i, getCaretPosition(textField));
            invokeAction(textField, "caret-backward");
        }
        assertEquals(1, getCaretPosition(textField));

        invokeAction(textField, "caret-backward");
        assertEquals(1, getCaretPosition(textField));
    }

    private int getCaretPosition(final JFormattedTextField textField) throws Exception {
        final int[] holder = { -1 };
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                holder[0] = textField.getCaret().getDot();
            }
        });
        return holder[0];
    }

    @Test
    public void testIncrement() throws Exception {
        Date date = new SimpleDateFormat("MM/dd/yyyy").parse("02/15/2013");
        JFormattedTextField textField = new JFormattedTextField(new DateMaskFormatter("/MM/dd/yyyy"));
        textField.setValue(date);
        showComponent(textField);

        invokeAction(textField, "increment");
        Date newDate = (Date) textField.getValue();
        assertEquals(MILLIS_PER_DAY, newDate.getTime() - date.getTime());
    }

    @Test
    public void testDecrement() throws Exception {
        Date date = new SimpleDateFormat("MM/dd/yyyy").parse("02/15/2013");
        JFormattedTextField textField = new JFormattedTextField(new DateMaskFormatter("/MM/dd/yyyy"));
        textField.setValue(date);
        showComponent(textField);

        invokeAction(textField, "decrement");
        Date newDate = (Date) textField.getValue();
        assertEquals(-MILLIS_PER_DAY, newDate.getTime() - date.getTime());
    }
}