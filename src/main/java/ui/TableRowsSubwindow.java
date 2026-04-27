package ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;

import domain.Column;
import domain.ColumnType;
import domain.Row;
import domain.Table;
import static ui.UIConstants.CLOSE_BUTTON_SIZE;
import static ui.UIConstants.HEADER_HEIGHT;
import static ui.UIConstants.MARGIN_WIDTH;
import static ui.UIConstants.PADDING;
import static ui.UIConstants.ROW_HEIGHT;
import static ui.UIConstants.TITLE_BAR_HEIGHT;
import ui.controllers.RowsController;
import ui.editors.RowValueAccess;
import ui.editors.RowValueEditor;

/**
 * A subwindow that displays and allows editing of rows in a table.
 * Supports cell editing, boolean toggling, and row deletion.
 */
public class TableRowsSubwindow extends Subwindow implements ScrollableWindow{

    private final RowsController controller;
    private final RowValueEditor rowValueEditor;
    private int selectedRowIndex = -1;
    private final ScrollablePanel scrollPanel;


    /**
     * Constructs a new TableRowsSubwindow.
     *
     * @param x      the x-coordinate of the window
     * @param y      the y-coordinate of the window
     * @param width  the width of the window
     * @param height the height of the window
     * @param title  the window title
     * @param table  the table to display rows for
     * @throws IllegalArgumentException if table is null
     */
    public TableRowsSubwindow(int x, int y, int width, int height, String title, Table table) {
        super(x, y, width, height, title);
        if (table == null) {
            throw new IllegalArgumentException("Table cannot be null");
        }
        this.controller = new RowsController(table);
        this.rowValueEditor = new RowValueEditor(new RowValueAccess(controller));
        this.scrollPanel = new ScrollablePanel();
        updateViewport();
    }


    private void updateViewport() {
        int contentHeight = calculateContentHeight();
        int contentWidth = calculateContentWidth();

        int viewportHeight = height - TITLE_BAR_HEIGHT - 2;
        int viewportWidth = width - 2;
        
        scrollPanel.setViewportSize(viewportWidth, viewportHeight);
        scrollPanel.setContentSize(contentWidth, contentHeight);
    }
    
    private int calculateContentHeight() {
        return controller.rowsRequest().size() * ROW_HEIGHT + HEADER_HEIGHT + 50;
    }

    private int calculateContentWidth() {
        List<Column> columns = controller.columnsRequest();
        return columns != null ? (columns.size() * 100) + PADDING * 2 : width - 2;
    }
    
    /**
     * Draws the contents of the subwindow including headers, rows, and the UI elements.
     *
     * @param g        the Graphics context to draw on
     * @param isActive whether this window is the active one
     */
    @Override
            
    public void draw(Graphics g, boolean isActive) {
        updateViewport();
        g.setColor(Color.BLACK);
        g.drawRect(x, y, width, height);

        g.setColor(isActive ? Color.BLUE : Color.LIGHT_GRAY);
        g.fillRect(x, y, width, TITLE_BAR_HEIGHT);
        g.setColor(Color.BLACK);
        g.drawString(title, x + 10, y + 20);

        int closeX = x + width - CLOSE_BUTTON_SIZE - PADDING;
        int closeY = y + PADDING;
        g.setColor(Color.RED);
        g.fillRect(closeX, closeY, CLOSE_BUTTON_SIZE, CLOSE_BUTTON_SIZE);
        g.setColor(Color.WHITE);
        g.drawString("X", closeX + 6, closeY + 16);

        g.setColor(Color.WHITE);
        g.fillRect(x + 1, y + TITLE_BAR_HEIGHT + 1, width - 2, height - TITLE_BAR_HEIGHT - 2);

        List<Column> columns = controller.columnsRequest();
        List<Row> rows = controller.rowsRequest();
        // Set up viewport rectangle
        Rectangle viewport = new Rectangle(
            x + 1, 
            y + TITLE_BAR_HEIGHT + 1, 
            width - 2, 
            
            height - TITLE_BAR_HEIGHT - 2
        );

        // Apply scroll transformation
        scrollPanel.applyScroll(g, viewport);
        int scrollY = scrollPanel.getScrollY();        
        // Draw headers (fixed position)
        int headerY = getListTopY() - PADDING - scrollY;
        for (int col = 0; col < columns.size(); col++) {
            int cellX = x + 10 + col * 100;
            Column column = columns.get(col);
            if (column != null) {
                g.setColor(Color.DARK_GRAY);
                g.drawString(column.getName(), cellX, headerY + 15);
            }
        }

        // Draw visible rows
        int firstVisible = Math.max(0, scrollY / ROW_HEIGHT);
        int lastVisible = Math.min(rows.size(), firstVisible + (height / ROW_HEIGHT) + 2);
        
        for (int rowIndex = firstVisible; rowIndex < lastVisible; rowIndex++) {
            
            drawRows(g,scrollY);
        }

        // Draw "add row" instruction
        g.setColor(Color.GRAY);
        g.drawString("Double-click to add a row", 
            x + PADDING, 
            getListTopY()
            + rows.size() * ROW_HEIGHT + HEADER_HEIGHT - scrollY);

        // Reset scroll transformation
        scrollPanel.resetScroll(g);

        // Draw scrollbars
        scrollPanel.drawScrollbars(g, viewport, isActive);

    }

