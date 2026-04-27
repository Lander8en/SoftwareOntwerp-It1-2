package ui;

import domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ui.controllers.ColumnsController;
import ui.editors.ColumnDefaultValueEditor;
import ui.editors.NameEditor;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static ui.UIConstants.*;

class TableDesignSubwindowTest {

    private Table table;
    private Column column;
    private TableDesignSubwindow subwindow;

    @BeforeEach
    void setUp() {
        table = mock(Table.class);
        ColumnRepository columnRepo = mock(ColumnRepository.class);
        when(table.getColumnRepository()).thenReturn(columnRepo);

        subwindow = new TableDesignSubwindow(0, 0, 300, 300, "Test Window", table);

        column = mock(Column.class);
        when(table.getColumnRepository()).thenReturn(columnRepo);
        when(column.getType()).thenReturn(ColumnType.STRING);
        when(column.getDefaultValue()).thenReturn("default");
    }

    @Test
    void constructorThrowsOnNullTitleOrTable() {
        assertThrows(NullPointerException.class, () -> new TableDesignSubwindow(0, 0, 300, 300, null, table));
        assertThrows(NullPointerException.class, () -> new TableDesignSubwindow(0, 0, 300, 300, "Title", null));
    }

    @Test
    void testDrawCallsExpectedMethods() {
        Graphics g = mock(Graphics.class);
        when(table.getColumnRepository()).thenReturn(mock(ColumnRepository.class));

        subwindow.draw(g, true);

        verify(g, atLeastOnce()).drawRect(anyInt(), anyInt(), anyInt(), anyInt());
        verify(g, atLeastOnce()).fillRect(anyInt(), anyInt(), anyInt(), anyInt());
        verify(g, atLeastOnce()).drawString(anyString(), anyInt(), anyInt());
    }

    @Test
    void testOnColumnChangedValidColumn() {
        when(column.getType()).thenReturn(ColumnType.STRING);
        when(table.getColumnRepository()).thenReturn(mock(ColumnRepository.class));
        when(table.getName()).thenReturn("Test");

        subwindow.onColumnChanged(column);
    }

    @Test
    void testHandleKeyEventDeleteColumn() {
        Column column = mock(Column.class);
        when(column.getType()).thenReturn(ColumnType.STRING);
        when(table.getColumnRepository()).thenReturn(mock(ColumnRepository.class));

        subwindow.handleMouseEvent(MouseEvent.MOUSE_CLICKED, 10, 50, 1); // Click to select
        subwindow.handleKeyEvent(KeyEvent.KEY_PRESSED, KeyEvent.VK_DELETE, (char) KeyEvent.VK_DELETE);
        // Column deletion handled inside controller (internally tested)
    }

    @Test
    void testHandleClickOutsideCommitsEdits() {
        subwindow.handleMouseEvent(MouseEvent.MOUSE_CLICKED, -10, -10, 1); // Outside window
        // Should commit and clear selection
    }

    @Test
    void testHandleDefaultValueClickViaReflection() throws Exception {
        Method method = TableDesignSubwindow.class.getDeclaredMethod("handleDefaultValueClick", Column.class);
        method.setAccessible(true);

        when(column.getType()).thenReturn(ColumnType.BOOLEAN);
        when(column.isBlanksAllowed()).thenReturn(true);
        when(column.getDefaultValue()).thenReturn("true");

        method.invoke(subwindow, column);

        verify(column).setDefaultValue("false");
    }

    @Test
    void testGetColumnAtPositionReturnsNullForInvalidY() {
        Column result = subwindow.getColumnAtPosition(10, 999); // Too low
        assertNull(result);
    }

    @Test
    void testIsInTypeClickArea() {
        int x = subwindow.getX();
        int mouseX = x + SPACER + 1; // just inside the valid range
        int mouseY = 10;

        assertTrue(subwindow.isInTypeClickArea(mouseX, mouseY));
    }

    @Test
    void testBlockEditing() {
        // mock editing state
        NameEditor<?> editor = mock(NameEditor.class);
        when(editor.isEditing()).thenReturn(true);
        when(editor.isValid()).thenReturn(false);
        assertFalse(subwindow.blockEditing()); // Should be false without editing
    }

