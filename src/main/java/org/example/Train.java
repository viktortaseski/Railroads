package org.example;

import java.util.ArrayList;
import java.util.List;

public class Train {
    private Tile startTile;
    private Tile endTile;
    private int pathCost;
    private List<String> path;
    private int id;

    public Train(Tile startTile, Tile endTile) {
        this.startTile = startTile;
        this.endTile = endTile;
        this.pathCost = 0;
        this.id = 0;
        this.path = new ArrayList<>();
    }
    public Tile getStartTile() {
        return startTile;
    }
    public Tile getEndTile() {
        return endTile;
    }
    public int getPathCost() {
        return pathCost;
    }

    public void setPathCost(int pathCost) {
        this.pathCost = pathCost;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public List<String> getPath() {
        return path;
    }
    public void setPath(List<String> path) {
        this.path = path;
    }
}
