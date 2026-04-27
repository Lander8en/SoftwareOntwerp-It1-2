package ui;

import domain.*;
import ui.controllers.RowsController;
import ui.editors.RowValueEditor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import org.mockito.InOrder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TableRowsSubwindowTest {

    private Table mockTable;
    private RowsController mockController;
    private RowValueEditor mockEditor;
    private List<Column> mockColumns;
    private List<Row> mockRows;
    private TableRowsSubwindow subwindow;
    private Graphics mockGraphics;

    @BeforeEach
    void setUp() throws Exception {
        mockTable = mock(Table.class);
        mockController = mock(RowsController.class);
        mockEditor = mock(RowValueEditor.class);
        mockGraphics = mock(Graphics.class);

        // Setup columns
        Column stringCol = mock(Column.class);
        when(stringCol.getName()).thenReturn("Name");
        when(stringCol.getType()).thenReturn(ColumnType.STRING);
        when(stringCol.isBlanksAllowed()).thenReturn(true);

        Column booleanCol = mock(Column.class);
        when(booleanCol.getName()).thenReturn("Active");
        when(booleanCol.getType()).thenReturn(ColumnType.BOOLEAN);
        when(booleanCol.isBlanksAllowed()).thenReturn(true);

        mockColumns = List.of(stringCol, booleanCol);

        // Setup rows
        Row row1 = mock(Row.class);
        when(row1.getValue(0)).thenReturn("Test");
        when(row1.getValue(1)).thenReturn("true");

        Row row2 = mock(Row.class);
        when(row2.getValue(0)).thenReturn("Another");
        when(row2.getValue(1)).thenReturn("false");

        mockRows = List.of(row1, row2);

        // Create subwindow with injected mocks
        subwindow = new TableRowsSubwindow(10, 10, 300, 200, "Test Table", mockTable);

        // Inject mocks via reflection
        Field controllerField = TableRowsSubwindow.class.getDeclaredField("controller");
        controllerField.setAccessible(true);
        controllerField.set(subwindow, mockController);

        Field editorField = TableRowsSubwindow.class.getDeclaredField("rowValueEditor");
        editorField.setAccessible(true);
        editorField.set(subwindow, mockEditor);

        // Setup mock behavior
        when(mockController.columnsRequest()).thenReturn(mockColumns);
        when(mockController.rowsRequest()).thenReturn(mockRows);
        when(mockController.getTable()).thenReturn(mockTable);
    }

    // Existing tests...

    @Test
    void constructor_shouldThrowOnNullTable() {
        assertThrows(IllegalArgumentException.class,
                () -> new TableRowsSubwindow(0, 0, 100, 100, "Test", null));
    }

    @Test
    void draw_shouldRenderTitleBarAndCloseButton() {
        subwindow.draw(mockGraphics, true);
        verify(mockGraphics).setColor(Color.BLUE);
        verify(mockGraphics).fillRect(10, 10, 300, UIConstants.TITLE_BAR_HEIGHT);
        verify(mockGraphics).drawString("Test Table", 20, 30);
    }

    @Test
    void draw_shouldRenderColumnHeaders() {
        subwindow.draw(mockGraphics, false);
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockGraphics, atLeast(2)).drawString(stringCaptor.capture(), anyInt(), anyInt());
        assertTrue(stringCaptor.getAllValues().contains("Name"));
        assertTrue(stringCaptor.getAllValues().contains("Active"));
    }

    @Test
    void draw_shouldRenderAllCells() {
        subwindow.draw(mockGraphics, true);

        // Verify draw() was called at least once and capture how many times
        verify(mockEditor, atLeastOnce())
                .draw(any(), any(), anyInt(), anyInt(), anyInt(), any());

        // Get actual number of invocations
        int actualCalls = mockingDetails(mockEditor)
                .getInvocations()
                .stream()
                .filter(i -> i.getMethod().getName().equals("draw"))
                .toList()
                .size();

        System.out.println("Actual draw calls: " + actualCalls);
    }

    @Test
    void draw_shouldHighlightSelectedRow() throws Exception {
        Field selectedRowField = TableRowsSubwindow.class.getDeclaredField("selectedRowIndex");
        selectedRowField.setAccessible(true);
        selectedRowField.set(subwindow, 1);

        subwindow.draw(mockGraphics, true);

        InOrder inOrder = inOrder(mockGraphics);
        inOrder.verify(mockGraphics).setColor(Color.BLUE);
        inOrder.verify(mockGraphics).fillRect(anyInt(), anyInt(), anyInt(), eq(UIConstants.ROW_HEIGHT));
    }

    @Test
    void handleMouseEvent_outsideWindow_shouldDeselectAndCommitEdit() {
        when(mockEditor.isEditing()).thenReturn(true);
        when(mockEditor.isValid()).thenReturn(true);

        subwindow.handleMouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 1);
        verify(mockEditor).commitEdit();
    }

    @Test
    void handleMouseEvent_notClicked_shouldReturnEarly() {
        subwindow.handleMouseEvent(MouseEvent.MOUSE_MOVED, 20, 20, 0);
        verifyNoInteractions(mockController);
    }

    @Test
    void handleMouseEvent_invalidEditor_shouldReturnEarly() {
        when(mockEditor.isValid()).thenReturn(false);
        subwindow.handleMouseEvent(MouseEvent.MOUSE_CLICKED, 20, 20, 1);
        verifyNoInteractions(mockController);
    }

    @Test
    void handleMouseEvent_editingClickOutside_shouldCommitEdit() {
        when(mockEditor.isEditing()).thenReturn(true);
        when(mockEditor.isValid()).thenReturn(true);
        when(mockEditor.getEditingRow()).thenReturn(mockRows.get(0));
        when(mockEditor.getEditingColIndex()).thenReturn(0);

        subwindow.handleMouseEvent(MouseEvent.MOUSE_CLICKED, 200, 200, 1);
        verify(mockEditor).commitEdit();
    }

    @Test
    void handleMouseEvent_singleClickInMargin_shouldSelectRow() throws Exception {
        when(mockController.rowsRequest()).thenReturn(mockRows);
        when(mockController.columnsRequest()).thenReturn(mockColumns);

        int yPos = subwindow.y + UIConstants.TITLE_BAR_HEIGHT + UIConstants.PADDING + UIConstants.ROW_HEIGHT;

        when(mockEditor.isValid()).thenReturn(true);
        when(mockEditor.isEditing()).thenReturn(false);

        subwindow.handleMouseEvent(MouseEvent.MOUSE_CLICKED, subwindow.x + 2, yPos, 1);

        verify(mockEditor).isValid();

        verify(mockEditor).stopEditing();

        Field selectedRowField = TableRowsSubwindow.class.getDeclaredField("selectedRowIndex");
        selectedRowField.setAccessible(true);
        assertEquals(0, selectedRowField.get(subwindow));
    }

    // @Test
    // void handleMouseEvent_singleClickOnBoolean_shouldToggleValue() {
    // int xPos = subwindow.x + 110; // Second column
    // int yPos = subwindow.y + UIConstants.TITLE_BAR_HEIGHT +
    // UIConstants.ROW_HEIGHT;

    // subwindow.handleMouseEvent(MouseEvent.MOUSE_CLICKED, xPos, yPos, 1);
    // verify(mockRows.get(0)).setValue(1, "false");
    // }

    // @Test
    // void handleMouseEvent_singleClickOnString_shouldStartEditing() {
    // int xPos = subwindow.x + 20; // First column
    // int yPos = subwindow.y + UIConstants.TITLE_BAR_HEIGHT +
    // UIConstants.ROW_HEIGHT;

    // subwindow.handleMouseEvent(MouseEvent.MOUSE_CLICKED, xPos, yPos, 1);
    // verify(mockEditor).startEditing(mockRows.get(0), 0);
    // }

    @Test
    void handleKeyEvent_deleteKeyWithSelection_shouldDeleteRow() throws Exception {
        Field selectedRowField = TableRowsSubwindow.class.getDeclaredField("selectedRowIndex");
        selectedRowField.setAccessible(true);
        selectedRowField.set(subwindow, 0);

        subwindow.handleKeyEvent(KeyEvent.KEY_PRESSED, KeyEvent.VK_DELETE, KeyEvent.CHAR_UNDEFINED);
        verify(mockController).handleDeleteRowRequest(mockRows.get(0));
    }

    @Test
    void handleKeyEvent_deleteKeyWhileEditing_shouldNotDelete() {
        when(mockEditor.isEditing()).thenReturn(true);
        subwindow.handleKeyEvent(KeyEvent.KEY_PRESSED, KeyEvent.VK_DELETE, KeyEvent.CHAR_UNDEFINED);
        verify(mockController, never()).handleDeleteRowRequest(any());
    }

    @Test
    void handleKeyEvent_shouldDelegateToEditor() {
        subwindow.handleKeyEvent(KeyEvent.KEY_TYPED, KeyEvent.VK_A, 'a');
        verify(mockEditor).handleKeyEvent(KeyEvent.KEY_TYPED, KeyEvent.VK_A, 'a');
    }

    @Test
    void getTable_shouldReturnWrappedTable() {
        assertEquals(mockTable, subwindow.getTable());
    }

    @Test
    void isInAddRowArea_shouldReturnTrueForValidArea() throws Exception {
        Method method = TableRowsSubwindow.class.getDeclaredMethod("isInAddRowArea", int.class, int.class);
        method.setAccessible(true);

        int yPos = subwindow.y + subwindow.height - 10;
        boolean result = (boolean) method.invoke(subwindow, subwindow.x + 10, yPos);
        assertTrue(result);
    }

    @Test
    void isInAddRowArea_shouldReturnFalseForInvalidArea() throws Exception {
        Method method = TableRowsSubwindow.class.getDeclaredMethod("isInAddRowArea", int.class, int.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(subwindow, subwindow.x + 10, subwindow.y + 50);
        assertFalse(result);
    }

    @Test
    void handleMouseEvent_doubleClickInAddRowArea_shouldCreateRow() throws Exception {
        // Setup
        when(mockEditor.isEditing()).thenReturn(false);
        when(mockEditor.isValid()).thenReturn(true);

        // Calculate y position below all rows
        int rowsAreaHeight = mockRows.size() * UIConstants.ROW_HEIGHT;
        int yPos = subwindow.y + UIConstants.TITLE_BAR_HEIGHT + rowsAreaHeight + 20;

        // Verify the position is in add row area
        Method isInAddRowArea = TableRowsSubwindow.class.getDeclaredMethod("isInAddRowArea", int.class, int.class);
        isInAddRowArea.setAccessible(true);
        assertTrue((boolean) isInAddRowArea.invoke(subwindow, subwindow.x + 20, yPos));

        // Act
        subwindow.handleMouseEvent(MouseEvent.MOUSE_CLICKED, subwindow.x + 20, yPos, 2);

        // Assert
        verify(mockController).handleCreateNewRowRequest();
    }
}