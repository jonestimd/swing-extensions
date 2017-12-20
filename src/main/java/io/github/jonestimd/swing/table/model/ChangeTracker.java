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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import io.github.jonestimd.collection.IdentityArrayList;

import static java.util.stream.Stream.*;

/**
 * Base class for tracking unsaved changes in a table.  Unsaved changes include
 * <ul>
 * <li>unsaved new rows</li>
 * <li>unsaved deletes</li>
 * <li>unsaved modifications to rows</li>
 * </ul>
 * Unsaved modifications to rows are keyed by the row bean and a property index (typically the index of the table column
 * displaying the property).
 * @param <T> the class representing a row in the table
 */
public abstract class ChangeTracker<T> {
    private final List<T> pendingAdds;
    private final Map<T, Map<Integer, Object>> originalValues;
    private final List<T> pendingDeletes;

    /**
     * @param useEquals if true then use {@link Object#equals(Object)} to match rows, otherwise use object identity
     */
    protected ChangeTracker(boolean useEquals) {
        if (useEquals) {
            pendingAdds = new ArrayList<>();
            originalValues = new HashMap<>();
            pendingDeletes = new ArrayList<>();
        }
        else {
            pendingAdds = new IdentityArrayList<>();
            originalValues = new IdentityHashMap<>();
            pendingDeletes = new IdentityArrayList<>();
        }
    }

    /**
     * Mark a row as a pending add.
     */
    public void pendingAdd(T item) {
        pendingAdds.add(item);
    }

    /**
     * Mark a row as a pending delete.
     */
    public void pendingDelete(T item) {
        if (!pendingAdds.remove(item)) {
            pendingDeletes.add(item);
        }
    }

    /**
     * Cancel pending deletes matching a predicate.
     */
    public void cancelDeletes(Predicate<? super T> predicate) {
        pendingDeletes.removeIf(predicate);
    }

    /**
     * Reset pending changes for a row.
     */
    public void resetItem(T item) {
        pendingAdds.remove(item);
        originalValues.remove(item);
        pendingDeletes.remove(item);
    }

    /**
     * Reset pending changes for rows matching a predicate.
     */
    public void resetItems(Predicate<? super T> predicate) {
        pendingAdds.removeIf(predicate);
        originalValues.keySet().removeIf(predicate);
        pendingDeletes.removeIf(predicate);
    }

    /**
     * @return true if the row is a pending add.
     */
    public boolean isPendingAdd(T item) {
        return pendingAdds.contains(item);
    }

    /**
     * @return true if the row is a pending delete.
     */
    public boolean isPendingDelete(T item) {
        return pendingDeletes.contains(item);
    }

    /**
     * Add or remove a pending change for a row property.
     * @param item the row bean
     * @param index the property index
     * @param oldValue the previous value of the property
     * @param newValue the replacement value for the property
     */
    public void setValue(T item, int index, Object oldValue, Object newValue) {
        if (!pendingAdds.contains(item)) {
            Map<Integer, Object> changes = originalValues.get(item);
            if (changes == null) {
                changes = new HashMap<>();
                changes.put(index, oldValue);
                originalValues.put(item, changes);
            }
            else if (changes.containsKey(index)) {
                if (Objects.equals(newValue, changes.get(index))) {
                    changes.remove(index);
                    if (changes.isEmpty()) {
                        originalValues.remove(item);
                    }
                }
            }
            else {
                changes.put(index, oldValue);
            }
        }
    }

    /**
     * @return true if there are no pending changes for any rows.
     */
    public boolean isEmpty() {
        return pendingAdds.isEmpty() && originalValues.isEmpty() && pendingDeletes.isEmpty();
    }

    /**
     * @param item a row bean
     * @param index a property index
     * @return true if the row property has been modified.
     */
    public boolean isChanged(T item, int index) {
        return pendingAdds.contains(item) || pendingDeletes.contains(item)
                || originalValues.containsKey(item) && originalValues.get(item).containsKey(index);
    }

    public Set<Integer> getChangeIndexes(T item) {
        return originalValues.getOrDefault(item, Collections.emptyMap()).keySet();
    }

    /**
     * @return all of the rows with pending changes.
     */
    public Stream<T> getChanges() {
        return concat(pendingAdds.stream(), concat(originalValues.keySet().stream(), pendingDeletes.stream()));
    }

    /**
     * @return unsaved new rows.
     */
    public List<T> getAdds() {
        return Collections.unmodifiableList(pendingAdds);
    }

    /**
     * @return rows pending deletion.
     */
    public List<T> getDeletes() {
        return Collections.unmodifiableList(pendingDeletes);
    }

    /**
     * @return unsaved new and modified rows.
     */
    public Stream<T> getUpdates() {
        return Stream.concat(pendingAdds.stream(), originalValues.keySet().stream());
    }

    /**
     * Restore the original value of a modified property of a row.  If the specified proeprty has been modified
     * then {@link #revertItemChange(Object, Object, int)} is called with the original value.
     * @param item the row to update
     * @param index the property index
     */
    public void undoChange(T item, int index) {
        Map<Integer, Object> changes = originalValues.get(item);
        if (changes != null && changes.containsKey(index)) {
            Object originalValue = changes.remove(index);
            if (changes.isEmpty()) {
                originalValues.remove(item);
            }
            revertItemChange(originalValue, item, index);
        }
    }

    public void undoDelete(T item) {
        if (pendingDeletes.remove(item)) {
            itemUpdated(item);
        }
    }

    /**
     * Revert all pending changes.
     */
    public void revert() {
        while (!pendingAdds.isEmpty()) {
            itemDeleted(pendingAdds.remove(0));
        }
        while (!pendingDeletes.isEmpty()) {
            itemUpdated(pendingDeletes.remove(0));
        }
        for (Iterator<Map.Entry<T, Map<Integer, Object>>> iter = originalValues.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry<T, Map<Integer, Object>> entry = iter.next();
            T item = entry.getKey();
            for (Map.Entry<Integer, Object> changeEntry : entry.getValue().entrySet()) {
                revertItemChange(changeEntry.getValue(), item, changeEntry.getKey());
            }
            iter.remove();
        }
    }

    /**
     * Implementation should update the table model with the original value <em>without notifying this change tracker</em>.
     */
    protected abstract void revertItemChange(Object originalValue, T item, int index);

    /**
     * Called when the modification status of a row changes.  Implementation should fire table model change events.
     */
    protected abstract void itemUpdated(T item);

    /**
     * Implementation should remove the row from the table <em>without notifying this change tracker</em>.
     */
    protected abstract void itemDeleted(T item);

    /**
     * Commit all pending changes.
     */
    public void commit() {
        while (!pendingAdds.isEmpty()) {
            itemUpdated(pendingAdds.remove(0));
        }
        while (!pendingDeletes.isEmpty()) {
            itemDeleted(pendingDeletes.remove(0));
        }
        for (Iterator<T> iter = originalValues.keySet().iterator(); iter.hasNext(); ) {
            T item = iter.next();
            iter.remove();
            itemUpdated(item);
        }
    }

    /**
     * Clear all pending changes.
     */
    public void reset() {
        pendingAdds.clear();
        originalValues.clear();
        pendingDeletes.clear();
    }
}