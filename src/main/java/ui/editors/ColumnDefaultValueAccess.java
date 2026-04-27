package ui.editors;

import java.util.List;

import domain.Column;
import domain.ColumnType;
import ui.controllers.ColumnsController;

public class ColumnDefaultValueAccess {

    private final ColumnsController controller;

    public ColumnDefaultValueAccess(ColumnsController controller) {
        this.controller = controller;
    }

    public String getValue(Column column) {
        return column.getDefaultValue();
    }

    public void setValue(Column column, String value) {
        controller.toggleDefaultValue(value, column);
    }

    public List<Column> getAllItems() {
        return controller.columnsRequest();
    }

    public ColumnType getType(Column column) {
        return column.getType();
    }

    public boolean allowsBlanks(Column column) {
        return column.isBlanksAllowed();
    }
}