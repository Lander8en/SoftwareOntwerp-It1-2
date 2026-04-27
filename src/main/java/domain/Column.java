package domain;

/**
 * Represents a column in a table, including its name, type,
 * blank-value policy, and default value.
 */
public class Column {

    private String name;
    private ColumnType type;
    private boolean blanksAllowed = true;
    private String defaultValue = "";

    /**
     * Constructs a new Column with the given name.
     * The initial type is {@link ColumnType#STRING}.
     *
     * @param name the name of the column, must not be null
     * @throws IllegalArgumentException if the name is null
     */
    public Column(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Column name cannot be null");
        }
        this.name = name;
        this.type = ColumnType.STRING;
    }

    /**
     * Returns the name of this column.
     *
     * @return the column name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this column.
     *
     * @param newName the new name to set, must not be null
     * @throws IllegalArgumentException if newName is null
     */
    public void setName(String newName) {
        if (newName == null) {
            throw new IllegalArgumentException("Column name cannot be null");
        }
        this.name = newName;
    }

    /**
     * Returns the type of this column.
     *
     * @return the column type
     */
    public ColumnType getType() {
        return type;
    }

    /**
     * Sets the type of this column.
     *
     * @param type the new column type, must not be null
     * @throws IllegalArgumentException if type is null
     */
    public void setType(ColumnType type) {
        if (type == null) {
            throw new IllegalArgumentException("Column type cannot be null");
        }
        this.type = type;
    }

    /**
     * Returns whether blank values are allowed in this column.
     *
     * @return true if blanks are allowed; false otherwise
     */
    public boolean isBlanksAllowed() {
        return blanksAllowed;
    }

    /**
     * Sets whether blank values are allowed in this column.
     *
     * @param allowBlanks true to allow blanks; false to disallow
     */
    public void setBlanksAllowed(boolean allowBlanks) {
        this.blanksAllowed = allowBlanks;
    }

    /**
     * Returns the default value for this column.
     *
     * @return the default value
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Sets the default value for this column.
     *
     * @param defaultValue the default value to set, must not be null
     * @throws IllegalArgumentException if defaultValue is null
     */
    public void setDefaultValue(String defaultValue) {
        if (defaultValue == null) {
            throw new IllegalArgumentException("Default value cannot be null");
        }
        this.defaultValue = defaultValue;
    }
}