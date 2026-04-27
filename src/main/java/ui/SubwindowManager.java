package ui;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import domain.Table;
import ui.interaction.CloseButtonStrategy;
import ui.interaction.DragStrategy;
import ui.interaction.EditAnySubwindowStrategy;
import ui.interaction.MouseInteractionDispatcher;
import ui.interaction.OpenTableStrategy;
import ui.interaction.ResizeStrategy;
import ui.interaction.ScrollInteractionStrategy;
public class SubwindowManager {

    private final List<Subwindow> subwindows = new ArrayList<>();
    private final SubwindowFactory factory;
    private final MouseInteractionDispatcher mouseDispatcher;
    private final Map<KeyCombo, Runnable> keyCommands = new HashMap<>();
    private boolean ctrlDown = false;

    /**
     * Initializes a SubwindowManager with a default SubwindowFactory
     * and sets up key command mappings.
     */
    public SubwindowManager() {
        this.factory = new SubwindowFactory();

        mouseDispatcher = new MouseInteractionDispatcher(List.of(
            new CloseButtonStrategy(),
            new ResizeStrategy(),
            new DragStrategy(),
            new OpenTableStrategy(),
            new EditAnySubwindowStrategy(),
            new ScrollInteractionStrategy()
        ));

        keyCommands.put(new KeyCombo(KeyEvent.VK_T, true), this::addNewTableSubwindow);
        keyCommands.put(new KeyCombo(KeyEvent.VK_ENTER, true), this::handleCtrlEnter);
    }

    private void handleCtrlEnter() {
        if (subwindows.isEmpty()) return;
    
        Subwindow active = subwindows.get(subwindows.size() - 1);
    
        if (active instanceof TableDesignSubwindow designWindow) {
            Table table = designWindow.getTable();
            addNewTableRowsSubwindow(table);
        } else if (active instanceof TableRowsSubwindow rowsWindow) {
            Table table = rowsWindow.getTable();
            addNewTableDesignSubwindow(table);
        }
    }

    /**
     * Adds a new TableSubwindow to the manager.
     */
    public void addNewTableSubwindow() {
        TableSubwindow newWindow = factory.createNewTableSubwindow();
        subwindows.add(newWindow);
    }

    /**
     * Adds a new TableDesignSubwindow for the specified table.
     * 
     * @param table the table whose design should be displayed
     * @throws IllegalArgumentException if the table is null
     */
    public void addNewTableDesignSubwindow(Table table) {
        if (table == null) {
            throw new IllegalArgumentException("Table cannot be null when creating a design subwindow.");
        }
        TableDesignSubwindow newWindow = factory.createNewTableDesignSubwindow(table);
        subwindows.add(newWindow);
    }

    /**
     * Adds a new TableRowsSubwindow based on the currently selected table,
     * if the current subwindow is a TableSubwindow and a table is selected.
     */
    public void addNewTableRowsSubwindow(Table table) {
        if (table == null) {
            throw new IllegalArgumentException("Table cannot be null when creating a table rows subwindow.");
        }
        TableRowsSubwindow newWindow = factory.createNewTableRowsSubwindow(table);
        subwindows.add(newWindow);
    }

    /**
     * Draws all managed subwindows in order.
     * The last window in the list is considered the active one.
     * 
     * @param g the Graphics context to draw with
     */
    public void drawAll(Graphics g) {
        for (int i = 0; i < subwindows.size(); i++) {
            Subwindow w = subwindows.get(i);
            boolean isActive = (i == subwindows.size() - 1);
            w.draw(g, isActive);
        }
    }

    /**
     * Handles key input events and delegates them to the topmost subwindow.
     * Also processes key commands mapped to Ctrl-combinations.
     */
    public void handleKeyEvent(int id, int keyCode, char keyChar) {
        updateCtrl(id, keyCode);

        if (id == KeyEvent.KEY_PRESSED) {
            Runnable command = keyCommands.get(new KeyCombo(keyCode, ctrlDown));

            Subwindow currentWindow = null;
            if (!subwindows.isEmpty()) {
                currentWindow = subwindows.get(subwindows.size() - 1);
            }

            if (command != null) {
                command.run();
            }

            if (currentWindow != null) {
                currentWindow.handleKeyEvent(id, keyCode, keyChar);
            }
        }
    }

    /**
     * Tracks whether the control key is pressed.
     */
    private void updateCtrl(int id, int keyCode) {
        if (keyCode == KeyEvent.VK_CONTROL) {
            switch (id) {
                case KeyEvent.KEY_PRESSED -> ctrlDown = true;
                case KeyEvent.KEY_RELEASED -> ctrlDown = false;
            }
        }
    }

    /**
     * Handles mouse events by delegating to an appropriate interaction strategy.
     */
    public void handleMouseEvent(int id, int x, int y, int clickCount) {
        Subwindow target = findSubwindowAt(x, y);
        if (target == null) return;

        bringToFront(target);

        mouseDispatcher.dispatchMouseEvent(this, target, id, x, y, clickCount);
    }

    /**
     * Moves a subwindow to the front (end of the list).
     * 
     * @param subwindow the subwindow to bring to front
     */
    public void bringToFront(Subwindow subwindow) {
        if (subwindow == null) return;
        if (subwindows.remove(subwindow)) {
            subwindows.add(subwindow);
        }
    }

    /**
     * Finds the topmost subwindow at a given screen coordinate.
     * 
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the topmost Subwindow at that location, or null if none
     */
    public Subwindow findSubwindowAt(int x, int y) {
        for (int i = subwindows.size() - 1; i >= 0; i--) {
            Subwindow w = subwindows.get(i);
            if (w.contains(x, y)) {
                return w;
            }
        }
        return null;
    }

    /**
     * Removes the given subwindow from the list of managed subwindows.
     * 
     * @param w the subwindow to remove
     */
    public void closeSubwindow(Subwindow w) {
        if (w != null) {
            subwindows.remove(w);
        }
    }
}
