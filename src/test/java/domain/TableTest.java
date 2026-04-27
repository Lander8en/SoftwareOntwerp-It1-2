package domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TableTest {

    private Table table;

    @BeforeEach
    public void setUp() {
        table = new Table("TestTable");
    }

    @Test
    public void testConstructorSetsName() {
        assertEquals("TestTable", table.getName());
        assertTrue(table.getRows().isEmpty());
        assertTrue(table.getColumns().isEmpty());
    }

    @Test
    public void testConstructorWithNullNameThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new Table(null));
        assertEquals("Table name cannot be null", exception.getMessage());
    }

    @Test
    public void testSetName() {
        table.setName("RenamedTable");
        assertEquals("RenamedTable", table.getName());
    }

    @Test
    public void testSetNameWithNullThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            table.setName(null);
        });
        assertEquals("Table name cannot be null", exception.getMessage());
    }

    @Test
    public void testCopyCreatesNewTableWithSameName() {
        Table copy = table.copy();
        assertNotSame(table, copy);
        assertEquals(table.getName(), copy.getName());
        assertTrue(copy.getRows().isEmpty());
        assertTrue(copy.getColumns().isEmpty());
    }

    @Test
    public void testCreateNewRowAddsRowWithDefaults() {
        table.getColumnRepository().createNewColumn();
        Column column = table.getColumns().get(0);
        column.setDefaultValue("default");

        table.createNewRow();

        List<Row> rows = table.getRows();
        assertEquals(1, rows.size());
        Row row = rows.get(0);
        assertEquals("default", row.getValue(0));
    }

    @Test
    public void testDeleteRow() {
        table.getColumnRepository().createNewColumn();
        table.createNewRow();

        Row row = table.getRows().get(0);
        table.deleteRow(row);

        assertTrue(table.getRows().isEmpty());
    }

    @Test
    public void testDeleteRowWithNullThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            table.deleteRow(null);
        });
        assertEquals("Row cannot be null", exception.getMessage());
    }

    @Test
    public void testGetTypeDelegatesToColumnRepository() {
        table.getColumnRepository().createNewColumn();
        Column col = table.getColumns().get(0);
        col.setType(ColumnType.INTEGER);

        assertEquals(ColumnType.INTEGER, table.getType(0));
    }

    @Test
    public void testColumnAllowsBlanksDelegatesToColumnRepository() {
        table.getColumnRepository().createNewColumn();
        Column col = table.getColumns().get(0);
        col.setBlanksAllowed(false);

        assertFalse(table.columnAllowsBlanks(0));
    }

    @Test
    public void testRenameColumnWithValidInput() {
        Column column = table.getColumnRepository().createNewColumn();
        table.renameColumn(column, "NewColumnName");
        assertEquals("NewColumnName", column.getName());
    }

    @Test
    public void testRenameColumnWithNullColumnThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            table.renameColumn(null, "SomeName");
        });
        assertEquals("Column and name must not be null", exception.getMessage());
    }

    @Test
    public void testRenameColumnWithNullNameThrowsException() {
        Column column = table.getColumnRepository().createNewColumn();
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            table.renameColumn(column, null);
        });
        assertEquals("Column and name must not be null", exception.getMessage());
    }

    @Test
    public void testRemoveColumn() {
        Column column = table.getColumnRepository().createNewColumn();
        assertTrue(table.getColumns().contains(column));

        table.removeColumn(column);
        assertFalse(table.getColumns().contains(column));
    }

    @Test
    public void testRemoveColumnWithNullThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            table.removeColumn(null);
        });
        assertEquals("Column must not be null", exception.getMessage());
    }

    @Test
    public void testCreateNewColumnAddsColumnAndUpdatesRows() {
        // Arrange: create one initial column with default value
        Column col1 = table.getColumnRepository().createNewColumn();
        col1.setDefaultValue("default-1");

        table.createNewRow(); // Adds a row with 1 value

        // Add second column via createNewColumn() — default is "" by default
        table.createNewColumn();

        // Assert: table should now have 2 columns
        assertEquals(2, table.getColumns().size());

        // Row should now have 2 values: "default-1" and "" (default of new column)
        Row row = table.getRows().get(0);
        assertEquals("default-1", row.getValue(0));
        assertEquals("", row.getValue(1)); // New column's default value
    }

}
