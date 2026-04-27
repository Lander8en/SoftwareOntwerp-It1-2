package domain;

/**
 * Utility class to validate if a column's current type is compatible
 * with all existing row values and the default value.
 */
public class ColumnTypeValidator {

    /**
     * Checks if all values in a given column are valid for the column's current type.
     * Includes both default value and all existing row values.
     *
     * @param column the column to validate
     * @param table the table containing rows for the column
     * @return true if all values are valid for the column's type; false otherwise
     * @throws NullPointerException if column or table is null
     */
    public static boolean isColumnTypeValid(Column column, Table table) {
        if (column == null || table == null) {
            throw new NullPointerException("Column and Table must not be null");
        }

        ColumnType type = column.getType();
        int colIndex = table.getColumns().indexOf(column);
        if (colIndex == -1) return false;

        // Check default value
        if (!isValidValue(type, column.getDefaultValue(), column.isBlanksAllowed())) {
            return false;
        }

        // Check all row values
        for (Row row : table.getRows()) {
            String value = row.getValue(colIndex);
            if (!isValidValue(type, value, column.isBlanksAllowed())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if a single value is valid for a given type and blank policy.
     */
    private static boolean isValidValue(ColumnType type, String value, boolean allowBlank) {
        if (value == null) return false;
        if (value.isBlank()) return allowBlank;

        return switch (type) {
            case STRING -> true;
            case EMAIL -> value.contains("@");
            case BOOLEAN -> value.equals("true") || value.equals("false");
            case INTEGER -> value.matches("-?\\d+");
        };
    }
}
