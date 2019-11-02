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
package io.github.jonestimd.swing.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

/**
 * An abstract class for an action that is configured using values from a resource bundle.  The following values
 * are used from the resource bundle:
 * <ul>
 *     <li>{@code keyPrefix + ".mnemonicAndName"} (optional)</li>
 *     <ul>
 *         <li>First character provides the mnemonic</li>
 *         <li>Remainder provides the name</li>
 *     </ul>
 *     <li>{@code keyPrefix + ".iconImage"} (optional, path to image resource)</li>
 *     <li>{@code keyPrefix + ".accelerator"} (optional, keystroke string)</li>
 * </ul>
 */
public abstract class LocalizedAction extends AbstractAction {
    /**
     * Create an action using an {@link ActionListener}.
     * @param bundle the resource bundle containing the action's properties.
     * @param keyPrefix the key prefix for the action's property resources.
     */
    public LocalizedAction(ResourceBundle bundle, String keyPrefix) {
        if (bundle.containsKey(keyPrefix + ".mnemonicAndName")) {
            String mnemonicAndName = bundle.getString(keyPrefix + ".mnemonicAndName");
            putValue(NAME, mnemonicAndName.substring(1));
            putValue(MNEMONIC_KEY, (int) mnemonicAndName.charAt(0));
        }
        if (bundle.containsKey(keyPrefix + ".iconImage")) {
            putValue(SMALL_ICON, new ImageIcon(getClass().getResource(bundle.getString(keyPrefix + ".iconImage"))));
        }
        if (bundle.containsKey(keyPrefix + ".accelerator")) {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(bundle.getString(keyPrefix + ".accelerator")));
        }
    }

    /**
     * Create an action using an {@link ActionListener}.
     * @param bundle the resource bundle containing the action's properties.
     * @param keyPrefix the key prefix for the action's property resources.
     * @param handler the action listener to invoke when the action is performed.
     */
    public static Action create(ResourceBundle bundle, String keyPrefix, ActionListener handler) {
        return new LocalizedAction(bundle, keyPrefix) {
            @Override
            public void actionPerformed(ActionEvent event) {
                handler.actionPerformed(event);
            }
        };
    }

    /**
     * A factory class for creating multiple actions using the same <code>ResourceBundle</code> and a common resource prefix.
     */
    public static class Factory {
        private final ResourceBundle bundle;
        private final String resourcePrefix;

        public Factory(ResourceBundle bundle, String resourcePrefix) {
            this.bundle = bundle;
            this.resourcePrefix = resourcePrefix;
        }

        /**
         * Create an action using an {@link ActionListener}.  The action's resource keys must begin with
         * <code>resourcePrefix+keySuffix</code>.
         */
        public Action newAction(String keySuffix, ActionListener handler) {
            return LocalizedAction.create(bundle, resourcePrefix + keySuffix, handler);
        }
    }
}