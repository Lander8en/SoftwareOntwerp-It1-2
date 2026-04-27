package ui.controllers;

import domain.Column;
import domain.ColumnType;
import domain.Row;
import domain.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Controller Test with main focus on:
 * - Behavior verification
 * - Input validation
 * - Prevention of "pass-through" bugs
 */

public class RowsControllerTest {

    private Table mockTable;
    private RowsController controller;

    @BeforeEach
    public void setUp() {
        mockTable = mock(Table.class);
        controller = new RowsController(mockTable);
    }

    @Test
    public void testRowsRequestReturnsRowsFromTable() {
        List<Row> mockRows = Arrays.asList(mock(Row.class), mock(Row.class));
        when(mockTable.getRows()).thenReturn(mockRows);

        List<Row> result = controller.rowsRequest();

        assertEquals(mockRows, result);
        verify(mockTable).getRows();
    }

    @Test
    public void testColumnsRequestReturnsColumnsFromTable() {
        List<Column> mockColumns = Arrays.asList(mock(Column.class), mock(Column.class));
        when(mockTable.getColumns()).thenReturn(mockColumns);

        List<Column> result = controller.columnsRequest();

        assertEquals(mockColumns, result);
        verify(mockTable).getColumns();
    }

    @Test
    public void testHandleCreateNewRowRequestCallsTable() {
        controller.handleCreateNewRowRequest();
        verify(mockTable).createNewRow();
    }

    @Test
    public void testTypeRequestReturnsCorrectColumnType() {
        when(mockTable.getType(0)).thenReturn(ColumnType.STRING);

        ColumnType result = controller.typeRequest(0);

        assertEquals(ColumnType.STRING, result);
        verify(mockTable).getType(0);
    }

    @Test
    public void testColumnAllowsBlanksReturnsCorrectValue() {
        when(mockTable.columnAllowsBlanks(1)).thenReturn(true);

        boolean result = controller.columnAllowsBlanks(1);

        assertTrue(result);
        verify(mockTable).columnAllowsBlanks(1);
    }

    @Test
    public void testHandleDeleteRowRequestCallsDeleteRow() {
        Row row = mock(Row.class);
        controller.handleDeleteRowRequest(row);

        verify(mockTable).deleteRow(row);
    }

    @Test
    public void testGetTableReturnsInjectedTable() {
        assertEquals(mockTable, controller.getTable());
    }

    @Test
    void testToggleValueSetsValueCorrectly() {
        Row mockRow = mock(Row.class);
        Column mockColumn = mock(Column.class);

        when(mockTable.getColumns()).thenReturn(Arrays.asList(mockColumn,
                mockColumn));

        controller.toggleValue(mockRow, 1, "true");

        verify(mockRow).setValue(1, "true");
    }

    @Test
    void testToggleValueThrowsIfRowIsNull() {
        assertThrows(NullPointerException.class, () -> controller.toggleValue(null,
                0, "true"));
    }

    @Test
    void testToggleValueThrowsIfValueIsNull() {
        // Use a real Row with 1 column
        Row realRow = new Row(1, List.of("true"));
        Column mockColumn = mock(Column.class);
        when(mockTable.getColumns()).thenReturn(List.of(mockColumn));

        assertThrows(NullPointerException.class, () -> controller.toggleValue(realRow, 0, null));
    }

    @Test
    void testToggleValueThrowsForInvalidColumnIndex() {
        Row mockRow = mock(Row.class);
        when(mockTable.getColumns()).thenReturn(List.of()); // empty list

        assertThrows(IndexOutOfBoundsException.class, () -> controller.toggleValue(mockRow, 0, "true"));
    }

    @Test
    void testHandleDeleteRowRequestThrowsIfRowIsNull() {
        assertThrows(NullPointerException.class, () -> controller.handleDeleteRowRequest(null));
    }

    @Test
    void testConstructorThrowsIfTableIsNull() {
        assertThrows(NullPointerException.class, () -> new RowsController(null));
    }

}
