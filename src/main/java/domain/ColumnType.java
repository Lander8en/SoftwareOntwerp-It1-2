package domain;

public enum ColumnType {
    STRING,
    EMAIL,
    BOOLEAN,
    INTEGER;

    public ColumnType next() {
        return switch (this) {
            case STRING -> EMAIL;
            case EMAIL -> BOOLEAN;
            case BOOLEAN -> INTEGER;
            case INTEGER -> STRING;
        };
    }
}