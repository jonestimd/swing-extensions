package io.github.jonestimd.swing.component;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.Random;

import javax.swing.JList;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class FormatListCellRendererTest {
    private final Format format = DecimalFormat.getInstance();
    private FormatListCellRenderer renderer = new FormatListCellRenderer(format);

    @Test
    public void usesSpaceForNull() throws Exception {
        renderer.getListCellRendererComponent(new JList<>(), null, 0, false, false);

        assertThat(renderer.getText()).isEqualTo(" ");
    }

    @Test
    public void usesFormatForNonNull() throws Exception {
        int value = new Random().nextInt();

        renderer.getListCellRendererComponent(new JList<>(), value, 0, false, false);

        assertThat(renderer.getText()).isEqualTo(format.format(value));
    }
}