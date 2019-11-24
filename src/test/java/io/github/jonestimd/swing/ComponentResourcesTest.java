// The MIT License (MIT)
//
// Copyright (c) 2019 Timothy D. Jones
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
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.ListResourceBundle;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.github.jonestimd.swing.SwingResource.*;
import static org.assertj.core.api.Assertions.*;

public class ComponentResourcesTest {
    private ResourceBundle bundle = ResourceBundle.getBundle(TestResources.class.getName());
    private static Field providersField;

    @BeforeClass
    public static void initField() throws Exception {
        providersField = ComponentResources.class.getDeclaredField("PROVIDERS");
        providersField.setAccessible(true);
    }

    @After
    public void resetProviders() throws Exception {
        providersField.set(null, Collections.emptyList());
    }

    @Test
    public void lookupColorParsesProviderValue() throws Exception {
        providersField.set(null, Collections.singletonList(bundle));

        assertThat(ComponentResources.lookupColor("filter.invalid.background")).isEqualTo(new Color(210, 255, 255));
    }

    @Test
    public void lookupColorReturnsProviderValue() throws Exception {
        providersField.set(null, Collections.singletonList(bundle));

        assertThat(ComponentResources.lookupColor(VALIDATION_MESSAGE_BACKGROUND.key)).isEqualTo(Color.YELLOW);
    }

    @Test
    public void lookupColorIgnoresInvalidProviderString() throws Exception {
        providersField.set(null, Collections.singletonList(bundle));

        assertThat(ComponentResources.lookupColor("calendarPanel.month.foreground")).isEqualTo(Color.black);
    }

    @Test
    public void lookupColorIgnoresInvalidProviderValue() throws Exception {
        providersField.set(null, Collections.singletonList(bundle));

        assertThat(ComponentResources.lookupColor("calendarPanel.selected.border")).isEqualTo(Color.RED);
    }

    @Test
    public void lookupColorReturnsDefaultValue() throws Exception {
        providersField.set(null, Collections.singletonList(bundle));

        assertThat(ComponentResources.lookupColor("calendarPanel.month.background")).isEqualTo(new Color(255, 255, 224));
    }

    @Test
    public void lookupIconReturnsProviderValue() throws Exception {
        providersField.set(null, Collections.singletonList(bundle));

        assertThat(ComponentResources.lookupIcon("filter.iconImage").toString()).endsWith("io/github/jonestimd/swing/component/next.png");
    }

    @Test
    public void lookupIconLoadsProviderValue() throws Exception {
        providersField.set(null, Collections.singletonList(bundle));

        assertThat(ComponentResources.lookupIcon("test.iconImage").toString()).endsWith("io/github/jonestimd/swing/component/next.png");
    }

    @Test(expected = MissingResourceException.class)
    public void lookupIconIgnoresInvalidProviderString() throws Exception {
        providersField.set(null, Collections.singletonList(bundle));

        ComponentResources.lookupIcon("missing.iconImage");
    }

    @Test(expected = MissingResourceException.class)
    public void lookupIconIgnoresInvalidProviderValue() throws Exception {
        providersField.set(null, Collections.singletonList(bundle));

        ComponentResources.lookupIcon("invalid.iconImage");
    }

    @Test
    public void lookupIntReturnsProviderValue() throws Exception {
        providersField.set(null, Collections.singletonList(bundle));

        assertThat(ComponentResources.lookupInt("exceptionDialog.exception.columns")).isEqualTo(99);
    }

    @Test
    public void lookupIntParsesProviderValue() throws Exception {
        providersField.set(null, Collections.singletonList(bundle));

        assertThat(ComponentResources.lookupInt("exceptionDialog.exception.rows")).isEqualTo(50);
    }

    @Test
    public void lookupIntIgnoresInvalidProviderValue() throws Exception {
        providersField.set(null, Collections.singletonList(bundle));

        assertThat(ComponentResources.lookupInt("multiSelectItem.outline.strokeWidth")).isEqualTo(1);
    }

    public static class TestResources extends ListResourceBundle {
        @Override
        protected Object[][] getContents() {
            return new Object[][]{
                    {"filter.iconImage", new ImageIcon(getClass().getResource("/io/github/jonestimd/swing/component/next.png"))},
                    {"test.iconImage", "/io/github/jonestimd/swing/component/next.png"},
                    {"missing.iconImage", "/io/github/jonestimd/swing/component/not_there.png"},
                    {"invalid.iconImage", 9},
                    {"filter.invalid.background", "210,255,255"},
                    {VALIDATION_MESSAGE_BACKGROUND.key, Color.YELLOW},
                    {"calendarPanel.month.foreground", "x"},
                    {"calendarPanel.selected.border", 3},
                    {"exceptionDialog.exception.columns", 99},
                    {"exceptionDialog.exception.rows", "50"},
                    {"multiSelectItem.outline.strokeWidth", Color.MAGENTA},
            };
        }
    }
}