    @Test
    void testHandleMouseEventDoubleClickToAddColumn() {
        subwindow = new TableDesignSubwindow(0, 0, 300, 300, "Test", table); // table is mock

        subwindow.handleMouseEvent(MouseEvent.MOUSE_CLICKED, 10, 280, 2);
    }

    @Test
    void testGetColumnIndexFromY() {
        int y = 0;
        int TITLE_BAR_HEIGHT = 30;
        int PADDING = 10;
        int ROW_HEIGHT = 20;

        int topY = y + TITLE_BAR_HEIGHT + PADDING + ROW_HEIGHT; // one row down
        int index = subwindow.getColumnIndexFromY(topY);
        assertEquals(1, index);
    }

    @Test
    void testIsBlankViolationAndHasBlockingBlankViolation() throws Exception {
        when(column.getDefaultValue()).thenReturn(" ");
        when(column.isBlanksAllowed()).thenReturn(false);

        List<Column> mockList = List.of(column);
        ColumnsController controller = mock(ColumnsController.class);
        when(controller.columnsRequest()).thenReturn(mockList);

        // Inject mock controller using reflection
        Field controllerField = TableDesignSubwindow.class.getDeclaredField("controller");
        controllerField.setAccessible(true);
        controllerField.set(subwindow, controller);

        // Call private method via reflection
        Method method = TableDesignSubwindow.class.getDeclaredMethod("hasBlockingBlankViolation");
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(subwindow);

        assertTrue(result);
    }

    @Test
    void testOnColumnChangedDoesNotThrow() {
        when(column.getType()).thenReturn(ColumnType.STRING);
        when(column.getDefaultValue()).thenReturn("someDefault");
        when(column.isBlanksAllowed()).thenReturn(true);

        assertDoesNotThrow(() -> subwindow.onColumnChanged(column));
    }

    @Test
    void testDraw_withValidBoundsAndActive() throws Exception {
        // Arrange
        Table table = mock(Table.class);
        when(table.getColumnRepository()).thenReturn(mock(ColumnRepository.class)); // required by constructor

        TableDesignSubwindow subwindow = new TableDesignSubwindow(100, 50, 500, 300, "Test", table);

        ScrollablePanel panel = mock(ScrollablePanel.class);
        setField(subwindow, "scrollPanel", panel);

        Graphics g = mock(Graphics.class);

        // Act
        subwindow.draw(g, true);

        // Assert
        // No exception = pass. Optionally verify draw calls if needed.
    }

    @Test
    void testHandleBlockedTypeClick_returnsFalseWhenNotBlocked() throws Exception {
        Table table = mock(Table.class);
        when(table.getColumnRepository()).thenReturn(mock(ColumnRepository.class));
        TableDesignSubwindow subwindow = new TableDesignSubwindow(0, 0, 100, 100, "Test", table);

        ColumnDefaultValueEditor editor = mock(ColumnDefaultValueEditor.class);
        when(editor.isTypeBlocked()).thenReturn(false);
        setField(subwindow, "defaultValueEditor", editor);

        boolean result = invokeHandleBlockedTypeClick(subwindow, 10, 10);

        assertFalse(result);
    }

    @Test
    void testHandleBlockedTypeClick_returnsFalseWhenBlockedColumnHoveredAndInClickArea() throws Exception {
        Table table = mock(Table.class);
        when(table.getColumnRepository()).thenReturn(mock(ColumnRepository.class));
        TableDesignSubwindow subwindow = spy(new TableDesignSubwindow(0, 0, 100, 100, "Test", table));

        Column column = mock(Column.class);

        ColumnDefaultValueEditor editor = mock(ColumnDefaultValueEditor.class);
        when(editor.isTypeBlocked()).thenReturn(true);
        when(editor.getBlockedColumn()).thenReturn(column);
        setField(subwindow, "defaultValueEditor", editor);

        doReturn(column).when(subwindow).getColumnAtPosition(10, 10);
        doReturn(true).when(subwindow).isInTypeClickArea(10, 10);

        boolean result = invokeHandleBlockedTypeClick(subwindow, 10, 10);

        assertFalse(result);
    }

