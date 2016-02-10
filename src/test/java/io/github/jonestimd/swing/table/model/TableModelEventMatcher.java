package io.github.jonestimd.swing.table.model;

import javax.swing.event.TableModelEvent;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.mockito.Mockito;

public class TableModelEventMatcher extends BaseMatcher<TableModelEvent> {
    private final int type;
    private final int firstRow;
    private final int lastRow;
    private final int column;

    public TableModelEventMatcher(int type, int firstRow, int lastRow, int column) {
        this.type = type;
        this.firstRow = firstRow;
        this.lastRow = lastRow;
        this.column = column;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(String.format("type=%d, firstRow=%d, lastRow=%d, column=%d", type, firstRow, lastRow, column));
    }

    @Override
    public boolean matches(Object arg0) {
        TableModelEvent event = (TableModelEvent) arg0;
        return event.getType() == type && event.getFirstRow() == firstRow
            && event.getLastRow() == lastRow && event.getColumn() == column;
    }

    public static TableModelEvent tableModelEvent(int type, int firstRow, int lastRow, int column) {
        return Mockito.argThat(new TableModelEventMatcher(type, firstRow, lastRow, column));
    }
}