package org.example;

public class Train {
    private final Tile startTile;
    private final Tile endTile;
    private PathResult bestResult;
    public int id;

    public Train(Tile startTile, Tile endTile) {
        this.startTile = startTile;
        this.endTile = endTile;
        this.id = 0;
        this.bestResult = new PathResult(false,0, null, 0, this.startTile);
    }
    public Tile getStartTile() {
        return startTile;
    }
    public Tile getEndTile() {
        return endTile;
    }

    public PathResult getResult() {
        return bestResult;
    }
    public void setResult(PathResult bestResult) {
        this.bestResult = bestResult;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
}
