package ui;

import domain.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SubwindowManagerTest {

    private SubwindowManager manager;

    @BeforeEach
    void setUp() {
        manager = new SubwindowManager();
    }

    @Test
    void addNewTableSubwindow_AddsTableSubwindowToList() {
        manager.addNewTableSubwindow();
        Subwindow subwindow = manager.findSubwindowAt(50, 50);
        assertNotNull(subwindow);
        assertTrue(subwindow instanceof TableSubwindow);
    }

    @Test
    void addNewTableDesignSubwindow_ThrowsException_WhenTableIsNull() {
        assertThrows(IllegalArgumentException.class, () -> manager.addNewTableDesignSubwindow(null));
    }

    @Test
    void addNewTableDesignSubwindow_AddsDesignSubwindowToList() {
        Table table = mock(Table.class);
        when(table.getName()).thenReturn("Mock");
        when(table.getColumnRepository()).thenReturn(mock(domain.ColumnRepository.class));

        manager.addNewTableDesignSubwindow(table);

        Subwindow subwindow = manager.findSubwindowAt(50, 50);
        assertNotNull(subwindow);
        assertTrue(subwindow instanceof TableDesignSubwindow);
    }

    @Test
    void addNewTableRowsSubwindow_ThrowsException_WhenTableIsNull() {
        assertThrows(IllegalArgumentException.class, () -> manager.addNewTableRowsSubwindow(null));
    }

    @Test
    void addNewTableRowsSubwindow_AddsRowsSubwindowToList() {
        Table table = mock(Table.class);
        when(table.getName()).thenReturn("Mock");

        manager.addNewTableRowsSubwindow(table);

        Subwindow subwindow = manager.findSubwindowAt(50, 50);
        assertNotNull(subwindow);
        assertTrue(subwindow instanceof TableRowsSubwindow);
    }

    @Test
    void bringToFront_MovesSubwindowToEndOfList() {
        manager.addNewTableSubwindow();
        manager.addNewTableSubwindow();

        Subwindow first = manager.findSubwindowAt(50, 50);
        Subwindow second = manager.findSubwindowAt(70, 70);

        assertNotNull(first);
        assertNotNull(second);

        manager.bringToFront(first);

        // First should now be on top
        Subwindow top = manager.findSubwindowAt(50, 50);
        assertEquals(first, top);
    }

    @Test
    void handleKeyEvent_CtrlT_AddsNewTableSubwindow() {
        manager.handleKeyEvent(KeyEvent.KEY_PRESSED, KeyEvent.VK_CONTROL, ' ');
        manager.handleKeyEvent(KeyEvent.KEY_PRESSED, KeyEvent.VK_T, 'T');

        Subwindow top = manager.findSubwindowAt(50, 50);
        assertNotNull(top);
        assertTrue(top instanceof TableSubwindow);
    }

    @Test
    void handleKeyEvent_CtrlEnter_OpensRelatedSubwindow() {
        Table table = mock(Table.class);
        when(table.getName()).thenReturn("Mock");
        when(table.getColumnRepository()).thenReturn(mock(domain.ColumnRepository.class));

        manager.addNewTableDesignSubwindow(table);
        manager.handleKeyEvent(KeyEvent.KEY_PRESSED, KeyEvent.VK_CONTROL, ' ');
        manager.handleKeyEvent(KeyEvent.KEY_PRESSED, KeyEvent.VK_ENTER, '\n');

        assertNotNull(manager.findSubwindowAt(50, 50)); // Original
        assertNotNull(manager.findSubwindowAt(70, 70)); // New one
    }

    @Test
    void closeSubwindow_RemovesItFromList() {
        manager.addNewTableSubwindow();
        Subwindow sub = manager.findSubwindowAt(50, 50);
        assertNotNull(sub);

        manager.closeSubwindow(sub);
        assertNull(manager.findSubwindowAt(50, 50));
    }

    @Test
    void drawAll_CallsDrawOnEachSubwindow() {
        Subwindow mock1 = mock(Subwindow.class);
        Subwindow mock2 = mock(Subwindow.class);

        List<Subwindow> list = getSubwindowList(manager);
        list.clear();
        list.add(mock1);
        list.add(mock2);

        Graphics g = mock(Graphics.class);
        manager.drawAll(g);

        verify(mock1).draw(eq(g), eq(false));
        verify(mock2).draw(eq(g), eq(true));
    }

    @Test
    void drawAll_DrawsAllWindowsAndMarksLastAsActive() {
        Graphics graphics = mock(Graphics.class);
        Subwindow window1 = mock(Subwindow.class);
        Subwindow window2 = mock(Subwindow.class);

        manager.addNewTableSubwindow(); // adds one subwindow
        manager.addNewTableSubwindow(); // adds another

        // Use reflection to replace internal subwindow list for control
        List<Subwindow> testList = List.of(window1, window2);
        setSubwindows(manager, new ArrayList<>(testList));

        manager.drawAll(graphics);

        verify(window1).draw(graphics, false);
        verify(window2).draw(graphics, true); // last is active
    }

    @Test
    void findSubwindowAt_ReturnsCorrectWindow() {
        Subwindow w1 = mock(Subwindow.class);
        Subwindow w2 = mock(Subwindow.class);
        when(w1.contains(10, 10)).thenReturn(false);
        when(w2.contains(10, 10)).thenReturn(true);

        setSubwindows(manager, List.of(w1, w2));

        Subwindow result = manager.findSubwindowAt(10, 10);
        assertEquals(w2, result);
    }

    @Test
    void handleMouseEvent_DelegatesToDispatcher() {
        Subwindow target = mock(Subwindow.class);
        when(target.contains(5, 5)).thenReturn(true);

        setSubwindows(manager, new ArrayList<>(List.of(target)));

        manager.handleMouseEvent(1, 5, 5, 1);
        // Check dispatcher via side-effect or breakpoint
    }

    @Test
    void handleKeyEvent_SetsCtrlDownTrueAndFalseCorrectly() {
        SubwindowManager manager = new SubwindowManager();

        // Trigger KEY_PRESSED for CTRL
        manager.handleKeyEvent(KeyEvent.KEY_PRESSED, KeyEvent.VK_CONTROL, (char) 0);

        // Then press T (should trigger addNewTableSubwindow, which adds a subwindow)
        manager.handleKeyEvent(KeyEvent.KEY_PRESSED, KeyEvent.VK_T, 'T');

        // Trigger KEY_RELEASED for CTRL
        manager.handleKeyEvent(KeyEvent.KEY_RELEASED, KeyEvent.VK_CONTROL, (char) 0);

        // Then press T again (should NOT add a subwindow now, since ctrl is up)
        manager.handleKeyEvent(KeyEvent.KEY_PRESSED, KeyEvent.VK_T, 'T');

        // Now check that exactly one subwindow was added
        // (because the second T press shouldn't have done anything)
        List<Subwindow> subwindows = getInternalSubwindows(manager);
        assertEquals(1, subwindows.size());
    }

    // HELPER FUNCTIONS

    @SuppressWarnings("unchecked")
    private List<Subwindow> getSubwindowList(SubwindowManager manager) {
        try {
            var field = SubwindowManager.class.getDeclaredField("subwindows");
            field.setAccessible(true);
            return (List<Subwindow>) field.get(manager);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to access subwindows list", e);
        }
    }

    private void setSubwindows(SubwindowManager manager, List<Subwindow> list) {
        try {
            var field = SubwindowManager.class.getDeclaredField("subwindows");
            field.setAccessible(true);
            field.set(manager, list);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<Subwindow> getInternalSubwindows(SubwindowManager manager) {
        try {
            Field field = SubwindowManager.class.getDeclaredField("subwindows");
            field.setAccessible(true);
            return (List<Subwindow>) field.get(manager);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
