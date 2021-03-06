package me.bulbazord.crawl;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.BufferStrategy;
import java.awt.image.DataBufferInt;
import javax.swing.JFrame;

import me.bulbazord.crawl.graphics.Screen;
import me.bulbazord.crawl.input.Keyboard;

public class Crawl extends Canvas implements Runnable {
    public static final int HEIGHT = 600;
    public static final int WIDTH = 800;

    private boolean running;
    private int tickCounter;
    private int down, right;
    private int[] pixels;
    private BufferedImage image;
    private JFrame frame;
    private Keyboard keyboard;
    private Screen screen;

    public Crawl() {
        // Set up game stuff
        running = false;
        System.out.println("Starting!");
        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
        screen = new Screen(WIDTH, HEIGHT);
        keyboard = new Keyboard();
        addKeyListener(keyboard);
        down = 0;
        right = 0;

        // Set up Game window
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        frame = new JFrame("Crawl");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(this, BorderLayout.CENTER);
        frame.setResizable(false);
        frame.setVisible(true);
        frame.pack();
    }

    public void start() {
        new Thread(this).start();
        running = true;
    }

    public void stop() {
        running = false;
    }

    public void run() {
        long lastTime = System.nanoTime();
        double unprocessed = 0;
        double nsPerTick = 1000000000.0 / 60.0;
        int frames = 0;
        int ticks = 0;
        long lastTimer1 = System.currentTimeMillis();
        while (running) {
            long now = System.nanoTime();
            unprocessed += (now - lastTime) / nsPerTick;
            lastTime = now;
            while (unprocessed >= 1) {
                ticks++;
                tick();
                unprocessed--;
            }
            frames++;
            render();

            if (System.currentTimeMillis() - lastTimer1 > 1000) {
                lastTimer1 += 1000;
                System.out.println(ticks + " ticks, " + frames + " fps");
                frames = 0;
                ticks = 0;
            }
        }
    }

    public void tick() {
        if (keyboard.up.move()) down = down - screen.TILE_SIZE;
        if (keyboard.down.move()) down = down + screen.TILE_SIZE;
        if (keyboard.left.move()) right = right - screen.TILE_SIZE;
        if (keyboard.right.move()) right = right + screen.TILE_SIZE;
    }

    public void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);
            return;
        }

        screen.clear();
        screen.render(right, down);
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = screen.pixels[i];
        }

        Graphics g = bs.getDrawGraphics();
        g.drawImage(image, 0, 0, WIDTH, HEIGHT, null);
        g.dispose();
        bs.show();
    }

    public static void main(String[] args) {
        Crawl crawl = new Crawl();
        crawl.start();
    }
}
