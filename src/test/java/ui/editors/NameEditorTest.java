package ui.editors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NameEditorTest {

    private NameAccess<Object> mockAccess;
    private Graphics mockGraphics;
    private Object target1, target2;
    private NameEditor<Object> editor;

    // Need to change this test, not a good test

    @BeforeEach
    void setUp() {
        mockAccess = mock(NameAccess.class); // Don't delete, this crashes the test
        mockGraphics = mock(Graphics.class);
        target1 = new Object();
        target2 = new Object();
        editor = new NameEditor<>(mockAccess);
    }

    @Test
    void startEditing_InitializesEditingState() {
        when(mockAccess.getName(target1)).thenReturn("Original");

        editor.startEditing(target1);

        assertTrue(editor.isEditing());
        assertEquals(target1, editor.getEditingTarget());
    }

    @Test
    void handleKeyEvent_ValidCharactersAppended() {
        when(mockAccess.getName(target1)).thenReturn("");
        editor.startEditing(target1);

        editor.handleKeyEvent(KeyEvent.KEY_PRESSED, 0, 'a');
        editor.handleKeyEvent(KeyEvent.KEY_PRESSED, 0, '1');
        editor.handleKeyEvent(KeyEvent.KEY_PRESSED, 0, ' ');

        editor.commitEdit();
        verify(mockAccess).setName(target1, "a1 ");
    }

    @Test
    void handleKeyEvent_BackspaceRemovesCharacters() {
        when(mockAccess.getName(target1)).thenReturn("abc");
        editor.startEditing(target1);

        editor.handleKeyEvent(KeyEvent.KEY_PRESSED, KeyEvent.VK_BACK_SPACE, '\0');
        editor.commitEdit();
        verify(mockAccess).setName(target1, "ab");
    }

    @Test
    void handleKeyEvent_EscapeCancelsEditing() {
        when(mockAccess.getName(target1)).thenReturn("original");
        editor.startEditing(target1);

        editor.handleKeyEvent(KeyEvent.KEY_PRESSED, 0, 'X');
        editor.handleKeyEvent(KeyEvent.KEY_PRESSED, KeyEvent.VK_ESCAPE, '\0');

        verify(mockAccess).setName(target1, "original");
        assertFalse(editor.isEditing());
    }

    @Test
    void isValid_BlankInputReturnsFalse() {
        when(mockAccess.getName(target1)).thenReturn("");
        when(mockAccess.getAll()).thenReturn(List.of(target1));

        editor.startEditing(target1);
        editor.handleKeyEvent(KeyEvent.KEY_PRESSED, KeyEvent.VK_BACK_SPACE, '\0');

        assertFalse(editor.isValid());
    }

    @Test
    void isValid_DuplicateNameReturnsFalse() {
        when(mockAccess.getName(target1)).thenReturn("Duplicate");
        when(mockAccess.getName(target2)).thenReturn("Duplicate");
        when(mockAccess.getAll()).thenReturn(Arrays.asList(target1, target2));

        editor.startEditing(target1);
        editor.handleKeyEvent(KeyEvent.KEY_PRESSED, KeyEvent.VK_ENTER, '\0');

        assertFalse(editor.isValid());
    }

    @Test
    void stopEditingIfChanged_StopsIfTargetMatches() {
        when(mockAccess.getName(target1)).thenReturn("Name");
        editor.startEditing(target1);
        editor.stopEditingIfChanged(target1);
        assertFalse(editor.isEditing());
    }

    @Test
    void drawName_ValidAndInvalidState() {
        when(mockAccess.getName(target1)).thenReturn("Name");
        when(mockAccess.getAll()).thenReturn(List.of(target1));
        editor.startEditing(target1);

        // cause it to be invalid
        editor.handleKeyEvent(KeyEvent.KEY_PRESSED, KeyEvent.VK_BACK_SPACE, '\0');
        editor.handleKeyEvent(KeyEvent.KEY_PRESSED, KeyEvent.VK_BACK_SPACE, '\0');
        editor.handleKeyEvent(KeyEvent.KEY_PRESSED, KeyEvent.VK_BACK_SPACE, '\0');
        editor.handleKeyEvent(KeyEvent.KEY_PRESSED, KeyEvent.VK_BACK_SPACE, '\0');

        editor.drawName(mockGraphics, target1, 10, 10, 100, 20);

        verify(mockGraphics).setColor(Color.RED);
        verify(mockGraphics).drawRect(10, 10, 100, 20);
        verify(mockGraphics).setColor(Color.BLACK);
    }

    @Test
    void handleKeyEvent_BackspaceOnEmptyInput_DoesNothing() {
        when(mockAccess.getName(target1)).thenReturn("Original");
        editor.startEditing(target1);

        // Clear the input manually
        editor.handleKeyEvent(KeyEvent.KEY_PRESSED, KeyEvent.VK_BACK_SPACE, '\0');
        editor.handleKeyEvent(KeyEvent.KEY_PRESSED, KeyEvent.VK_BACK_SPACE, '\0');

        // Should not throw or change editing state
        assertTrue(editor.isEditing());
    }

    @Test
    void commitEdit_InvalidName_DoesNotCommit() {
        when(mockAccess.getName(target1)).thenReturn("Dup");
        when(mockAccess.getAll()).thenReturn(List.of(target1, target2));
        when(mockAccess.getName(target2)).thenReturn("Dup");

        editor.startEditing(target1);

        editor.commitEdit();

        verify(mockAccess, never()).setName(eq(target1), anyString());
        assertTrue(editor.isEditing());
    }

    @Test
    void cancelEdit_RestoresOriginalName() {
        when(mockAccess.getName(target1)).thenReturn("Initial");
        editor.startEditing(target1);

        editor.handleKeyEvent(KeyEvent.KEY_PRESSED, 0, 'X');
        editor.cancelEdit();

        verify(mockAccess).setName(target1, "Initial");
        assertFalse(editor.isEditing());
    }

    @Test
    void stopEditingIfChanged_DoesNothingIfDifferentTarget() {
        when(mockAccess.getName(target1)).thenReturn("Test");
        editor.startEditing(target1);

        editor.stopEditingIfChanged(target2);

        assertTrue(editor.isEditing());
    }

    @Test
    void continueEditing_SetsTargetAndEnablesEditing() {
        // start with editor not editing anything
        assertFalse(editor.isEditing());
        assertNull(editor.getEditingTarget());

        editor.continueEditing(target1);

        assertTrue(editor.isEditing());
        assertEquals(target1, editor.getEditingTarget());
    }

    @Test
    void continueEditing_OverridesPreviousEditingTarget() {
        when(mockAccess.getName(target1)).thenReturn("First");
        editor.startEditing(target1);
        assertEquals(target1, editor.getEditingTarget());

        editor.continueEditing(target2);
        assertEquals(target2, editor.getEditingTarget());
        assertTrue(editor.isEditing());
    }

    @Test
    void handleKeyEvent_DoesNothingIfNotEditing() {
        editor.handleKeyEvent(KeyEvent.KEY_PRESSED, KeyEvent.VK_ENTER, '\0');
        assertFalse(editor.isEditing()); // Still false
        assertNull(editor.getEditingTarget()); // Nothing was assigned
    }

    @Test
    void handleKeyEvent_IgnoresControlCharacters() {
        when(mockAccess.getName(target1)).thenReturn("A");
        editor.startEditing(target1);

        editor.handleKeyEvent(KeyEvent.KEY_PRESSED, 0, KeyEvent.CHAR_UNDEFINED); // Ignored
        editor.handleKeyEvent(KeyEvent.KEY_PRESSED, 0, '\n'); // ISO control

        editor.commitEdit();

        verify(mockAccess).setName(target1, "A"); // Unchanged
    }

    @Test
    void drawName_NonEditingDrawsDefaultName() {
        when(mockAccess.getName(target1)).thenReturn("StaticName");

        editor.drawName(mockGraphics, target1, 0, 0, 100, 20);

        verify(mockGraphics).setColor(Color.BLACK);
        verify(mockGraphics).drawString("StaticName", 10, 15);
    }

    @Test
    void commitEdit_NoEditingTarget_DoesNothing() {
        editor.commitEdit(); // Nothing should happen
        verify(mockAccess, never()).setName(any(), any());
    }

    @Test
    void cancelEdit_NoEditingTarget_DoesNothing() {
        editor.cancelEdit(); // Should not throw
        verify(mockAccess, never()).setName(any(), any());
    }

}
