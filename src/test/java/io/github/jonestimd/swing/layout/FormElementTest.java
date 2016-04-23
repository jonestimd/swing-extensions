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
package io.github.jonestimd.swing.layout;

import java.awt.GridBagConstraints;

import org.junit.Test;

import static org.fest.assertions.Assertions.*;

public class FormElementTest {
    @Test
    public void topLabel() throws Exception {
        GridBagConstraints gbc = FormElement.TOP_LABEL.setConstraints(new GridBagConstraints());

        assertThat(FormElement.TOP_LABEL.getLabelConstraints()).isNull();
        assertThat(gbc.anchor).isEqualTo(GridBagConstraints.WEST);
        assertThat(gbc.gridx).isEqualTo(-1);
        assertThat(gbc.gridy).isEqualTo(-1);
        assertThat(gbc.gridwidth).isEqualTo(2);
        assertThat(gbc.gridheight).isEqualTo(1);
        assertThat(gbc.weightx).isEqualTo(0);
        assertThat(gbc.weighty).isEqualTo(0);
        assertThat(gbc.fill).isEqualTo(GridBagConstraints.NONE);
        assertThat(gbc.insets.left).isEqualTo(0);
    }

    @Test
    public void leftLabel() throws Exception {
        GridBagConstraints gbc = FormElement.LEFT_LABEL.setConstraints(new GridBagConstraints());

        assertThat(FormElement.LEFT_LABEL.getLabelConstraints()).isNull();
        assertThat(gbc.anchor).isEqualTo(GridBagConstraints.EAST);
        assertThat(gbc.gridx).isEqualTo(-1);
        assertThat(gbc.gridy).isEqualTo(-1);
        assertThat(gbc.gridwidth).isEqualTo(1);
        assertThat(gbc.gridheight).isEqualTo(1);
        assertThat(gbc.weightx).isEqualTo(0);
        assertThat(gbc.weighty).isEqualTo(0);
        assertThat(gbc.fill).isEqualTo(GridBagConstraints.NONE);
        assertThat(gbc.insets.left).isEqualTo(5);
    }

    @Test
    public void buttonGroup() throws Exception {
        GridBagConstraints gbc = FormElement.BUTTON_GROUP.setConstraints(new GridBagConstraints());

        assertThat(FormElement.BUTTON_GROUP.getLabelConstraints()).isSameAs(FormElement.TOP_LABEL);
        assertThat(gbc.anchor).isEqualTo(GridBagConstraints.WEST);
        assertThat(gbc.gridx).isEqualTo(-1);
        assertThat(gbc.gridy).isEqualTo(-1);
        assertThat(gbc.gridwidth).isEqualTo(2);
        assertThat(gbc.gridheight).isEqualTo(1);
        assertThat(gbc.weightx).isEqualTo(1);
        assertThat(gbc.weighty).isEqualTo(0);
        assertThat(gbc.fill).isEqualTo(GridBagConstraints.HORIZONTAL);
        assertThat(gbc.insets.left).isEqualTo(0);
    }

    @Test
    public void checkBox() throws Exception {
        GridBagConstraints gbc = FormElement.CHECK_BOX.setConstraints(new GridBagConstraints());

        assertThat(FormElement.BUTTON_GROUP.getLabelConstraints()).isSameAs(FormElement.TOP_LABEL);
        assertThat(gbc.anchor).isEqualTo(GridBagConstraints.WEST);
        assertThat(gbc.gridx).isEqualTo(1);
        assertThat(gbc.gridy).isEqualTo(-1);
        assertThat(gbc.gridwidth).isEqualTo(1);
        assertThat(gbc.gridheight).isEqualTo(1);
        assertThat(gbc.weightx).isEqualTo(1);
        assertThat(gbc.weighty).isEqualTo(0);
        assertThat(gbc.fill).isEqualTo(GridBagConstraints.HORIZONTAL);
        assertThat(gbc.insets.left).isEqualTo(0);
    }

