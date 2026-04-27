package domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ColumnTypeValidatorTest {

    @Test
    void validStringColumn_allValuesAccepted() {
        Column column = new Column("Name");
        column.setType(ColumnType.STRING);
        column.setDefaultValue("John");

        Table table = new Table("People");
        table.getColumnRepository().createNewColumn(); // Creates one column
        table.getColumns().set(0, column);
        table.createNewRow();
        table.getRows().get(0).setValue(0, "Doe");

        assertTrue(ColumnTypeValidator.isColumnTypeValid(column, table));
    }

    @Test
    void validEmailColumn_withProperEmails() {
        Column column = new Column("Email");
        column.setType(ColumnType.EMAIL);
        column.setDefaultValue("test@example.com");

        Table table = new Table("People");
        table.getColumnRepository().createNewColumn(); // Adds a column
        table.getColumns().set(0, column);
        table.createNewRow();
        table.getRows().get(0).setValue(0, "another@example.com");

        assertTrue(ColumnTypeValidator.isColumnTypeValid(column, table));
    }

    @Test
    void invalidEmailColumn_withMissingAtSymbol() {
        Column column = new Column("Email");
        column.setType(ColumnType.EMAIL);
        column.setDefaultValue("test@example.com");

        Table table = new Table("People");
        table.getColumnRepository().createNewColumn();
        table.getColumns().set(0, column);
        table.createNewRow();
        table.getRows().get(0).setValue(0, "invalidEmail.com");

        assertFalse(ColumnTypeValidator.isColumnTypeValid(column, table));
    }

    @Test
    void validBooleanColumn_withTrueFalse() {
        Column column = new Column("Active");
        column.setType(ColumnType.BOOLEAN);
        column.setDefaultValue("true");

        Table table = new Table("Flags");
        table.getColumnRepository().createNewColumn();
        table.getColumns().set(0, column);
        table.createNewRow();
        table.getRows().get(0).setValue(0, "false");

        assertTrue(ColumnTypeValidator.isColumnTypeValid(column, table));
    }

    @Test
    void invalidBooleanColumn_withRandomValue() {
        Column column = new Column("Active");
        column.setType(ColumnType.BOOLEAN);
        column.setDefaultValue("true");

        Table table = new Table("Flags");
        table.getColumnRepository().createNewColumn();
        table.getColumns().set(0, column);
        table.createNewRow();
        table.getRows().get(0).setValue(0, "yes");

        assertFalse(ColumnTypeValidator.isColumnTypeValid(column, table));
    }

    @Test
    void validIntegerColumn_withValidNumbers() {
        Column column = new Column("Age");
        column.setType(ColumnType.INTEGER);
        column.setDefaultValue("30");

        Table table = new Table("Data");
        table.getColumnRepository().createNewColumn();
        table.getColumns().set(0, column);
        table.createNewRow();
        table.getRows().get(0).setValue(0, "-42");

        assertTrue(ColumnTypeValidator.isColumnTypeValid(column, table));
    }

    @Test
    void invalidIntegerColumn_withLetters() {
        Column column = new Column("Age");
        column.setType(ColumnType.INTEGER);
        column.setDefaultValue("30");

        Table table = new Table("Data");
        table.getColumnRepository().createNewColumn();
        table.getColumns().set(0, column);
        table.createNewRow();
        table.getRows().get(0).setValue(0, "abc");

        assertFalse(ColumnTypeValidator.isColumnTypeValid(column, table));
    }

    @Test
    void blankValueAllowed_returnsTrue() {
        Column column = new Column("Optional");
        column.setType(ColumnType.STRING);
        column.setDefaultValue("");
        column.setBlanksAllowed(true);

        Table table = new Table("Data");
        table.getColumnRepository().createNewColumn();
        table.getColumns().set(0, column);
        table.createNewRow();
        table.getRows().get(0).setValue(0, "   ");

        assertTrue(ColumnTypeValidator.isColumnTypeValid(column, table));
    }

    @Test
    void blankValueDisallowed_returnsFalse() {
        Column column = new Column("Required");
        column.setType(ColumnType.STRING);
        column.setDefaultValue("");
        column.setBlanksAllowed(false);

        Table table = new Table("Data");
        table.getColumnRepository().createNewColumn();
        table.getColumns().set(0, column);
        table.createNewRow();
        table.getRows().get(0).setValue(0, " ");

        assertFalse(ColumnTypeValidator.isColumnTypeValid(column, table));
    }

    @Test
    void columnNotInTable_returnsFalse() {
        Column column = new Column("Ghost");
        Table table = new Table("Haunted");
        assertFalse(ColumnTypeValidator.isColumnTypeValid(column, table));
    }

    @Test
    void nullColumn_throwsException() {
        Table table = new Table("Test");
        assertThrows(NullPointerException.class, () -> ColumnTypeValidator.isColumnTypeValid(null, table));
    }

    @Test
    void nullTable_throwsException() {
        Column column = new Column("X");
        assertThrows(NullPointerException.class, () -> ColumnTypeValidator.isColumnTypeValid(column, null));
    }

    @Test
    void nullRowValue_returnsFalse() throws Exception {
        Column column = new Column("NullableTest");
        column.setType(ColumnType.STRING);
        column.setDefaultValue("NonNull");
        column.setBlanksAllowed(true);

        Table table = new Table("TestTable");
        table.getColumnRepository().createNewColumn();
        table.getColumns().set(0, column);
        table.createNewRow();

        Row row = table.getRows().get(0);

        // Use reflection to insert a null value into the row's internal values list
        var field = Row.class.getDeclaredField("values");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.List<String> values = (java.util.List<String>) field.get(row);
        values.set(0, null); // inject null directly

        assertFalse(ColumnTypeValidator.isColumnTypeValid(column, table));
    }

}
