package ui.editors;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import domain.Column;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ui.controllers.ColumnsController;

import java.util.Arrays;
import java.util.List;

public class ColumnNameAccessTest {

    private ColumnsController mockController;
    private ColumnNameAccess columnNameAccess;

    @BeforeEach
    public void setUp() {
        mockController = mock(ColumnsController.class);
        columnNameAccess = new ColumnNameAccess(mockController);
    }

    @Test
    public void testGetNameReturnsColumnName() {
        // Arrange
        Column mockColumn = mock(Column.class);
        when(mockColumn.getName()).thenReturn("TestColumn");

        // Act
        String result = columnNameAccess.getName(mockColumn);

        // Assert
        assertEquals("TestColumn", result);
        verify(mockColumn).getName();
    }

    @Test
    public void testSetNameCallsControllerRename() {
        // Arrange
        Column mockColumn = mock(Column.class);
        String newName = "RenamedColumn";

        // Act
        columnNameAccess.setName(mockColumn, newName);

        // Assert
        verify(mockController).rename(mockColumn, newName);
    }

    @Test
    public void testGetAllReturnsColumnsFromController() {
        // Arrange
        List<Column> mockColumns = Arrays.asList(
                mock(Column.class),
                mock(Column.class));
        when(mockController.columnsRequest()).thenReturn(mockColumns);

        // Act
        List<Column> result = columnNameAccess.getAll();

        // Assert
        assertEquals(mockColumns, result);
        verify(mockController).columnsRequest();
    }
}