package ui.controllers;

import domain.Table;
import domain.TableRepository;
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

public class TablesControllerTest {

    private TableRepository mockTableRepository;
    private TablesController controller;

    @BeforeEach
    public void setUp() {
        mockTableRepository = mock(TableRepository.class);
        controller = new TablesController(mockTableRepository);
    }

    @Test
    public void testHandleCreateNewTableRequestCallsRepository() {
        controller.handleCreateNewTableRequest();
        verify(mockTableRepository, times(1)).createNewTable();
    }

    @Test
    public void testTablesRequestReturnsTablesFromRepository() {
        List<Table> mockTables = Arrays.asList(mock(Table.class), mock(Table.class));
        when(mockTableRepository.getTables()).thenReturn(mockTables);

        List<Table> result = controller.tablesRequest();

        assertEquals(mockTables, result);
        verify(mockTableRepository).getTables();
    }

    @Test
    public void testRenameCallsRepositoryUpdateName() {
        Table table = mock(Table.class);
        controller.rename(table, "NewName");

        verify(mockTableRepository).updateTableName(table, "NewName");
    }

    @Test
    public void testIsTableNameValidDelegatesToRepository() {
        Table table = mock(Table.class);
        when(mockTableRepository.isTableNameValid("SomeName", table)).thenReturn(true);

        boolean result = controller.isTableNameValid("SomeName", table);

        assertTrue(result);
        verify(mockTableRepository).isTableNameValid("SomeName", table);
    }

    @Test
    public void testHandleDeleteTableRequestCallsRemoveOnRepo() {
        Table table = mock(Table.class);
        controller.handleDeleteTableRequest(table);

        verify(mockTableRepository).remove(table);
    }
}
