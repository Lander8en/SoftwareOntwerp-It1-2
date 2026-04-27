package ui.editors;

import domain.ColumnType;
import domain.Row;
import ui.controllers.RowsController;

import java.util.List;

public class RowValueAccess{

    private final RowsController controller;

    public RowValueAccess(RowsController controller) {
        this.controller = controller;
    }

    public String getValue(Row row, int columnIndex) {
        return row.getValue(columnIndex);
    }

    public void setValue(Row row, int columnIndex, String value) {
        controller.toggleValue(row, columnIndex, value);
    }

    public List<Row> getAllRows() {
        return controller.rowsRequest();
    }

    public ColumnType getType(int colIndex) {
        return controller.typeRequest(colIndex);
    }

    public boolean allowsBlanks(int colIndex) {
        return controller.columnAllowsBlanks(colIndex);
    }
}