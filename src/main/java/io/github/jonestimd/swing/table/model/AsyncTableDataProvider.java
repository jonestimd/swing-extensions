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
import java.beans.PropertyChangeSupport;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingWorker;

/**
 * Base class for loading table data in a background thread.
 * @param <Bean> the class representing a row in the table
 * @param <Query> the class representing the request for the data (e.g. a {@link java.util.Collection Collection} of IDs)
 * @param <Result> the class representing the collection of retrieved data (e.g. a {@link java.util.Map Map} of IDs to row data)
 */
public abstract class AsyncTableDataProvider<Bean, Query, Result> implements TableDataProvider<Bean> {
    private final Logger logger = Logger.getLogger(AsyncTableDataProvider.class.getName());
    protected final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    private List<Worker> workers = new LinkedList<>();

    @Override
    public void addStateChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(STATE_PROPERTY, listener);
    }

    @Override
    public void removeStateChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(STATE_PROPERTY, listener);
    }

    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        changeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    protected void fireStateChanged() {
        firePropertyChange(STATE_PROPERTY, null, null);
    }

    /**
     * Execute a query on a background thread.
     */
    protected abstract Result getData(Query query) throws Exception;

    /**
     * Update the model with the result of a query.
     * This method will only be called from the <i>Event Dispatch Thread</i>.
     */
    protected abstract void setResult(Result result);

    /**
     * Compare two queries for redundancy.
     * @param pendingQuery a query on the queue
     * @param newQuery a query to be added to the queue
     * @return true if {@code newQuery} is included in {@code pendingQuery}
     */
    protected abstract boolean matches(Query pendingQuery, Query newQuery);

    /**
     * Submit {@code query} if it is not already in the queue.
     * <p>Note: this method should only be called from the <i>Event Dispatch Thread</i>.</p>
     * @see #matches(Object, Object)
     */
    protected void submitIfNotPending(Query query) {
        if (! isPending(query)) {
            Worker worker = new Worker(query);
            workers.add(worker);
            worker.execute();
        }
    }

    /**
     * @return the number of queries in the queue.
     */
    public int activeQueries() {
        return workers.size();
    }

    private boolean isPending(Query query) {
        for (Worker worker : workers) {
            if (matches(worker.query, query) && !worker.isDone()) {
                return true;
            }
        }
        return false;
    }

    private class Worker extends SwingWorker<Result, Object> {
        private final Query query;

        private Worker(Query query) {
            this.query = query;
        }

        @Override
        protected Result doInBackground() throws Exception {
            return getData(query);
        }

        @Override
        protected void done() {
            if (!isCancelled()) {
                super.done();
                try {
                    setResult(get());
                    workers.remove(this);
                    fireStateChanged();
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "failed to get table data", ex);
                }
            }
        }
    }
}
