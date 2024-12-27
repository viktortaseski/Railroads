package org.example;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Gui extends JPanel implements Runnable {

    private final BufferedImage[] tileImages = {
            ImageIO.read(new File("TileImages/roadTexture_01.png")),  // Straight   (Vertical)
            ImageIO.read(new File("TileImages/roadTexture_18.png")),  // Turn       (North - East)
            ImageIO.read(new File("TileImages/roadTexture_29.png")),  // Threeway   (West - North - East)
            ImageIO.read(new File("TileImages/roadTexture_10.png")),  // Cross      (W, N, E and S)
            ImageIO.read(new File("TileImages/taxi.png")),            // Train
            ImageIO.read(new File("TileImages/pattern_16.png")),      // Station
    };


    public Gui() throws IOException {
        JFrame frame = new JFrame("Railroads Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(400, 0, 800, 830);
        frame.setResizable(false);
        frame.add(this);
        frame.setVisible(true);
    }

    @Override
    public void run() {
        Timer timer = new Timer(16, e -> this.repaint());
        timer.start();
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        for (int i = 0; i < Game.size; i++) {
            for (int j = 0; j < Game.size; j++) {
                Tile tile = Game.map[i][j];
                if ( tile != null ) {
                    int x = tile.getX() * 40;
                    int y = tile.getY() * 40;
                    BufferedImage roadImage = tileImages[tile.getTypeIndex() - 1 ];
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.rotate(Math.toRadians(90) * tile.getRotationIndex(), x + 20, y + 20);
                    g2d.drawImage(roadImage, x, y, 40, 40, null);
                    g2d.dispose();
                }
            }
        }

        // Check if the list of trains is not empty
        if (Game.TRAINS != null && !Game.TRAINS.isEmpty()) {
            // If the list is not empty, proceed with the for loop
            for (Train train : Game.TRAINS) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(Color.RED);
                g2d.drawString(train.id + "", train.startTile.x * 40, train.startTile.y * 40 + 40);
                g2d.drawString(train.id + "", train.endTile.x * 40, train.endTile.y * 40 + 40);
                g2d.dispose();
            }
        }

    }
}
