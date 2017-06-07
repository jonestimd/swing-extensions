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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import io.github.jonestimd.util.Streams;

/**
 * The model for a {@link FileSuggestField}.  Provides a list of files and/or directories based on the current
 * field value.
 */
public class FileSuggestModel extends SuggestModel<File> {
    private static final String TRAILING_DOT = "(\\" + File.separator + "\\.)+$";
    private final Predicate<File> filePredicate;
    private File directory;

    public FileSuggestModel(File startDirectory, Predicate<File> filePredicate) {
        this.directory = startDirectory;
        this.filePredicate = filePredicate;
        setElements(getFiles(), true);
    }

    private List<File> getFiles() {
        File[] files = directory.listFiles();
        if (files == null) return Collections.singletonList(directory);
        List<File> items = Streams.filter(Arrays.asList(files), filePredicate);
        items.add(0, directory);
        Collections.sort(items);
        return items;
    }

    @Override
    public File updateSuggestions(String editorText) {
        File currentDir = getParent(editorText.replaceAll(TRAILING_DOT, ""));
        if (currentDir != null && !currentDir.getAbsolutePath().equals(directory.getAbsolutePath())) {
            directory = currentDir;
            setElements(getFiles(), true);
        }
        return new File(editorText);
    }

    private File getParent(String text) {
        File file = new File(text);
        return text.endsWith(File.separator) ? file : file.getParentFile();
    }
}
