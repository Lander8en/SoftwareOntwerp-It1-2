package ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;

import domain.Table;
import domain.TableRepository;
import static ui.UIConstants.CLOSE_BUTTON_SIZE;
import static ui.UIConstants.PADDING;
import static ui.UIConstants.ROW_HEIGHT;
import static ui.UIConstants.SCROLLBAR_SIZE;
import static ui.UIConstants.TITLE_BAR_HEIGHT;
import ui.controllers.TablesController;
import ui.editors.NameEditor;
import ui.editors.TableNameAccess;

/**
 * A subwindow that displays and allows editing of tables in a TableRepository.
 * Supports selection, renaming, and deletion of tables via keyboard and mouse.
 */
public class TableSubwindow extends Subwindow implements ScrollableWindow {
    private final ScrollablePanel scrollPanel;
    private final TablesController controller;
    private final NameEditor<Table> tableEditor;
    private int selectedTableIndex = -1;

    /**
     * Constructs a new TableSubwindow with the given position, size, and table
     * repository.
     *
     * @param x               The x-coordinate of the subwindow.
     * @param y               The y-coordinate of the subwindow.
     * @param width           The width of the subwindow.
     * @param height          The height of the subwindow.
     * @param title           The title of the subwindow.
     * @param tableRepository The table repository to manage.
     * @throws NullPointerException     if title or tableRepository is null.
     * @throws IllegalArgumentException if width or height is non-positive.
     */
    public TableSubwindow(int x, int y, int width, int height, String title, TableRepository tableRepository) {
        super(x, y, width, height, Objects.requireNonNull(title));
        if (width <= 0 || height <= 0)
            throw new IllegalArgumentException("Subwindow dimensions must be positive");
        Objects.requireNonNull(tableRepository);
        this.scrollPanel = new ScrollablePanel();
        this.controller = new TablesController(tableRepository);
        this.tableEditor = new NameEditor<>(new TableNameAccess(controller));
        updateViewport();
    }

    private void updateViewport() {
        int contentHeight = calculateContentHeight();
        int viewportHeight = height - TITLE_BAR_HEIGHT - 2; // Subtract title bar and borders
        int viewportWidth = width - 2; // Subtract borders

        scrollPanel.setViewportSize(viewportWidth, viewportHeight);
        scrollPanel.setContentSize(viewportWidth - SCROLLBAR_SIZE, contentHeight);
    }

    private int calculateContentHeight() {
        List<Table> tables = controller.tablesRequest();
        // Height of all tables + padding + "Double-click to add" text area
        return tables.size() * ROW_HEIGHT + 70;
    }

    @Override
    public void draw(Graphics g, boolean isActive) {
        List<Table> tables = controller.tablesRequest();
        updateViewport();

        // Border
        g.setColor(Color.BLACK);
        g.drawRect(x, y, width, height);

        // Title bar
        g.setColor(isActive ? Color.BLUE : Color.LIGHT_GRAY);
        g.fillRect(x, y, width, TITLE_BAR_HEIGHT);
        g.setColor(Color.BLACK);
        g.drawString(title, x + PADDING, y + 20);

        // Close button
        int closeX = x + width - CLOSE_BUTTON_SIZE - PADDING;
        int closeY = y + PADDING;
        g.setColor(Color.RED);
        g.fillRect(closeX, closeY, CLOSE_BUTTON_SIZE, CLOSE_BUTTON_SIZE);
        g.setColor(Color.WHITE);
        g.drawString("X", closeX + 6, closeY + 16);

        // Content area background
        g.setColor(Color.WHITE);
        g.fillRect(x + 1, y + TITLE_BAR_HEIGHT + 1, width - 2, height - TITLE_BAR_HEIGHT - 2);

        // Set up viewport rectangle for scrollable area
        Rectangle viewport = new Rectangle(
                x + 1,
                y + TITLE_BAR_HEIGHT + 1,
                width - 2,
                height - TITLE_BAR_HEIGHT - 2);

        // Apply scroll transformation
        scrollPanel.applyScroll(g, viewport);

        // Draw content with scroll offset
        int scrollY = scrollPanel.getScrollY();
        int visibleHeight = viewport.height;

        // Calculate visible range of items
        int firstVisible = Math.max(0, scrollY / ROW_HEIGHT);
        int lastVisible = Math.min(tables.size(), firstVisible + (visibleHeight / ROW_HEIGHT) + 2);

        for (int i = firstVisible; i < lastVisible; i++) {
            Table table = tables.get(i);
            int rowX = x + 1;
            int rowY = getListTopY() + i * ROW_HEIGHT - scrollY;
            int rowWidth = width - 2;

            if (i == selectedTableIndex) {
                g.setColor(Color.LIGHT_GRAY);
                g.fillRect(rowX, rowY, rowWidth, ROW_HEIGHT);
            }

            tableEditor.drawName(g, table, rowX, rowY, rowWidth, ROW_HEIGHT);
        }

        // Draw "add table" instruction
        g.setColor(Color.GRAY);
        g.drawString("Double-click to add a table",
                x + PADDING,
                y + 50 + tables.size() * ROW_HEIGHT - scrollY);

        // Reset scroll transformation
        scrollPanel.resetScroll(g);

        // Draw scrollbars
        scrollPanel.drawScrollbars(g, viewport, isActive);
    }

