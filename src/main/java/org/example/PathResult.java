package org.example;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PathResult {
    private int id;
    private boolean pathExists;
    private int pathCost;
    private List<Tile> path;
    private int distance;
    private int fitness;
    private Tile closestTileToEnd;
    private Set<Tile> visited;

    PathResult(boolean pathExists, int pathCost, List<Tile> path, int distance, Tile closestTileToEnd) {
        this.pathExists = pathExists;
        this.pathCost = pathCost;
        this.path = path;
        this.distance = distance;
        this.fitness = 0;
        this.visited = new HashSet<>();
        this.closestTileToEnd = closestTileToEnd;
    }
    //Copy Constructor
    PathResult(PathResult pathResult) {
        this.pathExists = pathResult.pathExists;
        this.pathCost = pathResult.pathCost;
        this.path = pathResult.path;
        this.distance = pathResult.distance;
        this.fitness = pathResult.fitness;
        this.id = pathResult.id;
        this.visited = new HashSet<>(pathResult.visited);
        this.closestTileToEnd = pathResult.closestTileToEnd;
    }

    public int getId() {return id;}
    public void setId(int id) {this.id = id;}
    public boolean isPathExists() { return pathExists; }
    public int getPathCost() {
        return pathCost;
    }
    public List<Tile> getPath() {
        return path;
    }
    public void setPathExists(boolean pathExists) {
        this.pathExists = pathExists;
    }
    public void setPathCost(int pathCost) {
        this.pathCost = pathCost;
    }
    public void setPath(List<Tile> path) {
        this.path = path;
    }
    public int getDistance() {
        return distance;
    }
    public void setDistance(int distance) {
        this.distance = distance;
    }
    public int getFitness() { return fitness; }
    public void setFitness(int fitness) { this.fitness = fitness; }
    public Set<Tile> getVisited() { return visited; }
    public void setVisited(Set<Tile> visited) { this.visited = visited; }
    public Tile getClosestTileToEnd() { return closestTileToEnd; }
    public void setClosestTileToEnd(Tile closestTileToEnd) {}
}