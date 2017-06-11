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

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class RelativePathFileFormatTest {
    private final File file = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getFile());

    @Test
    public void getBasePath() throws Exception {
        RelativePathFileFormat format = new RelativePathFileFormat(file.getParentFile());

        assertThat(format.getBasePath()).isEqualTo(file.getParentFile());
    }

    @Test
    public void formatReturnsEmptyStringForNull() throws Exception {
        RelativePathFileFormat format = new RelativePathFileFormat(null);

        assertThat(format.format(null)).isEqualTo("");
    }

    @Test
    public void formatReturnsFullPathForNullBasePath() throws Exception {
        RelativePathFileFormat format = new RelativePathFileFormat(null);

        assertThat(format.format(file)).isEqualTo(file.toString());
    }

    @Test
    public void formatReturnsEllipsisForBasePath() throws Exception {
        RelativePathFileFormat format = new RelativePathFileFormat(file);

        assertThat(format.format(file)).isEqualTo("..." + File.separator);
    }

    @Test
    public void formatReturnsTrailingElements() throws Exception {
        RelativePathFileFormat format = new RelativePathFileFormat(file.getParentFile());

        assertThat(format.format(file)).isEqualTo(".../" + file.getName());
    }

    @Test
    public void formatReturnsFullPathWhenBasePathNotAnAncestor() throws Exception {
        RelativePathFileFormat format = new RelativePathFileFormat(new File(file, "x"));

        assertThat(format.format(file)).isEqualTo(file.toString());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void parseThrowsException() throws Exception {
        new RelativePathFileFormat(null).parseObject("");
    }
}