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
package io.github.jonestimd.swing.table;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import io.github.jonestimd.swing.HighlightText;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HighlightTableCellRendererTest {
    @Mock
    private Highlighter highlighter;
    private List<String> highlightText;

    private JTable createPanel() {
        HighlightPanel panel = new HighlightPanel();
        JTable table = new JTable();
        panel.add(new JScrollPane(table));
        return table;
    }

    @Test
    public void doesNotCallHighlighterForNullValue() throws Exception {
        JTable table = createPanel();
        highlightText = Collections.singletonList("abc");
        HighlightTableCellRenderer renderer = new HighlightTableCellRenderer(highlighter);

        renderer.getTableCellRendererComponent(table, null, false, false, 0, 0);

        verifyZeroInteractions(highlighter);
    }

    @Test
    public void passesValueToHighlighter() throws Exception {
        JTable table = createPanel();
        highlightText = Collections.singletonList("abc");
        HighlightTableCellRenderer renderer = new HighlightTableCellRenderer(highlighter);
        Integer value = new Random().nextInt();

        renderer.getTableCellRendererComponent(table, value, false, false, 0, 0);

        verify(highlighter).highlight(value.toString(), highlightText);
    }

    @Test
    public void passesEmptyListToHighlighterWhenNoHighlightSource() throws Exception {
        HighlightTableCellRenderer renderer = new HighlightTableCellRenderer(highlighter);

        renderer.getTableCellRendererComponent(new JTable(), "", false, false, 0, 0);

        verify(highlighter).highlight("", Collections.emptyList());
    }

    private class HighlightPanel extends JPanel implements HighlightText {
        @Override
        public Collection<String> getHighlightText() {
            return highlightText;
        }
    }
}