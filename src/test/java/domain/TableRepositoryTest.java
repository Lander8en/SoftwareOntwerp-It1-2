package domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TableRepositoryTest {

    private TableRepository repository;

    @BeforeEach
    public void setUp() {
        repository = new TableRepository();
    }

    @Test
    public void testCreateNewTableAddsTableWithUniqueName() {
        repository.createNewTable();
        repository.createNewTable();

        List<Table> tables = repository.getTables();
        assertEquals(2, tables.size());
        assertNotEquals(tables.get(0).getName(), tables.get(1).getName());
        assertTrue(tables.get(0).getName().startsWith("Table"));
        assertTrue(tables.get(1).getName().startsWith("Table"));
    }

    @Test
    public void testUpdateTableNameWithValidNameChangesName() {
        repository.createNewTable();
        Table table = repository.getTables().get(0);

        repository.updateTableName(table, "NewTableName");
        assertEquals("NewTableName", table.getName());
    }

    @Test
    public void testUpdateTableNameWithNullTableThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.updateTableName(null, "SomeName");
        });
        assertEquals("Table and newName must not be null", exception.getMessage());
    }

    @Test
    public void testUpdateTableNameWithNullNameThrowsException() {
        repository.createNewTable();
        Table table = repository.getTables().get(0);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.updateTableName(table, null);
        });
        assertEquals("Table and newName must not be null", exception.getMessage());
    }

    @Test
    public void testUpdateTableNameWithBlankOrDuplicateNameDoesNotChange() {
        repository.createNewTable();
        Table table = repository.getTables().get(0);
        String originalName = table.getName();

        // Blank name
        repository.updateTableName(table, "   ");
        assertEquals(originalName, table.getName());

        // Duplicate name
        repository.createNewTable();
        Table secondTable = repository.getTables().get(1);
        repository.updateTableName(secondTable, originalName); // duplicate name
        assertNotEquals(originalName, secondTable.getName()); // name should remain unchanged
    }

    @Test
    public void testIsTableNameValid() {
        repository.createNewTable();
        Table table = repository.getTables().get(0);

        assertFalse(repository.isTableNameValid(null, table));
        assertFalse(repository.isTableNameValid("", table));
        assertFalse(repository.isTableNameValid("   ", table));
        assertFalse(repository.isTableNameValid(table.getName(), null)); // name already exists

        assertTrue(repository.isTableNameValid("UniqueName", table));
    }

    @Test
    public void testRemoveTable() {
        repository.createNewTable();
        Table table = repository.getTables().get(0);

        repository.remove(table);
        assertFalse(repository.getTables().contains(table));
    }

    @Test
    public void testRemoveNullThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.remove(null);
        });
        assertEquals("Table must not be null", exception.getMessage());
    }
}