    @Test
    public void textField() throws Exception {
        GridBagConstraints gbc = FormElement.TEXT_FIELD.setConstraints(new GridBagConstraints());

        assertThat(FormElement.TEXT_FIELD.getLabelConstraints()).isSameAs(FormElement.LEFT_LABEL);
        assertThat(gbc.anchor).isEqualTo(GridBagConstraints.WEST);
        assertThat(gbc.gridx).isEqualTo(-1);
        assertThat(gbc.gridy).isEqualTo(-1);
        assertThat(gbc.gridwidth).isEqualTo(1);
        assertThat(gbc.gridheight).isEqualTo(1);
        assertThat(gbc.weightx).isEqualTo(1);
        assertThat(gbc.weighty).isEqualTo(0);
        assertThat(gbc.fill).isEqualTo(GridBagConstraints.HORIZONTAL);
        assertThat(gbc.insets.left).isEqualTo(0);
    }

    @Test
    public void textArea() throws Exception {
        GridBagConstraints gbc = FormElement.TEXT_AREA.setConstraints(new GridBagConstraints());

        assertThat(FormElement.TEXT_AREA.getLabelConstraints()).isSameAs(FormElement.TOP_LABEL);
        assertThat(gbc.anchor).isEqualTo(GridBagConstraints.WEST);
        assertThat(gbc.gridx).isEqualTo(-1);
        assertThat(gbc.gridy).isEqualTo(-1);
        assertThat(gbc.gridwidth).isEqualTo(2);
        assertThat(gbc.gridheight).isEqualTo(1);
        assertThat(gbc.weightx).isEqualTo(1);
        assertThat(gbc.weighty).isEqualTo(0.3);
        assertThat(gbc.fill).isEqualTo(GridBagConstraints.BOTH);
        assertThat(gbc.insets.left).isEqualTo(0);
    }

    @Test
    public void list() throws Exception {
        GridBagConstraints gbc = FormElement.LIST.setConstraints(new GridBagConstraints());

        assertThat(FormElement.LIST.getLabelConstraints()).isSameAs(FormElement.TOP_LABEL);
        assertThat(gbc.anchor).isEqualTo(GridBagConstraints.WEST);
        assertThat(gbc.gridx).isEqualTo(-1);
        assertThat(gbc.gridy).isEqualTo(-1);
        assertThat(gbc.gridwidth).isEqualTo(2);
        assertThat(gbc.gridheight).isEqualTo(1);
        assertThat(gbc.weightx).isEqualTo(1);
        assertThat(gbc.weighty).isEqualTo(1);
        assertThat(gbc.fill).isEqualTo(GridBagConstraints.BOTH);
        assertThat(gbc.insets.left).isEqualTo(0);
    }

    @Test
    public void table() throws Exception {
        GridBagConstraints gbc = FormElement.TABLE.setConstraints(new GridBagConstraints());

        assertThat(FormElement.TABLE.getLabelConstraints()).isSameAs(FormElement.TOP_LABEL);
        assertThat(gbc.anchor).isEqualTo(GridBagConstraints.WEST);
        assertThat(gbc.gridx).isEqualTo(-1);
        assertThat(gbc.gridy).isEqualTo(-1);
        assertThat(gbc.gridwidth).isEqualTo(2);
        assertThat(gbc.gridheight).isEqualTo(1);
        assertThat(gbc.weightx).isEqualTo(1);
        assertThat(gbc.weighty).isEqualTo(1);
        assertThat(gbc.fill).isEqualTo(GridBagConstraints.BOTH);
        assertThat(gbc.insets.left).isEqualTo(0);
    }

    @Test
    public void panel() throws Exception {
        GridBagConstraints gbc = FormElement.PANEL.setConstraints(new GridBagConstraints());

        assertThat(FormElement.PANEL.getLabelConstraints()).isNull();
        assertThat(gbc.anchor).isEqualTo(GridBagConstraints.WEST);
        assertThat(gbc.gridx).isEqualTo(-1);
        assertThat(gbc.gridy).isEqualTo(-1);
        assertThat(gbc.gridwidth).isEqualTo(2);
        assertThat(gbc.gridheight).isEqualTo(1);
        assertThat(gbc.weightx).isEqualTo(1);
        assertThat(gbc.weighty).isEqualTo(1);
        assertThat(gbc.fill).isEqualTo(GridBagConstraints.BOTH);
        assertThat(gbc.insets.left).isEqualTo(0);
    }
}