package ui.editors;

import domain.Column;
import domain.ColumnType;
import ui.controllers.ColumnsController;
import java.awt.*;

import static ui.UIConstants.*;

public class ColumnRowRenderer {

    private final NameEditor<Column> columnEditor;
    private final ColumnDefaultValueEditor defaultValueEditor;
    private final ColumnsController controller;

    public ColumnRowRenderer(NameEditor<Column> columnEditor,
                             ColumnDefaultValueEditor defaultValueEditor,
                             ColumnsController controller) {
        this.columnEditor = columnEditor;
        this.defaultValueEditor = defaultValueEditor;
        this.controller = controller;
    }

    public void drawRow(Graphics g, Column column, int rowX, int rowY, boolean isSelected) {
        if (isSelected) {
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(rowX, rowY, NAME_AREA_WIDTH, ROW_HEIGHT);
        }

        // Draw column name
        columnEditor.drawName(g, column, rowX, rowY, NAME_AREA_WIDTH, ROW_HEIGHT);

        // Draw type
        g.setColor(Color.DARK_GRAY);
        g.drawString("[" + column.getType().name() + "]", rowX + SPACER, rowY + 15);

        if (defaultValueEditor.isTypeBlocked(column)) {
            g.setColor(Color.RED);
            g.drawRect(rowX + SPACER - 2, rowY, 80, ROW_HEIGHT - 2);
        }

        // Draw "blanks allowed" checkbox
        int checkboxX = rowX + SPACER + 100;
        int checkboxY = rowY + 3;
        int boxSize = 14;

        g.setColor(Color.WHITE);
        g.fillRect(checkboxX, checkboxY, boxSize, boxSize);
        g.setColor(Color.BLACK);
        g.drawRect(checkboxX, checkboxY, boxSize, boxSize);

        if (column.isBlanksAllowed()) {
            g.drawLine(checkboxX + 3, checkboxY + 7, checkboxX + 6, checkboxY + 10);
            g.drawLine(checkboxX + 6, checkboxY + 10, checkboxX + 11, checkboxY + 4);
        }

        if (isBlankViolation(column)) {
            g.setColor(Color.RED);
            g.drawRect(checkboxX - 1, checkboxY - 1, boxSize + 2, boxSize + 2);
        }

        // Draw default value
        if (defaultValueEditor.isEditing() && defaultValueEditor.getEditingTarget() == column && column.getType() != ColumnType.BOOLEAN) {
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(rowX + DEFAULT_VALUE_OFFSET, rowY, DEFAULT_VALUE_WIDTH, ROW_HEIGHT);
            defaultValueEditor.drawValue(g, column, rowX + DEFAULT_VALUE_OFFSET, rowY);
        } else {
            drawDefaultValue(g, column, rowX + DEFAULT_VALUE_OFFSET, rowY);
        }
    }

    private void drawDefaultValue(Graphics g, Column column, int x, int y) {
        String value = column.getDefaultValue();
        ColumnType type = column.getType();

        if (type == ColumnType.BOOLEAN) {
            boolean isBlank = value.isBlank();

            int boxSize = 14;
            int boxX = x + 2;
            int boxY = y + (ROW_HEIGHT - boxSize) / 2;

            if (isBlank) {
                g.setColor(Color.LIGHT_GRAY);
                g.drawRect(boxX, boxY, boxSize, boxSize);
            } else {
                g.setColor(Color.BLACK);
                g.drawRect(boxX, boxY, boxSize, boxSize);
                if ("true".equalsIgnoreCase(value.trim())) {
                    g.drawLine(boxX + 3, boxY + 7, boxX + 6, boxY + 10);
                    g.drawLine(boxX + 6, boxY + 10, boxX + 11, boxY + 4);
                }
            }
        } else {
            if (!defaultValueEditor.isValid(column)) {
                g.setColor(Color.RED);
            } else {
                g.setColor(Color.BLACK);
            }
            g.drawRect(x - 1, y, DEFAULT_VALUE_WIDTH, ROW_HEIGHT - 1);
            g.setColor(Color.BLACK);
            g.drawString(value, x + 2, y + 15);
        }
    }

    private boolean isBlankViolation(Column column) {
        return !column.isBlanksAllowed() && column.getDefaultValue().isBlank();
    }
}