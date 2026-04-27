package ui.editors;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

import domain.ColumnType;
import domain.Row;

public class RowValueEditor {

    private Row editingRow = null;
    private int editingColIndex = -1;
    private String currentInput = "";
    private boolean editing = false;

    private final RowValueAccess access;

    public RowValueEditor(RowValueAccess access) {
        this.access = access;
    }

    public void startEditing(Row row, int colIndex) {
        this.editingRow = row;
        this.editingColIndex = colIndex;
        this.currentInput = access.getValue(row, colIndex);
        if (this.currentInput == null) {
            this.currentInput = "";
        }
        this.editing = true;
    }

    public void handleKeyEvent(int id, int keyCode, char keyChar) {
        if (!editing)
            return;

        switch (keyCode) {
            case KeyEvent.VK_ENTER -> commitEdit();
            case KeyEvent.VK_ESCAPE -> stopEditing();
            case KeyEvent.VK_BACK_SPACE -> {
                if (!currentInput.isEmpty()) {
                    currentInput = currentInput.substring(0, currentInput.length() - 1);
                }
            }
            default -> {
                if (!Character.isISOControl(keyChar) && keyChar != KeyEvent.CHAR_UNDEFINED) {
                    currentInput += keyChar;
                }
            }
        }
    }

    public void draw(Graphics g, Row row, int colIndex, int x, int y, ColumnType type) {
        boolean isTarget = editing && editingRow == row && editingColIndex == colIndex;
        String value = isTarget ? currentInput : access.getValue(row, colIndex);

        if (type == ColumnType.BOOLEAN) {
            int boxSize = 14;
            int boxX = x + 2;
            int boxY = y - boxSize;

            if (isTarget) {
                g.setColor(Color.LIGHT_GRAY);
                g.fillRect(x - 2, y - 15, 100, 20);
            }

            g.setColor(value.isBlank() ? Color.LIGHT_GRAY : Color.BLACK);
            g.drawRect(boxX, boxY, boxSize, boxSize);

            if ("true".equalsIgnoreCase(value.trim())) {
                g.drawLine(boxX + 3, boxY + 7, boxX + 6, boxY + 10);
                g.drawLine(boxX + 6, boxY + 10, boxX + 11, boxY + 4);
            }

        } else {
            if (isTarget) {
                g.setColor(Color.LIGHT_GRAY);
                g.fillRect(x - 2, y - 15, 95, 18);

                if (!isValid()) {
                    g.setColor(Color.RED);
                    g.drawRect(x - 3, y - 16, 97, 20); // red border
                }
            }

            g.setColor(Color.BLACK);
            g.drawRect(x - 2, y - 15, 95, 18);
            g.drawString(value + (isTarget ? "_" : ""), x, y);
        }
    }

    public void commitEdit() {
        if (editingRow != null && isValid()) {
            access.setValue(editingRow, editingColIndex, currentInput);
            stopEditing();
        }
    }

    public void stopEditing() {
        editing = false;
        editingRow = null;
        editingColIndex = -1;
        currentInput = "";
    }

    public boolean isEditing() {
        return editing;
    }

    public Row getEditingRow() {
        return editingRow;
    }

    public int getEditingColIndex() {
        return editingColIndex;
    }

    public boolean isValid() {
        if (!editing)
            return true;

        String value = currentInput.trim();
        ColumnType type = access.getType(editingColIndex);
        boolean blanksAllowed = access.allowsBlanks(editingColIndex);

        return switch (type) {
            case STRING -> !value.isEmpty() || blanksAllowed;
            case EMAIL -> (value.chars().filter(c -> c == '@').count() == 1) || (value.isEmpty() && blanksAllowed);
            case INTEGER -> {
                if (value.isEmpty())
                    yield blanksAllowed;
                try {
                    int parsed = Integer.parseInt(value);
                    yield Integer.toString(parsed).equals(value);
                } catch (NumberFormatException e) {
                    yield false;
                }
            }
            case BOOLEAN -> value.equals("true") || value.equals("false") || (value.isEmpty() && blanksAllowed);
        };
    }
}