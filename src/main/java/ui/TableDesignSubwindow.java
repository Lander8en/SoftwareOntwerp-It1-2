package ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;

import domain.Column;
import domain.ColumnRepositoryObserver;
import domain.ColumnType;
import domain.ColumnTypeValidator;
import domain.Table;
import static ui.UIConstants.CLOSE_BUTTON_SIZE;
import static ui.UIConstants.DEFAULT_VALUE_OFFSET;
import static ui.UIConstants.DEFAULT_VALUE_WIDTH;
import static ui.UIConstants.NAME_AREA_WIDTH;
import static ui.UIConstants.PADDING;
import static ui.UIConstants.ROW_HEIGHT;
import static ui.UIConstants.SPACER;
import static ui.UIConstants.TITLE_BAR_HEIGHT;
import static ui.UIConstants.TYPE_AREA_WIDTH;
import ui.controllers.ColumnsController;
import ui.editors.ColumnDefaultValueAccess;
import ui.editors.ColumnDefaultValueEditor;
import ui.editors.ColumnNameAccess;
import ui.editors.ColumnRowRenderer;
import ui.editors.NameEditor;

/**
 * A subwindow that provides an interface to edit the structure (columns) of a table.
 * <p>
 * Allows users to edit column names, types, default values, and constraints such as
 * whether blanks are allowed. Also supports adding and deleting columns.
 */
public class TableDesignSubwindow extends Subwindow implements ColumnRepositoryObserver, ScrollableWindow {

    private final ColumnsController controller;
    private final NameEditor<Column> columnEditor;
    private final ColumnDefaultValueEditor defaultValueEditor;
    private final ColumnRowRenderer rowRenderer;
    private int selectedColumnIndex = -1;
    private final ScrollablePanel scrollPanel;


    /**
     * Constructs a TableDesignSubwindow with the given parameters.
     *
     * @param x      the x-position of the window
     * @param y      the y-position of the window
     * @param width  the width of the window
     * @param height the height of the window
     * @param title  the window title
     * @param table  the table whose columns are to be edited
     * @throws NullPointerException if title or table is null
     */
    public TableDesignSubwindow(int x, int y, int width, int height, String title, Table table) {
        super(x, y, width, height, Objects.requireNonNull(title, "title must not be null"));
        Objects.requireNonNull(table, "table must not be null");
        this.controller = new ColumnsController(table);
        this.columnEditor = new NameEditor<>(new ColumnNameAccess(controller));
        this.defaultValueEditor = new ColumnDefaultValueEditor(new ColumnDefaultValueAccess(controller));
        this.rowRenderer = new ColumnRowRenderer(columnEditor, defaultValueEditor, controller);
        table.getColumnRepository().addObserver(this);
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
        return controller.columnsRequest().size() * ROW_HEIGHT + 70;
    }

    private int calculateContentWidth() {
        if(controller.columnsRequest().isEmpty()){ return 200;}
        return DEFAULT_VALUE_OFFSET + DEFAULT_VALUE_WIDTH + PADDING * 2;
    }

    /**
     * Notifies the subwindow when a column has changed.
     *
     * @param changedColumn the column that was changed
     */
    @Override
    public void onColumnChanged(Column changedColumn) {
        if (changedColumn != null) {
            columnEditor.stopEditingIfChanged(changedColumn);
    
            // Check if this column is now invalid due to default value or cell values
            boolean blocked = !ColumnTypeValidator.isColumnTypeValid(changedColumn, controller.getTable());
    
            if (blocked) {
                defaultValueEditor.setTypeBlocked(changedColumn);
            } else if (defaultValueEditor.isTypeBlocked() && defaultValueEditor.getBlockedColumn() == changedColumn) {
                defaultValueEditor.clearTypeBlock();
            }
        }
    }

    /**
     * Draws the subwindow, including the title bar, close button, and column rows.
     *
     * @param g        the graphics context
     * @param isActive whether this subwindow is currently active
     */
    @Override
    public void draw(Graphics g, boolean isActive) {
        updateViewport();
        g.setColor(Color.BLACK);
        g.drawRect(x, y, width, height);

        // Title bar
        g.setColor(isActive ? Color.BLUE : Color.LIGHT_GRAY);
        g.fillRect(x, y, width, TITLE_BAR_HEIGHT);
        g.setColor(Color.BLACK);
        g.drawString(title, x + 10, y + 20);

        // Close button
        int closeX = x + width - CLOSE_BUTTON_SIZE - PADDING;
        int closeY = y + PADDING;
        g.setColor(Color.RED);
        g.fillRect(closeX, closeY, CLOSE_BUTTON_SIZE, CLOSE_BUTTON_SIZE);
        g.setColor(Color.WHITE);
        g.drawString("X", closeX + 6, closeY + 16);

        // Content area
        g.setColor(Color.WHITE);
        g.fillRect(x + 1, y + TITLE_BAR_HEIGHT + 1, width - 2, height - TITLE_BAR_HEIGHT - 2);

        // Draw column rows
        List<Column> columns = controller.columnsRequest();
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
        int visibleHeight = viewport.height;
        
        // Calculate visible range
        int firstVisible = Math.max(0, scrollY / ROW_HEIGHT);
        int lastVisible = Math.min(columns.size(), firstVisible + (visibleHeight / ROW_HEIGHT) + 2);

        for (int i = firstVisible; i < lastVisible; i++) {
            Column column = columns.get(i);
            int rowX = x + 1;
            int rowY = getColumnListTopY() + i * ROW_HEIGHT - scrollY;

            rowRenderer.drawRow(g, column, rowX, rowY, i == selectedColumnIndex);
        }

        // Draw "add column" instruction
        g.setColor(Color.GRAY);
        g.drawString("Double-click to add a column", 
            x + PADDING, 
            y + 50 + columns.size() * ROW_HEIGHT - scrollY);

        // Reset scroll transformation
        scrollPanel.resetScroll(g);

        // Draw scrollbars
        scrollPanel.drawScrollbars(g, viewport, isActive);
    }

