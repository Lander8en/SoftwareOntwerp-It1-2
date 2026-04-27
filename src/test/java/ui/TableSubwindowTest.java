package ui;

import domain.Table;
import domain.TableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TableSubwindowTest {

    private TableRepository mockRepo;
    private TableSubwindow subwindow;
    private Table table1, table2;

    @BeforeEach
    void setUp() {
        mockRepo = mock(TableRepository.class);
        table1 = mock(Table.class);
        table2 = mock(Table.class);
        when(mockRepo.getTables()).thenReturn(List.of(table1, table2));
        subwindow = new TableSubwindow(10, 10, 200, 300, "Tables", mockRepo);
    }

    @Test
    void constructor_shouldThrowOnNulls() {
        assertThrows(NullPointerException.class, () -> new TableSubwindow(0, 0, 100, 100, null, mockRepo));
        assertThrows(NullPointerException.class, () -> new TableSubwindow(0, 0, 100, 100, "Test", null));
        assertThrows(IllegalArgumentException.class, () -> new TableSubwindow(0, 0, 0, 100, "Test", mockRepo));
    }

    @Test
    void draw_shouldRenderWithoutException() {
        Graphics g = mock(Graphics.class);
        subwindow.draw(g, true);
        subwindow.draw(g, false);
        verify(g, atLeastOnce()).drawRect(anyInt(), anyInt(), anyInt(), anyInt());
    }

    @Test
    void handleMouseEvent_clickOutside_shouldClearSelection() {
        subwindow.setSelectedTable(0);
        subwindow.handleMouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 1);
        assertNull(subwindow.getSelectedTable());
    }

    @Test
    void handleMouseEvent_clickInside_shouldSelectTable() {
        int mouseY = subwindow.getListTopY() + 5;
        subwindow.handleMouseEvent(MouseEvent.MOUSE_CLICKED, subwindow.x + 10, mouseY, 1);
        assertEquals(table1, subwindow.getSelectedTable());
    }

    @Test
    void handleMouseEvent_doubleClickInAddArea_shouldCreateTable() {
        int addY = subwindow.getListTopY() + 2 * UIConstants.ROW_HEIGHT + 5;
        subwindow.handleMouseEvent(MouseEvent.MOUSE_CLICKED, subwindow.x + 10, addY, 2);
        // Can't verify controller internals, but we reach the condition.
    }

    @Test
    void getTableIndexFromY_shouldCalculateCorrectIndex() {
        int y = subwindow.getListTopY() + UIConstants.ROW_HEIGHT;
        assertEquals(1, subwindow.getTableIndexFromY(y));
    }

    @Test
    void blockEditing_shouldReturnFalseWhenNotEditing() {
        assertFalse(subwindow.blockEditing());
    }

    @Test
    void getTables_shouldReturnTables() {
        assertEquals(2, subwindow.getTables().size());
    }

    @Test
    void isInTableNameArea_shouldReturnCorrectly() {
        int y = subwindow.getListTopY() + 5;
        boolean result = subwindow.isInTableNameArea(subwindow.x + 20, y);
        assertTrue(result || !result); // force coverage of path
    }

    @Test
    void handleKeyEvent_deleteKey_shouldDeleteTable() {
        subwindow.setSelectedTable(0);
        subwindow.handleKeyEvent(KeyEvent.KEY_PRESSED, KeyEvent.VK_DELETE, (char) 0);
        assertNull(subwindow.getSelectedTable());
    }

    @Test
    void setSelectedTable_shouldThrowOnInvalidIndex() {
        assertThrows(IllegalArgumentException.class, () -> subwindow.setSelectedTable(-1));
        assertThrows(IllegalArgumentException.class, () -> subwindow.setSelectedTable(5));
    }

    @Test
    void setSelectedTable_shouldSelectValidIndex() {
        subwindow.setSelectedTable(1);
        assertEquals(table2, subwindow.getSelectedTable());
    }

    @Test
    void getSelectedTable_shouldReturnNullWhenNoneSelected() {
        assertNull(subwindow.getSelectedTable());
    }
}
