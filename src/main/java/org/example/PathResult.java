package org.example;

import java.util.List;

public class PathResult {
    private boolean pathExists;
    private int pathCost;
    private List<String> path;
    private int changes;

    PathResult(boolean pathExists, int pathCost, List<String> path, int changes) {
        this.pathExists = pathExists;
        this.pathCost = pathCost;
        this.path = path;
        this.changes = changes;
    }

    public boolean isPathExists() {
        return pathExists;
    }
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
    public int getChanges() {
        return changes;
    }
    public void setChanges(int changes) {
        this.changes = changes;
    }
}