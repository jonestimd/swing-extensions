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
package io.github.jonestimd.swing.table;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

import io.github.jonestimd.swing.table.model.BeanTableModel;
import io.github.jonestimd.swing.table.model.ValidatedTableModel;
import io.github.jonestimd.swing.validation.ValidationBorder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ValidationDecoratorTest {
    @Mock
    private DecoratedTable<Object, TestModel<Object>> table;
    @Mock
    private JComponent renderer;
    @Mock
    private TestModel<Object> model;
    private ValidationDecorator decorator = new ValidationDecorator();
    @Captor
    private ArgumentCaptor<Border> borderCaptor;

    @Test
    public void prepareRendererSetsBorder() throws Exception {
        when(table.getModel()).thenReturn(model);
        when(renderer.getBorder()).thenReturn(null);

        decorator.prepareRenderer(table, renderer, 0, 0);

        verify(renderer).setBorder(borderCaptor.capture());
        assertThat(borderCaptor.getValue()).isInstanceOf(ValidationBorder.class);
    }

    @Test
    public void prepareRendererReplacesBorder() throws Exception {
        final Border currentBorder = BorderFactory.createEmptyBorder();
        when(table.getModel()).thenReturn(model);
        when(renderer.getBorder()).thenReturn(currentBorder);

        decorator.prepareRenderer(table, renderer, 0, 0);

        verify(renderer).setBorder(borderCaptor.capture());
        assertThat(borderCaptor.getValue()).isInstanceOf(CompoundBorder.class);
        CompoundBorder border = (CompoundBorder) borderCaptor.getValue();
        assertThat(border.getInsideBorder()).isInstanceOf(ValidationBorder.class);
        assertThat(border.getOutsideBorder()).isSameAs(currentBorder);
    }

    @Test
    public void prepareRendererResetsTooltip() throws Exception {
        doReturn(model).when(table).getModel();
        when(model.validateAt(anyInt(), anyInt())).thenReturn(null);
        when(renderer.getBorder()).thenReturn(null);

        decorator.prepareRenderer(table, renderer, 0, 0);

        verify(renderer).setBorder(borderCaptor.capture());
        assertThat(borderCaptor.getValue()).isInstanceOf(ValidationBorder.class);
        assertThat(((ValidationBorder) borderCaptor.getValue()).isValid()).isTrue();
        verify(renderer).setToolTipText(null);
    }

    @Test
    public void prepareRendererSetsTooltip() throws Exception {
        doReturn(model).when(table).getModel();
        when(model.validateAt(anyInt(), anyInt())).thenReturn("error message");
        when(renderer.getBorder()).thenReturn(null);

        decorator.prepareRenderer(table, renderer, 0, 0);

        verify(renderer).setBorder(borderCaptor.capture());
        assertThat(borderCaptor.getValue()).isInstanceOf(ValidationBorder.class);
        assertThat(((ValidationBorder) borderCaptor.getValue()).isValid()).isFalse();
        verify(renderer).setToolTipText("error message");
    }

    private interface TestModel<T> extends ValidatedTableModel, BeanTableModel<T> {}
}