package domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a table, containing a list of columns and rows.
 * Provides operations to manipulate rows and columns and query structure.
 */
public class Table {

    private String name;
    private final ColumnRepository columnRepository = new ColumnRepository();
    private final List<Row> rows = new ArrayList<>();

    /**
     * Constructs a new Table with the given name.
     *
     * @param name the name of the table, must not be null
     * @throws IllegalArgumentException if name is null
     */
    public Table(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Table name cannot be null");
        }
        this.name = name;
    }

    /**
     * Creates a new row using the current columns' default values and adds it to the table.
     */
    public void createNewRow() {
        List<Column> columns = getColumns();
        List<String> defaults = new ArrayList<>();
        for (Column col : columns) {
            defaults.add(col.getDefaultValue());
        }
        rows.add(new Row(columns.size(), defaults));
    }

    /**
     * Returns the name of the table.
     *
     * @return the table name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a copy of the table with the same name. The copy does not include rows or columns.
     *
     * @return a new Table instance with the same name
     */
    public Table copy() {
        return new Table(name);
    }

    /**
     * Sets the name of the table.
     *
     * @param newName the new name to assign, must not be null
     * @throws IllegalArgumentException if newName is null
     */
    public void setName(String newName) {
        if (newName == null) {
            throw new IllegalArgumentException("Table name cannot be null");
        }
        this.name = newName;
    }

    /**
     * Returns the column repository of this table.
     *
     * @return the column repository
     */
    public ColumnRepository getColumnRepository() {
        return columnRepository;
    }

    /**
     * Returns the list of columns in the table.
     *
     * @return the list of columns
     */
    public List<Column> getColumns() {
        return columnRepository.getColumns();
    }

    /**
     * Returns the list of rows in the table.
     *
     * @return the list of rows
     */
    public List<Row> getRows() {
        return rows;
    }

    /**
     * Returns the type of the column at the given index.
     *
     * @param colIndex the column index
     * @return the column type
     */
    public ColumnType getType(int colIndex) {
        return columnRepository.getType(colIndex);
    }

    /**
     * Returns whether blanks are allowed in the column at the given index.
     *
     * @param colIndex the column index
     * @return true if blanks are allowed; false otherwise
     */
    public boolean columnAllowsBlanks(int colIndex) {
        return columnRepository.allowsBlanks(colIndex);
    }

    /**
     * Deletes the specified row from the table.
     *
     * @param row the row to delete, must not be null
     * @throws IllegalArgumentException if row is null
     */
    public void deleteRow(Row row) {
        if (row == null) {
            throw new IllegalArgumentException("Row cannot be null");
        }
        rows.remove(row);
    }

    /**
     * Creates a new column and adds a default value to every existing row.
     */
    public void createNewColumn() {
        Column newColumn = columnRepository.createNewColumn();
        for (Row row : rows) {
            row.addValue(newColumn.getDefaultValue());
        }
    }

    /**
     * Renames the given column.
     *
     * @param column the column to rename, must not be null
     * @param name the new name to assign, must not be null
     * @throws IllegalArgumentException if either parameter is null
     */
    public void renameColumn(Column column, String name) {
        if (column == null || name == null) {
            throw new IllegalArgumentException("Column and name must not be null");
        }
        columnRepository.rename(column, name);
    }

    /**
     * Removes the specified column from the table.
     *
     * @param column the column to remove, must not be null
     * @throws IllegalArgumentException if column is null
     */
    public void removeColumn(Column column) {
        if (column == null) {
            throw new IllegalArgumentException("Column must not be null");
        }
        columnRepository.remove(column);
    }
}