    /**
     * Draws all rows and highlights the selected row.
     *
     * @param g the Graphics context
     */
    private void drawRows(Graphics g, int scrollY) {
        List<Column> columns = controller.columnsRequest();
        List<Row> rows = controller.rowsRequest();
        if (columns == null || rows == null) return;

        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            Row row = rows.get(rowIndex);
            int rowY = getListTopY() + rowIndex * ROW_HEIGHT + HEADER_HEIGHT - scrollY;

            for (int col = 0; col < columns.size(); col++) {
                int cellX = x + 10 + col * 100;
                Column column = columns.get(col);
                if (column != null && row != null) {
                    rowValueEditor.draw(g, row, col, cellX, rowY, column.getType());
                }
            }
            

            
            if (rowIndex == selectedRowIndex) {
                int marginX = x + 1;
                g.setColor(Color.BLUE);
                g.fillRect(marginX, rowY - 16, MARGIN_WIDTH - 5, ROW_HEIGHT);
            }
        }
    }

    /**
     * Handles key events like deletion of a row.
     *
     * @param id      the key event ID
     * @param keyCode the key code
     * @param keyChar the key character
     */
    @Override
    public void handleKeyEvent(int id, int keyCode, char keyChar) {
        List<Row> rows = controller.rowsRequest();
        if (rows == null) return;

        if (keyCode == KeyEvent.VK_DELETE && !rowValueEditor.isEditing()) {
            if (selectedRowIndex >= 0 && selectedRowIndex < rows.size()) {
                Row toDelete = rows.get(selectedRowIndex);
                controller.handleDeleteRowRequest(toDelete);
                selectedRowIndex = -1;
            
            }
        }
            

        rowValueEditor.handleKeyEvent(id, keyCode, keyChar);
    }

    /**
     * Handles mouse interactions for row selection, editing, and row creation.
     *
     * @param id         the mouse event ID
     * @param mouseX     the x-position of the mouse
     * @param mouseY     the y-position of the mouse
     * @param clickCount the number of clicks
     */
            
    public void handleMouseEvent(int id, int mouseX, int mouseY, int clickCount) {
        if (!contains(mouseX, mouseY)) {
            if (rowValueEditor.isEditing() && rowValueEditor.isValid()) {
                rowValueEditor.commitEdit();
            }
            selectedRowIndex = -1;
            return;
        }

        if (id != MouseEvent.MOUSE_CLICKED) return;
        if (!rowValueEditor.isValid()) return;

        if (rowValueEditor.isEditing() && !handleEditingClick(mouseX, mouseY)) {
            return;
        }

        if (clickCount == 2 && isInAddRowArea(mouseX, mouseY)) {
            controller.handleCreateNewRowRequest();
        } else if (clickCount == 1) {
            handleSingleClick(mouseX, mouseY);
        }
    }

    /**
     * Handles single click actions for selection or editing.
     *
     * @param mouseX the x-position of the mouse
     * @param mouseY the y-position of the mouse
     */
            
    private void handleSingleClick(int mouseX, int mouseY) {
        int colIdx = (mouseX + scrollPanel.getScrollX() - (x + 10)) / 100;
        int rowIdx = (mouseY - getListTopY() - ROW_HEIGHT + scrollPanel.getScrollY()*2 ) / ROW_HEIGHT;

        List<Row> rows = controller.rowsRequest();
        List<Column> columns = controller.columnsRequest();
        if (rows == null || columns == null) return;
        if (rowIdx < 0 || rowIdx >= rows.size() || colIdx < 0 || colIdx >= columns.size()) return;
                
        int marginX = x + 1;
        if (isInSelectionMargin(mouseX, marginX)) {
            selectedRowIndex = rowIdx;
            rowValueEditor.stopEditing();
            return;
        }

        Row row = rows.get(rowIdx);
        Column column = columns.get(colIdx);

        if (row == null || column == null) return;

        if (column.getType() == ColumnType.BOOLEAN) {
            String current = row.getValue(colIdx);
            boolean allowBlank = column.isBlanksAllowed();

            String next = switch (current) {
                case "true" -> "false";
                case "false" -> allowBlank ? "" : "true";
                default -> "true";
            };

                selectedRowIndex = -1;
                controller.toggleValue(row, colIdx, next);
        } else {
            selectedRowIndex = -1;
            rowValueEditor.startEditing(row, colIdx);
        }
    }

    /**
     * Handles clicks while editing to determine if commit or cancel is needed.
     *
     * @param mouseX the x-position of the mouse
     * @param mouseY the y-position of the mouse
     * @return true if the click is accepted, false if invalid
     */
    private boolean handleEditingClick(int mouseX, int mouseY) {
        Row editingRow = rowValueEditor.getEditingRow();
        if (editingRow == null) return false;

        int editingRowIdx = controller.rowsRequest().indexOf(editingRow);
        int editingColIdx = rowValueEditor.getEditingColIndex();

        int cellX = x + 10 + editingColIdx * 100;
        int cellY = getListTopY() + editingRowIdx * ROW_HEIGHT + HEADER_HEIGHT;

        boolean clickedInSameCell =
            mouseX >= cellX && mouseX <= cellX + 100 &&
            mouseY >= cellY - 15 && mouseY <= cellY + 5;

        if (!clickedInSameCell) {
            if (rowValueEditor.isValid()) {
                rowValueEditor.commitEdit();
                return true;
            }
            return false;
        }

        return true;
    }

    /**
     * Determines whether the mouse click occurred in the "add row" area.
     *
     * @param mouseX the x-position of the mouse
     * @param mouseY the y-position of the mouse
     * @return true if the click is in the add row zone
     */
    private boolean isInAddRowArea(int mouseX, int mouseY) {
        int top = getListTopY() + controller.rowsRequest().size() * getRowHeight() - scrollPanel.getScrollY()*2;
        return mouseY >= top && mouseY <= y + height &&
               mouseX >= x && mouseX <= x + width;
    }

    public Table getTable() {
        return controller.getTable();
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
            height - TITLE_BAR_HEIGHT - 2
        );
    }
}