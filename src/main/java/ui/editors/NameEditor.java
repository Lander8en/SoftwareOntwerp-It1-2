package ui.editors;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.List;

public class NameEditor<T> {

    private T editingTarget = null;
    private String currentInput = "";
    private String originalName = "";
    private boolean editing = false;

    private final NameAccess<T> access;

    public NameEditor(NameAccess<T> access) {
        this.access = access;
    }

    public T getEditingTarget() {
        return editingTarget;
    }

    public void startEditing(T target) {
        this.editingTarget = target;
        this.originalName = access.getName(target);
        this.currentInput = originalName;
        this.editing = true;
    }

    public void continueEditing(T target) {
        this.editingTarget = target;
        this.editing = true;
    }

    public void handleKeyEvent(int id, int keyCode, char keyChar) {
        if (!editing) return;

        switch (keyCode) {
            case KeyEvent.VK_ENTER -> commitEdit();
            case KeyEvent.VK_ESCAPE -> cancelEdit();
            case KeyEvent.VK_BACK_SPACE -> {
                if (!currentInput.isEmpty()) {
                    currentInput = currentInput.substring(0, currentInput.length() - 1);
                }
            }
            default -> {
                if (!Character.isISOControl(keyChar) && keyChar != KeyEvent.CHAR_UNDEFINED) {
                    currentInput += keyChar;
                }
            }
        }
    }

    public void drawName(Graphics g, T target, int x, int y, int width, int height) {
        boolean isTarget = editing && target == editingTarget;
        boolean isValid = !isTarget || isValid();
        String text = isTarget ? currentInput + "_" : access.getName(target);

        if (isTarget && !isValid) {
            g.setColor(Color.RED);
            g.drawRect(x, y, width, height);
        }

        g.setColor(Color.BLACK);
        g.drawString(text, x + 10, y + 15);
    }

    public void commitEdit() {
        if (editingTarget != null && isValid()) {
            access.setName(editingTarget, currentInput);
            stopEditing();
        }
    }

    public void cancelEdit() {
        if (editingTarget != null) {
            access.setName(editingTarget, originalName);
            stopEditing();
        }
    }

    public void stopEditing() {
        editing = false;
        editingTarget = null;
        currentInput = "";
        originalName = "";
    }

    public boolean isEditing() {
        return editing;
    }

    public boolean isValid() {
        if (currentInput.isBlank()) return false;
        List<T> items = access.getAll();
        return items.stream().noneMatch(t -> t != editingTarget && access.getName(t).equals(currentInput));
    }

    public void stopEditingIfChanged(T changedItem) {
        if (changedItem == editingTarget) {
            stopEditing();
        }
    }
}