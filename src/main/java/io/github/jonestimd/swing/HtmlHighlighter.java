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

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import io.github.jonestimd.swing.table.Highlighter;

/**
 * Highlights occurrences of values in a string by wrapping them with a HTML tag.
 */
public class HtmlHighlighter implements Highlighter {
    private static final Comparator<Range> RANGE_SORT = (r1, r2) -> {
        int diff = r1.lower - r2.lower;
        return diff == 0 ? r2.upper - r1.upper : diff;
    };
    private final String startTag;
    private final String endTag;

    public HtmlHighlighter(String startTag, String endTag) {
        this.startTag = startTag;
        this.endTag = endTag;
    }

    /**
     * @return an HTML string with values in {@code highlightText} wrapped by the start and end tags.
     */
    public String highlight(String value, Collection<String> highlightText) {
        Set<Range> ranges = findMatches(value.toLowerCase(), highlightText);
        return ranges.isEmpty() ? value : highlightRanges(value, ranges);
    }

    protected String highlightRanges(String value, Set<Range> ranges) {
        StringBuilder buffer = new StringBuilder("<html>");
        int from = 0;
        for (Range range : ranges) {
            if (range.lower > from) {
                buffer.append(value.substring(from, range.lower));
            }
            if (range.upper >= from) {
                buffer.append(startTag).append(value.substring(range.lower, range.upper + 1)).append(endTag);
                from = range.upper + 1;
            }
        }
        if (from < value.length()) {
            buffer.append(value.substring(from));
        }
        return buffer.append("</html>").toString();
    }

    protected Set<Range> findMatches(String lowerValue, Collection<String> highlightText) {
        Set<Range> ranges = new TreeSet<>(RANGE_SORT);
        for (String text : highlightText) {
            findMatches(lowerValue, text.toLowerCase(), ranges);
        }
        return ranges;
    }

    private void findMatches(String lowerValue, String highlightText, Set<Range> ranges) {
        int start = lowerValue.indexOf(highlightText);
        while (start >= 0) {
            int end = start + highlightText.length();
            ranges.add(new Range(start, end-1));
            start = lowerValue.indexOf(highlightText, end);
        }
    }

    protected static class Range {
        public final int lower;
        public final int upper;

        public Range(int lower, int upper) {
            this.lower = lower;
            this.upper = upper;
        }
    }
}
