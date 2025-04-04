package org.example;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PathResult implements Serializable {
    private int id;
    private boolean pathExists;
    private int pathCost;
    private List<Tile> path;
    private int distance;

    PathResult(boolean pathExists, int pathCost, List<Tile> path, int distance, Tile closestTileToEnd) {
        this.pathExists = pathExists;
        this.pathCost = pathCost;
        this.path = path;
        this.distance = distance;
    }

    public boolean isPathExists() { return pathExists; }
    public int getPathCost() { return pathCost; }
    public List<Tile> getPath() {
        return path;
    }
    public int getDistance() {
        return distance;
    }

}