package ui.editors;

import java.util.List;
import domain.Column;
import ui.controllers.ColumnsController;

public class ColumnNameAccess implements NameAccess<Column> {
    private final ColumnsController controller;

    public ColumnNameAccess(ColumnsController controller) {
        this.controller = controller;
    }

    public String getName(Column column) {
        return column.getName();
    }

    public void setName(Column column, String name) {
        controller.rename(column, name);
    }

    public List<Column> getAll() {
        return controller.columnsRequest();
    }
}