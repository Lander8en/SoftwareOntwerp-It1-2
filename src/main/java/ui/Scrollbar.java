package ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import static ui.UIConstants.SCROLLBAR_MIN_THUMB_SIZE;


public class Scrollbar {
    
    private final boolean vertical;
    private int position;
    private int thumbPosition;
    private int thumbSize;
    private boolean enabled;
    private boolean pressed;
    
    public Scrollbar(boolean vertical) {
        this.vertical = vertical;
    }
    
    public void update(int contentSize, int viewportSize) {
        this.enabled = contentSize > viewportSize || position > 40;
        if (enabled) {
            this.thumbSize = Math.max(SCROLLBAR_MIN_THUMB_SIZE, 
                viewportSize * viewportSize / contentSize);
        }
    }
    
    public void setPosition(int position) {
        this.position = position;
    }
    
    public void draw(Graphics g, Rectangle bounds, boolean active) {
        if (!enabled) {
            drawDisabled(g, bounds, active);
            return;
        }
        
        // Draw track
        g.setColor(Color.lightGray);
        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        
        // Draw thumb
        g.setColor(Color.GRAY);
        
        if (vertical) {
            g.fillRect(bounds.x + 2, bounds.y + thumbPosition, bounds.width - 4, thumbSize);
        } else {
            g.fillRect(bounds.x + thumbPosition, bounds.y + 2, thumbSize, bounds.height - 3);
        }
        
        // Draw border
        g.setColor(Color.GRAY);
        g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
    
    private void drawDisabled(Graphics g, Rectangle bounds, boolean active) {
        g.setColor(active ? new Color(240, 240, 240) : new Color(250, 250, 250));
        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        g.setColor(Color.LIGHT_GRAY);
        g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
    
    public boolean contains(int x, int y, Rectangle bounds) {
        return bounds.contains(x, y);
    }

    public boolean isVertical() { return vertical; }
    public int getPosition() { return position; }
    public int getThumbPosition() { return thumbPosition; }
    public int getThumbSize() { return thumbSize; }
    public boolean isEnabled() { return enabled; }
    public boolean isPressed() { return pressed; }
    
    // Setters
    public void setThumbPosition(int thumbPosition) { 
        this.thumbPosition = thumbPosition; 
    }
    
    public void setPressed(boolean pressed) {
        this.pressed = pressed;
    }
    
}
