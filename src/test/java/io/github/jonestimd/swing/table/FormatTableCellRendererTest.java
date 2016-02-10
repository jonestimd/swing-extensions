package io.github.jonestimd.swing.table;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JTable;

import org.junit.Test;

import static org.junit.Assert.*;

public class FormatTableCellRendererTest {
    @Test
    public void testDateTableCellRenderer() throws Exception {
        DateTableCellRenderer renderer = new DateTableCellRenderer("MM/dd/yyyy");
        Date value = new Date();

        renderer.getTableCellRendererComponent(new JTable(), value, false, false, 0, 0);

        assertEquals(new SimpleDateFormat("MM/dd/yyyy").format(value), renderer.getText());
        assertEquals(JLabel.LEADING, renderer.getHorizontalAlignment());
    }

    @Test
    public void testFormatTableCellRenderer_NumberFormat() throws Exception {
        NumberFormat numberInstance = NumberFormat.getNumberInstance();
        numberInstance.setMaximumFractionDigits(9);
        FormatTableCellRenderer renderer = new FormatTableCellRenderer(numberInstance, Highlighter.NOOP_HIGHLIGHTER);
        BigDecimal value = new BigDecimal("98765.4321");

        renderer.getTableCellRendererComponent(new JTable(), value, false, false, 0, 0);

        assertEquals(9, numberInstance.getMaximumFractionDigits());
        assertEquals("98,765.4321", renderer.getText());
        assertEquals(JLabel.RIGHT, renderer.getHorizontalAlignment());
    }

    @Test
    public void testFormatTableCellRenderer_NumberFormatWithNull() throws Exception {
        FormatTableCellRenderer renderer = new FormatTableCellRenderer(NumberFormat.getNumberInstance(), Highlighter.NOOP_HIGHLIGHTER);

        renderer.getTableCellRendererComponent(new JTable(), null, false, false, 0, 0);

        assertEquals("", renderer.getText());
        assertEquals(JLabel.RIGHT, renderer.getHorizontalAlignment());
    }
}