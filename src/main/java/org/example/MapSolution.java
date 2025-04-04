package org.example;
import java.io.Serializable;

public class MapSolution extends HelperFunctions implements Serializable {

    private Tile[][] mapLayout;
    private int mapCost;
    private int fitness;
    public PathResult pathResult;

    public MapSolution(Tile[][] mapLayout, int mapCost) {
        this.mapCost = mapCost;
        this.mapLayout = mapLayout;
        this.fitness = 0;
    }

    public void evaluateFitness() {
        // Lower fitness is better. We start at 200 so that fitness doesn't go negative.
        int fitness = 200;

        // For every train, add the fitness independently since each train has its own path.
        for (Train train : Game.TRAINS) {
            PathResult result = dfs(train, this);
            int distance = result.getDistance();
            int cost = result.getPathCost();
            boolean hasReachedEnd = result.isPathExists();
            train.setResult(result);

            if (!hasReachedEnd) {
                fitness += 100;   // Penalty for not reaching the end.
            }

            fitness += distance;                    // Penalty for solutions that are not close.
            fitness += result.getPath().size() ;    // Penalty for longer paths.
            fitness -= cost;                        // Reward exploration.
        }
        fitness += this.calculateMapCost();         // Reward low map costs since lower cost will be better fitness.

        this.setFitness(fitness);
    }

    public int calculateMapCost() {
        int cost = 0;
        // Goes through all map tiles and sums the cost.
        for (Tile[] tiles : mapLayout) {
            for (Tile tile : tiles) {
                if (tile.getType() != TileType.TRAIN &&
                        tile.getType() != TileType.STATION) {
                    cost += tile.getTypeIndex();
                }
            }
        }
        return cost;
    }

    public int getFitness() {
        return fitness;
    }
    public void setFitness(int fitness) {
        this.fitness = fitness;
    }
    public Tile[][] getMapLayout() {
        return mapLayout;
    }
    public void setMapLayout(Tile[][] mapLayout) {
        this.mapLayout = mapLayout;
    }
    public void setMapCost(int mapCost) {
        this.mapCost = mapCost;
    }
}
