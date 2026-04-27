package ui.interaction;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import ui.ScrollablePanel;
import ui.ScrollableWindow;
import ui.Scrollbar;
import ui.Subwindow;
import ui.SubwindowManager;
import static ui.UIConstants.SCROLLBAR_SIZE;


public class ScrollInteractionStrategy implements MouseInteractionStrategy {
    @Override
    public boolean wantsToHandle(Subwindow window, int x, int y, int clickCount) {
        if (!(window instanceof ScrollableWindow)) return false;
        
        ScrollableWindow scrollWindow = (ScrollableWindow)window;
        Rectangle viewport = scrollWindow.getViewport();
        
        // Check if mouse is over scrollbars
        System.out.println(scrollWindow.getScrollPanel().getVerticalScrollbar().contains(x, y, 
        new Rectangle(
            viewport.x + viewport.width - SCROLLBAR_SIZE,
            viewport.y,
            SCROLLBAR_SIZE,
            viewport.height
        ))
    || scrollWindow.getScrollPanel().getHorizontalScrollbar().contains(x, y,
        new Rectangle(
            viewport.x,
            viewport.y + viewport.height - SCROLLBAR_SIZE,
            viewport.width,
            SCROLLBAR_SIZE
        )));
        return scrollWindow.getScrollPanel().getVerticalScrollbar().contains(x, y, 
                new Rectangle(
                    viewport.x + viewport.width - SCROLLBAR_SIZE,
                    viewport.y,
                    SCROLLBAR_SIZE,
                    viewport.height
                ))
            || scrollWindow.getScrollPanel().getHorizontalScrollbar().contains(x, y,
                new Rectangle(
                    viewport.x,
                    viewport.y + viewport.height - SCROLLBAR_SIZE,
                    viewport.width,
                    SCROLLBAR_SIZE
                ));
    }

    @Override
    public void handle(SubwindowManager manager, Subwindow window, 
                     int id, int x, int y, int clickCount) {
        ScrollableWindow scrollWindow = (ScrollableWindow)window;
        ScrollablePanel scrollPanel = scrollWindow.getScrollPanel();
        Rectangle viewport = scrollWindow.getViewport();
        
        // Calculate coordinates relative to viewport
        int relX = x - viewport.x;
        int relY = y - viewport.y;
        
        Scrollbar vertical = scrollPanel.getVerticalScrollbar();
        Scrollbar horizontal = scrollPanel.getHorizontalScrollbar();
        
        // Check which scrollbar is being interacted with
        boolean verticalHit = vertical.isEnabled() && 
            vertical.contains(relX, relY, new Rectangle(
                viewport.width - SCROLLBAR_SIZE,
                0,
                SCROLLBAR_SIZE,
                viewport.height - (horizontal.isEnabled() ? SCROLLBAR_SIZE : 0)
            ));
        
        boolean horizontalHit = horizontal.isEnabled() && 
            horizontal.contains(relX, relY, new Rectangle(
                0,
                viewport.height - SCROLLBAR_SIZE,
                viewport.width - (vertical.isEnabled() ? SCROLLBAR_SIZE : 0),
                SCROLLBAR_SIZE)
            );
        
        switch (id) {
            case MouseEvent.MOUSE_PRESSED:
                if (verticalHit) {
                    vertical.setPressed(true);
                    // Calculate new scroll position based on click location
                    int thumbPos = vertical.getThumbPosition();
                    if (relY < thumbPos) {
                        // Clicked above thumb - page up
                        scrollPanel.setScrollY(scrollPanel.getScrollY() - viewport.height);
                    } else if (relY > thumbPos + vertical.getThumbSize()) {
                        // Clicked below thumb - page down
                        scrollPanel.setScrollY(scrollPanel.getScrollY() + viewport.height);
                    } else {
                        // Clicked on thumb - prepare for drag
                        vertical.setPressed(true);
                        // Store initial drag offset
                        vertical.setPosition(relY - thumbPos);
                    }
                } else if (horizontalHit) {
                    horizontal.setPressed(true);
                    // Similar logic for horizontal scroll
                    int thumbPos = horizontal.getThumbPosition();
                    if (relX < thumbPos) {
                        // Clicked left of thumb - page left
                        scrollPanel.setScrollX(scrollPanel.getScrollX() - viewport.width);
                    } else if (relX > thumbPos + horizontal.getThumbSize()) {
                        // Clicked right of thumb - page right
                        scrollPanel.setScrollX(scrollPanel.getScrollX() + viewport.width);
                    } else {
                        // Clicked on thumb - prepare for drag
                        horizontal.setPressed(true);
                        // Store initial drag offset
                        horizontal.setPosition(relX - thumbPos);
                    }
                }
                break;
                
            case MouseEvent.MOUSE_DRAGGED:
                if (vertical.isPressed()) {
                    // Calculate new scroll position based on drag
                    int trackHeight = viewport.height - vertical.getThumbSize();
                    int contentHeight = scrollPanel.getContentHeight();
                    int viewportHeight = viewport.height;
                    
                    float ratio = (float)(relY - vertical.getPosition()) / trackHeight;
                    int newScrollY = (int)(ratio * (contentHeight - viewportHeight));
                    scrollPanel.setScrollY(newScrollY);
                } else if (horizontal.isPressed()) {
                    // Similar logic for horizontal drag
                    int trackWidth = viewport.width - horizontal.getThumbSize();
                    int contentWidth = scrollPanel.getContentWidth();
                    int viewportWidth = viewport.width;
                    
                    float ratio = (float)(relX - horizontal.getPosition()) / trackWidth;
                    int newScrollX = (int)(ratio * (contentWidth - viewportWidth));
                    scrollPanel.setScrollX(newScrollX);
                }
                break;
                
            case MouseEvent.MOUSE_RELEASED:
                // Reset scrollbar states
                vertical.setPressed(false);
                horizontal.setPressed(false);
                break;
        }
        
        // Redraw the window to show scrollbar state changes
        manager.bringToFront(window);
    }
}