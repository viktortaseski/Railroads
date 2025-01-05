package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

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
    Tile (Tile tile) {
        this.x = tile.x;
        this.y = tile.y;
        this.rotation = tile.rotation;
        this.type = tile.type;
        this.visitedByTrains = tile.visitedByTrains;
    }

    public static Tile randomRoad(int x, int y) {
        Random random = new Random();
        // For type, it only can be a road therefore we have to return
        return new Tile(x,y, Rotation.values()[random.nextInt(Rotation.values().length)], TileType.values()[random.nextInt(TileType.values().length-2)]);
    }

    public List<Tile> getNeighbors() {
        List<Tile> neighbors = new ArrayList<>();
        int rows = Game.getMap().length;
        int cols = Game.getMap()[0].length;

        // Check bounds for all possible neighbors
        if (x > 0) neighbors.add(Game.getMap()[x - 1][y]); // North
        if (x < rows - 1) neighbors.add(Game.getMap()[x + 1][y]); // South
        if (y > 0) neighbors.add(Game.getMap()[x][y - 1]); // West
        if (y < cols - 1) neighbors.add(Game.getMap()[x][y + 1]); // East

        return neighbors;
    }

    public List<Tile> getVisitedByTrains() {
        return visitedByTrains;
    }

    public void addVisited(Tile tile) {
        visitedByTrains.add(tile);
    }

    public boolean isStartTile(){
        return this.getType() == TileType.TRAIN ;
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
            case ZERO -> {
                return 0;
            }
            case ONE -> {
                return 1;
            }case TWO -> {
                return 2;
            }case THREE -> {
                return 3;
            }
            case null, default -> {
                return -1;
            }
        }
    }
    public int getTypeIndex(){
        switch (type) {
            case STRAIGHT -> {
                return 0;
            }case TURN -> {
                return 1;
            }case THREEWAY -> {
                return 2;
            }case CROSS -> {
                return 3;
            }case TRAIN -> {
                return 4;
            }case STATION -> {
                return 5;
            }default -> {
                return -1;
            }

        }
    }
}