    /**
     * Handles mouse events within the subwindow.
     *
     * @param id         the type of mouse event
     * @param mouseX     the x-position of the mouse
     * @param mouseY     the y-position of the mouse
     * @param clickCount the number of clicks
     */
    public void handleMouseEvent(int id, int mouseX, int mouseY, int clickCount) {
        if (handleOutsideClickIfNecessary(mouseX, mouseY)) return;
        if (blockInteractionDueToInvalidEditing(mouseX, mouseY)) return;
        if (handleBlockedTypeClick(mouseX, mouseY)) return;

        if (id == MouseEvent.MOUSE_CLICKED) {
            handleClick(mouseX, mouseY, clickCount);
        }
    }

    private boolean handleOutsideClickIfNecessary(int mouseX, int mouseY) {
        if (!contains(mouseX, mouseY)) {
            handleClickOutside();
            return true;
        }
        return false;
    }

    private boolean blockInteractionDueToInvalidEditing(int mouseX, int mouseY) {
        if (blockEditing()) return true;

        if (hasBlockingBlankViolation()) {
            List<Column> columns = controller.columnsRequest();
            int index = getColumnIndexFromY(mouseY);
            if (index < 0 || index >= columns.size()) return true;

            Column column = columns.get(index);
            if (!isBlankViolation(column)) return true;
            if (!isInBlanksCheckboxArea(mouseX, getRowX())) return true;
        }

        return false;
    }

    private boolean handleBlockedTypeClick(int mouseX, int mouseY) {
        if (defaultValueEditor.isTypeBlocked()) {
            Column blocked = defaultValueEditor.getBlockedColumn();
            Column hovered = getColumnAtPosition(mouseX, mouseY);
            return hovered != blocked || !isInTypeClickArea(mouseX, mouseY);
        }
        return false;
    }

    private void handleClick(int mouseX, int mouseY, int clickCount) {
        if (clickCount == 2 && isInAddColumnArea(mouseX, mouseY)) {
            controller.handleCreateNewColumnRequest();
        } else if (clickCount == 1) {
            handleDefaultValueCommit(mouseX, mouseY);
            handleClickAt(mouseX, mouseY);
        }
    }

    private void handleDefaultValueCommit(int mouseX, int mouseY) {
        if (!defaultValueEditor.isEditing()) return;

        Column editing = defaultValueEditor.getEditingTarget();
        if (editing == null) return;

        int editingIndex = controller.columnsRequest().indexOf(editing);
        if (editingIndex == -1) return;

        int rowX = getRowX();
        int rowY = getColumnListTopY() + editingIndex * ROW_HEIGHT;
        int boxX = rowX + DEFAULT_VALUE_OFFSET;
        int boxY = rowY;

        boolean clickedInBox = mouseX >= boxX && mouseX <= boxX + DEFAULT_VALUE_WIDTH &&
                               mouseY >= boxY && mouseY <= boxY + ROW_HEIGHT;

        if (!clickedInBox && defaultValueEditor.isValid()) {
            defaultValueEditor.commitEdit();
        }
    }

    private void handleClickAt(int mouseX, int mouseY) {
        List<Column> columns = controller.columnsRequest();
        int index = getColumnIndexFromY(mouseY);
        if (index < 0 || index >= columns.size()) {
            commitIfEditing();
            selectedColumnIndex = -1;
            return;
        }

        int rowX = getRowX();
        Column target = columns.get(index);

        if (isInSelectionMargin(mouseX, rowX)) {
            handleMarginClick(index);
        } else if (isInNameClickArea(mouseX, rowX)) {
            handleNameClick(target, index);
        } else if (isInTypeClickArea(mouseX, mouseY)) {
            handleTypeClick(target);
        } else if (isInBlanksCheckboxArea(mouseX, rowX)) {
            handleBlanksCheckboxClick(target);
        } else if (isInDefaultValueArea(mouseX, rowX)) {
            handleDefaultValueClick(target);
        }
    }

    private int getRowX() {
        return x + 1 - scrollPanel.getScrollX();
    }

    private void handleClickOutside() {
        commitIfEditing();
        selectedColumnIndex = -1;
    }

    private void handleMarginClick(int index) {
        selectedColumnIndex = index;
        columnEditor.stopEditing();
    }

