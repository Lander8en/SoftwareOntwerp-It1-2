package ui;

import domain.Table;
import domain.TableRepository;

/**
 * Factory class responsible for creating all types of subwindows in a consistent manner.
 * Keeps track of creation count to offset window positions.
 */
public class SubwindowFactory {
    
    private int createdCount = 0;

    public SubwindowFactory() {
    }

    /**
     * Creates a new TableSubwindow with a fresh TableRepository.
     * 
     * @return a new TableSubwindow instance
     */
    public TableSubwindow createNewTableSubwindow() {
        TableRepository newRepo = new TableRepository();
        return createTableSubwindow(newRepo);
    }

    /**
     * Creates a new TableDesignSubwindow for the given table.
     *
     * @param table the table to design
     * @return a new TableDesignSubwindow instance
     */
    public TableDesignSubwindow createNewTableDesignSubwindow(Table table) {
        return createTableDesignSubwindow(table);
    }

    /**
     * Creates a new TableRowsSubwindow for the given table.
     *
     * @param table the table to show rows for
     * @return a new TableRowsSubwindow instance
     */
    public TableRowsSubwindow createNewTableRowsSubwindow(Table table) {
        return createRowSubwindow(table);
    }

    // ---- Internal creation logic ---- //

    private TableSubwindow createTableSubwindow(TableRepository tableRepository) {
        int offset = createdCount * 20;
        createdCount++;

        int x = 50 + offset;
        int y = 50 + offset;
        int width = 300;
        int height = 200;

        return new TableSubwindow(x, y, width, height, "Tables", tableRepository);
    }

    private TableDesignSubwindow createTableDesignSubwindow(Table table) {
        int offset = createdCount * 20;
        createdCount++;

        int x = 50 + offset;
        int y = 50 + offset;
        int width = 300;
        int height = 200;

        return new TableDesignSubwindow(x, y, width, height, "Table Design - " + table.getName(), table);
    }

    private TableRowsSubwindow createRowSubwindow(Table table) {
        int offset = createdCount * 20;
        createdCount++;

        int x = 50 + offset;
        int y = 50 + offset;
        int width = 300;
        int height = 200;

        return new TableRowsSubwindow(x, y, width, height, "Table Rows - " + table.getName(), table);
    }
}