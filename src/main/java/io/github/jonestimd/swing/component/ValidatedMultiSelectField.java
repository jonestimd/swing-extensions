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

import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.function.BiPredicate;

import io.github.jonestimd.swing.validation.ValidatedComponent;
import io.github.jonestimd.swing.validation.ValidationSupport;
import io.github.jonestimd.swing.validation.Validator;

public class ValidatedMultiSelectField extends MultiSelectField implements ValidatedComponent {
    private final ValidationSupport<List<String>> validationSupport = new ValidationSupport<>(this);

    public ValidatedMultiSelectField(boolean showItemDelete, boolean opaqueItems) {
        this(showItemDelete, opaqueItems, DEFAULT_IS_VALID_ITEM);
    }

    public ValidatedMultiSelectField(boolean showItemDelete, boolean opaqueItems, BiPredicate<MultiSelectField, String> isValidItem) {
        super(showItemDelete, opaqueItems, isValidItem);
        validateValue();
    }

    @Override
    protected void fireItemsChanged() {
        super.fireItemsChanged();
        validateValue();
    }

    public void setValidator(Validator<List<String>> validator) {
        validationSupport.setValidator(validator, getItems());
    }

    @Override
    public void validateValue() {
        validationSupport.validateValue(getItems());
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
}
