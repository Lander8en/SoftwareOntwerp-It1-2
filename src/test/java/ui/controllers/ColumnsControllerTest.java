package ui.controllers;

import domain.Column;
import domain.ColumnType;
import domain.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class ColumnsControllerTest {

    private Table mockTable;
    private ColumnsController controller;

    @BeforeEach
    public void setUp() {
        mockTable = mock(Table.class);
        controller = new ColumnsController(mockTable);
    }

    @Test
    public void testHandleCreateNewColumnRequestCallsTable() {
        controller.handleCreateNewColumnRequest();
        verify(mockTable).createNewColumn();
    }

    @Test
    public void testColumnsRequestReturnsFromTable() {
        List<Column> mockColumns = Arrays.asList(mock(Column.class), mock(Column.class));
        when(mockTable.getColumns()).thenReturn(mockColumns);

        List<Column> result = controller.columnsRequest();

        assertEquals(mockColumns, result);
        verify(mockTable).getColumns();
    }

    @Test
    public void testRenameCallsTableRenameColumn() {
        Column column = mock(Column.class);
        controller.rename(column, "NewName");

        verify(mockTable).renameColumn(column, "NewName");
    }

    @Test
    public void testHandleDeleteColumnRequestCallsTableRemoveColumn() {
        Column column = mock(Column.class);
        controller.handleDeleteColumnRequest(column);

        verify(mockTable).removeColumn(column);
    }

    @Test
    public void testSetColumnTypeDelegatesToColumnRepositoryFromTable() {
        Column column = mock(Column.class);
        ColumnType type = ColumnType.INTEGER;

        var mockRepo = mock(domain.ColumnRepository.class);
        when(mockTable.getColumnRepository()).thenReturn(mockRepo);

        controller.setColumnType(column, type);

        verify(mockRepo).setType(column, type);
    }

    @Test
    public void testToggleBlanksAllowedTrueToFalse() {
        Column column = mock(Column.class);
        when(column.isBlanksAllowed()).thenReturn(true);

        controller.toggleBlanksAllowed(column);

        verify(column).setBlanksAllowed(false);
    }

    @Test
    public void testToggleBlanksAllowedFalseToTrue() {
        Column column = mock(Column.class);
        when(column.isBlanksAllowed()).thenReturn(false);

        controller.toggleBlanksAllowed(column);

        verify(column).setBlanksAllowed(true);
    }

    @Test
    public void constructorThrowsIfTableIsNull() {
        assertThrows(NullPointerException.class, () -> new ColumnsController(null));
    }

    @Test
    public void renameThrowsIfColumnIsNull() {
        assertThrows(NullPointerException.class, () -> controller.rename(null, "name"));
    }

    @Test
    public void renameThrowsIfNameIsNull() {
        Column col = mock(Column.class);
        assertThrows(NullPointerException.class, () -> controller.rename(col, null));
    }

    @Test
    public void handleDeleteColumnRequestThrowsIfColumnIsNull() {
        assertThrows(NullPointerException.class, () -> controller.handleDeleteColumnRequest(null));
    }

    @Test
    public void setColumnTypeThrowsIfColumnIsNull() {
        assertThrows(NullPointerException.class, () -> controller.setColumnType(null, ColumnType.STRING));
    }

    @Test
    public void setColumnTypeThrowsIfTypeIsNull() {
        Column col = mock(Column.class);
        assertThrows(NullPointerException.class, () -> controller.setColumnType(col, null));
    }

    @Test
    public void toggleBlanksAllowedThrowsIfColumnIsNull() {
        assertThrows(NullPointerException.class, () -> controller.toggleBlanksAllowed(null));
    }

    @Test
    public void testGetTableReturnsInjectedTable() {
        assertEquals(mockTable, controller.getTable());
    }

}
