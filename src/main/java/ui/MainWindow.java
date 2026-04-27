package ui;

import java.awt.Color;
import java.awt.Graphics;

public class MainWindow extends CanvasWindow{

    private final SubwindowManager subwindowManager;

    public MainWindow() {
        super("Database Application");
        this.subwindowManager = new SubwindowManager();
    }

    @Override
    protected void paint(Graphics g) {
        g.setColor(Color.GRAY);
        var bounds = g.getClipBounds();
        g.fillRect(0, 0, bounds.width, bounds.height);

        subwindowManager.drawAll(g);
    }

    @Override
    protected void handleKeyEvent(int id, int keyCode, char keyChar) {
        subwindowManager.handleKeyEvent(id, keyCode, keyChar);
        repaint();
    }

    @Override
    protected void handleMouseEvent(int id, int x, int y, int ClickCount) {
        subwindowManager.handleMouseEvent(id, x, y, ClickCount);
        repaint();
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            new MainWindow().show();
        });
    }
}
