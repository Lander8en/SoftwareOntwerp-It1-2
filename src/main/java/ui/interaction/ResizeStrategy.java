package ui.interaction;

import java.awt.event.MouseEvent;
import ui.Subwindow;
import ui.Subwindow.ResizeZone;
import ui.SubwindowManager;

/**
 * A {@link MouseInteractionStrategy} that allows a user to resize a subwindow
 * by dragging its borders or corners.
 */
public class ResizeStrategy implements MouseInteractionStrategy {

    private Subwindow resizingSubwindow = null;
    private ResizeZone resizeZone = ResizeZone.NONE;
    private int resizeStartX, resizeStartY;

    private static final int MIN_WIDTH = 100;
    private static final int MIN_HEIGHT = 100;

    /**
     * Determines whether this strategy wants to handle the mouse interaction
     * at the specified coordinates.
     *
     * @param w          the subwindow under the mouse
     * @param x          the x-coordinate of the mouse
     * @param y          the y-coordinate of the mouse
     * @param clickCount the number of mouse clicks
     * @return true if the cursor is in a resize zone of the subwindow
     */
    @Override
    public boolean wantsToHandle(Subwindow w, int x, int y, int clickCount) {
        return w.getResizeZone(x, y) != ResizeZone.NONE;
    }

    /**
     * Handles the mouse event for resizing a subwindow.
     *
     * @param manager    the subwindow manager
     * @param window     the subwindow under the mouse
     * @param id         the event ID (e.g. mouse pressed, dragged, released)
     * @param x          the x-coordinate of the mouse
     * @param y          the y-coordinate of the mouse
     * @param clickCount the number of mouse clicks
     */
    @Override
    public void handle(SubwindowManager manager, Subwindow window, int id, int x, int y, int clickCount) {
        switch (id) {
            case MouseEvent.MOUSE_PRESSED -> {
                resizeZone = window.getResizeZone(x, y);
                if (resizeZone != ResizeZone.NONE) {
                    resizingSubwindow = window;
                    resizeStartX = x;
                    resizeStartY = y;
                    manager.bringToFront(window);
                }
            }
            case MouseEvent.MOUSE_DRAGGED -> {
                if (resizingSubwindow != null && resizeZone != ResizeZone.NONE) {
                    int dx = x - resizeStartX;
                    int dy = y - resizeStartY;

                    resizingSubwindow.applyResize(resizeZone, dx, dy, MIN_WIDTH, MIN_HEIGHT);

                    // It’s safe to update the drag start coordinates unconditionally.
                    resizeStartX = x;
                    resizeStartY = y;
                }
            }
            case MouseEvent.MOUSE_RELEASED -> {
                resizingSubwindow = null;
                resizeZone = ResizeZone.NONE;
            }
        }
    }
}