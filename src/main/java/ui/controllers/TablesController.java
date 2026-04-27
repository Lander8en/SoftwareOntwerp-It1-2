package ui.controllers;

import java.util.List;
import java.util.Objects;
import domain.Table;
import domain.TableRepository;

/**
 * Controller for managing operations on tables through the TableRepository.
 * Provides methods to create, retrieve, rename, validate, and delete tables.
 */
public class TablesController {

    private final TableRepository tableRepository;

    /**
     * Constructs a TablesController using the given repository.
     *
     * @param tableRepository the repository to use; must not be null
     * @throws NullPointerException if tableRepository is null
     */
    public TablesController(TableRepository tableRepository) {
        this.tableRepository = Objects.requireNonNull(tableRepository, "tableRepository must not be null");
    }

    /**
     * Requests the creation of a new table in the repository.
     */
    public void handleCreateNewTableRequest() {
        tableRepository.createNewTable();
    }

    /**
     * Returns all tables managed by the repository.
     *
     * @return a list of Table objects; never null
     */
    public List<Table> tablesRequest() {
        return tableRepository.getTables();
    }

    /**
     * Renames the specified table.
     *
     * @param table the table to rename; must not be null
     * @param newName the new name to assign; must not be null
     * @throws NullPointerException if table or newName is null
     */
    public void rename(Table table, String newName) {
        tableRepository.updateTableName(
            Objects.requireNonNull(table, "table must not be null"),
            Objects.requireNonNull(newName, "newName must not be null")
        );
    }

    /**
     * Checks whether a given table name is valid.
     *
     * @param name the name to validate; must not be null
     * @param excludeTable a table to exclude from the name check (e.g., the table being renamed); may be null
     * @return true if the name is valid; false otherwise
     * @throws NullPointerException if name is null
     */
    public boolean isTableNameValid(String name, Table excludeTable) {
        return tableRepository.isTableNameValid(
            Objects.requireNonNull(name, "name must not be null"), 
            excludeTable
        );
    }

    /**
     * Removes the specified table from the repository.
     *
     * @param table the table to remove; must not be null
     * @throws NullPointerException if table is null
     */
    public void handleDeleteTableRequest(Table table) {
        tableRepository.remove(Objects.requireNonNull(table, "table must not be null"));
    }
}