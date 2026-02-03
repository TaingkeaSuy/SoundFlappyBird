package game;

import javax.sound.sampled.*;

public class AudioInput {
    private TargetDataLine line;
    private boolean isLoud = false;
    private final int THRESHOLD = 3000;

    // NEW VARIABLES HERE
    private long lastJumpTime = 0;
    private final int JUMP_COOLDOWN = 200; // Controls flap speed (milliseconds)

    public void start() {
        try {
            AudioFormat format = new AudioFormat(44100, 16, 1, true, true);
            line = AudioSystem.getTargetDataLine(format);
            line.open(format);
            line.start();

            Thread thread = new Thread(() -> {
                byte[] buffer = new byte[1024];
                while (line != null && line.isOpen()) {
                    int read = line.read(buffer, 0, buffer.length);
                    long sum = 0;
                    for (int i = 0; i < read; i += 2) {
                        short sample = (short) ((buffer[i] << 8) | (buffer[i + 1] & 0xFF));
                        sum += Math.abs(sample);
                    }
                    double volume = sum / (double)(read / 2);
                    isLoud = (volume > THRESHOLD);
                }
            });
            thread.setDaemon(true);
            thread.start();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // POSITION CHANGED: Logic updated for continuous jumping
    public boolean shouldJump() {
        long currentTime = System.currentTimeMillis();
        if (isLoud && (currentTime - lastJumpTime > JUMP_COOLDOWN)) {
            lastJumpTime = currentTime;
            return true;
        }
        return false;
    }

    public void stop() {
        if (line != null) { line.stop(); line.close(); }
    }
}