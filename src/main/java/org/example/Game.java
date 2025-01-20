package org.example;

import java.util.*;

public class Game {
    public static int size;
    private static int mode;
    private static Tile[][] board;
    private static int numTrains;
    static List<Train> TRAINS;
    private final long seed; // Seed for consistent randomness

    public Game(int mode, int size, int numTrains, long seed) {
        Game.mode = mode;
        Game.size = size;
        board = new Tile[size][size];
        Game.numTrains = numTrains;
        TRAINS = new ArrayList<>();
        this.seed = seed; // Store the seed
    }

    public void init() {
        board = new Tile[size][size];
        TRAINS = new ArrayList<>();
        Random rand = new Random(seed); // Use the seed for consistent results

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                board[i][j] = Tile.randomRoad(i, j, rand); // Assuming Tile.randomRoad() is deterministic
            }
        }

        Set<String> usedCoordinates = new HashSet<>(); // To track used coordinates
        for (int i = 0; i < numTrains; i++) {
            // Generate unique start coordinates for the train
            int x = rand.nextInt(size);
            int y = rand.nextInt(size);
            while (usedCoordinates.contains(x + "," + y)) { // Ensure the coordinate is unique
                x = rand.nextInt(size);
                y = rand.nextInt(size);
            }
            usedCoordinates.add(x + "," + y); // Add the coordinate to the used set
            Tile start = new Tile(x, y, Rotation.ZERO, TileType.TRAIN);
            board[x][y] = start;

            // Generate unique end coordinates for the station
            x = rand.nextInt(size);
            y = rand.nextInt(size);
            while (usedCoordinates.contains(x + "," + y)) { // Ensure the coordinate is unique
                x = rand.nextInt(size);
                y = rand.nextInt(size);
            }
            usedCoordinates.add(x + "," + y); // Add the coordinate to the used set
            Tile end = new Tile(x, y, Rotation.ZERO, TileType.STATION);
            board[x][y] = end;

            // Create a train with unique start and end positions
            Train train = new Train(start, end);
            train.setResult(new PathResult(false, 0, new ArrayList<>(), 0, train.getStartTile()));
            TRAINS.add(train);
        }

        int index = 0;
        for (Train train : TRAINS) {
            index++;
            train.setId(index);
            System.out.println("Train[" + index + "] (" + train.getStartTile().getX() + ", " + train.getStartTile().getY() + ") (" + train.getEndTile().getX() + ", " + train.getEndTile().getY() + ")");
        }
    }

    public static Tile[][] getMap() {
        return board;
    }

    public static int getSize() {
        return size;
    }

    public static int getMode() {
        return mode;
    }

    public Tile[][] getBoard() {
        return board;
    }

    public static void setBoard(Tile[][] board) {
        Game.board = board;
    }

    public static int getNumTrains() {
        return numTrains;
    }

    public static List<Train> getTrains() {
        return TRAINS;
    }
}
