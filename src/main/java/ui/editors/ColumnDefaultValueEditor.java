package ui.editors;

import static ui.UIConstants.DEFAULT_VALUE_WIDTH;
import static ui.UIConstants.ROW_HEIGHT;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import domain.Column;
import domain.ColumnType;

public class ColumnDefaultValueEditor {

    private Column editingTarget = null;
    private String currentInput = "";
    private boolean editing = false;
    private Column blockedTypeColumn;

    private final ColumnDefaultValueAccess valueAccess;

    public ColumnDefaultValueEditor(ColumnDefaultValueAccess valueAccess) {
        this.valueAccess = valueAccess;
    }

    public void startEditing(Column column) {
        this.editingTarget = column;
        this.currentInput = valueAccess.getValue(column);
        this.editing = true;
    }

    public void handleKeyEvent(int id, int keyCode, char keyChar) {
        if (!editing) return;

        switch (keyCode) {
            case KeyEvent.VK_ENTER -> {
                if (isValid()) commitEdit();
            }
            case KeyEvent.VK_ESCAPE -> stopEditing();
            case KeyEvent.VK_BACK_SPACE -> {
                if (!currentInput.isEmpty()) {
                    currentInput = currentInput.substring(0, currentInput.length() - 1);
                }
            }
            default -> {
                if ("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789@._-".indexOf(keyChar) >= 0) {
                    currentInput += keyChar;
                }
            }
        }
    }

    public void drawValue(Graphics g, Column column, int x, int y) {
        boolean isTarget = editing && column == editingTarget;
        String value = isTarget ? currentInput + "_" : valueAccess.getValue(column);

        if (isTarget && !isValid()) {
            g.setColor(Color.RED);
            g.drawRect(x - 1, y, DEFAULT_VALUE_WIDTH, ROW_HEIGHT); // example dimensions
        }

        g.setColor(Color.BLACK);
        g.drawString(value, x + 2, y + 15);
    }

    public void commitEdit() {
        if (editingTarget != null) {
            valueAccess.setValue(editingTarget, currentInput);
            stopEditing();
        }
    }

    public void stopEditing() {
        editing = false;
        editingTarget = null;
        currentInput = "";
    }

    public boolean isEditing() {
        return editing;
    }

    public Column getEditingTarget() {
        return editingTarget;
    }

    public boolean isValid() {
        if (editingTarget == null) return true;

        String value = currentInput.trim();
        ColumnType type = editingTarget.getType();
        boolean blanksAllowed = editingTarget.isBlanksAllowed();

        return switch (type) {
            case BOOLEAN -> true; // never editable manually
            case STRING -> !value.isEmpty() || (value.isEmpty() && blanksAllowed);
            case EMAIL -> value.chars().filter(c -> c == '@').count() == 1 || (value.isEmpty() && blanksAllowed);
            case INTEGER -> {
                if (value.isEmpty()) yield blanksAllowed;
                try {
                    int parsed = Integer.parseInt(value);
                    yield Integer.toString(parsed).equals(value);
                } catch (NumberFormatException e) {
                    yield false;
                }
            }
        };
    }

    public boolean isValid(Column column) {
        String value = isEditing() && getEditingTarget() == column
            ? currentInput
            : column.getDefaultValue();
    
        if (value.isBlank()) {
            return column.isBlanksAllowed();
        }
    
        return switch (column.getType()) {
            case STRING -> true;
            case EMAIL -> value.chars().filter(ch -> ch == '@').count() == 1;
            case INTEGER -> value.matches("(-?[1-9]\\d*|0)");
            case BOOLEAN -> value.equals("true") || value.equals("false");
        };
    }

    public boolean isTypeBlocked(Column column) {
        return blockedTypeColumn == column;
    }
    
    public void setTypeBlocked(Column column) {
        blockedTypeColumn = column;
    }
    
    public boolean isTypeBlocked() {
        return blockedTypeColumn != null;
    }
    
    public void clearTypeBlock() {
        blockedTypeColumn = null;
    }

    public Column getBlockedColumn() {
        return blockedTypeColumn;
    }
}