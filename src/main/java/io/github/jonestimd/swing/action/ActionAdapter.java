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

import javax.swing.AbstractAction;
import javax.swing.Action;

public class ActionAdapter extends AbstractAction {
    private ActionListener handler;

    public static Action forMnemonicAndName(ActionListener handler, String mnemonicAndName) {
        ActionAdapter action = new ActionAdapter(handler, mnemonicAndName.substring(1));
        action.putValue(MNEMONIC_KEY, Integer.valueOf(mnemonicAndName.charAt(0)));
        return action;
    }

    public ActionAdapter(ActionListener handler) {
        this.handler = handler;
    }

    public ActionAdapter(ActionListener handler, String name) {
        super(name);
        this.handler = handler;
    }

    public void actionPerformed(ActionEvent e) {
        handler.actionPerformed(e);
    }
}