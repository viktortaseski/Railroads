package org.example;

import java.util.ArrayList;
import java.util.List;

public class Train {
    private Tile startTile;
    private Tile endTile;
    private PathResult bestResult;
    private int id;

    public Train(Tile startTile, Tile endTile) {
        this.startTile = startTile;
        this.endTile = endTile;
        this.id = 0;
        this.bestResult = new PathResult(false, 0, new ArrayList<>(), Integer.MAX_VALUE);
    }
    public Tile getStartTile() {
        return startTile;
    }
    public Tile getEndTile() {
        return endTile;
    }

    public PathResult getBestResult() {
        return bestResult;
    }
    public void setBestResult(PathResult bestResult) {
        this.bestResult = bestResult;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
}
