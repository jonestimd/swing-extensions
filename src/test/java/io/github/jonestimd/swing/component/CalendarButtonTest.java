package io.github.jonestimd.swing.component;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import java.util.Date;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class CalendarButtonTest {
    public static void main(String ... args) throws ParseException {
        try {
            UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        final JFrame frame = new JFrame("Calendar button test");

        addDateField(frame);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                frame.pack();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
//                frame.requestFocus();
                frame.toFront();
            }
        });
    }

    private static void addDateField(JFrame frame) throws ParseException {
        frame.setContentPane(new JPanel(new GridBagLayout()));
        Box box = Box.createHorizontalBox();
        DateField field = new DateField("MM/dd/yyyy");
        field.setFont(field.getFont().deriveFont(field.getFont().getSize() * 2f));
        field.setToolTipText("some tooltip");
        field.addPropertyChangeListener("value", new DateChangeListener());
        field.setValue(new Date());
        box.add(field);
        frame.getContentPane().add(box, new GridBagConstraints(0, 0, 1, 1, 1d, 0d, GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL, new Insets(0,0,0,0), 0, 0));
    }

    private static class DateChangeListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            System.out.println(evt.getOldValue() + " => " + evt.getNewValue());
        }
    }
}