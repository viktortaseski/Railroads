package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Tile {
    int x;
    int y;
    Rotation rotation;
    TileType type;
    boolean visitedByTrain = false;

    public Tile(int x, int y, Rotation rotation, TileType type) {
        this.x = x;
        this.y = y;
        this.rotation = rotation;
        this.type = type;
    }

    public static Tile randomRoad(int x, int y) {
        Random random = new Random();
        // For type, it only can be a road therefore we have to return
        return new Tile(x,y, Rotation.values()[random.nextInt(Rotation.values().length)], TileType.values()[random.nextInt(TileType.values().length-2)]);
    }

    public List<Tile> getNeighbors() {
        List<Tile> neighbors = new ArrayList<>();
        int rows = Game.map.length;
        int cols = Game.map[0].length;

        // Check bounds for all possible neighbors
        if (x > 0) neighbors.add(Game.map[x - 1][y]); // North
        if (x < rows - 1) neighbors.add(Game.map[x + 1][y]); // South
        if (y > 0) neighbors.add(Game.map[x][y - 1]); // West
        if (y < cols - 1) neighbors.add(Game.map[x][y + 1]); // East

        return neighbors;
    }


    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }

    // Don't need to set the x and y because we just change its type or rotation.

    public Rotation getRotation() {
        return rotation;
    }
    public TileType getType() {
        return type;
    }
    public void setType(TileType type) {
        this.type = type;
    }
    public void setRotation(Rotation rotation) {
        this.rotation = rotation;
    }
    public int getRotationIndex(){
        switch (rotation) {
            case ONE -> {
                return 1;
            }case TWO -> {
                return 2;
            }case THREE -> {
                return 3;
            }
            case null, default -> {
                return 0;
            }
        }
    }
    public int getTypeIndex(){
        switch (type) {
            case STRAIGHT -> {
                return 1;
            }case TURN -> {
                return 2;
            }case THREEWAY -> {
                return 3;
            }case CROSS -> {
                return 4;
            }case TRAIN -> {
                return 5;
            }case STATION -> {
                return 6;
            }default -> {
                return 0;
            }

        }
    }
}
