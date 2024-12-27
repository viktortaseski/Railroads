package org.example;

import java.util.List;

public class PathResult {
    boolean pathExists;
    int pathCost;
    List<String> path;

    PathResult(boolean pathExists, int pathCost, List<String> path) {
        this.pathExists = pathExists;
        this.pathCost = pathCost;
        this.path = path;
    }
}