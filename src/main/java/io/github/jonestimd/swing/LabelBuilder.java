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
import java.util.ResourceBundle;

import javax.swing.JLabel;

/**
 * A builder class for creating labels.
 */
public class LabelBuilder {
    /** Character to use for a combined mnemonic and name when the label has no mnemonic. */
    public static final char NO_MNEMONIC = '_';
    private final JLabel label = new JLabel();

    /**
     * Set the label's mnemonic and text from a resource bundle value.  The first character of the value
     * indicates the mnemonic and the rest of the string indicates the label text.  If the value starts with
     * {@code '_'} then the label will not have a mnemonic.
     */
    public LabelBuilder mnemonicAndName(String mnemonicAndName) {
        if (mnemonicAndName.charAt(0) != NO_MNEMONIC) {
            mnemonic(mnemonicAndName.charAt(0));
        }
        return name(mnemonicAndName.substring(1));
    }

    /**
     * Set the label text.
     */
    public LabelBuilder name(String text) {
        label.setText(text);
        return this;
    }

    /**
     * Set the label mnemonic.
     */
    public LabelBuilder mnemonic(char mnemonic) {
        label.setDisplayedMnemonic(mnemonic);
        return this;
    }

    /**
     * @param component the component to be associated with the label
     */
    public LabelBuilder forComponent(Component component) {
        label.setLabelFor(component);
        return this;
    }

    /**
     * Make the label font bold.
     */
    public LabelBuilder bold() {
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        return this;
    }

    public JLabel get() {
        return label;
    }

    /**
     * A factory class for building multiple labels using the same <code>ResourceBundle</code> and a common resource prefix.
     */
    public static class Factory {
        private final ResourceBundle bundle;
        private final String resourcePrefix;

        public Factory(ResourceBundle bundle, String resourcePrefix) {
            this.bundle = bundle;
            this.resourcePrefix = resourcePrefix;
        }

        /**
         * Return a new factory for creating bold labels using this factory's <code>ResourceBundle</code> and resource prefix.
         */
        public Factory bold() {
            return new Factory(bundle, resourcePrefix) {
                @Override
                protected LabelBuilder builder(String resourceKey) {
                    return super.builder(resourceKey).bold();
                }
            };
        }

        /**
         * @param resourceSuffix the suffix for the label's mnemonic and name resource key
         * @see LabelBuilder#mnemonicAndName(String)
         */
        public JLabel newLabel(String resourceSuffix) {
            return builder(resourceSuffix).get();
        }

        /**
         * @param resourceSuffix the suffix for the label's mnemonic and name resource key
         * @param component the component to be associated with the label
         * @see LabelBuilder#mnemonicAndName(String)
         */
        public JLabel newLabel(String resourceSuffix, Component component) {
            return builder(resourceSuffix).forComponent(component).get();
        }

        /**
         * Create a label builder using <code>resourceSuffix</code> to get the mnemonic and name.
         */
        protected LabelBuilder builder(String resourceSuffix) {
            return new LabelBuilder().mnemonicAndName(bundle.getString(resourcePrefix + resourceSuffix));
        }
    }
}