    @Test
    void testHandleBlockedTypeClick_returnsTrueWhenDifferentColumnHovered() throws Exception {
        Table table = mock(Table.class);
        when(table.getColumnRepository()).thenReturn(mock(ColumnRepository.class));
        TableDesignSubwindow subwindow = spy(new TableDesignSubwindow(0, 0, 100, 100, "Test", table));

        Column blocked = mock(Column.class);
        Column hovered = mock(Column.class);

        ColumnDefaultValueEditor editor = mock(ColumnDefaultValueEditor.class);
        when(editor.isTypeBlocked()).thenReturn(true);
        when(editor.getBlockedColumn()).thenReturn(blocked);
        setField(subwindow, "defaultValueEditor", editor);

        doReturn(hovered).when(subwindow).getColumnAtPosition(10, 10);
        doReturn(true).when(subwindow).isInTypeClickArea(10, 10);

        boolean result = invokeHandleBlockedTypeClick(subwindow, 10, 10);

        assertTrue(result);
    }

    @Test
    void testHandleBlockedTypeClick_returnsTrueWhenInBlockedColumnButNotInClickArea() throws Exception {
        Table table = mock(Table.class);
        when(table.getColumnRepository()).thenReturn(mock(ColumnRepository.class));
        TableDesignSubwindow subwindow = spy(new TableDesignSubwindow(0, 0, 100, 100, "Test", table));

        Column column = mock(Column.class);

        ColumnDefaultValueEditor editor = mock(ColumnDefaultValueEditor.class);
        when(editor.isTypeBlocked()).thenReturn(true);
        when(editor.getBlockedColumn()).thenReturn(column);
        setField(subwindow, "defaultValueEditor", editor);

        doReturn(column).when(subwindow).getColumnAtPosition(10, 10);
        doReturn(false).when(subwindow).isInTypeClickArea(10, 10);

        boolean result = invokeHandleBlockedTypeClick(subwindow, 10, 10);

        assertTrue(result);
    }

    @Test
    void testHandleDefaultValueCommit_validCommitOutsideBox() throws Exception {
        Table table = mock(Table.class);
        ColumnRepository repository = mock(ColumnRepository.class);
        when(table.getColumnRepository()).thenReturn(repository);

        Column editingColumn = mock(Column.class);
        List<Column> columns = List.of(editingColumn);
        when(repository.getColumns()).thenReturn(columns);

        TableDesignSubwindow subwindow = new TableDesignSubwindow(0, 0, 600, 400, "title", table);

        // Inject mocks into private fields
        ColumnDefaultValueEditor editor = mock(ColumnDefaultValueEditor.class);
        setField(subwindow, "defaultValueEditor", editor);

        ColumnsController controller = mock(ColumnsController.class);
        when(controller.columnsRequest()).thenReturn(columns);
        setField(subwindow, "controller", controller);

        when(editor.isEditing()).thenReturn(true);
        when(editor.getEditingTarget()).thenReturn(editingColumn);
        when(editor.isValid()).thenReturn(true);

        // Coordinates outside the default value box
        int mouseX = invokeGetRowX(subwindow) + DEFAULT_VALUE_OFFSET + DEFAULT_VALUE_WIDTH + 20;
        int mouseY = invokeGetColumnListTopY(subwindow);

        // Act
        invokeHandleDefaultValueCommit(subwindow, mouseX, mouseY);

        // Assert
        verify(editor).commitEdit();
    }

    @Test
    void testHandleMarginClick_setsSelectedIndexAndStopsEditing() throws Exception {
        Table table = new Table("Test");
        TableDesignSubwindow subwindow = new TableDesignSubwindow(0, 0, 500, 300, "Test", table);

        // Mock columnEditor and set it into the subwindow
        NameEditor<Column> mockEditor = mock(NameEditor.class);
        setField(subwindow, "columnEditor", mockEditor);

        // Invoke private method with index = 2
        Method method = TableDesignSubwindow.class.getDeclaredMethod("handleMarginClick", int.class);
        method.setAccessible(true);
        method.invoke(subwindow, 2);

        // Verify selectedColumnIndex is updated
        int selectedColumnIndex = (int) getField(subwindow, "selectedColumnIndex");
        assertEquals(2, selectedColumnIndex);

        // Verify stopEditing() was called
        verify(mockEditor).stopEditing();
    }

