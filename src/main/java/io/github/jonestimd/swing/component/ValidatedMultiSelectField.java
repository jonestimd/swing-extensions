// The MIT License (MIT)
//
// Copyright (c) 2019 Timothy D. Jones
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

import java.awt.Cursor;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.function.BiPredicate;

import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

import io.github.jonestimd.swing.validation.ValidatedComponent;
import io.github.jonestimd.swing.validation.ValidationSupport;
import io.github.jonestimd.swing.validation.ValidationTooltipBorder;
import io.github.jonestimd.swing.validation.Validator;

public class ValidatedMultiSelectField extends MultiSelectField implements ValidatedComponent {
    private final ValidationSupport<List<String>> validationSupport = new ValidationSupport<>(this);
    private ValidationTooltipBorder validationBorder;

    public ValidatedMultiSelectField(boolean showItemDelete, boolean opaqueItems) {
        this(showItemDelete, opaqueItems, DEFAULT_IS_VALID_ITEM);
    }

    public ValidatedMultiSelectField(boolean showItemDelete, boolean opaqueItems, BiPredicate<MultiSelectField, String> isValidItem) {
        super(showItemDelete, opaqueItems, isValidItem);
        this.validationBorder = new ValidationTooltipBorder(this);
        super.setBorder(new CompoundBorder(super.getBorder(), validationBorder));
        validateValue();
    }

    /**
     * Overridden to return the border wrapping the {@link ValidationTooltipBorder}.
     */
    public Border getBorder() {
        Border border = super.getBorder();
        if (border instanceof CompoundBorder) return ((CompoundBorder) border).getOutsideBorder();
        return null;
    }

    /**
     * Overridden to wrap the {@link ValidationTooltipBorder} with the input border.
     * @param border the border to add around the {@code ValidationBorder} or null
     *        to use the {@code ValidationBorder} alone
     */
    @Override
    public void setBorder(Border border) {
        if (border == null) {
            super.setBorder(validationBorder);
        }
        else {
            super.setBorder(new CompoundBorder(border, validationBorder));
        }
    }

    @Override
    protected void fireItemsChanged() {
        super.fireItemsChanged();
        validateValue();
    }

    protected ValidationTooltipBorder getValidationBorder() {
        return validationBorder;
    }

    public void setValidator(Validator<List<String>> validator) {
        validationBorder.setValid(validationSupport.setValidator(validator, getItems()) == null);
    }

    @Override
    public void validateValue() {
        validationBorder.setValid(validationSupport.validateValue(getItems()) == null);
    }

    @Override
    public String getValidationMessages() {
        return validationSupport.getMessages();
    }

    @Override
    public void addValidationListener(PropertyChangeListener listener) {
        validationSupport.addValidationListener(listener);
    }

    @Override
    public void removeValidationListener(PropertyChangeListener listener) {
        validationSupport.removeValidationListener(listener);
    }

    @Override
    public Cursor getCursor() {
        return validationBorder.isMouseOverIndicator() ? Cursor.getDefaultCursor() : super.getCursor();
    }

    @Override
    public String getToolTipText() {
        return validationBorder.isMouseOverIndicator() ? validationSupport.getMessages() : super.getToolTipText();
    }
}
