package ui.editors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ui.UIConstants.DEFAULT_VALUE_WIDTH;
import static ui.UIConstants.ROW_HEIGHT;

import java.awt.*;
import java.awt.event.KeyEvent;

import domain.Column;
import domain.ColumnType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ColumnDefaultValueEditorTest {

    private ColumnDefaultValueAccess mockValueAccess;
    private Column mockColumn;
    private Graphics mockGraphics;
    private ColumnDefaultValueEditor editor;

    @BeforeEach
    void setUp() {
        mockValueAccess = mock(ColumnDefaultValueAccess.class);
        mockColumn = mock(Column.class);
        mockGraphics = mock(Graphics.class);
        editor = new ColumnDefaultValueEditor(mockValueAccess);
    }

    @Test
    void startEditing_SetsUpEditingState() {
        when(mockValueAccess.getValue(mockColumn)).thenReturn("initial");

        editor.startEditing(mockColumn);

        assertTrue(editor.isEditing());
        assertEquals(mockColumn, editor.getEditingTarget());
    }

    @Test
    void handleKeyEvent_EnterCommitsValidEdit() {
        when(mockValueAccess.getValue(mockColumn)).thenReturn("");
        editor.startEditing(mockColumn);
        when(mockColumn.getType()).thenReturn(ColumnType.STRING);
        when(mockColumn.isBlanksAllowed()).thenReturn(true);

        typeCharacters("test");
        editor.handleKeyEvent(KeyEvent.KEY_PRESSED, KeyEvent.VK_ENTER, '\0');

        verify(mockValueAccess).setValue(mockColumn, "test");
        assertFalse(editor.isEditing());
    }

    @Test
    void handleKeyEvent_EscapeCancelsEditing() {
        editor.startEditing(mockColumn);
        editor.handleKeyEvent(KeyEvent.KEY_PRESSED, KeyEvent.VK_ESCAPE, '\0');

        assertFalse(editor.isEditing());
        assertNull(editor.getEditingTarget());
    }

    @Test
    void handleKeyEvent_BackspaceRemovesCharacters() {
        when(mockValueAccess.getValue(mockColumn)).thenReturn("test");
        when(mockColumn.getType()).thenReturn(ColumnType.STRING);
        when(mockColumn.isBlanksAllowed()).thenReturn(true);
        editor.startEditing(mockColumn);

        drawAndVerifyString("test_");

        editor.handleKeyEvent(KeyEvent.KEY_PRESSED, KeyEvent.VK_BACK_SPACE, '\0');
        drawAndVerifyString("tes_");
    }

    @Test
    void handleKeyEvent_AddsValidCharacters() {
        when(mockValueAccess.getValue(mockColumn)).thenReturn("");
        when(mockColumn.getType()).thenReturn(ColumnType.STRING);
        when(mockColumn.isBlanksAllowed()).thenReturn(true);

        editor.startEditing(mockColumn);
        typeCharacters("a1@");
        drawAndVerifyString("a1@_");
    }

    @Test
    void drawValue_DrawsEditingState() {
        when(mockValueAccess.getValue(mockColumn)).thenReturn("");
        when(mockColumn.getType()).thenReturn(ColumnType.STRING);
        when(mockColumn.isBlanksAllowed()).thenReturn(true);

        editor.startEditing(mockColumn);
        typeCharacters("edit");

        drawAndVerifyString("edit_");
    }

    @Test
    void isValid_StringTypeValidation() {
        when(mockColumn.getType()).thenReturn(ColumnType.STRING);

        when(mockColumn.getDefaultValue()).thenReturn("");
        when(mockColumn.isBlanksAllowed()).thenReturn(true);
        assertTrue(editor.isValid(mockColumn));

        when(mockColumn.getDefaultValue()).thenReturn("   ");
        when(mockColumn.isBlanksAllowed()).thenReturn(false);
        assertFalse(editor.isValid(mockColumn));
    }

    @Test
    void isValid_EmailTypeValidation() {
        when(mockColumn.getType()).thenReturn(ColumnType.EMAIL);
        when(mockColumn.getDefaultValue()).thenReturn("test@example.com");
        assertTrue(editor.isValid(mockColumn));

        when(mockColumn.getDefaultValue()).thenReturn("invalid");
        assertFalse(editor.isValid(mockColumn));
    }

    @Test
    void isValid_IntegerTypeValidation() {
        when(mockColumn.getType()).thenReturn(ColumnType.INTEGER);
        when(mockColumn.getDefaultValue()).thenReturn("123");
        assertTrue(editor.isValid(mockColumn));

        when(mockColumn.getDefaultValue()).thenReturn("abc");
        assertFalse(editor.isValid(mockColumn));
    }

    @Test
    void typeBlocking_BlocksInvalidColumns() {
        editor.setTypeBlocked(mockColumn);
        assertTrue(editor.isTypeBlocked());
        assertEquals(mockColumn, editor.getBlockedColumn());

        editor.clearTypeBlock();
        assertFalse(editor.isTypeBlocked());
    }

    @Test
    void commitEdit_OnlyWhenValid() {
        when(mockValueAccess.getValue(mockColumn)).thenReturn("");
        editor.startEditing(mockColumn);
        when(mockColumn.getType()).thenReturn(ColumnType.STRING);
        when(mockColumn.isBlanksAllowed()).thenReturn(false);

        typeCharacters("valid");
        editor.commitEdit();
        verify(mockValueAccess).setValue(mockColumn, "valid");
    }

    @Test
    void handleKeyEvent_EnterDoesNotCommitInvalidEdit() {
        when(mockValueAccess.getValue(mockColumn)).thenReturn("");
        when(mockColumn.getType()).thenReturn(ColumnType.EMAIL);
        when(mockColumn.isBlanksAllowed()).thenReturn(false);

        editor.startEditing(mockColumn);
        typeCharacters("invalidemail");

        editor.handleKeyEvent(KeyEvent.KEY_PRESSED, KeyEvent.VK_ENTER, '\0');

        verify(mockValueAccess, never()).setValue(any(), any());
        assertTrue(editor.isEditing());
    }

    @Test
    void handleKeyEvent_IgnoresInvalidCharacters() {
        when(mockValueAccess.getValue(mockColumn)).thenReturn("");
        when(mockColumn.getType()).thenReturn(ColumnType.STRING);
        when(mockColumn.isBlanksAllowed()).thenReturn(true);

        editor.startEditing(mockColumn);
        editor.handleKeyEvent(KeyEvent.KEY_PRESSED, 0, '#'); // not allowed

        drawAndVerifyString("_"); // nothing added
    }

    @Test
    void commitEdit_DoesNothingIfNotEditing() {
        editor.commitEdit();
        verify(mockValueAccess, never()).setValue(any(), any());
    }

    @Test
    void isValid_BooleanTypeValidation() {
        when(mockColumn.getType()).thenReturn(ColumnType.BOOLEAN);

        when(mockColumn.getDefaultValue()).thenReturn("true");
        assertTrue(editor.isValid(mockColumn));

        when(mockColumn.getDefaultValue()).thenReturn("false");
        assertTrue(editor.isValid(mockColumn));

        when(mockColumn.getDefaultValue()).thenReturn("maybe");
        assertFalse(editor.isValid(mockColumn));
    }

    @Test
    void clearTypeBlock_ClearsBlockedColumn() {
        editor.setTypeBlocked(mockColumn);
        assertNotNull(editor.getBlockedColumn());

        editor.clearTypeBlock();
        assertNull(editor.getBlockedColumn());
    }

    @Test
    void handleKeyEvent_IgnoredWhenNotEditing() {
        editor.handleKeyEvent(KeyEvent.KEY_PRESSED, KeyEvent.VK_ENTER, '\0');
        // Nothing should happen, no exception
        assertFalse(editor.isEditing());
    }

    @Test
    void commitEdit_DoesNothingWhenEditingTargetNull() {
        editor.commitEdit(); // editingTarget is null
        verify(mockValueAccess, never()).setValue(any(), any());
    }

    @Test
    void isValid_ReturnsTrueWhenEditingTargetNull() {
        assertTrue(editor.isValid());
    }

    @Test
    void drawValue_WhenNotEditing_DrawsStoredValue() {
        when(mockValueAccess.getValue(mockColumn)).thenReturn("stored");

        editor.drawValue(mockGraphics, mockColumn, 10, 20);

        verify(mockGraphics).setColor(Color.BLACK);
        verify(mockGraphics).drawString("stored", 12, 35);
    }

    @Test
    void isValidColumn_StringAlwaysTrueIfNotBlank() {
        when(mockColumn.getType()).thenReturn(ColumnType.STRING);
        when(mockColumn.getDefaultValue()).thenReturn("some text");

        assertTrue(editor.isValid(mockColumn));
    }

    @Test
    void isValidColumn_IntegerRejectsLeadingZero() {
        when(mockColumn.getType()).thenReturn(ColumnType.INTEGER);
        when(mockColumn.getDefaultValue()).thenReturn("012");

        assertFalse(editor.isValid(mockColumn));
    }

    @Test
    void isValidColumn_BooleanInvalidValue() {
        when(mockColumn.getType()).thenReturn(ColumnType.BOOLEAN);
        when(mockColumn.getDefaultValue()).thenReturn("yes");

        assertFalse(editor.isValid(mockColumn));
    }

    @Test
    void drawValue_InvalidHighlighting() {
        when(mockColumn.getType()).thenReturn(ColumnType.EMAIL);
        when(mockColumn.isBlanksAllowed()).thenReturn(false);
        when(mockValueAccess.getValue(mockColumn)).thenReturn("");
        editor.startEditing(mockColumn);
        typeCharacters("invalid");

        editor.drawValue(mockGraphics, mockColumn, 10, 20);

        verify(mockGraphics).setColor(Color.RED); // highlight invalid input
        verify(mockGraphics).drawRect(9, 20, DEFAULT_VALUE_WIDTH, ROW_HEIGHT);
    }

    private void typeCharacters(String text) {
        for (char c : text.toCharArray()) {
            editor.handleKeyEvent(KeyEvent.KEY_PRESSED, 0, c);
        }
    }

    private void drawAndVerifyString(String expected) {
        reset(mockGraphics);
        editor.drawValue(mockGraphics, mockColumn, 10, 20);
        verify(mockGraphics).drawString(expected, 12, 35);
    }

    @Test
    void isValid_NoArg_ValidationsForEachType() {
        // STRING - valid
        when(mockColumn.getType()).thenReturn(ColumnType.STRING);
        when(mockColumn.isBlanksAllowed()).thenReturn(false);
        when(mockValueAccess.getValue(mockColumn)).thenReturn("abc");
        editor.startEditing(mockColumn);
        assertTrue(editor.isValid());

        // STRING - invalid (blank not allowed)
        editor.startEditing(mockColumn);
        when(mockValueAccess.getValue(mockColumn)).thenReturn("   ");
        editor.startEditing(mockColumn); // restart to refresh input
        assertFalse(editor.isValid());

        // EMAIL - valid
        when(mockColumn.getType()).thenReturn(ColumnType.EMAIL);
        when(mockValueAccess.getValue(mockColumn)).thenReturn("a@b.com");
        editor.startEditing(mockColumn);
        assertTrue(editor.isValid());

        // EMAIL - invalid
        when(mockValueAccess.getValue(mockColumn)).thenReturn("invalid-email");
        editor.startEditing(mockColumn);
        assertFalse(editor.isValid());

        // INTEGER - valid
        when(mockColumn.getType()).thenReturn(ColumnType.INTEGER);
        when(mockValueAccess.getValue(mockColumn)).thenReturn("123");
        editor.startEditing(mockColumn);
        assertTrue(editor.isValid());

        // INTEGER - invalid
        when(mockValueAccess.getValue(mockColumn)).thenReturn("12a3");
        editor.startEditing(mockColumn);
        assertFalse(editor.isValid());

        // INTEGER - blank but allowed
        when(mockColumn.isBlanksAllowed()).thenReturn(true);
        when(mockValueAccess.getValue(mockColumn)).thenReturn("");
        editor.startEditing(mockColumn);
        assertTrue(editor.isValid());

        // BOOLEAN - always valid
        when(mockColumn.getType()).thenReturn(ColumnType.BOOLEAN);
        editor.startEditing(mockColumn);
        assertTrue(editor.isValid());
    }

    @Test
    void isTypeBlockedColumn_ReturnsCorrectly() {
        Column otherColumn = mock(Column.class);

        editor.setTypeBlocked(mockColumn);
        assertTrue(editor.isTypeBlocked(mockColumn));
        assertFalse(editor.isTypeBlocked(otherColumn));
    }

}
