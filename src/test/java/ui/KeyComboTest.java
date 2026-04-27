package ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KeyComboTest {

    @Test
    void equals_ReturnsTrue_WhenSameKeyCodeAndCtrl() {
        KeyCombo combo1 = new KeyCombo(65, true); // Ctrl + A
        KeyCombo combo2 = new KeyCombo(65, true);

        assertEquals(combo1, combo2);
        assertEquals(combo1.hashCode(), combo2.hashCode());
    }

    @Test
    void equals_ReturnsFalse_WhenDifferentKeyCode() {
        KeyCombo combo1 = new KeyCombo(65, true);
        KeyCombo combo2 = new KeyCombo(66, true);

        assertNotEquals(combo1, combo2);
    }

    @Test
    void equals_ReturnsFalse_WhenDifferentCtrlFlag() {
        KeyCombo combo1 = new KeyCombo(65, true);
        KeyCombo combo2 = new KeyCombo(65, false);

        assertNotEquals(combo1, combo2);
    }

    @Test
    void equals_ReturnsFalse_WhenObjectIsNull() {
        KeyCombo combo = new KeyCombo(65, true);

        assertNotEquals(combo, null);
    }

    @Test
    void equals_ReturnsFalse_WhenDifferentClass() {
        KeyCombo combo = new KeyCombo(65, true);

        assertNotEquals(combo, "NotAKeyCombo");
    }

    @Test
    void hashCode_Adds1000_WhenCtrlIsTrue() {
        KeyCombo combo = new KeyCombo(42, true);
        assertEquals(1042, combo.hashCode());
    }

    @Test
    void hashCode_EqualsKeyCode_WhenCtrlIsFalse() {
        KeyCombo combo = new KeyCombo(42, false);
        assertEquals(42, combo.hashCode());
    }
}
