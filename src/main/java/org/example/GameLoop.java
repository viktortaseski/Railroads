package org.example;

import javax.swing.*;
import java.sql.SQLOutput;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GameLoop implements Runnable {
    public static Queue<Integer> events = new ConcurrentLinkedQueue<>();
    Tile tileToRemove = null;
    boolean enteredX = false;
    boolean enteredY = false;
    int x;
    int y;
    Rotation rotation = null;
    TileType tileType = null;
    Scanner scanner = new Scanner(System.in);
    int mode = 1;

    @Override
    public void run() {
        System.out.println("Starting GameLoop...");
        System.out.println("Enter game mode [1-Sequential, 2-Parallel, 3-Distributed]");
        mode = scanner.nextInt();
        if (mode > 0 && mode < 4) {
            System.out.println("Creating game... Mode: " + mode);
            Game game = new Game(10, 2);
            Evaluator evaluator = new Evaluator(game, mode);
            game.init();
        }else {
            System.out.println("Invalid mode entered.");
        }

        Timer timer = new Timer(16, e -> tick());
        timer.start();
    }

    private void tick() {
        Integer character = events.poll();  // Poll the event queue


        if (character != null) {

            // Handle other game logic (tile placement, etc.)
            if (character == (int) 's') {
                    System.out.println("Current mode: " + mode);  // Print the current mode
                    int score = Evaluator.evaluate();
                    System.out.println("Score: " + score);
            } else if (Integer.parseInt(character.toString()) < Game.size && !enteredX) {
                x = Integer.parseInt(character.toString());
                System.out.println("Successfully entered X = " + x);
                enteredX = true;
            } else if (Integer.parseInt(character.toString()) < Game.size && !enteredY) {
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

        // Proceed with tile placement if conditions are met
        if (enteredX && enteredY && rotation != null && tileType != null) {
            tileToRemove = new Tile(x, y, rotation, tileType);
            if (Game.getMap()[x][y].getType() != TileType.TRAIN && Game.getMap()[x][y].getType() != TileType.STATION) {
                Game.getMap()[x][y] = tileToRemove;
                System.out.println("Successfully entered a new Tile. Position (" + x + ", " + y + ") Rotation: " + rotation.name() + " TileType: " + tileType.name());
                tileToRemove = null;
                enteredX = false;
                enteredY = false;
                rotation = null;
                tileType = null;
            } else {
                System.out.println("This tile is a train or a station, please enter a new one.");
                tileToRemove = null;
                enteredX = false;
                enteredY = false;
                rotation = null;
                tileType = null;
            }
        }
    }


}
