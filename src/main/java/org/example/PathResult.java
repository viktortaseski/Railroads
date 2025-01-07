package org.example;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PathResult {
    private int id;
    private boolean pathExists;
    private int pathCost;
    private List<String> path;
    private int distance;
    private int fitness;
    private Set<Tile> visited;

    PathResult(boolean pathExists, int pathCost, List<String> path, int distance) {
        this.pathExists = pathExists;
        this.pathCost = pathCost;
        this.path = path;
        this.distance = distance;
        this.fitness = 0;
        this.visited = new HashSet<>();
    }
    PathResult(PathResult pathResult) {
        this.pathExists = pathResult.pathExists;
        this.pathCost = pathResult.pathCost;
        this.path = pathResult.path;
        this.distance = pathResult.distance;
        this.fitness = pathResult.fitness;
        this.id = pathResult.id;
        this.visited = new HashSet<>(pathResult.visited);
    }

    public int getId() {return id;}
    public void setId(int id) {this.id = id;}
    public boolean isPathExists() { return pathExists; }
    public int getPathCost() {
        return pathCost;
    }
    public List<String> getPath() {
        return path;
    }
    public void setPathExists(boolean pathExists) {
        this.pathExists = pathExists;
    }
    public void setPathCost(int pathCost) {
        this.pathCost = pathCost;
    }
    public void setPath(List<String> path) {
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
}