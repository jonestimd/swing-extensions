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

import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;

public class LabelFactory {
    private final JLabel label = new JLabel();

    /**
     * Set the label's mnemonic and text from a resource bundle value.  The first character of the value
     * indicates the mnemonic and the rest of the string indicates the label text.  If the value starts with
     * {@code '_'} then the label will not have a mnemonic.
     */
    public LabelFactory mnemonicAndName(String mnemonicAndName) {
        if (mnemonicAndName.charAt(0) != '_') {
            mnemonic(mnemonicAndName.charAt(0));
        }
        return name(mnemonicAndName.substring(1));
    }

    public LabelFactory name(String name) {
        label.setText(name);
        return this;
    }

    public LabelFactory mnemonic(char mnemonic) {
        label.setDisplayedMnemonic(mnemonic);
        return this;
    }

    public LabelFactory forComponent(Component component) {
        label.setLabelFor(component);
        return this;
    }

    public LabelFactory bold() {
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        return this;
    }

    public JLabel get() {
        return label;
    }
}
