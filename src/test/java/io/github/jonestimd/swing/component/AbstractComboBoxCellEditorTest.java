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

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

public abstract class AbstractComboBoxCellEditorTest {
    protected static class TestBean implements Comparable<TestBean> {
        public final String name;

        public TestBean(String name) {
            this.name = name;
        }

        @Override
        public int compareTo(TestBean that) {
            return name.compareTo(that.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof TestBean && ((TestBean)obj).name.equals(name);
        }
    }

    protected static class TestFormat extends Format {
        @Override
        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            if (obj != null) {
                toAppendTo.append(((TestBean)obj).name);
            }
            return toAppendTo;
        }

        @Override
        public Object parseObject(String source, ParsePosition pos) {
            pos.setIndex(source.length() + 1);
            return new TestBean(source);
        }
    }
}
