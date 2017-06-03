// The MIT License (MIT)
//
// Copyright (c) 2017 Timothy D. Jones
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
import java.util.ResourceBundle;

import org.junit.Test;

import static io.github.jonestimd.swing.SwingResource.*;
import static org.assertj.core.api.Assertions.*;

public class SwingResourceTest {
    private static final ResourceBundle bundle1 = ResourceBundle.getBundle(TestResources1.class.getName());
    private static final ResourceBundle bundle2 = ResourceBundle.getBundle(TestResources2.class.getName());
    private static final ResourceBundle bundle3 = ResourceBundle.getBundle(TestResources3.class.getName());

    @Test
    public void keyConvertsToDotSeparatedLowerCase() throws Exception {
        assertThat(VALIDATION_MESSAGE_BACKGROUND.key).isEqualTo("validation.message.background");
    }

    @Test
    public void getCharReturnsCharValue() throws Exception {
        assertThat(FILTER_OPERATOR_SYMBOL_GROUP_START.getChar(bundle1)).isEqualTo('x');
    }

    @Test
    public void getCharReturnsFirstCharOfString() throws Exception {
        assertThat(FILTER_OPERATOR_SYMBOL_GROUP_START.getChar(bundle2)).isEqualTo('x');
    }

    @Test
    public void getCharReturnsDefaultValue() throws Exception {
        assertThat(FILTER_OPERATOR_SYMBOL_GROUP_END.getChar(bundle1)).isEqualTo(')');
    }

    @Test
    public void getColorReturnsColorValue() throws Exception {
        assertThat(VALIDATION_MESSAGE_BACKGROUND.getColor(bundle1)).isEqualTo(Color.black);
    }

    @Test
    public void getColorConvertsString() throws Exception {
        assertThat(VALIDATION_MESSAGE_BACKGROUND.getColor(bundle2)).isEqualTo(Color.black);
    }

    @Test
    public void getColorReturnsDefaultValue() throws Exception {
        assertThat(VALIDATION_MESSAGE_BACKGROUND.getColor(bundle3)).isEqualTo(new Color(255, 255, 200));
    }

    @Test
    public void getStringReturnsValue() throws Exception {
        assertThat(BUTTON_TOOLTIP_ACCELERATOR_FORMAT.getString(bundle1)).isEqualTo(" - %s");
    }

    @Test
    public void getStringReturnsDefaultValue() throws Exception {
        assertThat(BUTTON_TOOLTIP_ACCELERATOR_FORMAT.getString(bundle3)).isEqualTo(" (%s)");
    }

    @Test
    public void getFilterOperatorGroupStart() throws Exception {
        assertThat(FILTER_OPERATOR_SYMBOL_GROUP_START.getChar()).isEqualTo('(');
    }

    @Test
    public void getFilterOperatorGroupEnd() throws Exception {
        assertThat(FILTER_OPERATOR_SYMBOL_GROUP_END.getChar()).isEqualTo(')');
    }

    public static class TestResources1 extends ListResourceBundle {
        @Override
        protected Object[][] getContents() {
            return new Object[][] {
                { "filter.operator.symbol.group.start", 'x' },
                { "validation.message.background", Color.black },
                { "button.tooltip.accelerator.format", " - %s"},
            };
        }
    }

    public static class TestResources2 extends ListResourceBundle {
        @Override
        protected Object[][] getContents() {
            return new Object[][] {
                { "filter.operator.symbol.group.start", "xyz" },
                { "validation.message.background", "0,0,0" },
            };
        }
    }

    public static class TestResources3 extends ListResourceBundle {
        @Override
        protected Object[][] getContents() {
            return new Object[0][];
        }
    }
}