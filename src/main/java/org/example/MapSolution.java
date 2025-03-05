package org.example;

import java.util.*;



public class MapSolution extends HelperFunctions {

    private Tile[][] mapLayout;
    private int mapCost;
    private int fitness;
    public PathResult pathResult;

    MapSolution(Tile[][] mapLayout, int mapCost, boolean pathExists) {
        this.mapCost = mapCost;
        this.mapLayout = mapLayout;
        this.fitness = 0;
    }

    public void evaluateFitness(){
        // Lower fitness is better starting is 10 so we don't go in '-'
        int fitness = 10;


        // For every train add the fitness independently since every train has different path.
        for (Train train: Game.TRAINS) {
            PathResult result = dfs(train, this);
            int distance = result.getDistance();
            int cost = result.getPathCost();
            boolean hasReachedEnd = result.isPathExists();
            train.setResult(result);

            if (!hasReachedEnd) {
                fitness += 100;                          // Penalty for not reaching the end.
            }

            fitness += distance;                        // Penalty for solutions that are not close
            //fitness += (result.getPath().size()-2) * 2;   // Penalty for longer paths
            fitness -= cost;                            // Reward exploration

        }
        fitness += this.calculateMapCost();             // Reward low map costs

        this.setFitness(fitness);
    }

    public int calculateMapCost() {
        int cost = 0;
        for (int x = 0; x < mapLayout.length; x++) {
            for (int y = 0; y < mapLayout[x].length; y++) {
                if (mapLayout[x][y].getType() != TileType.TRAIN && mapLayout[x][y].getType() != TileType.STATION) {
                    cost += mapLayout[x][y].getTypeIndex();
                }
            }
        }
        return cost;
    }

    public int getFitness() {return fitness;}
    public void setFitness(int fitness) {this.fitness = fitness;}
    public Tile[][] getMapLayout() {return mapLayout;}
    public void setMapLayout(Tile[][] mapLayout) {this.mapLayout = mapLayout;}
    public void setMapCost(int mapCost) {this.mapCost = mapCost;}


}
