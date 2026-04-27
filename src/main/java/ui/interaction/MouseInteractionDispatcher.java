package ui.interaction;

import ui.Subwindow;
import ui.SubwindowManager;

import java.util.List;

/**
 * Handles delegation of mouse events to the appropriate interaction strategy.
 */
public class MouseInteractionDispatcher {

    private final List<MouseInteractionStrategy> strategies;

    public MouseInteractionDispatcher(List<MouseInteractionStrategy> strategies) {
        if (strategies == null || strategies.isEmpty()) {
            throw new IllegalArgumentException("Strategies must not be null or empty");
        }
        this.strategies = strategies;
    }

    /**
     * Delegates the mouse event to the first strategy that wants to handle it.
     *
     * @param manager the subwindow manager
     * @param target  the subwindow under the mouse
     * @param id      the mouse event ID
     * @param x       the x-coordinate of the mouse
     * @param y       the y-coordinate of the mouse
     * @param clicks  the number of clicks (e.g. 1 for single-click)
     */
    public void dispatchMouseEvent(SubwindowManager manager, Subwindow target,
                                    int id, int x, int y, int clicks) {
        for (MouseInteractionStrategy strategy : strategies) {
            if (strategy.wantsToHandle(target, x, y, clicks)) {
                strategy.handle(manager, target, id, x, y, clicks);
                return;
            }
        }
    }
}