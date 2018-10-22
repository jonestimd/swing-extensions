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
package io.github.jonestimd.swing.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

/**
 * Converts an {@link ActionListener} to an {@link Action}.
 */
public class ActionAdapter extends AbstractAction {
    private ActionListener handler;

    public static Action forMnemonicAndName(ActionListener handler, String mnemonicAndName) {
        ActionAdapter action = new ActionAdapter(handler, mnemonicAndName.substring(1));
        action.putValue(MNEMONIC_KEY, (int) mnemonicAndName.charAt(0));
        return action;
    }

    public static <T extends Action> T initialize(T action, ResourceBundle bundle, String keyPrefix) {
        if (bundle.containsKey(keyPrefix + ".mnemonicAndName")) {
            String mnemonicAndName = bundle.getString(keyPrefix+".mnemonicAndName");
            action.putValue(NAME, mnemonicAndName.substring(1));
            action.putValue(MNEMONIC_KEY, (int) mnemonicAndName.charAt(0));
        }
        if (bundle.containsKey(keyPrefix + ".iconImage")) {
            action.putValue(SMALL_ICON, new ImageIcon(action.getClass().getResource(bundle.getString(keyPrefix + ".iconImage"))));
        }
        if (bundle.containsKey(keyPrefix + ".accelerator")) {
            action.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(bundle.getString(keyPrefix + ".accelerator")));
        }
        return action;
    }

    public ActionAdapter(ActionListener handler) {
        this.handler = handler;
    }

    public ActionAdapter(ActionListener handler, String name) {
        this(handler);
        putValue(NAME, name);
    }

    /**
     * Create an action using settings from a resource bundle.  The following resource bundle keys will be used if available:
     * <ul>
     *     <li><em>keyPrefix</em>.mnemonicAndName - string containing the mnemonic character followed by the action name</li>
     *     <li><em>keyPrefix</em>.imageIcon - the location of the image to use for the {@link Action#SMALL_ICON}</li>
     *     <li><em>keyPrefix</em>.accelerator - the accelerator keystroke for the action</li>
     * </ul>
     * @param handler action handler
     * @param bundle the resource bundle containing the action settings
     * @param keyPrefix the key prefix for the action settings
     */
    public ActionAdapter(ActionListener handler, ResourceBundle bundle, String keyPrefix) {
        this(handler);
        initialize(this, bundle, keyPrefix);
    }

    public void actionPerformed(ActionEvent e) {
        handler.actionPerformed(e);
    }
}