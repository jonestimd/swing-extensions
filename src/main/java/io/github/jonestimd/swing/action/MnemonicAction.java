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
package io.github.jonestimd.swing.action;

import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

/**
 * An abstract class for an action that is configured using values from a resource bundle.  The following values
 * are used from the resource bundle:
 * <ul>
 *     <li>{@code resourcePrefix + ".mnemonicAndName"} (required)</li>
 *     <ul>
 *         <li>First character provides the mnemonic</li>
 *         <li>Remainder provides the name</li>
 *     </ul>
 *     <li>{@code resourcePrefix + ".iconImage"} (optional, path to image resource)</li>
 *     <li>{@code resourcePrefix + ".accelerator"} (optional, keystroke string)</li>
 * </ul>
 */
public abstract class MnemonicAction extends AbstractAction {
    public MnemonicAction(String mnemonicAndName) {
        this(mnemonicAndName, null);
    }

    public MnemonicAction(String mnemonicAndName, Icon icon) {
        putValue(NAME, mnemonicAndName.substring(1));
        putValue(SMALL_ICON, icon);
        putValue(MNEMONIC_KEY, (int) mnemonicAndName.charAt(0));
    }

    public MnemonicAction(ResourceBundle bundle, String keyPrefix) {
        String mnemonicAndName = bundle.getString(keyPrefix + ".mnemonicAndName");
        putValue(NAME, mnemonicAndName.substring(1));
        putValue(MNEMONIC_KEY, (int) mnemonicAndName.charAt(0));
        if (bundle.containsKey(keyPrefix + ".iconImage")) {
            putValue(SMALL_ICON, new ImageIcon(getClass().getResource(bundle.getString(keyPrefix + ".iconImage"))));
        }
        if (bundle.containsKey(keyPrefix + ".accelerator")) {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(bundle.getString(keyPrefix + ".accelerator")));
        }
    }
}