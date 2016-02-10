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
package io.github.jonestimd.swing.table.model;

import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.List;

/**
 * Interface for providing additional columns in a table.
 * @param <Bean> the class representing a row in the table
 */
public interface TableDataProvider<Bean> {
    /**
     * The property name to be used for state change events.
     */
    public static final String STATE_PROPERTY = "state";

    List<? extends ColumnAdapter<Bean, ?>> getColumnAdapters();

    /**
     * Notification that the table data has been replaced.
     * This method will only be called from the <i>Event Dispatch Thread</i>.
     */
    void setBeans(Collection<Bean> beans);

    /**
     * Notification that a table row has been added.
     * This method will only be called from the <i>Event Dispatch Thread</i>.
     */
    void addBean(Bean bean);

    /**
     * Notification that a table row has been modified.
     * This method will only be called from the <i>Event Dispatch Thread</i>.
     * @return true if column values for this provider are effected
     */
    boolean updateBean(Bean bean, String columnId, Object oldValue);

    /**
     * Notification that a table row has been removed.
     * This method will only be called from the <i>Event Dispatch Thread</i>.
     */
    void removeBean(Bean bean);

    /**
     * Add {@code PropertyChangeListener} to be notified when the data has been loaded.
     */
    void addStateChangeListener(PropertyChangeListener listener);

    void removeStateChangeListener(PropertyChangeListener listener);
}
