package ui.interaction;

import java.awt.event.MouseEvent;

import domain.Table;
import ui.Subwindow;
import ui.SubwindowManager;
import ui.TableSubwindow;

/**
 * A {@link MouseInteractionStrategy} that opens a new TableDesignSubwindow
 * when the user double-clicks on a table name in a {@link TableSubwindow}.
 */
public class OpenTableStrategy implements MouseInteractionStrategy {

    /**
     * Determines whether this strategy wants to handle the given mouse interaction.
     *
     * @param window     the subwindow under the cursor
     * @param x          the x-coordinate of the mouse
     * @param y          the y-coordinate of the mouse
     * @param clickCount the number of mouse clicks
     * @return true if the event is a double-click in the table name area of a TableSubwindow
     */
    @Override
    public boolean wantsToHandle(Subwindow window, int x, int y, int clickCount) {
        System.out.println(window instanceof TableSubwindow);

        return (window instanceof TableSubwindow tableWindow)
                && tableWindow.isInTableNameArea(x, y)
                && !tableWindow.blockEditing()
                && clickCount == 2;
    }

    /**
     * Handles the mouse event to open a new TableDesignSubwindow if the user
     * double-clicked a valid table row.
     *
     * @param manager    the subwindow manager
     * @param window     the subwindow under the mouse
     * @param id         the type of mouse event (e.g. MOUSE_CLICKED)
     * @param x          the x-coordinate of the mouse
     * @param y          the y-coordinate of the mouse
     * @param clickCount the number of clicks
     */
    @Override
    public void handle(SubwindowManager manager, Subwindow window, int id, int x, int y, int clickCount) {
        if (!(window instanceof TableSubwindow tableWindow)) return;
        if (id != MouseEvent.MOUSE_CLICKED) return;

        int index = tableWindow.getTableIndexFromY(y);
        System.out.println(index);
        if (index < 0 || index >= tableWindow.getTables().size()) return;

        tableWindow.setSelectedTable(index);
        Table clickedTable = tableWindow.getTables().get(index);

        if (clickedTable.getColumns().isEmpty()) {
            manager.addNewTableDesignSubwindow(clickedTable);
        } else {
            manager.addNewTableRowsSubwindow(clickedTable);
        }
    }
}