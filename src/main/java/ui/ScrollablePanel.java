package ui;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;

import static ui.UIConstants.SCROLLBAR_SIZE;


public class ScrollablePanel {
    
    private final Scrollbar verticalScrollbar;
    private final Scrollbar horizontalScrollbar;
    private int scrollX;
    private int scrollY;
    private int contentWidth;
    private int contentHeight;
    private int viewportWidth;
    private int viewportHeight;
    
    public ScrollablePanel() {
        this.verticalScrollbar = new Scrollbar(true);
        this.horizontalScrollbar = new Scrollbar(false);
    }
    
    public void setViewportSize(int width, int height) {
        this.viewportWidth = width;
        this.viewportHeight = height;
        updateScrollbars();
    }
    
    public void setContentSize(int width, int height) {
        this.contentWidth =  width + (verticalScrollbar.isEnabled() ? SCROLLBAR_SIZE : 0);
        this.contentHeight = height + (horizontalScrollbar.isEnabled() ? SCROLLBAR_SIZE : 0);
        updateScrollbars();
    }
    
    private void updateScrollbars() {
        verticalScrollbar.update(contentHeight, viewportHeight);
        horizontalScrollbar.update(contentWidth, viewportWidth);
        
        if (verticalScrollbar.isEnabled()) {
            verticalScrollbar.setThumbPosition(
                scrollY * (viewportHeight - verticalScrollbar.getThumbSize()) / 
                Math.max(1, contentHeight - viewportHeight)
            );
        }
        
        if (horizontalScrollbar.isEnabled()) {
            horizontalScrollbar.setThumbPosition(
                scrollX * (viewportWidth - horizontalScrollbar.getThumbSize()) / 
                Math.max(1, contentWidth - viewportWidth) - (verticalScrollbar.isEnabled() ? SCROLLBAR_SIZE : 0)
            );
        }
    }
    
    public void applyScroll(Graphics g, Rectangle viewport) {
        Shape originalClip = g.getClip();
        g.setClip(viewport);
        g.translate(-scrollX, -scrollY);
    }
    
    public void resetScroll(Graphics g) {
        g.translate(scrollX, scrollY);
        g.setClip(null);
    }
    
    public void drawScrollbars(Graphics g, Rectangle viewport, boolean active) {
        if (verticalScrollbar.isEnabled()) {
            Rectangle vScrollBounds = new Rectangle(
                viewport.x + viewport.width - SCROLLBAR_SIZE,
                viewport.y,
                SCROLLBAR_SIZE,
                viewport.height
            );
            verticalScrollbar.draw(g, vScrollBounds, active);
        }
        
        if (horizontalScrollbar.isEnabled()) {
            Rectangle hScrollBounds = new Rectangle(
            viewport.x,
            viewport.y + viewport.height - SCROLLBAR_SIZE,
            viewport.width - (verticalScrollbar.isEnabled() ? SCROLLBAR_SIZE : 0),
                SCROLLBAR_SIZE
            );
            horizontalScrollbar.draw(g, hScrollBounds, active);  
        }
    }
    
    // Getters and setters
    public Scrollbar getVerticalScrollbar() { return verticalScrollbar; }
    public Scrollbar getHorizontalScrollbar() { return horizontalScrollbar; }
    public int getScrollX() { return scrollX; }
    public int getScrollY() { return scrollY; }
    public int getContentHeight(){ return contentHeight;}
    public int getContentWidth(){ return contentWidth;}

    public void setScrollX(int scrollX) { 
        this.scrollX = Math.max(0, Math.min(contentWidth - viewportWidth, scrollX));
        updateScrollbars();
    }
    
    public void setScrollY(int scrollY) { 
        this.scrollY = Math.max(0, Math.min(contentHeight - viewportHeight, scrollY));
        updateScrollbars();
    }
}