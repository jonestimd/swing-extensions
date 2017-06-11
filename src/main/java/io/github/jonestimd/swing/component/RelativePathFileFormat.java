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
package io.github.jonestimd.swing.component;

import java.io.File;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

/**
 * Displays the trailing portion of a file's path relative to a base path.  The base path is represented by
 * a substitution string.
 */
public class RelativePathFileFormat extends Format {
    private final String prefixSubstitute;
    private File basePath;
    private String prefix;

    /**
     * Create a new format using {@code "..."} as the replacement for {@code basePath}.
     * @param basePath the path to replace in the output format
     */
    public RelativePathFileFormat(File basePath) {
        this(basePath, "...");
    }

    /**
     * Create a new format.
     * @param basePath the path to replace in the output format
     * @param basePathSubstitute the string to use as a replacement for {@code basePath}
     */
    public RelativePathFileFormat(File basePath, String basePathSubstitute) {
        this.prefixSubstitute = basePathSubstitute + File.separator;
        setBasePath(basePath);
    }

    public File getBasePath() {
        return basePath;
    }

    public void setBasePath(File basePath) {
        this.basePath = basePath;
        prefix = basePath == null ? null : basePath.toString() + File.separator;
    }

    @Override
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        if (obj instanceof File) {
            File file = (File) obj;
            toAppendTo.append(file.toString());
            if (basePath != null && (basePath.equals(file) || toAppendTo.toString().startsWith(prefix))) {
                toAppendTo.replace(0, prefix.length(), prefixSubstitute);
            }
        }
        return toAppendTo;
    }

    @Override
    public Object parseObject(String source, ParsePosition pos) {
        throw new UnsupportedOperationException();
    }
}
