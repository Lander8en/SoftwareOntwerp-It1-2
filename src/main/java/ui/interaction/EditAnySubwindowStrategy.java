package ui.interaction;

import ui.Subwindow;
import ui.SubwindowManager;

/**
 * Generic strategy for delegating mouse interactions to any editable subwindow.
 */
public class EditAnySubwindowStrategy implements MouseInteractionStrategy {

    @Override
    public boolean wantsToHandle(Subwindow window, int x, int y, int clickCount) {
        return window.isInEditingZone(x, y);
    }

    @Override
    public void handle(SubwindowManager manager, Subwindow window, int id, int x, int y, int clickCount) {
        window.handleMouseEvent(id, x, y, clickCount);
    }
}