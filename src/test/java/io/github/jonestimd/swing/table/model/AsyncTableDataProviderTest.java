package io.github.jonestimd.swing.table.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.SwingUtilities;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AsyncTableDataProviderTest {
    private static final Runnable NO_OP = new Runnable() {
        @Override
        public void run() {
        }
    };
    private final Runnable submit = new Runnable() {
        @Override
        public void run() {
            dataProvider.submitIfNotPending(expectedQuery);
        }
    };
    @Mock
    private PropertyChangeListener stateChangeListener;
    private TestTableDataProvider dataProvider;
    private Collection<String> expectedQuery;
    private List<String> expectedResult = new ArrayList<String>();
    private List<String> queryResult;
    private volatile boolean queryDelay = false;

    @Before
    public void createProvider() {
        dataProvider = new TestTableDataProvider();
        dataProvider.addStateChangeListener(stateChangeListener);
    }

    @Test
    public void submitWhenNotPending() throws Exception {
        expectedQuery = Arrays.asList("query");

        SwingUtilities.invokeAndWait(submit);
        waitForQuery();

        assertThat(queryResult).isSameAs(expectedResult);
        verify(stateChangeListener, times(1)).propertyChange(any(PropertyChangeEvent.class));
    }

    @Test
    public void submitWhenPending() throws Exception {
        expectedQuery = Arrays.asList("query");
        queryDelay = true;

        SwingUtilities.invokeAndWait(submit);
        SwingUtilities.invokeAndWait(submit);
        queryDelay = false;
        waitForQuery();

        assertThat(queryResult).isSameAs(expectedResult);
        verify(stateChangeListener, times(1)).propertyChange(any(PropertyChangeEvent.class));
    }

    private void waitForQuery() throws InterruptedException, InvocationTargetException {
        while (dataProvider.activeQueries() > 0) {
            Thread.yield();
        }
        SwingUtilities.invokeAndWait(NO_OP);
    }

    private class TestTableDataProvider extends AsyncTableDataProvider<String, Collection<String>, List<String>> {
        @Override
        protected List<String> getData(Collection<String> query) throws Exception {
            assertThat(query).isSameAs(expectedQuery);
            while (queryDelay) {
                Thread.yield();
            }
            return expectedResult;
        }

        @Override
        protected void setResult(List<String> result) {
            queryResult = result;
        }

        @Override
        protected boolean matches(Collection<String> pendingQuery, Collection<String> newQuery) {
            return true;
        }

        @Override
        public List<? extends ColumnAdapter<String, ?>> getColumnAdapters() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setBeans(Collection<String> strings) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addBean(String s) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean updateBean(String s, String columnId, Object oldValue) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void removeBean(String s) {
            throw new UnsupportedOperationException();
        }
    }
}
