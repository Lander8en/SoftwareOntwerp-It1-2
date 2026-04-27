package ui.interaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ui.Subwindow;
import ui.SubwindowManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MouseInteractionDispatcherTest {

    private MouseInteractionStrategy strategy1;
    private MouseInteractionStrategy strategy2;
    private SubwindowManager mockManager;
    private Subwindow mockWindow;

    @BeforeEach
    void setUp() {
        strategy1 = mock(MouseInteractionStrategy.class);
        strategy2 = mock(MouseInteractionStrategy.class);
        mockManager = mock(SubwindowManager.class);
        mockWindow = mock(Subwindow.class);
    }

    @Test
    void constructor_ThrowsException_WhenStrategiesNull() {
        assertThrows(IllegalArgumentException.class, () -> new MouseInteractionDispatcher(null));
    }

    @Test
    void constructor_ThrowsException_WhenStrategiesEmpty() {
        assertThrows(IllegalArgumentException.class, () -> new MouseInteractionDispatcher(List.of()));
    }

    @Test
    void dispatchMouseEvent_DelegatesToFirstMatchingStrategy() {
        when(strategy1.wantsToHandle(mockWindow, 10, 10, 1)).thenReturn(false);
        when(strategy2.wantsToHandle(mockWindow, 10, 10, 1)).thenReturn(true);

        MouseInteractionDispatcher dispatcher = new MouseInteractionDispatcher(List.of(strategy1, strategy2));

        dispatcher.dispatchMouseEvent(mockManager, mockWindow, 123, 10, 10, 1);

        verify(strategy1).wantsToHandle(mockWindow, 10, 10, 1);
        verify(strategy2).wantsToHandle(mockWindow, 10, 10, 1);
        verify(strategy2).handle(mockManager, mockWindow, 123, 10, 10, 1);
        verify(strategy1, never()).handle(any(), any(), anyInt(), anyInt(), anyInt(), anyInt());
    }

    @Test
    void dispatchMouseEvent_DoesNotCallHandle_IfNoStrategyWantsToHandle() {
        when(strategy1.wantsToHandle(mockWindow, 10, 10, 1)).thenReturn(false);
        when(strategy2.wantsToHandle(mockWindow, 10, 10, 1)).thenReturn(false);

        MouseInteractionDispatcher dispatcher = new MouseInteractionDispatcher(List.of(strategy1, strategy2));
        dispatcher.dispatchMouseEvent(mockManager, mockWindow, 123, 10, 10, 1);

        verify(strategy1, never()).handle(any(), any(), anyInt(), anyInt(), anyInt(), anyInt());
        verify(strategy2, never()).handle(any(), any(), anyInt(), anyInt(), anyInt(), anyInt());
    }

    @Test
    void dispatchMouseEvent_StopsAtFirstHandler() {
        MouseInteractionStrategy handler = mock(MouseInteractionStrategy.class);
        MouseInteractionStrategy neverChecked = mock(MouseInteractionStrategy.class);

        when(handler.wantsToHandle(any(), anyInt(), anyInt(), anyInt())).thenReturn(true);

        MouseInteractionDispatcher dispatcher = new MouseInteractionDispatcher(List.of(handler, neverChecked));
        dispatcher.dispatchMouseEvent(mockManager, mockWindow, 1, 20, 30, 1);

        verify(handler).handle(mockManager, mockWindow, 1, 20, 30, 1);
        verifyNoInteractions(neverChecked);
    }
}