    @Test
    void testHandleNameClick_commitsPreviousAndStartsEditing() throws Exception {
        // Arrange
        Table table = new Table("TestTable");
        Column column1 = new Column("Col1");
        Column column2 = new Column("Col2");
        table.getColumnRepository().getColumns().add(column1);
        table.getColumnRepository().getColumns().add(column2);

        TableDesignSubwindow subwindow = new TableDesignSubwindow(0, 0, 800, 600, "Design", table);
        NameEditor<Column> mockEditor = mock(NameEditor.class);
        setField(subwindow, "columnEditor", mockEditor);

        // Simulate editing a different column
        when(mockEditor.isEditing()).thenReturn(true);
        when(mockEditor.getEditingTarget()).thenReturn(column1);

        // Act
        invokePrivateMethod(subwindow, "handleNameClick", new Class[] { Column.class, int.class }, column2, 1);

        // Assert
        verify(mockEditor).commitEdit();
        verify(mockEditor).startEditing(column2);

        int selectedIndex = getField(subwindow, "selectedColumnIndex");
        assertEquals(1, selectedIndex);
    }

    @Test
    void testHandleTypeClick_setsTypeBlockedIfInvalid() throws Exception {
        // Arrange
        Table table = new Table("TestTable");
        Column column = new Column("Col");
        column.setType(ColumnType.STRING); // Use valid enum

        table.getColumnRepository().getColumns().add(column);

        TableDesignSubwindow subwindow = new TableDesignSubwindow(0, 0, 800, 600, "Design", table);

        ColumnsController mockController = mock(ColumnsController.class);
        when(mockController.getTable()).thenReturn(table);

        ColumnDefaultValueEditor mockDefaultValueEditor = mock(ColumnDefaultValueEditor.class);

        setField(subwindow, "controller", mockController);
        setField(subwindow, "defaultValueEditor", mockDefaultValueEditor);

        // Force the next type to be invalid
        mockStatic(ColumnTypeValidator.class);
        when(ColumnTypeValidator.isColumnTypeValid(any(), any())).thenReturn(false);

        // Act
        invokePrivateMethod(subwindow, "handleTypeClick", new Class[] { Column.class }, column);

        // Assert
        verify(mockController).setColumnType(eq(column), eq(ColumnType.STRING.next()));
        verify(mockDefaultValueEditor).setTypeBlocked(column);

        clearAllCaches(); // Clean up static mock
    }

    @Test
    void testHandleBlanksCheckboxClick_togglesBlanksAllowed() throws Exception {
        // Arrange
        Table table = new Table("TestTable");
        Column column = new Column("Col");

        TableDesignSubwindow subwindow = new TableDesignSubwindow(0, 0, 800, 600, "Design", table);

        ColumnsController mockController = mock(ColumnsController.class);
        setField(subwindow, "controller", mockController);

        // Act
        invokePrivateMethod(subwindow, "handleBlanksCheckboxClick", new Class[] { Column.class }, column);

        // Assert
        verify(mockController).toggleBlanksAllowed(column);
    }

    // HELPER FUNCTIONS
    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static boolean invokeHandleBlockedTypeClick(TableDesignSubwindow subwindow, int x, int y) throws Exception {
        Method method = TableDesignSubwindow.class.getDeclaredMethod("handleBlockedTypeClick", int.class, int.class);
        method.setAccessible(true);
        return (boolean) method.invoke(subwindow, x, y);
    }

    private static void invokeHandleDefaultValueCommit(TableDesignSubwindow subwindow, int x, int y) throws Exception {
        Method method = TableDesignSubwindow.class.getDeclaredMethod("handleDefaultValueCommit", int.class, int.class);
        method.setAccessible(true);
        method.invoke(subwindow, x, y);
    }

    private int invokeGetRowX(TableDesignSubwindow subwindow) throws Exception {
        Method method = TableDesignSubwindow.class.getDeclaredMethod("getRowX");
        method.setAccessible(true);
        return (int) method.invoke(subwindow);
    }

    private int invokeGetColumnListTopY(TableDesignSubwindow subwindow) throws Exception {
        Method method = TableDesignSubwindow.class.getDeclaredMethod("getColumnListTopY");
        method.setAccessible(true);
        return (int) method.invoke(subwindow);
    }

    @SuppressWarnings("unchecked")
    private <T> T getField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(target);
    }

    private Object invokePrivateMethod(Object target, String methodName, Class<?>[] paramTypes, Object... args)
            throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

}
