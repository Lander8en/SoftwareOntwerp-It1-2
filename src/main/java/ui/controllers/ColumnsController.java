package ui.controllers;

import java.util.List;
import java.util.Objects;

import domain.ColumnType;
import domain.Table;
import domain.Column;

/**
 * Controller for managing column-related operations within a single table.
 * Provides methods to create, rename, delete, and modify columns.
 * Follows a GRASP Controller responsibility, delegating work to the Table domain model.
 */
public class ColumnsController {

    private final Table table;

    /**
     * Creates a new ColumnsController for a given table.
     *
     * @param table the Table to control; must not be null
     * @throws NullPointerException if the table is null
     */
    public ColumnsController(Table table) {
        this.table = Objects.requireNonNull(table, "table must not be null");
    }

    /**
     * Requests the table to create a new column.
     */
    public void handleCreateNewColumnRequest() {
        table.createNewColumn();
    }

    /**
     * Returns the list of columns from the table.
     *
     * @return a list of Column objects; never null
     */
    public List<Column> columnsRequest() {
        return table.getColumns();
    }

    /**
     * Renames the given column.
     *
     * @param column the column to rename; must not be null
     * @param name the new name to assign; must not be null
     * @throws NullPointerException if column or name is null
     */
    public void rename(Column column, String name) {
        table.renameColumn(
            Objects.requireNonNull(column, "column must not be null"),
            Objects.requireNonNull(name, "name must not be null")
        );
    }

    /**
     * Requests the removal of a column from the table.
     *
     * @param column the column to remove; must not be null
     * @throws NullPointerException if column is null
     */
    public void handleDeleteColumnRequest(Column column) {
        table.removeColumn(Objects.requireNonNull(column, "column must not be null"));
    }

    /**
     * Sets the type of the given column.
     *
     * @param type the new type to set; must not be null
     * @throws NullPointerException if column or type is null
     */
    public void setColumnType(Column column, ColumnType type) {
        table.getColumnRepository().setType(
            Objects.requireNonNull(column, "column must not be null"),
            Objects.requireNonNull(type, "type must not be null")
        );
    }

    /**
     * Toggles whether blanks are allowed for the given column.
     *
     * @param column the column to modify; must not be null
     * @throws NullPointerException if column is null
     */
    public void toggleBlanksAllowed(Column column) {
        Column c = Objects.requireNonNull(column, "column must not be null");
        c.setBlanksAllowed(!c.isBlanksAllowed());
    }

    public Table getTable() {
        return table;
    }

    /**
     * Sets the defaultvalue of the given column.
     *
     * @param value the new type to set; must not be null
     * @throws NullPointerException if column or type is null
     */
    public void toggleDefaultValue(String value, Column column) {
        column.setDefaultValue(
            Objects.requireNonNull(value, "value must not be null")
        );
    }
}