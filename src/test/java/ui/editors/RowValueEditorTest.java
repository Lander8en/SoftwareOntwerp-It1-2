package ui.editors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.awt.Color;
import java.awt.Graphics;

import java.awt.event.KeyEvent;
import domain.ColumnType;
import domain.Row;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RowValueEditorTest {

    private RowValueAccess mockAccess;
    private Graphics mockGraphics;
    private Row row;
    private RowValueEditor editor;

    @BeforeEach
    void setUp() {
        mockAccess = mock(RowValueAccess.class);
        mockGraphics = mock(Graphics.class);
        row = mock(Row.class);
        editor = new RowValueEditor(mockAccess);
    }

    @Test
    void startEditing_SetsInitialState() {
        when(mockAccess.getValue(row, 1)).thenReturn("123");

        editor.startEditing(row, 1);

        assertTrue(editor.isEditing());
        assertEquals(row, editor.getEditingRow());
        assertEquals(1, editor.getEditingColIndex());
    }

    @Test
    void handleKeyEvent_AppendsValidCharacters() {
        when(mockAccess.getValue(row, 0)).thenReturn("");
        when(mockAccess.getType(0)).thenReturn(ColumnType.STRING);
        when(mockAccess.allowsBlanks(0)).thenReturn(true);

        editor.startEditing(row, 0);

        editor.handleKeyEvent(0, 0, 'A');
        editor.handleKeyEvent(0, 0, '1');
        editor.handleKeyEvent(0, 0, ' ');

        editor.commitEdit();

        verify(mockAccess).setValue(row, 0, "A1 ");
    }

    @Test
    void handleKeyEvent_Backspace_RemovesCharacters() {
        when(mockAccess.getValue(row, 0)).thenReturn("ABC");
        when(mockAccess.getType(0)).thenReturn(ColumnType.STRING);
        when(mockAccess.allowsBlanks(0)).thenReturn(false);

        editor.startEditing(row, 0);
        editor.handleKeyEvent(0, KeyEvent.VK_BACK_SPACE, '\0');

        editor.commitEdit();
        verify(mockAccess).setValue(row, 0, "AB");
    }

    @Test
    void handleKeyEvent_Escape_StopsEditing() {
        when(mockAccess.getValue(row, 0)).thenReturn("Initial");

        editor.startEditing(row, 0);
        editor.handleKeyEvent(0, KeyEvent.VK_ESCAPE, '\0');

        assertFalse(editor.isEditing());
    }

    @Test
    void commitEdit_InvalidEmail_DoesNotCommit() {
        when(mockAccess.getValue(row, 0)).thenReturn("testemail");
        when(mockAccess.getType(0)).thenReturn(ColumnType.EMAIL);
        when(mockAccess.allowsBlanks(0)).thenReturn(false);

        editor.startEditing(row, 0);
        editor.handleKeyEvent(0, 0, '@'); // makes it valid: testemail@
        editor.handleKeyEvent(0, 0, '@'); // makes it invalid: testemail@@
        editor.commitEdit();

        verify(mockAccess, never()).setValue(eq(row), anyInt(), anyString());
        assertTrue(editor.isEditing());
    }

    @Test
    void commitEdit_ValidInteger_CommitsSuccessfully() {
        when(mockAccess.getValue(row, 0)).thenReturn("");
        when(mockAccess.getType(0)).thenReturn(ColumnType.INTEGER);
        when(mockAccess.allowsBlanks(0)).thenReturn(false);

        editor.startEditing(row, 0);
        editor.handleKeyEvent(0, 0, '4');
        editor.handleKeyEvent(0, 0, '2');
        editor.commitEdit();

        verify(mockAccess).setValue(row, 0, "42");
        assertFalse(editor.isEditing());
    }

    @Test
    void draw_WithBooleanValue_RendersCorrectly() {
        when(mockAccess.getValue(row, 0)).thenReturn("true");

        editor.startEditing(row, 0);
        editor.draw(mockGraphics, row, 0, 10, 20, ColumnType.BOOLEAN);

        verify(mockGraphics).drawRect(anyInt(), anyInt(), anyInt(), anyInt());
        verify(mockGraphics, times(2)).drawLine(anyInt(), anyInt(), anyInt(), anyInt());
    }

    @Test
    void isValid_BooleanColumn_InvalidValue_ReturnsFalse() {
        when(mockAccess.getType(0)).thenReturn(ColumnType.BOOLEAN);
        when(mockAccess.allowsBlanks(0)).thenReturn(false);

        editor.startEditing(row, 0);
        editor.handleKeyEvent(0, 0, 'y'); // not "true" or "false"

        assertFalse(editor.isValid());
    }

    @Test
    void commitEdit_InvalidInteger_DoesNotSave() {
        when(mockAccess.getValue(row, 0)).thenReturn("abc");
        when(mockAccess.getType(0)).thenReturn(ColumnType.INTEGER);
        when(mockAccess.allowsBlanks(0)).thenReturn(false);

        editor.startEditing(row, 0);
        for (char c : "abc".toCharArray()) {
            editor.handleKeyEvent(0, 0, c);
        }

        editor.commitEdit();

        verify(mockAccess, never()).setValue(any(), anyInt(), anyString());
        assertTrue(editor.isEditing());
    }

    @Test
    void isValid_EmailWithMultipleAts_ReturnsFalse() {
        when(mockAccess.getType(0)).thenReturn(ColumnType.EMAIL);
        when(mockAccess.allowsBlanks(0)).thenReturn(false);

        editor.startEditing(row, 0);
        for (char c : "a@b@c".toCharArray()) {
            editor.handleKeyEvent(0, 0, c);
        }

        assertFalse(editor.isValid());
    }

    @Test
    void draw_WithInvalidText_ShowsRedBorder() {
        when(mockAccess.getValue(row, 0)).thenReturn("invalid");
        when(mockAccess.getType(0)).thenReturn(ColumnType.INTEGER);
        when(mockAccess.allowsBlanks(0)).thenReturn(false);

        editor.startEditing(row, 0);
        editor.handleKeyEvent(0, 0, 'X'); // Invalid integer input
        editor.draw(mockGraphics, row, 0, 10, 20, ColumnType.INTEGER);

        verify(mockGraphics).setColor(Color.RED);
        verify(mockGraphics, atLeastOnce()).drawRect(anyInt(), anyInt(), anyInt(), anyInt());
    }

    @Test
    void stopEditing_ClearsEditingState() {
        when(mockAccess.getValue(row, 0)).thenReturn("data");

        editor.startEditing(row, 0);
        editor.stopEditing();

        assertFalse(editor.isEditing());
        assertNull(editor.getEditingRow());
        assertEquals(-1, editor.getEditingColIndex());
    }

    @Test
    void handleKeyEvent_BackspaceOnEmptyInput_DoesNotCrash() {
        when(mockAccess.getValue(row, 0)).thenReturn("");
        editor.startEditing(row, 0);

        editor.handleKeyEvent(0, KeyEvent.VK_BACK_SPACE, '\0');

        assertTrue(editor.isEditing()); // still editing, just nothing to delete
    }

    @Test
    void isValid_String_BlankAllowed_ReturnsTrue() {
        when(mockAccess.getType(0)).thenReturn(ColumnType.STRING);
        when(mockAccess.allowsBlanks(0)).thenReturn(true);
        when(mockAccess.getValue(row, 0)).thenReturn("");

        editor.startEditing(row, 0);
        editor.handleKeyEvent(0, KeyEvent.VK_BACK_SPACE, '\0'); // empty the input

        assertTrue(editor.isValid());
    }

    @Test
    void isValid_IntegerWithLeadingZeros_ReturnsFalse() {
        when(mockAccess.getType(0)).thenReturn(ColumnType.INTEGER);
        when(mockAccess.allowsBlanks(0)).thenReturn(false);

        editor.startEditing(row, 0);
        for (char c : "007".toCharArray()) {
            editor.handleKeyEvent(0, 0, c);
        }

        assertFalse(editor.isValid());
    }

    @Test
    void handleKeyEvent_Enter_CommitsValidInput() {
        when(mockAccess.getValue(row, 0)).thenReturn("");
        when(mockAccess.getType(0)).thenReturn(ColumnType.STRING);
        when(mockAccess.allowsBlanks(0)).thenReturn(false);

        editor.startEditing(row, 0);
        editor.handleKeyEvent(0, 0, 'X');
        editor.handleKeyEvent(0, KeyEvent.VK_ENTER, '\n');

        verify(mockAccess).setValue(row, 0, "X");
        assertFalse(editor.isEditing());
    }

    @Test
    void startEditing_NullInitialValue_SetsEmptyInput() {
        when(mockAccess.getValue(row, 0)).thenReturn(null);
        when(mockAccess.getType(0)).thenReturn(ColumnType.STRING);
        when(mockAccess.allowsBlanks(0)).thenReturn(true);

        editor.startEditing(row, 0);

        // Enter something and commit to confirm input wasn't null
        editor.handleKeyEvent(0, 0, 'A');
        editor.commitEdit();

        verify(mockAccess).setValue(row, 0, "A");
    }

    @Test
    void stopEditing_ResetsInput() {
        when(mockAccess.getValue(row, 0)).thenReturn("abc");
        when(mockAccess.getType(0)).thenReturn(ColumnType.STRING); // initial type
        when(mockAccess.allowsBlanks(0)).thenReturn(true);

        editor.startEditing(row, 0);
        editor.stopEditing();

        // Start again with different value
        when(mockAccess.getValue(row, 0)).thenReturn(""); // new input
        when(mockAccess.getType(0)).thenReturn(ColumnType.STRING);
        when(mockAccess.allowsBlanks(0)).thenReturn(true);

        editor.startEditing(row, 0);
        editor.commitEdit();

        verify(mockAccess, never()).setValue(any(), anyInt(), eq("abc"));
    }

    @Test
    void draw_WithBooleanFalse_DoesNotDrawCheckmark() {
        when(mockAccess.getValue(row, 0)).thenReturn("false");

        editor.startEditing(row, 0);
        editor.draw(mockGraphics, row, 0, 10, 20, ColumnType.BOOLEAN);

        verify(mockGraphics, never()).drawLine(anyInt(), anyInt(), anyInt(), anyInt());
    }

    @Test
    void handleKeyEvent_WhenNotEditing_DoesNothing() {
        editor.handleKeyEvent(0, 0, 'A'); // never called startEditing
        // Should not throw, crash, or do anything
        assertFalse(editor.isEditing()); // still false
    }

    @Test
    void draw_WhenNotEditing_DrawsValueNormally() {
        when(mockAccess.getValue(row, 0)).thenReturn("abc");

        editor.draw(mockGraphics, row, 0, 10, 20, ColumnType.STRING);

        verify(mockGraphics).drawRect(anyInt(), anyInt(), anyInt(), anyInt());
        verify(mockGraphics).drawString(eq("abc"), anyInt(), anyInt());
    }

    @Test
    void isValid_Email_BlankAllowed_ReturnsTrue() {
        when(mockAccess.getType(0)).thenReturn(ColumnType.EMAIL);
        when(mockAccess.allowsBlanks(0)).thenReturn(true);

        editor.startEditing(row, 0); // currentInput = ""
        editor.handleKeyEvent(0, KeyEvent.VK_BACK_SPACE, '\0'); // just to simulate input

        assertTrue(editor.isValid());
    }

    @Test
    void isValid_Integer_BlankAllowed_ReturnsTrue() {
        when(mockAccess.getType(0)).thenReturn(ColumnType.INTEGER);
        when(mockAccess.allowsBlanks(0)).thenReturn(true);

        editor.startEditing(row, 0); // input is "" by default

        assertTrue(editor.isValid());
    }

    @Test
    void isValid_WhenNotEditing_ReturnsTrue() {
        assertFalse(editor.isEditing()); // sanity check
        assertTrue(editor.isValid()); // should return true when not editing
    }

}
