package ui;

import java.awt.Graphics;

import static ui.UIConstants.CLOSE_BUTTON_SIZE;
import static ui.UIConstants.MARGIN_WIDTH;
import static ui.UIConstants.PADDING;
import static ui.UIConstants.ROW_HEIGHT;
import static ui.UIConstants.SCROLLBAR_SIZE;
import static ui.UIConstants.TITLE_BAR_HEIGHT;

/**
 * Abstract base class for all types of subwindows in the UI.
 * Provides common layout logic, boundary checks, and resizing behavior.
 */
public abstract class Subwindow {

    protected int x, y, width, height;
    protected final String title;

    /**
     * Creates a new subwindow with given bounds and title.
     *
     * @param x      the x-coordinate of the top-left corner
     * @param y      the y-coordinate of the top-left corner
     * @param width  the width of the subwindow (must be positive)
     * @param height the height of the subwindow (must be positive)
     * @param title  the title to display in the title bar (must not be null)
     * @throws IllegalArgumentException if any dimension is non-positive or title is
     *                                  null
     */
    public Subwindow(int x, int y, int width, int height, String title) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Subwindow dimensions must be positive.");
        }
        if (title == null) {
            throw new IllegalArgumentException("Subwindow title cannot be null.");
        }

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.title = title;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setPosition(int newX, int newY) {
        this.x = newX;
        this.y = newY;
    }

    public boolean contains(int mx, int my) {
        return isInside(mx, my, x, y, width, height);
    }

    public boolean isInTitleBar(int mx, int my) {
        return isInside(mx, my, x, y, width, TITLE_BAR_HEIGHT);
    }

    public boolean isInEditingZone(int mx, int my) { // MOET NOG AANGEPAST WORDEN
        if (this instanceof ScrollableWindow window) {
            if (window.getScrollPanel().getHorizontalScrollbar().isEnabled()
                    && window.getScrollPanel().getVerticalScrollbar().isEnabled()) {
                return isInside(mx, my, x, y + TITLE_BAR_HEIGHT + 1, width - SCROLLBAR_SIZE,
                        height - TITLE_BAR_HEIGHT - SCROLLBAR_SIZE);
            } else if (window.getScrollPanel().getHorizontalScrollbar().isEnabled()) {
                return isInside(mx, my, x, y + TITLE_BAR_HEIGHT + 1, width, height - TITLE_BAR_HEIGHT - SCROLLBAR_SIZE);
            } else {
                return isInside(mx, my, x, y + TITLE_BAR_HEIGHT + 1, width - SCROLLBAR_SIZE, height - TITLE_BAR_HEIGHT);
            }
        }
        return isInside(mx, my, x, y + TITLE_BAR_HEIGHT + 1, width, height - TITLE_BAR_HEIGHT);
    }

    public boolean isInCloseButton(int mx, int my) {
        int closeX = x + width - CLOSE_BUTTON_SIZE - PADDING;
        int closeY = y + PADDING;
        return isInside(mx, my, closeX, closeY, CLOSE_BUTTON_SIZE, CLOSE_BUTTON_SIZE);
    }

    public boolean isInSelectionMargin(int mouseX, int rowX) {
        return isInside(mouseX, 0, rowX, 0, MARGIN_WIDTH, Integer.MAX_VALUE);
    }

    public boolean isInScrollBar(int mouseX, int rowX) {
        return isInside(mouseX, 0, rowX, 0, MARGIN_WIDTH, Integer.MAX_VALUE);
    }

    public boolean isOnScrollBar(int mouseX, int rowX) {
        return isInside(mouseX, 0, rowX, 0, MARGIN_WIDTH, Integer.MAX_VALUE);
    }

    public int getListTopY() {
        return y + TITLE_BAR_HEIGHT + PADDING;
    }

    public int getRowHeight() {
        return ROW_HEIGHT;
    }

    /**
     * Returns which resize zone a point lies in (if any).
     */
    public ResizeZone getResizeZone(int mx, int my) {
        int border = 5;
        boolean left = mx >= x && mx <= x + border;
        boolean right = mx >= x + width - border && mx <= x + width;
        boolean top = my >= y && my <= y + border;
        boolean bottom = my >= y + height - border && my <= y + height;

        if (top && left)
            return ResizeZone.TOP_LEFT;
        if (top && right)
            return ResizeZone.TOP_RIGHT;
        if (bottom && left)
            return ResizeZone.BOTTOM_LEFT;
        if (bottom && right)
            return ResizeZone.BOTTOM_RIGHT;
        if (top)
            return ResizeZone.TOP;
        if (bottom)
            return ResizeZone.BOTTOM;
        if (left)
            return ResizeZone.LEFT;
        if (right)
            return ResizeZone.RIGHT;

        return ResizeZone.NONE;
    }

    /**
     * Applies a resize to the subwindow based on the given zone and delta values.
     */

    public void applyResize(ResizeZone zone, int dx, int dy, int minWidth, int minHeight) {
        int newX = x;
        int newY = y;
        int newWidth = width;
        int newHeight = height;

        switch (zone) {
            case RIGHT -> newWidth += dx;
            case BOTTOM -> newHeight += dy;
            case LEFT -> {
                newX += dx;
                newWidth -= dx;
            }
            case TOP -> {
                newY += dy;
                newHeight -= dy;
            }
            case TOP_LEFT -> {
                newX += dx;
                newWidth -= dx;
                newY += dy;
                newHeight -= dy;
            }
            case TOP_RIGHT -> {
                newWidth += dx;
                newY += dy;
                newHeight -= dy;
            }
            case BOTTOM_LEFT -> {
                newX += dx;
                newWidth -= dx;
                newHeight += dy;
            }
            case BOTTOM_RIGHT -> {
                newWidth += dx;
                newHeight += dy;
            }
            case NONE -> {
            }
        }

        if (newWidth >= minWidth && newHeight >= minHeight) {
            this.x = newX;
            this.y = newY;
            this.width = newWidth;
            this.height = newHeight;
        }
    }

    /**
     * Returns whether a point (mx, my) lies within a rectangle at (rx, ry) of given
     * size.
     */
    protected boolean isInside(int mx, int my, int rx, int ry, int rWidth, int rHeight) {
        return mx >= rx && mx <= rx + rWidth && my >= ry && my <= ry + rHeight;
    }

    /**
     * Draws this subwindow.
     * 
     * @param g        the graphics context
     * @param isActive true if this subwindow is active (focused)
     */
    public abstract void draw(Graphics g, boolean isActive);

    /**
     * Handles a key event sent to this subwindow.
     */
    public abstract void handleKeyEvent(int id, int keyCode, char keyChar);

    /**
     * Enum representing the 9 zones that can be used to resize a subwindow.
     */
    public enum ResizeZone {
        NONE, LEFT, RIGHT, TOP, BOTTOM, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }

    public abstract void handleMouseEvent(int id, int x, int y, int clickCount);
}
