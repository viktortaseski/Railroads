package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Tile {
    private int x;
    private int y;
    private Rotation rotation;
    private TileType type;
    private List<Tile> visitedByTrains;

    public Tile(int x, int y, Rotation rotation, TileType type) {
        this.x = x;
        this.y = y;
        this.rotation = rotation;
        this.type = type;
        visitedByTrains = new ArrayList<>();
    }

    public static Tile randomRoad(int x, int y, Random random) {
        // For type, it only can be a road therefore we have to return
        return new Tile(x, y, Rotation.values()[random.nextInt(Rotation.values().length)], TileType.values()[random.nextInt(TileType.values().length - 2)]);
    }

    public List<Tile> getNeighbors(Tile[][] map) {
        List<Tile> neighbors = new ArrayList<>();
        int rows = map.length;
        int cols = map[0].length;

        // Check bounds for all possible neighbors

        if (x > 0) neighbors.add(map[x - 1][y]); // North
        if (x < rows - 1) neighbors.add(map[x + 1][y]); // South
        if (y > 0) neighbors.add(map[x][y - 1]); // West
        if (y < cols - 1) neighbors.add(map[x][y + 1]); // East

        return neighbors;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public TileType getType() {
        return type;
    }
    public Rotation getRotation() {
        return rotation;
    }

    public void setType(TileType type) {
        this.type = type;
    }

    public void setRotation(Rotation rotation) {
        this.rotation = rotation;
    }

    public int getRotationIndex() {
        switch (rotation) {
            case ZERO -> {
                return 0;
            }
            case ONE -> {
                return 1;
            }
            case TWO -> {
                return 2;
            }
            case THREE -> {
                return 3;
            }
            case null, default -> {
                return -1;
            }
        }
    }

    public int getTypeIndex() {
        switch (type) {
            case STRAIGHT -> {
                return 0;
            }
            case TURN -> {
                return 1;
            }
            case THREEWAY -> {
                return 2;
            }
            case CROSS -> {
                return 3;
            }
            case TRAIN -> {
                return 4;
            }
            case STATION -> {
                return 5;
            }
            default -> {
                return -1;
            }

        }
    }
}
