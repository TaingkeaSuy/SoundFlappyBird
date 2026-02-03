package game;

import java.awt.*;
import java.util.Random;

public class Pipe {
    // 1. DIMENSIONS FOR 1080x566
    public int x = 1080;
    public int width = 90;          // Slightly wider for the wide screen
    private int gap = 180;          // Comfortable gap for 566px height
    private int topHeight;

    public boolean isPassed = false;
    private Random random = new Random();

    // Colors
    private final Color PIPE_GREEN = new Color(115, 190, 46);
    private final Color PIPE_DARK = new Color(83, 128, 34);
    private final Color PIPE_LIGHT = new Color(173, 230, 110);
    private final Color PIPE_BORDER = new Color(25, 18, 20);

    public Pipe() {
        // 2. ADJUSTED HEIGHT RANGE
        // Minimum height 50, maximum height around 330 to leave room for the gap
        topHeight = random.nextInt(280) + 50;
    }

    public void update() {
        // 3. SPEED ADJUSTMENT
        x -= 6; // Balanced speed for 1080 width

        if (x + width < 0) {
            x = GamePanel.WIDTH;
            topHeight = random.nextInt(280) + 50;
            isPassed = false;
        }
    }

    public void draw(Graphics2D g2d) {
        drawStyledPipe(g2d, x, 0, width, topHeight, true);
        drawStyledPipe(g2d, x, topHeight + gap, width, GamePanel.HEIGHT - (topHeight + gap), false);
    }

    private void drawStyledPipe(Graphics2D g2d, int x, int y, int w, int h, boolean isTop) {
        // Main Body
        g2d.setColor(PIPE_GREEN);
        g2d.fillRect(x, y, w, h);

        // Shading
        g2d.setColor(PIPE_LIGHT);
        g2d.fillRect(x + 8, y, 15, h);
        g2d.setColor(PIPE_DARK);
        g2d.fillRect(x + w - 23, y, 15, h);

        // 4. SCALED PIPE LIP
        int lipWidth = w + 16;
        int lipHeight = 35;
        int lipX = x - 8;
        int lipY = isTop ? (y + h - lipHeight) : y;

        g2d.setColor(PIPE_BORDER);
        g2d.setStroke(new BasicStroke(4));
        g2d.drawRect(lipX, lipY, lipWidth, lipHeight);

        g2d.setColor(PIPE_GREEN);
        g2d.fillRect(lipX + 2, lipY + 2, lipWidth - 4, lipHeight - 4);

        g2d.setColor(PIPE_LIGHT);
        g2d.fillRect(lipX + 10, lipY + 4, 15, lipHeight - 8);

        // Final Outline
        g2d.setColor(PIPE_BORDER);
        g2d.drawRect(x, y, w, h);
    }

    public boolean collides(Bird bird) {
        Rectangle birdRect = bird.getBounds();
        return birdRect.intersects(new Rectangle(x, 0, width, topHeight)) ||
                birdRect.intersects(new Rectangle(x, topHeight + gap, width, GamePanel.HEIGHT));
    }

    public int getX() { return x; }
    public int getWidth() { return width; }
}