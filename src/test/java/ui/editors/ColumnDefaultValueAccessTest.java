package ui.editors;

import domain.Column;
import domain.ColumnType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ui.controllers.ColumnsController;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ColumnDefaultValueAccessTest {

    private ColumnsController mockController;
    private ColumnDefaultValueAccess valueAccess;
    private Column mockColumn;

    @BeforeEach
    void setUp() {
        mockController = mock(ColumnsController.class);
        valueAccess = new ColumnDefaultValueAccess(mockController);
        mockColumn = mock(Column.class);
    }

    @Test
    void getValue_ReturnsDefaultValue() {
        when(mockColumn.getDefaultValue()).thenReturn("default");

        String result = valueAccess.getValue(mockColumn);
        assertEquals("default", result);
    }

    @Test
    void setValue_DelegatesToController() {
        valueAccess.setValue(mockColumn, "newValue");

        verify(mockController).toggleDefaultValue("newValue", mockColumn);
    }

    @Test
    void setValue_NullValue_DelegatesToController() {
        valueAccess.setValue(mockColumn, null);

        verify(mockController).toggleDefaultValue(null, mockColumn);
    }

    @Test
    void getAllItems_ReturnsColumnsFromController() {
        List<Column> mockList = List.of(mock(Column.class), mock(Column.class));
        when(mockController.columnsRequest()).thenReturn(mockList);

        List<Column> result = valueAccess.getAllItems();
        assertEquals(mockList, result);
    }

    @Test
    void getAllItems_ReturnsEmptyList() {
        when(mockController.columnsRequest()).thenReturn(Collections.emptyList());

        List<Column> result = valueAccess.getAllItems();
        assertTrue(result.isEmpty());
    }

    @Test
    void getType_ReturnsColumnType() {
        when(mockColumn.getType()).thenReturn(ColumnType.EMAIL);

        ColumnType result = valueAccess.getType(mockColumn);
        assertEquals(ColumnType.EMAIL, result);
    }

    @Test
    void getType_NullType_ReturnsNull() {
        when(mockColumn.getType()).thenReturn(null);
        assertNull(valueAccess.getType(mockColumn));
    }

    @Test
    void allowsBlanks_ReturnsBlanksAllowedFlag() {
        when(mockColumn.isBlanksAllowed()).thenReturn(true);

        assertTrue(valueAccess.allowsBlanks(mockColumn));
    }

    @Test
    void allowsBlanks_ReturnsFalseIfNotAllowed() {
        when(mockColumn.isBlanksAllowed()).thenReturn(false);

        assertFalse(valueAccess.allowsBlanks(mockColumn));
    }
}
