package io.github.jonestimd.swing;

import java.awt.Color;

import org.junit.Test;

import static org.junit.Assert.*;

public class ColorFactoryTest {

    @Test
    public void testCreateColorWithColorName() throws Exception {
        assertEquals(Color.PINK, ColorFactory.createColor("PINK"));
        assertEquals(Color.BLUE, ColorFactory.createColor("blue"));
        assertEquals(Color.darkGray, ColorFactory.createColor("darkGray"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidColorName() {
        assertNull(ColorFactory.createColor("grEen"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidFieldName() {
        assertNull(ColorFactory.createColor("TRANSLUCENT"));
    }

    @Test
    public void testCreateWithRgb() throws Exception {
        Color color = ColorFactory.createColor("5,6,7");
        assertEquals(5, color.getRed());
        assertEquals(6, color.getGreen());
        assertEquals(7, color.getBlue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void toFewComponents() {
        assertNull(ColorFactory.createColor("1,2"));
    }
}