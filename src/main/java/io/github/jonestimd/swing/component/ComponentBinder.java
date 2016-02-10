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
package io.github.jonestimd.swing.component;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.Format;

import javax.swing.JTextField;

import io.github.jonestimd.beans.ObservableBean;

public class ComponentBinder {
    private static final String LISTENER_PROPERTY = "com.jonestim.swing.component.ComponentBinder.listener";
    private static final String SOURCE_PROPERTY = "com.jonestim.swing.component.ComponentBinder.source";

    public static <T extends JTextField> T bind(ObservableBean source, String propertyName, Object currentValue, T label, Format format) {
        ValueChangeHandler listener = (ValueChangeHandler) label.getClientProperty(LISTENER_PROPERTY);
        if (listener == null) {
            listener = new ValueChangeHandler(label, format);
            label.putClientProperty(LISTENER_PROPERTY, listener);
        }
        ObservableBean oldSource = (ObservableBean) label.getClientProperty(SOURCE_PROPERTY);
        if (oldSource != null) {
            oldSource.removePropertyChangeListener(propertyName, listener);
        }
        label.putClientProperty(SOURCE_PROPERTY, source);
        source.addPropertyChangeListener(propertyName, listener);
        label.setText(format.format(currentValue));
        return label;
    }

    private static class ValueChangeHandler implements PropertyChangeListener {
        private final JTextField label;
        private final Format format;

        public ValueChangeHandler(JTextField label, Format format) {
            this.label = label;
            this.format = format;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            label.setText(format.format(evt.getNewValue()));
        }
    }
}
