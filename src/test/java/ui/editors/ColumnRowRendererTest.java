package ui.editors;

import domain.Column;
import domain.ColumnType;
import ui.controllers.ColumnsController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.mockito.Mockito.*;

public class ColumnRowRendererTest {

    private NameEditor<Column> mockNameEditor;
    private ColumnDefaultValueEditor mockDefaultValueEditor;
    private ColumnsController mockController;
    private Column mockColumn;
    private Graphics mockGraphics;
    private ColumnRowRenderer renderer;

    @BeforeEach
    void setUp() {
        mockNameEditor = mock(NameEditor.class); // Don't delete, this crashes the test
        mockDefaultValueEditor = mock(ColumnDefaultValueEditor.class);
        mockController = mock(ColumnsController.class);
        mockColumn = mock(Column.class);
        mockGraphics = mock(Graphics.class);

        renderer = new ColumnRowRenderer(mockNameEditor, mockDefaultValueEditor, mockController);
    }

    @Test
    void drawRow_DrawsSelectedRowBackground() {
        when(mockColumn.getType()).thenReturn(ColumnType.STRING);
        when(mockColumn.isBlanksAllowed()).thenReturn(true);
        when(mockDefaultValueEditor.isEditing()).thenReturn(false);

        renderer.drawRow(mockGraphics, mockColumn, 10, 20, true);

        verify(mockGraphics).setColor(Color.LIGHT_GRAY);
        verify(mockGraphics).fillRect(10, 20, 200, 20);
    }

    @Test
    void drawRow_DelegatesNameDrawing() {
        when(mockColumn.getType()).thenReturn(ColumnType.STRING);
        when(mockColumn.isBlanksAllowed()).thenReturn(true);
        when(mockDefaultValueEditor.isEditing()).thenReturn(false);

        renderer.drawRow(mockGraphics, mockColumn, 0, 0, false);

        verify(mockNameEditor).drawName(mockGraphics, mockColumn, 0, 0, 200, 20);
    }

    @Test
    void drawRow_DrawsTypeText() {
        when(mockColumn.getType()).thenReturn(ColumnType.EMAIL);
        when(mockColumn.isBlanksAllowed()).thenReturn(true);
        when(mockDefaultValueEditor.isEditing()).thenReturn(false);

        renderer.drawRow(mockGraphics, mockColumn, 0, 0, false);

        verify(mockGraphics).drawString("[EMAIL]", 215, 15);
    }

    @Test
    void drawRow_DrawsBlanksAllowedCheckbox_Unchecked() {
        when(mockColumn.getType()).thenReturn(ColumnType.STRING);
        when(mockColumn.isBlanksAllowed()).thenReturn(false);
        when(mockColumn.getDefaultValue()).thenReturn("someValue"); // prevent NullPointerException
        when(mockDefaultValueEditor.isEditing()).thenReturn(false);

        renderer.drawRow(mockGraphics, mockColumn, 0, 0, false);

        verify(mockGraphics).fillRect(215 + 100, 3, 14, 14);
        verify(mockGraphics).drawRect(215 + 100, 3, 14, 14);
    }

    @Test
    void drawRow_HighlightsCheckboxIfBlankViolation() {
        when(mockColumn.getType()).thenReturn(ColumnType.STRING);
        when(mockColumn.getDefaultValue()).thenReturn("   "); // triggers blank violation
        when(mockColumn.isBlanksAllowed()).thenReturn(false);
        when(mockDefaultValueEditor.isEditing()).thenReturn(false);
        when(mockDefaultValueEditor.isValid(mockColumn)).thenReturn(true); // prevent red default box

        renderer.drawRow(mockGraphics, mockColumn, 0, 0, false);

        verify(mockGraphics, atLeastOnce()).setColor(Color.RED); // red highlight
        verify(mockGraphics).drawRect(314, 2, 16, 16); // corrected coordinates
    }

    @Test
    void drawRow_DelegatesDefaultValueEditor_WhenEditing() {
        when(mockColumn.getType()).thenReturn(ColumnType.STRING);
        when(mockDefaultValueEditor.isEditing()).thenReturn(true);
        when(mockDefaultValueEditor.getEditingTarget()).thenReturn(mockColumn);
        when(mockColumn.isBlanksAllowed()).thenReturn(true);

        renderer.drawRow(mockGraphics, mockColumn, 0, 0, false);

        verify(mockDefaultValueEditor).drawValue(mockGraphics, mockColumn, 365, 0);
    }

    @Test
    void drawRow_DrawsBooleanDefaultValue() {
        when(mockColumn.getType()).thenReturn(ColumnType.BOOLEAN);
        when(mockColumn.getDefaultValue()).thenReturn("true");
        when(mockColumn.isBlanksAllowed()).thenReturn(true);
        when(mockDefaultValueEditor.isEditing()).thenReturn(false);

        renderer.drawRow(mockGraphics, mockColumn, 0, 0, false);

        verify(mockGraphics).drawLine(370, 10, 373, 13);
        verify(mockGraphics).drawLine(373, 13, 378, 7);
    }

    @Test
    void drawRow_HighlightsInvalidDefaultValue() {
        when(mockColumn.getType()).thenReturn(ColumnType.STRING);
        when(mockColumn.getDefaultValue()).thenReturn("invalid");
        when(mockDefaultValueEditor.isEditing()).thenReturn(false);
        when(mockDefaultValueEditor.isValid(mockColumn)).thenReturn(false);
        when(mockColumn.isBlanksAllowed()).thenReturn(true);

        renderer.drawRow(mockGraphics, mockColumn, 0, 0, false);

        verify(mockGraphics).setColor(Color.RED);
        verify(mockGraphics).drawRect(364, 0, 100, 19);
    }

    @Test
    void drawRow_MarksTypeBlockedColumn() {
        when(mockColumn.getType()).thenReturn(ColumnType.STRING);
        when(mockColumn.isBlanksAllowed()).thenReturn(true);
        when(mockDefaultValueEditor.isTypeBlocked(mockColumn)).thenReturn(true);
        when(mockDefaultValueEditor.isEditing()).thenReturn(false);
        when(mockDefaultValueEditor.isValid(mockColumn)).thenReturn(true); // prevents 2nd red color

        renderer.drawRow(mockGraphics, mockColumn, 0, 0, false);

        verify(mockGraphics).setColor(Color.RED);
        verify(mockGraphics).drawRect(213, 0, 80, 18);
    }

}
