package ui;

public class KeyCombo {

    private final int keyCode;
    private final boolean ctrl;
    
    public KeyCombo (int keyCode, boolean ctrl) {
        this.keyCode = keyCode;
        this.ctrl = ctrl;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof KeyCombo other)) return false;
        return keyCode == other.keyCode && ctrl == other.ctrl;
    }

    @Override
    public int hashCode() {
        return keyCode + (ctrl ? 1000 : 0);
    }
}
