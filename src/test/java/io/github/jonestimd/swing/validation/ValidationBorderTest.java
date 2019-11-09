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
package io.github.jonestimd.swing.validation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class ValidationBorderTest {
    private List<PropertyChangeListener> validationListeners = new ArrayList<>();
    private String validationMessages;

    @Test
    public void addToViewport_wrapsViewportBorder() throws Exception {
        ValidatedPanel panel = new ValidatedPanel();
        Border viewportBorder = new EmptyBorder(1, 1, 1, 1);
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setViewportBorder(viewportBorder);

        ValidationBorder.addToViewport(panel);

        assertThat(((CompoundBorder) scrollPane.getViewportBorder()).getInsideBorder()).isSameAs(viewportBorder);
        Border outsideBorder = ((CompoundBorder) scrollPane.getViewportBorder()).getOutsideBorder();
        assertThat(outsideBorder).isInstanceOf(ValidationBorder.class);
        assertThat(validationListeners).isNotEmpty();
        assertThat(((ValidationBorder) outsideBorder).isValid()).isTrue();
    }

    @Test
    public void addToViewport_updatesBorder() throws Exception {
        ValidatedPanel panel = new ValidatedPanel();
        JScrollPane scrollPane = new JScrollPane(panel);
        ValidationBorder.addToViewport(panel);

        validationListeners.forEach(listener -> listener.propertyChange(new PropertyChangeEvent(this, "messages", null, "error")));

        Border outsideBorder = ((CompoundBorder) scrollPane.getViewportBorder()).getOutsideBorder();
        ValidationBorder validationBorder = (ValidationBorder) outsideBorder;
        assertThat(validationBorder.isValid()).isFalse();
    }

    public class ValidatedPanel extends JPanel implements ValidatedComponent {
        @Override
        public void validateValue() {
        }

        @Override
        public String getValidationMessages() {
            return validationMessages;
        }

        @Override
        public void addValidationListener(PropertyChangeListener listener) {
            validationListeners.add(listener);
        }

        @Override
        public void removeValidationListener(PropertyChangeListener listener) {

        }
    }
}