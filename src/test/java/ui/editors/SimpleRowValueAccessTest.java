package ui.editors;

import domain.ColumnType;
import domain.Row;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ui.controllers.RowsController;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SimpleRowValueAccessTest {

    private RowsController mockController;
    private RowValueAccess access;
    private Row mockRow;

    @BeforeEach
    void setUp() {
        mockController = mock(RowsController.class);
        mockRow = mock(Row.class);
        access = new RowValueAccess(mockController);
    }

    @Test
    void getValue_ReturnsCorrectValueFromRow() {
        when(mockRow.getValue(2)).thenReturn("example");
        String result = access.getValue(mockRow, 2);
        assertEquals("example", result);
    }

    @Test
    void setValue_DelegatesToController() {
        access.setValue(mockRow, 1, "newValue");

        verify(mockController).toggleValue(mockRow, 1, "newValue");
    }

    @Test
    void getAllRows_ReturnsControllerRows() {
        List<Row> rows = Arrays.asList(mockRow);
        when(mockController.rowsRequest()).thenReturn(rows);

        List<Row> result = access.getAllRows();
        assertEquals(rows, result);
    }

    @Test
    void getType_ReturnsCorrectColumnType() {
        when(mockController.typeRequest(0)).thenReturn(ColumnType.EMAIL);
        ColumnType type = access.getType(0);
        assertEquals(ColumnType.EMAIL, type);
    }

    @Test
    void allowsBlanks_ReturnsTrueOrFalseBasedOnController() {
        when(mockController.columnAllowsBlanks(1)).thenReturn(true);
        assertTrue(access.allowsBlanks(1));

        when(mockController.columnAllowsBlanks(2)).thenReturn(false);
        assertFalse(access.allowsBlanks(2));
    }
}