    private void handleNameClick(Column column, int index) {
        if (isEditingAnotherColumn(column)) {
            columnEditor.commitEdit();
        }
        selectedColumnIndex = index;
        if (!columnEditor.isEditing() || columnEditor.getEditingTarget() != column) {
            columnEditor.startEditing(column);
        }
    }

    private void handleTypeClick(Column column) {
        ColumnType next = column.getType().next();
        Table table = controller.getTable();
    
        controller.setColumnType(column, next);
    
        boolean valid = ColumnTypeValidator.isColumnTypeValid(column, table);
    
        if (valid) {
            defaultValueEditor.clearTypeBlock();
        } else {
            defaultValueEditor.setTypeBlocked(column);
        }
    }

    private void handleBlanksCheckboxClick(Column column) {
        controller.toggleBlanksAllowed(column);
    }

    private void handleDefaultValueClick(Column column) {
        if (column.getType() == ColumnType.BOOLEAN) {
            cycleBooleanDefaultValue(column);
        } else {
            defaultValueEditor.startEditing(column);
        }
    }

    /**
     * Handles key input such as deletion or editing.
     *
     * @param id      the type of key event
     * @param keyCode the key code
     * @param keyChar the character associated with the key
     */
    @Override
    public void handleKeyEvent(int id, int keyCode, char keyChar) {
        List<Column> columns = controller.columnsRequest();

        if (keyCode == KeyEvent.VK_DELETE && !columnEditor.isEditing()) {
            if (selectedColumnIndex >= 0 && selectedColumnIndex < columns.size()) {
                Column toDelete = columns.get(selectedColumnIndex);
                controller.handleDeleteColumnRequest(toDelete);
                selectedColumnIndex = -1;
            }
        }

        columnEditor.handleKeyEvent(id, keyCode, keyChar);
        defaultValueEditor.handleKeyEvent(id, keyCode, keyChar);
    }

    /**
     * Returns the column located at the specified position, or null if none.
     *
     * @param mouseX the x-position
     * @param mouseY the y-position
     * @return the column or null
     */
    public Column getColumnAtPosition(int mouseX, int mouseY) {
        int index = getColumnIndexFromY(mouseY);
        List<Column> columns = controller.columnsRequest();
        if (index >= 0 && index < columns.size()) {
            return columns.get(index);
        }
        return null;
    }

    public boolean isInTypeClickArea(int mouseX, int mouseY) {
        int typeAreaXStart = x + SPACER;
        return mouseX >= typeAreaXStart - scrollPanel.getScrollX() && mouseX <= x + TYPE_AREA_WIDTH - scrollPanel.getScrollX();
    }

    private boolean isInNameClickArea(int mouseX, int rowX) {
        return mouseX >= rowX  && mouseX <= rowX + NAME_AREA_WIDTH ;
    }

    private boolean isEditingAnotherColumn(Column target) {
        return columnEditor.isEditing() && columnEditor.getEditingTarget() != target;
    }

    private boolean isInBlanksCheckboxArea(int mouseX, int rowX) {
        int checkboxStart = rowX + SPACER + 100;
        return mouseX >= checkboxStart && mouseX <= checkboxStart + 14;
    }

    private boolean isInDefaultValueArea(int mouseX, int rowX) {
        int startX = rowX + SPACER + 150;
        return mouseX >= startX && mouseX <= startX + DEFAULT_VALUE_WIDTH;
    }

    private boolean isInAddColumnArea(int mouseX, int mouseY) {
        int top = getColumnListTopY() + controller.columnsRequest().size() * ROW_HEIGHT - scrollPanel.getScrollY()*2;
        return mouseY >= top && mouseY <= y + height &&
            mouseX >= x - scrollPanel.getScrollX() && mouseX <= x + width - scrollPanel.getScrollX();
    }

    private void cycleBooleanDefaultValue(Column column) {
        String current = column.getDefaultValue();
        boolean allowBlank = column.isBlanksAllowed();

        if (allowBlank) {
            switch (current) {
                case "true" -> controller.toggleDefaultValue("false", column);
                case "false" -> controller.toggleDefaultValue("", column);
                default -> controller.toggleDefaultValue("true", column);
            }
        } else {
            controller.toggleDefaultValue("true".equals(current) ? "false" : "true", column);
        }
    }

    private void commitIfEditing() {
        if (columnEditor.isEditing()) {
            columnEditor.commitEdit();
        }
    }

    private int getColumnListTopY() {
        return y + TITLE_BAR_HEIGHT + PADDING;
    }

    public int getColumnIndexFromY(int mouseY) {
        return (mouseY - getColumnListTopY() + scrollPanel.getScrollY()*2) / ROW_HEIGHT;
    }

    public boolean blockEditing() {
        return !columnEditor.isValid() && selectedColumnIndex != -1 && columnEditor.isEditing();
    }

    private boolean isBlankViolation(Column column) {
        return !column.isBlanksAllowed() && column.getDefaultValue().isBlank();
    }

    private boolean hasBlockingBlankViolation() {
        return controller.columnsRequest().stream().anyMatch(this::isBlankViolation);
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