package ui.interaction;

import ui.Subwindow;
import ui.SubwindowManager;

public interface MouseInteractionStrategy {
    boolean wantsToHandle(Subwindow window, int x, int y, int clickCount);
    void handle(SubwindowManager manager, Subwindow window, int id, int x, int y, int clickCount);
}
