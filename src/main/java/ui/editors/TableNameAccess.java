package ui.editors;

import java.util.List;
import domain.Table;
import ui.controllers.TablesController;

public class TableNameAccess implements NameAccess<Table> {
    private final TablesController controller;

    public TableNameAccess(TablesController controller) {
        this.controller = controller;
    }

    @Override
    public String getName(Table t) {
        return t.getName();
    }

    @Override
    public void setName(Table t, String name) {
        controller.rename(t, name);
    }

    @Override
    public List<Table> getAll() {
        return controller.tablesRequest();
    }
}