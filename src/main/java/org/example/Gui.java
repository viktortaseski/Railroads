package org.example;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Gui extends JPanel implements Runnable {
    int SIZE = 800;

    private final BufferedImage[] tileImages = {
            ImageIO.read(new File("TileImages/roadTexture_01.png")),  // Straight   (Vertical)
            ImageIO.read(new File("TileImages/roadTexture_18.png")),  // Turn       (North - East)
            ImageIO.read(new File("TileImages/roadTexture_29.png")),  // Threeway   (North - East - South)
            ImageIO.read(new File("TileImages/roadTexture_10.png")),  // Cross      (W, N, E and S)
            ImageIO.read(new File("TileImages/taxi.png")),            // Train
            ImageIO.read(new File("TileImages/pattern_16.png")),      // Station
    };


    public Gui() throws IOException {
        JFrame frame = new JFrame("Railroads Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(400, 0, SIZE, SIZE + 20);
        frame.add(this);
        frame.setVisible(true);
    }

    @Override
    public void run() {
        Timer timer = new Timer(16, e -> this.repaint());
        timer.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Tile[][] map = Game.getMap();
        int tileSize = SIZE / Game.getSize(); // Calculate tile size once

        for (int i = 0; i < Game.getSize(); i++) {
            for (int j = 0; j < Game.getSize(); j++) {
                Tile tile = map[i][j];
                if (tile != null) {
                    int x = j * tileSize; // Corrected to column (j) for x-coordinate
                    int y = i * tileSize; // Corrected to row (i) for y-coordinate

                    BufferedImage roadImage = tileImages[tile.getTypeIndex()];
                    Graphics2D g2d = (Graphics2D) g.create();

                    // Rotate around the center of the tile
                    int centerX = x + tileSize / 2;
                    int centerY = y + tileSize / 2;
                    g2d.rotate(Math.toRadians(90) * tile.getRotationIndex(), centerX, centerY);

                    // Draw the image
                    g2d.drawImage(roadImage, x, y, tileSize, tileSize, null);
                    g2d.dispose();
                }
            }
        }

        // Draw trains
        if (Game.TRAINS != null && !Game.getTrains().isEmpty()) {
            for (Train train : Game.TRAINS) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(Color.RED);

                // Draw train IDs
                int startX = train.getStartTile().getY() * tileSize + tileSize / 2;     // The difference in Y and X is because of the row = y and col = x
                int startY = train.getStartTile().getX() * tileSize + tileSize / 2;
                int endX = train.getEndTile().getY() * tileSize + tileSize / 2;
                int endY = train.getEndTile().getX() * tileSize + tileSize / 2;

                g2d.drawString(train.getId() + "", startX, startY);
                g2d.drawString(train.getId() + "", endX, endY);
                g2d.dispose();
            }
        }
    }


}
