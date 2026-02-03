package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;

public class GamePanel extends JPanel implements ActionListener {

    public static final int WIDTH = 1080;
    public static final int HEIGHT = 566;

    private javax.swing.Timer timer;
    private Bird bird;
    private Pipe pipe;
    private AudioInput audio;

    private Image backgroundImg;
    private int bgX = 0;
    private int bgSpeed = 2;

    private int score = 0;
    private int highScore = 0;
    private int groundOffset = 0;

    private boolean gameOver = false;
    private boolean gameStarted = false;

    // Button Rectangles
    private Rectangle startBtn = new Rectangle(WIDTH / 2 - 100, HEIGHT / 2 - 30, 200, 60);
    private Rectangle exitBtn = new Rectangle(WIDTH / 2 - 100, HEIGHT / 2 + 50, 200, 60);
    private Point mousePoint = new Point(0, 0);

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);

        try {
            backgroundImg = ImageIO.read(new File("background.png"));
        } catch (IOException e) {
            System.out.println("Background image not found.");
        }

        bird = new Bird();
        pipe = new Pipe();
        audio = new AudioInput();

        // Start the timer immediately so the background scrolls on the menu
        timer = new javax.swing.Timer(16, this);
        timer.start();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_R && gameOver) restartGame();
                if (e.getKeyCode() == KeyEvent.VK_SPACE && gameStarted && !gameOver) bird.jump();
            }
        });

        // Mouse listeners for interaction
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!gameStarted) {
                    if (startBtn.contains(e.getPoint())) {
                        gameStarted = true;
                        audio.start();
                    } else if (exitBtn.contains(e.getPoint())) {
                        System.exit(0);
                    }
                }
            }
            @Override
            public void mouseMoved(MouseEvent e) {
                mousePoint = e.getPoint(); // Track mouse for hover effects
            }
        };
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Background and ground move even in the menu for a "live" feel
        bgX -= bgSpeed;
        if (bgX <= -WIDTH) bgX = 0;

        groundOffset -= 6;
        if (groundOffset <= -WIDTH) groundOffset = 0;

        if (gameStarted && !gameOver) {
            if (audio.shouldJump()) bird.jump();

            bird.update();
            pipe.update();

            if (!pipe.isPassed && bird.x > pipe.x + pipe.width) {
                score++;
                pipe.isPassed = true;
                if (score > highScore) highScore = score;
            }

            if (pipe.collides(bird) || bird.y > HEIGHT - 60 || bird.y < 0) {
                gameOver = true;
                audio.stop();
            }
        }
        repaint();
    }

    private void restartGame() {
        bird = new Bird();
        pipe = new Pipe();
        score = 0;
        gameOver = false;
        audio.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBackground(g2d);

        if (!gameStarted) {
            drawMenu(g2d);
        } else {
            pipe.draw(g2d);
            drawGround(g2d);
            bird.draw(g2d);
            drawScore(g2d);
            if (gameOver) drawGameOver(g2d);
        }
    }
    private void drawMenu(Graphics2D g2d) {
        // Darken background slightly
        g2d.setColor(new Color(0, 0, 0, 80));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // Title with shadow
        g2d.setFont(new Font("Arial", Font.BOLD, 90));
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.drawString("FLAPPY BIRD", WIDTH / 2 - 277, HEIGHT / 2 - 117); // Shadow
        g2d.setColor(Color.YELLOW);
        g2d.drawString("FLAPPY BIRD", WIDTH / 2 - 280, HEIGHT / 2 - 120);

        drawStyledButton(g2d, startBtn, "START", new Color(75, 213, 91), mousePoint);
        drawStyledButton(g2d, exitBtn, "EXIT", new Color(231, 76, 60), mousePoint);
    }

    private void drawStyledButton(Graphics2D g2d, Rectangle rect, String text, Color baseColor, Point mouse) {
        boolean isHovered = rect.contains(mouse);

        // Shadow
        g2d.setColor(new Color(0, 0, 0, 60));
        g2d.fillRoundRect(rect.x + 4, rect.y + 4, rect.width, rect.height, 15, 15);

        // Button Body
        g2d.setColor(isHovered ? baseColor.brighter() : baseColor);
        g2d.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 15, 15);

        // Border
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 15, 15);

        // Text
        g2d.setFont(new Font("Arial", Font.BOLD, 28));
        FontMetrics fm = g2d.getFontMetrics();
        int textX = rect.x + (rect.width - fm.stringWidth(text)) / 2;
        int textY = rect.y + (rect.height + fm.getAscent()) / 2 - 5;
        g2d.drawString(text, textX, textY);
    }

    private void drawBackground(Graphics2D g2d) {
        if (backgroundImg != null) {
            g2d.drawImage(backgroundImg, bgX, 0, WIDTH, HEIGHT, null);
            g2d.drawImage(backgroundImg, bgX + WIDTH, 0, WIDTH, HEIGHT, null);
        } else {
            g2d.setColor(new Color(60, 180, 204));
            g2d.fillRect(0, 0, WIDTH, HEIGHT);
        }
    }

    private void drawGround(Graphics2D g2d) {
        int groundY = HEIGHT - 40;
        g2d.setColor(new Color(222, 216, 149));
        g2d.fillRect(0, groundY, WIDTH, 40);
        g2d.setColor(new Color(115, 190, 46));
        g2d.fillRect(groundOffset, groundY - 5, WIDTH, 5);
        g2d.fillRect(groundOffset + WIDTH, groundY - 5, WIDTH, 5);
    }

    private void drawScore(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 60));
        String s = String.valueOf(score);
        g2d.drawString(s, WIDTH / 2 - g2d.getFontMetrics().stringWidth(s) / 2, 80);
    }

    private void drawGameOver(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 80));
        String msg = "GAME OVER";
        g2d.drawString(msg, WIDTH / 2 - g2d.getFontMetrics().stringWidth(msg) / 2, HEIGHT / 2 - 60);

        g2d.setFont(new Font("Arial", Font.PLAIN, 30));
        g2d.drawString("Score: " + score, WIDTH / 2 - 50, HEIGHT / 2);
        g2d.setColor(Color.YELLOW);
        g2d.drawString("Best: " + highScore, WIDTH / 2 - 50, HEIGHT / 2 + 40);

        g2d.setColor(Color.WHITE);
        g2d.drawString("Press 'R' to Restart", WIDTH / 2 - 110, HEIGHT / 2 + 100);
    }
}