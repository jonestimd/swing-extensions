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
package io.github.jonestimd.swing.component;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.junit.Test;

import static org.fest.assertions.Assertions.*;

public class AutosizeTextFieldTest {
    private JFrame frame = new JFrame("Autosize text field test");
    private AutosizeTextField textField = new AutosizeTextField(false);
    private int width;

    @Test
    public void setTextUpdatesSize() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Box box = Box.createHorizontalBox();
            box.add(Box.createHorizontalGlue());
            box.add(textField);
            frame.getContentPane().add(box);
            textField.setText("start");
            frame.setMinimumSize(new Dimension(500, 100));
            frame.pack();
            frame.setVisible(true);
            width = textField.getWidth();
        });

        SwingUtilities.invokeAndWait(() -> {
            textField.setText("start plus more");
        });

        SwingUtilities.invokeAndWait(() -> {
            assertThat(textField.getWidth()).isGreaterThan(width);
            frame.dispose();
        });
    }
}