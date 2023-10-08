package io.github.jonestimd.swing.component;

import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JTable;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class DateFieldTest {
    @Test
    public void commitNullValue() throws ParseException {
        DateField dateField = new DateField("MM/dd/yyyy");
        dateField.setValue(new Date());

        dateField.setText(DateField.NULL_TEXT);
        dateField.commitEdit();

        assertThat(dateField.getValue()).isNull();
    }

    @Test
    public void commitDate() throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
        Date date = format.parse("01/12/2023");
        DateField dateField = new DateField("MM/dd/yyyy");

        dateField.setText(format.format(date));
        dateField.commitEdit();

        assertThat(dateField.getValue()).isEqualTo(date);
    }

    @Test
    public void setNullValue() throws Exception {
        DateField dateField = new DateField("MM/dd/yyyy");

        dateField.setValue(null);

        assertThat(dateField.getText()).isEqualTo(DateField.NULL_TEXT);
        assertThat(dateField.getCaretPosition()).isEqualTo(0);
    }

    @Test
    public void setValueSelectsFirstField() throws Exception {
        DateField dateField = new DateField("MM/dd/yyyy");

        dateField.setValue(new Date());

        assertThat(dateField.getSelectionStart()).isEqualTo(0);
        assertThat(dateField.getSelectionEnd()).isEqualTo(2);
    }

    @Test
    public void replaceSelectionIgnoresNonNumericText() throws Exception {
        DateField dateField = new DateField("MM/dd/yyyy");
        dateField.setValue(new Date());
        String text = dateField.getText();

        dateField.replaceSelection("a");

        assertThat(dateField.getText()).isEqualTo(text);
    }

    @Test
    public void replaceSelectionRemovesExtraField() throws Exception {
        DateField dateField = new DateField("MM/dd/yyyy");
        dateField.setValue(new Date());
        String text = dateField.getText();

        dateField.replaceSelection("1/");

        assertThat(dateField.getText()).isEqualTo("1/" + text.substring(2,5));

    }

    @Test
    public void typingSeparatorSelectsNextField() throws Exception {
        DateField dateField = new DateField("MM/dd/yyyy");
        dateField.setText("11/10/2012");
        dateField.select(0, 0);

        dateField.processComponentKeyEvent(new KeyEvent(dateField, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, '/'));
        assertThat(dateField.getSelectionStart()).isEqualTo(3);
        assertThat(dateField.getSelectionEnd()).isEqualTo(5);

        dateField.processComponentKeyEvent(new KeyEvent(dateField, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, '/'));
        assertThat(dateField.getSelectionStart()).isEqualTo(6);
        assertThat(dateField.getSelectionEnd()).isEqualTo(10);

        dateField.processComponentKeyEvent(new KeyEvent(dateField, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, '/'));
        assertThat(dateField.getSelectionStart()).isEqualTo(0);
        assertThat(dateField.getSelectionEnd()).isEqualTo(2);
    }

    @Test
    public void backspaceRemovesSelectedDigitsAndLeavesSeparators() throws Exception {
        DateField dateField = new DateField("MM/dd/yyyy");
        dateField.setText("11/10/2012");
        dateField.setSelectionStart(0);
        dateField.setSelectionEnd(10);
        KeyEvent event = new KeyEvent(dateField, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_BACK_SPACE, '\000');

        dateField.processComponentKeyEvent(event);

        assertThat(dateField.getText()).isEqualTo("//");
        assertThat(event.isConsumed()).isTrue();
    }

    @Test
    public void backspaceIgnoredAtBeginningOfField() throws Exception {
        DateField dateField = new DateField("MM/dd/yyyy");
        dateField.setText("12/10/2012");
        dateField.setCaretPosition(0);
        KeyEvent event = new KeyEvent(dateField, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_BACK_SPACE, '\000');

        dateField.processComponentKeyEvent(event);

        assertThat(dateField.getText()).isEqualTo("12/10/2012");
        assertThat(dateField.getCaretPosition()).isEqualTo(0);
        assertThat(event.isConsumed()).isTrue();
    }

    @Test
    public void backspaceRemovesDigitToLeft() throws Exception {
        DateField dateField = new DateField("MM/dd/yyyy");
        dateField.setText("12/10/2012");
        dateField.setCaretPosition(2);
        KeyEvent event = new KeyEvent(dateField, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_BACK_SPACE, '\000');

        dateField.processComponentKeyEvent(event);

        assertThat(dateField.getText()).isEqualTo("1/10/2012");
        assertThat(dateField.getCaretPosition()).isEqualTo(1);
        assertThat(event.isConsumed()).isTrue();
    }

    @Test
    public void backspaceSkipsSeparatorToLeft() throws Exception {
        DateField dateField = new DateField("MM/dd/yyyy");
        dateField.setText("12/10/2012");
        dateField.setCaretPosition(3);
        KeyEvent event = new KeyEvent(dateField, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_BACK_SPACE, '\000');

        dateField.processComponentKeyEvent(event);

        assertThat(dateField.getText()).isEqualTo("12/10/2012");
        assertThat(dateField.getCaretPosition()).isEqualTo(2);
        assertThat(event.isConsumed()).isTrue();
    }

    @Test
    public void deleteRemovesSelectedDigitsAndLeavesSeparators() throws Exception {
        DateField dateField = new DateField("MM/dd/yyyy");
        dateField.setText("11/10/2012");
        dateField.setSelectionStart(0);
        dateField.setSelectionEnd(10);
        KeyEvent event = new KeyEvent(dateField, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_DELETE, '\000');

        dateField.processComponentKeyEvent(event);

        assertThat(dateField.getText()).isEqualTo("//");
        assertThat(event.isConsumed()).isTrue();
    }

    @Test
    public void deleteIgnoredAtEndOfField() throws Exception {
        DateField dateField = new DateField("MM/dd/yyyy");
        dateField.setText("11/10/2012");
        dateField.setSelectionStart(10);
        dateField.setSelectionEnd(10);
        KeyEvent event = new KeyEvent(dateField, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_DELETE, '\000');

        dateField.processComponentKeyEvent(event);

        assertThat(dateField.getText()).isEqualTo("11/10/2012");
        assertThat(dateField.getCaretPosition()).isEqualTo(10);
        assertThat(event.isConsumed()).isTrue();
    }

    @Test
    public void deleteRemovesFollowingDigit() throws Exception {
        DateField dateField = new DateField("MM/dd/yyyy");
        dateField.setText("11/10/2012");
        dateField.setSelectionStart(4);
        dateField.setSelectionEnd(4);
        KeyEvent event = new KeyEvent(dateField, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_DELETE, '\000');

        dateField.processComponentKeyEvent(event);

        assertThat(dateField.getText()).isEqualTo("11/1/2012");
        assertThat(dateField.getCaretPosition()).isEqualTo(4);
        assertThat(event.isConsumed()).isTrue();
    }

    @Test
    public void deleteRemovesDigitAfterSeparator() throws Exception {
        DateField dateField = new DateField("MM/dd/yyyy");
        dateField.setText("11/10/2012");
        dateField.setSelectionStart(5);
        dateField.setSelectionEnd(5);
        KeyEvent event = new KeyEvent(dateField, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_DELETE, '\000');

        dateField.processComponentKeyEvent(event);

        assertThat(dateField.getText()).isEqualTo("11/10/012");
        assertThat(dateField.getCaretPosition()).isEqualTo(5);
        assertThat(event.isConsumed()).isTrue();
    }

    @Test
    public void upKeyReformatsField() throws Exception {
        DateField dateField = new DateField("MM/dd/yyyy");
        dateField.setText("1/1/2012");
        dateField.select(4, 4);

        dateField.processComponentKeyEvent(new KeyEvent(dateField, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_UP, '\000'));

        assertThat(dateField.getText()).isEqualTo("01/01/2012");
        assertThat(dateField.getCaretPosition()).isEqualTo(6);
    }

    @Test
    public void keypadUpKeyReformatsField() throws Exception {
        DateField dateField = new DateField("MM/dd/yyyy");
        dateField.setText("1/1/2012");
        dateField.select(2, 2);

        dateField.processComponentKeyEvent(new KeyEvent(dateField, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_KP_UP, '\000'));

        assertThat(dateField.getText()).isEqualTo("01/01/2012");
        assertThat(dateField.getCaretPosition()).isEqualTo(3);
    }

    @Test
    public void keypadDownKeyReformatsField() throws Exception {
        DateField dateField = new DateField("MM/dd/yyyy");
        dateField.setText("1/1/2012");
        dateField.select(0, 0);

        dateField.processComponentKeyEvent(new KeyEvent(dateField, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_KP_DOWN, '\000'));

        assertThat(dateField.getText()).isEqualTo("01/01/2012");
        assertThat(dateField.getSelectionStart()).isEqualTo(0);
    }

    @Test
    public void downKeyReformatsField() throws Exception {
        DateField dateField = new DateField("MM/dd/yyyy");
        dateField.setText("1/1/2012");
        dateField.select(8, 8);

        dateField.processComponentKeyEvent(new KeyEvent(dateField, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_DOWN, '\000'));

        assertThat(dateField.getText()).isEqualTo("01/01/2012");
        assertThat(dateField.getSelectionStart()).isEqualTo(6);
    }

    @Test
    public void ignoreTemporaryFocusGainedFromAncestor() throws Exception {
        DateField dateField = new DateField("MM/dd/yyyy");
        dateField.setText("11/10/2012");
        dateField.setCaretPosition(2);
        JTable parent = new JTable();
        parent.add(dateField);

        dateField.processFocusEvent(new FocusEvent(dateField, FocusEvent.FOCUS_GAINED, true, parent));

        assertThat(dateField.getSelectionStart()).isEqualTo(2);
        assertThat(dateField.getSelectionEnd()).isEqualTo(2);
    }

    @Test
    public void ignoreFocusLost() throws Exception {
        DateField dateField = new DateField("MM/dd/yyyy");
        dateField.setText("11/10/2012");
        dateField.setCaretPosition(0);
        JTable parent = new JTable();
        parent.add(dateField);

        dateField.processFocusEvent(new FocusEvent(dateField, FocusEvent.FOCUS_LOST, false, null));

        assertThat(dateField.getSelectionStart()).isEqualTo(0);
        assertThat(dateField.getSelectionEnd()).isEqualTo(0);
    }

    @Test
    public void focusGainedFromAncestorSelectsMonth() throws Exception {
        DateField dateField = new DateField("MM/dd/yyyy");
        dateField.setText("11/10/2012");
        dateField.setCaretPosition(2);
        JTable parent = new JTable();
        parent.add(dateField);

        dateField.processFocusEvent(new FocusEvent(dateField, FocusEvent.FOCUS_GAINED, false, parent));

        assertThat(dateField.getSelectionStart()).isEqualTo(3);
        assertThat(dateField.getSelectionEnd()).isEqualTo(5);
    }

    @Test
    public void focusGainedFromAncestorDoesNotSelectsDay() throws Exception {
        DateField dateField = new DateField("MM/dd/yyyy");
        dateField.setText("11/10/2012");
        dateField.setCaretPosition(1);
        JTable parent = new JTable();
        parent.add(dateField);

        dateField.processFocusEvent(new FocusEvent(dateField, FocusEvent.FOCUS_GAINED, false, parent));

        assertThat(dateField.getSelectionStart()).isEqualTo(1);
        assertThat(dateField.getSelectionEnd()).isEqualTo(1);
    }

    @Test
    public void focusGainedFromAnotherWindowSelectsCurrentField() throws Exception {
        DateField dateField = new DateField("MM/dd/yyyy");
        dateField.setText("11/10/2012");
        dateField.setCaretPosition(7);
        JTable parent = new JTable();
        parent.add(dateField);

        dateField.processFocusEvent(new FocusEvent(dateField, FocusEvent.FOCUS_GAINED, false, null));

        assertThat(dateField.getSelectionStart()).isEqualTo(6);
        assertThat(dateField.getSelectionEnd()).isEqualTo(10);
    }
}