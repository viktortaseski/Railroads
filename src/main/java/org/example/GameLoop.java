package org.example;

import javax.swing.*;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GameLoop implements Runnable {
    public static Queue<Integer> events = new ConcurrentLinkedQueue<>();
    Tile replacementTile = null;
    boolean enteredX = false;
    boolean enteredY = false;
    int x;
    int y;
    Rotation rotation = null;
    TileType tileType = null;
    Game game;
    boolean hasEnded = false;

    GameLoop(Game game) {
        this.game = game;
    }

    @Override
    public void run() {
        System.out.println("Starting GameLoop...");
        // Timer is to update the map changes in case of customizing the map
        // Also to constantly listen to any input.
        Timer timer = new Timer(16, e -> tick());
        timer.start();
    }

    private void tick() {
        Integer character = events.poll();  // Poll the event queue
        if (character != null) {
            // Handle other game logic (tile placement, etc.)
            if (character == (int) 's') {
                GeneticAlgorithm.start();
                hasEnded = true;
            }else if (Integer.parseInt(character.toString()) < Game.getSize() && !enteredX) {
                x = Integer.parseInt(character.toString());
                System.out.println("Successfully entered X = " + x);
                enteredX = true;
            } else if (Integer.parseInt(character.toString()) < Game.getSize() && !enteredY) {
                y = Integer.parseInt(character.toString());
                System.out.println("Successfully entered Y = " + y);
                enteredY = true;
            } else if (Integer.parseInt(character.toString()) < Rotation.values().length && rotation == null) {
                rotation = Rotation.values()[Integer.parseInt(character.toString())];
                System.out.println("Successfully entered Rotation. -> " + rotation.name());
            } else if (Integer.parseInt(character.toString()) < TileType.values().length - 2 && tileType == null) {
                tileType = TileType.values()[Integer.parseInt(character.toString())];
                System.out.println("Successfully entered TileType. -> " + tileType.name());
            }else {
                System.out.println("The input '" + Integer.parseInt(character.toString()) + "' is invalid. Try again.");
            }
        }

        // Proceed with tile placement if all conditions are met.
        if (enteredX && enteredY && rotation != null && tileType != null) {
            // This is actually the tile that replaces the tile that is removed.
            replacementTile = new Tile(x, y, rotation, tileType);
            if (Game.getMap()[x][y].getType() != TileType.TRAIN && Game.getMap()[x][y].getType() != TileType.STATION) {
                Game.getMap()[x][y] = replacementTile;
                Game.changeTile(x, y, replacementTile);
                System.out.println("Successfully entered a new Tile. Position (" + x + ", " + y + ") Rotation: " + rotation.name() + " TileType: " + tileType.name());
            } else {
                System.out.println("This tile is a train or a station, please enter a new one.");
            }
            // Reset the tileToReplace so it is ready for the next input
            replacementTile = null;
            enteredX = false;
            enteredY = false;
            rotation = null;
            tileType = null;
        }
    }


}