    public void handleMouseEvent(int id, int mouseX, int mouseY, int clickCount) {
        if (!contains(mouseX, mouseY)) {
            handleClickOutside();
            return;
        }

        if (blockEditing())
            return;
        if (id == MouseEvent.MOUSE_CLICKED) {
            if (clickCount == 2 && isInAddTableArea(mouseX, mouseY)) {
                controller.handleCreateNewTableRequest();
                return;
            }

            if (clickCount == 1) {
                handleTableSelection(mouseX, mouseY);
            }
        }
    }

    private void handleClickOutside() {
        if (tableEditor.isEditing())
            tableEditor.commitEdit();
        selectedTableIndex = -1;
    }

    private void handleTableSelection(int mouseX, int mouseY) {
        List<Table> tables = controller.tablesRequest();
        int index = getTableIndexFromY(mouseY);

        if (index < 0 || index >= tables.size()) {
            if (tableEditor.isEditing()) {
                tableEditor.commitEdit();
                selectedTableIndex = -1;
            }
            return;
        }

        int rowX = x + 1;
        if (isInSelectionMargin(mouseX, rowX)) {
            selectedTableIndex = index;
            return;
        }

        if (tableEditor.isEditing()) {
            tableEditor.commitEdit();
        }

        selectedTableIndex = index;
        tableEditor.startEditing(tables.get(index));
    }

    /**
     * Returns the index of the table corresponding to the given y-coordinate.
     */
    public int getTableIndexFromY(int mouseY) {
        return (mouseY - getListTopY() + scrollPanel.getScrollY() * 2) / ROW_HEIGHT;
    }

    private boolean isInAddTableArea(int mouseX, int mouseY) {
        int top = getListTopY() + controller.tablesRequest().size() * ROW_HEIGHT - scrollPanel.getScrollY() * 2;
        return isInside(mouseX, mouseY, x, top, width, height - (top - y));
    }

    /**
     * Returns true if editing should be blocked (due to invalid name input).
     */
    public boolean blockEditing() {
        return !tableEditor.isValid() && selectedTableIndex != -1 && tableEditor.isEditing();
    }

    /**
     * Returns the list of tables shown in this subwindow.
     */
    public List<Table> getTables() {
        return controller.tablesRequest();
    }

    /**
     * Returns true if the given point is in the table name area (but not the
     * margin).
     */
    public boolean isInTableNameArea(int mouseX, int mouseY) {
        int index = getTableIndexFromY(mouseY);
        List<Table> tables = controller.tablesRequest();
        if (index < 0 || index >= tables.size())
            return false;

        int rowX = x + 1;
        int rowY = getListTopY() + index * ROW_HEIGHT;
        int rowWidth = width - 2;

        boolean insideRow = isInside(mouseX, mouseY, rowX, rowY - scrollPanel.getScrollY() * 2, rowWidth, ROW_HEIGHT);
        return insideRow && !isInSelectionMargin(mouseX, rowX);
    }

    @Override
    public void handleKeyEvent(int id, int keyCode, char keyChar) {
        List<Table> tables = controller.tablesRequest();

        if (keyCode == KeyEvent.VK_DELETE && !tableEditor.isEditing()) {
            if (blockEditing())
                return;
            if (hasSelectedTable()) {
                Table toDelete = tables.get(selectedTableIndex);
                controller.handleDeleteTableRequest(toDelete);
                selectedTableIndex = -1;
            }
        }

        tableEditor.handleKeyEvent(id, keyCode, keyChar);
    }

    /**
     * Returns the currently selected table, or null if none is selected.
     */
    public Table getSelectedTable() {
        return hasSelectedTable() ? controller.tablesRequest().get(selectedTableIndex) : null;
    }

    /**
     * Selects a table by its index in the list.
     *
     * @param index Index to select.
     * @throws IllegalArgumentException if index is invalid.
     */
    public void setSelectedTable(int index) {
        if (index < 0 || index >= controller.tablesRequest().size()) {
            throw new IllegalArgumentException("Invalid table index: " + index);
        }
        selectedTableIndex = index;
    }

    private boolean hasSelectedTable() {
        return selectedTableIndex >= 0 && selectedTableIndex < controller.tablesRequest().size();
    }

    @Override
    public ScrollablePanel getScrollPanel() {
        return scrollPanel;
    }

    @Override
    public Rectangle getViewport() {
        return new Rectangle(
                x + 1,
                y + TITLE_BAR_HEIGHT + 1,
                width - 2,
                height - TITLE_BAR_HEIGHT - 2);
    }
}
