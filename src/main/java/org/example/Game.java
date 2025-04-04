package org.example;

import java.io.Serializable;
import java.util.*;

public class Game implements Serializable {
    public static int size;
    private static int mode;
    private static int iterations;
    private static Tile[][] board;
    private static int numTrains;
    public static List<Train> TRAINS;
    private final long seed;

    public Game(int mode, int iterations, int size, int numTrains, long seed) {
        Game.mode = mode;
        Game.size = size;
        Game.iterations = iterations;
        board = new Tile[size][size];
        Game.numTrains = numTrains;
        TRAINS = new ArrayList<>();
        this.seed = seed;
    }

    public void init() {
        board = new Tile[size][size];
        TRAINS = new ArrayList<>();
        Random rand = new Random(seed);

        // Initialize board with random roads.
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                board[i][j] = Tile.randomRoad(i, j, rand);
            }
        }

        // Now we add the trains with their start and end locations
        // But we have to make sure all locations are unique
        Set<String> usedCoordinates = new HashSet<>(); // To track used coordinates
        for (int i = 0; i < numTrains; i++) {
            // Generate unique start coordinates for the train.
            int x = rand.nextInt(size);
            int y = rand.nextInt(size);
            while (usedCoordinates.contains(x + "," + y)) {
                x = rand.nextInt(size);
                y = rand.nextInt(size);
            }
            // update the used coordinates and add the start location of the train
            usedCoordinates.add(x + "," + y);
            Tile start = new Tile(x, y, Rotation.ZERO, TileType.TRAIN);
            board[x][y] = start;

            // Generate unique end coordinates for the station.
            do {
                x = rand.nextInt(size);
                y = rand.nextInt(size);
            } while (usedCoordinates.contains(x + "," + y));

            // Update coordinates and add the end location of the train
            usedCoordinates.add(x + "," + y);
            Tile end = new Tile(x, y, Rotation.ZERO, TileType.STATION);
            board[x][y] = end;

            // Create a train with unique start and end positions.
            Train train = new Train(start, end);
            train.setResult(new PathResult(false, 0, new ArrayList<>(), 0, train.getStartTile()));
            TRAINS.add(train);
        }

        // Add indexes to each train so we can differentiate them.
        int id = 0;
        for (Train train : TRAINS) {
            id++;
            train.setId(id);
        }
    }

    public static void changeTile(int x, int y, Tile tile) {
        board[x][y] = tile;
    }

    public static Tile[][] getMap() {
        return board;
    }

    public static int getSize() {
        return size;
    }

    public static int getIterations() {
        return iterations;
    }

    public static int getMode() {
        return mode;
    }

    public static void setBoard(Tile[][] board) {
        Game.board = board;
    }

    public static int getBoardFitness() {
        MapSolution map = new MapSolution(board, 0);
        map.evaluateFitness();
        return map.getFitness();
    }

    public static List<Train> getTrains() {
        return TRAINS;
    }
}
