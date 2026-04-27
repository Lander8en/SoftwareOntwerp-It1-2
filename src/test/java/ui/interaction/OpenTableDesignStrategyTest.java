package ui.interaction;

import domain.Column;
import domain.ColumnRepository;
import domain.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ui.SubwindowManager;
import ui.TableSubwindow;

import java.awt.event.MouseEvent;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OpenTableDesignStrategyTest {

    private OpenTableStrategy strategy;
    private TableSubwindow mockWindow;
    private SubwindowManager mockManager;

    @BeforeEach
    void setUp() {
        strategy = new OpenTableStrategy();
        mockWindow = mock(TableSubwindow.class);
        mockManager = mock(SubwindowManager.class);
    }

    @Test
    void wantsToHandle_ReturnsTrue_WhenInNameAreaAndNotBlocked() {
        when(mockWindow.isInTableNameArea(10, 20)).thenReturn(true);
        when(mockWindow.blockEditing()).thenReturn(false);
        assertTrue(strategy.wantsToHandle(mockWindow, 10, 20, 2)); // clickCount added
    }

    @Test
    void wantsToHandle_ReturnsFalse_WhenNotInNameArea() {
        when(mockWindow.isInTableNameArea(10, 20)).thenReturn(false);
        when(mockWindow.blockEditing()).thenReturn(false);
        assertFalse(strategy.wantsToHandle(mockWindow, 10, 20, 2)); // clickCount added
    }

    @Test
    void wantsToHandle_ReturnsFalse_WhenEditingBlocked() {
        when(mockWindow.isInTableNameArea(10, 20)).thenReturn(true);
        when(mockWindow.blockEditing()).thenReturn(true);
        assertFalse(strategy.wantsToHandle(mockWindow, 10, 20, 2)); // clickCount added
    }

    @Test
    void handle_DoesNothing_WhenClickNotDouble() {
        strategy.handle(mockManager, mockWindow, MouseEvent.MOUSE_CLICKED, 10, 50, 1);
        verify(mockManager, never()).addNewTableDesignSubwindow(any());
    }

    @Test
    void handle_DoesNothing_WhenNotMouseClicked() {
        strategy.handle(mockManager, mockWindow, MouseEvent.MOUSE_PRESSED, 10, 50, 2);
        verify(mockManager, never()).addNewTableDesignSubwindow(any());
    }

    @Test
    void handle_DoesNothing_WhenIndexInvalid() {
        when(mockWindow.getTableIndexFromY(50)).thenReturn(-1);
        strategy.handle(mockManager, mockWindow, MouseEvent.MOUSE_CLICKED, 10, 50, 2);
        verify(mockManager, never()).addNewTableDesignSubwindow(any());
    }

    @Test
    void handle_AddsTableDesignSubwindow_WhenConditionsMet() {
        Table table = mock(Table.class);
        ColumnRepository mockColumns = mock(ColumnRepository.class);

        when(mockWindow.getTableIndexFromY(50)).thenReturn(0);
        when(mockWindow.getTables()).thenReturn(List.of(table));
        when(table.getColumnRepository()).thenReturn(mockColumns);

        strategy.handle(mockManager, mockWindow, MouseEvent.MOUSE_CLICKED, 10, 50, 2);

        verify(mockManager).addNewTableDesignSubwindow(table);
    }

    @Test
    void handle_DoesNothing_WhenIndexOutOfBounds() {
        when(mockWindow.getTableIndexFromY(50)).thenReturn(2);
        when(mockWindow.getTables()).thenReturn(List.of()); // empty list

        strategy.handle(mockManager, mockWindow, MouseEvent.MOUSE_CLICKED, 10, 50, 2);
        verify(mockManager, never()).addNewTableDesignSubwindow(any());
    }

    @Test
    void handle_AddsTableRowsSubwindow_WhenTableHasColumns() {
        Table table = mock(Table.class);

        // Return a non-empty list of columns directly from getColumns() on the Table
        when(table.getColumns()).thenReturn(List.of(new Column("col1")));

        when(mockWindow.getTableIndexFromY(50)).thenReturn(0);
        when(mockWindow.getTables()).thenReturn(List.of(table));

        strategy.handle(mockManager, mockWindow, MouseEvent.MOUSE_CLICKED, 10, 50, 2);

        verify(mockManager).addNewTableRowsSubwindow(table);
        verify(mockManager, never()).addNewTableDesignSubwindow(any());
    }

}
