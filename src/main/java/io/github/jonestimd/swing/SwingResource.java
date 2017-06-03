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
package io.github.jonestimd.swing;

import java.awt.Color;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static io.github.jonestimd.swing.ComponentFactory.*;

public enum SwingResource {
    BUTTON_TOOLTIP_ACCELERATOR_FORMAT,
    FILTER_OPERATOR_SYMBOL_AND,
    FILTER_OPERATOR_SYMBOL_OR,
    FILTER_OPERATOR_SYMBOL_NOT,
    FILTER_OPERATOR_SYMBOL_GROUP_START,
    FILTER_OPERATOR_SYMBOL_GROUP_END,
    VALIDATION_MESSAGE_BACKGROUND;

    public static final char NAME_SEPARATOR = '_';
    public static final char KEY_SEPARATOR = '.';
    public final String key = name().toLowerCase().replace(NAME_SEPARATOR, KEY_SEPARATOR);

    public String getString(ResourceBundle bundle) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException ex) {
            return getString();
        }
    }

    public String getString() {
        return DEFAULT_BUNDLE.getString(key);
    }

    public char getChar(ResourceBundle bundle) {
        try {
            Object value = bundle.getObject(key);
            return value instanceof Character ? (char) value : value.toString().charAt(0);
        } catch (MissingResourceException ex) {
            return getChar();
        }
    }

    public char getChar() {
        return (char) DEFAULT_BUNDLE.getObject(key);
    }

    public Color getColor(ResourceBundle bundle) {
        try {
            Object value = bundle.getObject(key);
            return value instanceof Color ? (Color) value : ColorFactory.createColor(value.toString());
        } catch (MissingResourceException ex) {
            return getColor();
        }
    }

    public Color getColor() {
        return (Color) DEFAULT_BUNDLE.getObject(key);
    }

    public static String getString(String key) {
        return DEFAULT_BUNDLE.getString(key);
    }
}