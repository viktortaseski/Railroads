package org.example;
import java.util.*;

import static org.example.GeneticAlgorithm.updateTileConnection;

class Fitness {

    // Map rotations to the connections they enable
    static int[][] connections = new int[][]{
            {0b0101, 0b1010, 0b0101, 0b1010}, // STRAIGHT
            {0b0011, 0b0110, 0b1100, 0b1001}, // TURN
            {0b0111, 0b1110, 0b1101, 0b1011}, // THREEWAY
            {0b1111, 0b1111, 0b1111, 0b1111}  // CROSS
    };

    public static PathResult evaluate(Tile[][] map, Train train) {
        PathResult result = findPath(map, train.getStartTile(), train.getEndTile());
        int fitness = result.isPathExists() ? 100 - result.getPathCost() : -100;

        train.setPath(result.getPath());
        train.setPathCost(fitness);
        return result;
    }

    private static PathResult findPath(Tile[][] map, Tile start, Tile end) {
        Set<Tile> visited = new HashSet<>();
        List<String> path = new ArrayList<>();
        return dfs(map, start, end, visited, 0, path, 0);
    }

    private static PathResult dfs(Tile[][] map, Tile current, Tile end, Set<Tile> visited, int cost, List<String> path, int changes) {
        // Base case: invalid start or end tile
        if (current == null || end == null || !isValidTile(current) || !isValidTile(end)) {
            return new PathResult(false, Integer.MAX_VALUE, new ArrayList<>(), Integer.MAX_VALUE);
        }

        // Log current state
        System.out.println("Visiting: (" + current.getX() + ", " + current.getY() + ") Current Path: " + path);

        // Base case: reached the end tile
        if (current.equals(end)) {
            return new PathResult(true, cost, new ArrayList<>(path), changes);
        }

        visited.add(current); // Mark the current tile as visited

        // Get neighbors and sort by heuristic (distance to end)
        List<Tile> neighbors = new ArrayList<>(current.getNeighbors());
        neighbors.sort(Comparator.comparingInt(neighbor -> calculateDistance(neighbor, end)));

        // Iterate through neighbors
        for (Tile neighbor : neighbors) {
            if (visited.contains(neighbor)) continue; // Skip already visited tiles

            String move = determineMove(current, neighbor);
            if (isValidConnection(current, neighbor)) {
                // Valid connection: proceed without changing tile
                path.add(move);
                neighbor.addVisited(current);
                PathResult result = dfs(map, neighbor, end, visited, cost, path, changes);
                if (result.isPathExists()) {
                    return result; // Path found
                }
                path.remove(path.size() - 1); // Backtrack
            } else {
                // Invalid connection: modify the tile and increase cost
                path.add(move);
                updateTileConnection(current, neighbor);
                changes++;
                PathResult result = dfs(map, neighbor, end, visited, cost + neighbor.getTypeIndex(), path, changes);
                if (result.isPathExists()) {
                    return result; // Path found
                }
                path.remove(path.size() - 1); // Backtrack
            }
        }

        visited.remove(current); // Backtrack: unmark the current tile
        return new PathResult(false, cost, new ArrayList<>(path), changes); // No path found
    }


    private static int calculateDistance(Tile a, Tile b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }

    private static boolean isValidTile(Tile tile) {
        return tile.getX() >= 0 && tile.getX() < Game.size && tile.getY() >= 0 && tile.getY() < Game.size;
    }

    private static String determineMove(Tile from, Tile to) {
        if (to.getX() == from.getX() && to.getY() == from.getY() - 1) return "N";
        if (to.getX() == from.getX() && to.getY() == from.getY() + 1) return "S";
        if (to.getX() == from.getX() - 1 && to.getY() == from.getY()) return "W";
        if (to.getX() == from.getX() + 1 && to.getY() == from.getY()) return "E";
        return "";
    }

    public static boolean isValidConnection(Tile current, Tile neighbor) {
        int currentX = current.getX();
        int currentY = current.getY();
        int neighborX = neighbor.getX();
        int neighborY = neighbor.getY();

        // Determine the direction of the neighbor relative to the current tile
        int dir = -1;
        if (neighborX == currentX + 1) {
            dir = 1; // Right
        } else if (neighborX == currentX - 1) {
            dir = 3; // Left
        } else if (neighborY == currentY + 1) {
            dir = 2; // Down
        } else if (neighborY == currentY - 1) {
            dir = 0; // Up
        } else {
            return false; // Neighbor is not adjacent
        }

        // Opposite direction
        int oppDir = (dir + 2) % 4;

        if (neighbor.getType() == TileType.TRAIN) {
            return false; // Cannot connect to a TRAIN in any case except the first move
        }

        // Special handling for TRAIN tile
        if (current.getType() == TileType.TRAIN) {
            // Check if this is the starting move (neighbor is not visited yet)
            boolean isFirstMove = true; // This should be determined based on context (e.g., using a visited set or other logic)

            if (isFirstMove) {
                // Treat TRAIN as a CROSS only on the first move it can go any direction.
                boolean currentToNeighbor = (0b1111 & (1 << dir)) != 0;
                boolean neighborToCurrent;
                if (neighbor.getType() == TileType.STATION){
                    neighborToCurrent = true;
                } else if (neighbor.getType() == TileType.TRAIN) {
                    neighborToCurrent = false;
                } else{
                    neighborToCurrent = (connections[neighbor.getTypeIndex()][neighbor.getRotationIndex()] & (1 << oppDir)) != 0;
                }
                return currentToNeighbor && neighborToCurrent;
            } else {
                // Any other encounter with a TRAIN tile is invalid
                return false;
            }
        }


        if (neighbor.getType() == TileType.STATION) {
            return (0b1111 & (1 << dir)) != 0;  // Just check if the current tile is connecting to the station.
        }


        // Validate indices for non-TRAIN tiles
        int currentType = current.getTypeIndex();
        int neighborType = neighbor.getTypeIndex();
        int currentRotation = current.getRotationIndex();
        int neighborRotation = neighbor.getRotationIndex();

        if (currentType < 0 || currentType >= connections.length ||
                neighborType < 0 || neighborType >= connections.length ||
                currentRotation < 0 || currentRotation >= 4 || neighborRotation < 0 || neighborRotation >= 4) {
            System.out.println("Invalid connection: Indices out of bounds.");
            return false;
        }

        // Check if the connection is valid in both directions
        boolean currentToNeighbor = (connections[currentType][currentRotation] & (1 << dir)) != 0;
        boolean neighborToCurrent = (connections[neighborType][neighborRotation] & (1 << oppDir)) != 0;

        return currentToNeighbor && neighborToCurrent;
    }


}