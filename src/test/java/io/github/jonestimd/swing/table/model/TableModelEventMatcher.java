package io.github.jonestimd.swing.table.model;

import javax.swing.event.TableModelEvent;

import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

public class TableModelEventMatcher implements ArgumentMatcher<TableModelEvent> {
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
    public boolean matches(TableModelEvent event) {
        return event.getType() == type && event.getFirstRow() == firstRow
            && event.getLastRow() == lastRow && event.getColumn() == column;
    }

    public static TableModelEvent tableModelEvent(int type, int firstRow, int lastRow, int column) {
        return Mockito.argThat(new TableModelEventMatcher(type, firstRow, lastRow, column));
    }
}