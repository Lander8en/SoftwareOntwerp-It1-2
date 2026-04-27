package ui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScrollbarTest {

    private Scrollbar verticalScrollbar;
    private Scrollbar horizontalScrollbar;
    private Graphics graphics;
    private Rectangle bounds;

    @BeforeEach
    void setUp() {
        verticalScrollbar = new Scrollbar(true);
        horizontalScrollbar = new Scrollbar(false);
        graphics = mock(Graphics.class);
        bounds = new Rectangle(0, 0, 100, 200);
    }

    @Test
    void testIsVertical() {
        assertTrue(verticalScrollbar.isVertical());
        assertFalse(horizontalScrollbar.isVertical());
    }

    @Test
    void testUpdate_whenContentSmallerThanViewport_scrollbarDisabled() {
        verticalScrollbar.update(100, 200);
        assertFalse(verticalScrollbar.isEnabled());
        assertEquals(0, verticalScrollbar.getThumbSize());
    }

    @Test
    void testUpdate_whenContentLargerThanViewport_scrollbarEnabledAndThumbSizeCalculated() {
        verticalScrollbar.update(400, 200);
        assertTrue(verticalScrollbar.isEnabled());
        int expectedSize = Math.max(UIConstants.SCROLLBAR_MIN_THUMB_SIZE, 200 * 200 / 400);
        assertEquals(expectedSize, verticalScrollbar.getThumbSize());
    }

    @Test
    void testSetAndGetPosition() {
        verticalScrollbar.setPosition(50);
        assertEquals(50, verticalScrollbar.getPosition());
    }

    @Test
    void testSetAndGetThumbPosition() {
        verticalScrollbar.setThumbPosition(42);
        assertEquals(42, verticalScrollbar.getThumbPosition());
    }

    // @Test
    // void testSetAndGetHovered() {
    // verticalScrollbar.setHovered(true);
    // assertTrue(verticalScrollbar.isHovered());
    // }

    @Test
    void testSetAndGetPressed() {
        verticalScrollbar.setPressed(true);
        assertTrue(verticalScrollbar.isPressed());
    }

    @Test
    void testContains_insideBounds() {
        Rectangle rect = new Rectangle(10, 10, 50, 50);
        assertTrue(verticalScrollbar.contains(30, 30, rect));
    }

    @Test
    void testContains_outsideBounds() {
        Rectangle rect = new Rectangle(10, 10, 50, 50);
        assertFalse(verticalScrollbar.contains(70, 30, rect));
    }

    @Test
    void testDraw_whenDisabled() {
        verticalScrollbar.update(100, 200); // disabled
        verticalScrollbar.draw(graphics, bounds, true);

        verify(graphics).setColor(new Color(240, 240, 240));
        verify(graphics).fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        verify(graphics).setColor(Color.LIGHT_GRAY);
        verify(graphics).drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    @Test
    void testDraw_whenEnabledAndVertical() {
        verticalScrollbar.update(400, 200); // enabled
        verticalScrollbar.setThumbPosition(20);
        verticalScrollbar.draw(graphics, bounds, true);

        verify(graphics).setColor(Color.lightGray);
        verify(graphics).fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        verify(graphics, atLeastOnce()).setColor(Color.GRAY);
        verify(graphics).fillRect(bounds.x + 2, bounds.y + 20, bounds.width - 4, verticalScrollbar.getThumbSize());
        verify(graphics).drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    // NAKIJKEN

    // @Test
    // void testDraw_whenEnabledAndHorizontal() {
    // horizontalScrollbar.update(400, 200); // enabled
    // horizontalScrollbar.setThumbPosition(30);
    // horizontalScrollbar.draw(graphics, bounds, false);

    // verify(graphics).setColor(Color.lightGray);
    // verify(graphics).fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
    // verify(graphics, atLeastOnce()).setColor(Color.GRAY);
    // verify(graphics).fillRect(bounds.x + 30, bounds.y,
    // horizontalScrollbar.getThumbSize(), bounds.height);
    // verify(graphics).drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
    // }
}
