// The MIT License (MIT)
//
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
package io.github.jonestimd.swing;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class LabelBuilderTest {
    @BeforeClass
    public static void setLookAndFeel() throws Exception {
        UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
    }

    @Test
    public void mnemonicAndNameSetsMnemonicAndText() throws Exception {
        JLabel label = new LabelBuilder().mnemonicAndName("FField Name").get();

        assertThat(label.getDisplayedMnemonic()).isEqualTo('F');
        assertThat(label.getText()).isEqualTo("Field Name");
    }

    @Test
    public void mnemonicAndNameDoesNotSetMnemonicToUnderscore() throws Exception {
        JLabel label = new LabelBuilder().mnemonicAndName("_Field Name").get();

        assertThat(label.getDisplayedMnemonic()).isEqualTo(0);
        assertThat(label.getText()).isEqualTo("Field Name");
    }

    @Test
    public void forComponentSetsLabelComponent() throws Exception {
        JTextField field = new JTextField();

        JLabel label = new LabelBuilder().forComponent(field).get();

        assertThat(label.getLabelFor()).isSameAs(field);
    }

    @Test
    public void boldUsesBoldFont() throws Exception {
        JLabel label = new LabelBuilder().name("Field").bold().get();

        assertThat(label.getFont().isBold()).isTrue();
    }
}