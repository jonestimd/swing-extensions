package io.github.jonestimd.swing;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import static org.fest.assertions.Assertions.*;

public class HtmlHighlighterTest {
    private final HtmlHighlighter highlighter = new HtmlHighlighter("<span>", "</span>");

    @Test
    public void returnsValueIfNoMatches() throws Exception {
        assertThat(highlighter.highlight("", Collections.emptyList())).isEqualTo("");
    }

    @Test
    public void highlightsMatchAtBeginning() throws Exception {
        assertThat(highlighter.highlight("abc", Collections.singleton("a"))).isEqualTo("<html><span>a</span>bc</html>");
    }

    @Test
    public void highlightsMatchAtEnd() throws Exception {
        assertThat(highlighter.highlight("abc", Collections.singleton("c"))).isEqualTo("<html>ab<span>c</span></html>");
    }

    @Test
    public void highlightsMatchIgnoringCase() throws Exception {
        assertThat(highlighter.highlight("abcXYZ", Collections.singleton("Cx"))).isEqualTo("<html>ab<span>cX</span>YZ</html>");
    }

    @Test
    public void highlightsAnyValuesInList() throws Exception {
        assertThat(highlighter.highlight("abcXYZ", Arrays.asList("xyz", "123"))).isEqualTo("<html>abc<span>XYZ</span></html>");
    }

    @Test
    public void highlightsMultipleMatches() throws Exception {
        assertThat(highlighter.highlight("abc XYZ abc 123", Arrays.asList("abc", "123"))).isEqualTo("<html><span>abc</span> XYZ <span>abc</span> <span>123</span></html>");
    }

    @Test
    public void doesNotHighlightTags() throws Exception {
        assertThat(highlighter.highlight("abc XYZ abc 123", Arrays.asList("xyz", "an"))).isEqualTo("<html>abc <span>XYZ</span> abc 123</html>");
    }

    @Test
    public void highlightsLongestMatch() throws Exception {
        assertThat(highlighter.highlight("abc XYZ abc 123", Arrays.asList("xy", "xyz"))).isEqualTo("<html>abc <span>XYZ</span> abc 123</html>");
    }

    @Test
    public void highlightsOnce() throws Exception {
        assertThat(highlighter.highlight("abc XYZ abc 123", Arrays.asList("xyz", "XYZ"))).isEqualTo("<html>abc <span>XYZ</span> abc 123</html>");
    }
}