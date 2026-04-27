package ui.controllers;

import java.util.List;
import java.util.Objects;

import domain.Column;
import domain.ColumnType;
import domain.Row;
import domain.Table;

/**
 * Controller for managing row-related operations within a table.
 * Provides access to rows and columns, and delegates row operations to the Table domain model.
 */
public class RowsController {

    private final Table table;

    /**
     * Creates a new RowsController for the given table.
     *
     * @param table the Table to control; must not be null
     * @throws NullPointerException if table is null
     */
    public RowsController(Table table) {
        this.table = Objects.requireNonNull(table, "table must not be null");
    }

    /**
     * Returns the list of rows from the table.
     *
     * @return a list of Row objects; never null
     */
    public List<Row> rowsRequest() {
        return table.getRows();
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
     * Requests the creation of a new row in the table.
     */
    public void handleCreateNewRowRequest() {
        table.createNewRow();
    }

    /**
     * Returns the type of the column at the given index.
     *
     * @param colIndex the column index; must be valid
     * @return the type of the column at the given index
     */
    public ColumnType typeRequest(int colIndex) {
        return table.getType(colIndex);
    }

    /**
     * Returns whether the column at the given index allows blanks.
     *
     * @param colIndex the column index; must be valid
     * @return true if the column allows blank values, false otherwise
     */
    public boolean columnAllowsBlanks(int colIndex) {
        return table.columnAllowsBlanks(colIndex);
    }

    /**
     * Deletes the specified row from the table.
     *
     * @param row the Row to delete; must not be null
     * @throws NullPointerException if row is null
     */
    public void handleDeleteRowRequest(Row row) {
        table.deleteRow(Objects.requireNonNull(row, "row must not be null"));
    }

    public Table getTable() {
        return table;
    }

    /**
     * Toggles the value of a boolean column in the specified row.
     *
     * @param row          the Row to modify; must not be null
     * @param columnIndex  the index of the column to toggle; must be valid
     * @param value        the new value to set; must not be null
     * @throws NullPointerException      if row or value is null
     * @throws IndexOutOfBoundsException if columnIndex is out of range
     */
    public void toggleValue(Row row, int columnIndex, String value) {
        Objects.requireNonNull(row, "row must not be null");
        if (columnIndex < 0 || columnIndex >= table.getColumns().size()) {
            throw new IndexOutOfBoundsException("Invalid column index: " + columnIndex);
        }
        row.setValue(columnIndex, value);
    }
}