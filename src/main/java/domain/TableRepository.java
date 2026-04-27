package domain;

import java.util.ArrayList;
import java.util.List;

import domain.naming.NameGenerator;
import domain.naming.SequentialNamingStrategy;

/**
 * Repository for managing multiple tables.
 * Provides functionality to create, retrieve, rename, and remove tables.
 */
public class TableRepository {

    private final List<Table> tables = new ArrayList<>();
    private final NameGenerator nameGenerator = new NameGenerator(new SequentialNamingStrategy());

    /**
     * Creates a new table with a unique name and adds it to the repository.
     */
    public void createNewTable() {
        String name = nameGenerator.generateUniqueName("Table", tables);
        tables.add(new Table(name));
    }

    /**
     * Returns the list of tables in this repository.
     *
     * @return the list of tables
     */
    public List<Table> getTables() {
        return this.tables;
    }

    /**
     * Updates the name of the given table if the new name is valid.
     *
     * @param table    the table to rename, must not be null
     * @param newName  the new name to set, must not be null
     * @throws IllegalArgumentException if table or newName is null
     */
    public void updateTableName(Table table, String newName) {
        if (table == null || newName == null) {
            throw new IllegalArgumentException("Table and newName must not be null");
        }
        if (this.isTableNameValid(newName, table)) {
            table.setName(newName);
        }
    }

    /**
     * Removes the given table from the repository.
     *
     * @param table the table to remove, must not be null
     * @throws IllegalArgumentException if table is null
     */
    public void remove(Table table) {
        if (table == null) {
            throw new IllegalArgumentException("Table must not be null");
        }
        tables.remove(table);
    }

    /**
     * Checks whether the given name is valid and unique among all tables,
     * excluding the specified table.
     *
     * @param name          the name to check
     * @param excludeTable  a table to exclude from the uniqueness check (can be null)
     * @return true if the name is valid and unique, false otherwise
     */
    public boolean isTableNameValid(String name, Table excludeTable) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        return tables.stream()
            .filter(t -> t != excludeTable)
            .noneMatch(t -> t.getName().equals(name));
    }
}