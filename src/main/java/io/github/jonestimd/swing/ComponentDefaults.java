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

import java.awt.Color;
import java.util.ResourceBundle;

import javax.swing.UIManager;
import javax.swing.border.Border;

public class ComponentDefaults {
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(UIDefaultsBundle.class.getName());

    public static Color getColor(String key) {
        Color color = UIManager.getColor(key);
        if (color == null) {
            color = (Color) BUNDLE.getObject(key);
        }
        return color;
    }

    public static Border getBorder(String key) {
        Border border = UIManager.getBorder(key);
        if (border == null) {
            border = (Border) BUNDLE.getObject(key);
        }
        return border;
    }
}
