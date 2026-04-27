package ui.editors;

import domain.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ui.controllers.TablesController;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class TableNameAccessTest {

    private TablesController mockController;
    private TableNameAccess nameAccess;

    @BeforeEach
    public void setUp() {
        mockController = mock(TablesController.class);
        nameAccess = new TableNameAccess(mockController);
    }

    @Test
    public void testGetNameReturnsTableName() {
        Table table = mock(Table.class);
        when(table.getName()).thenReturn("TestTable");

        String result = nameAccess.getName(table);

        assertEquals("TestTable", result);
        verify(table).getName();
    }

    @Test
    public void testSetNameCallsControllerRename() {
        Table table = mock(Table.class);

        nameAccess.setName(table, "RenamedTable");

        verify(mockController).rename(table, "RenamedTable");
    }

    @Test
    public void testGetAllReturnsTablesFromController() {
        List<Table> mockTables = Arrays.asList(mock(Table.class), mock(Table.class));
        when(mockController.tablesRequest()).thenReturn(mockTables);

        List<Table> result = nameAccess.getAll();

        assertEquals(mockTables, result);
        verify(mockController).tablesRequest();
    }
}
