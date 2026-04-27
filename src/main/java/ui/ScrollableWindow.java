package ui;

import java.awt.Rectangle;

public interface ScrollableWindow {
    ScrollablePanel getScrollPanel();
    Rectangle getViewport();
}
