package org.example;

import java.util.*;

public class Fitness {

    public static PathResult evaluate(Tile[][] map, Train train) {
        int fitness = 0;
        PathResult result = new PathResult(false, 0, new ArrayList<>());

        result.setPathCost(fitness);
        result = findPath(map, train.getStartTile(), train.getEndTile());
        train.setPathCost(result.getPathCost());
        train.setPath(result.getPath());

        if (!result.isPathExists()) {
            fitness -= 100; // Penalty for no path
        } else {
            fitness += 100; // Reward for path existence
            fitness -= result.getPathCost(); // Subtract cost
        }

        result.setPathCost(fitness);
        return result;
    }

    private static PathResult findPath(Tile[][] map, Tile start, Tile end) {
        Set<Tile> visited = new HashSet<>();
        List<String> path = new ArrayList<>();
        return dfs(map, start, end, visited, 0, path);
    }

    private static PathResult dfs(Tile[][] roadMap, Tile current, Tile end, Set<Tile> visited, int pathCost, List<String> path) {
        if (current.equals(end)) {
            return new PathResult(true, pathCost, new ArrayList<>(path)); // Return a copy of the path
        }

        visited.add(current);
        List<Tile> neighbors = new ArrayList<>(current.getNeighbors());

        // Sort neighbors based on the distance to the target tile
        neighbors.sort(Comparator.comparingInt(neighbor -> calculateDistance(neighbor, end)));

        PathResult result;
        for (Tile neighbor : neighbors) {
            // First check the roads that already are connected, not visited and, they are actually a valid Tile
            if (!visited.contains(neighbor) && isValid(neighbor, roadMap) && isValidConnection(current, neighbor)) {
                path.add(determineMove(current, neighbor));
                neighbor.addVisited(current);
                // Add to path and continue searching...
                result = dfs(roadMap, neighbor, end, visited, pathCost + neighbor.getTypeIndex(), path);
                if (result.isPathExists()) {
                    return result;
                }
                path.remove(path.size() - 1); // Backtrack
            } else if (!visited.contains(neighbor) && isValid(neighbor, roadMap)) {
                // Modify the road such that it connects to the neighbor
                Tile temp = changeRoad(current, neighbor);
                roadMap[neighbor.getX()][neighbor.getY()] = temp;

                neighbor.addVisited(current);
                pathCost += neighbor.getTypeIndex();    // Since we performed a change we add cost.

                path.add(determineMove(current, neighbor)); // Add to path
                result = dfs(roadMap, neighbor, end, visited, pathCost, path);  // Continue searching...

                if (result.isPathExists()) {
                    return result;
                }
                path.remove(path.size() - 1); // Backtrack
            }
        }

        visited.remove(current); // Allow revisiting this tile in a different path
        return new PathResult(false, pathCost, path); // No path found
    }

    private static int calculateDistance(Tile a, Tile b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }

    private static String determineMove(Tile from, Tile to) {
        if (to.getX() == from.getX() && to.getY() == from.getY() - 1) return "N";
        if (to.getX() == from.getX() && to.getY() == from.getY() + 1) return "S";
        if (to.getX() == from.getX() - 1 && to.getY() == from.getY()) return "W";
        if (to.getX() == from.getX() + 1 && to.getY() == from.getY()) return "E";
        return ""; // Invalid move
    }

    private static boolean isValid(Tile tile, Tile[][] roadMap) {
        return tile.getX() >= 0 && tile.getX() < roadMap.length &&
                tile.getY() >= 0 && tile.getY() < roadMap[0].length &&
                tile.getType() != TileType.TRAIN; // Avoid TRAIN tiles
    }

    // Returns the changed neighbourTile that connects to the current and to all previously connected tiles.
    private static Tile changeRoad(Tile currentTile, Tile neighborTile) {
        // Iterate through tile types and rotations to find a valid configuration
        for (int i = 0; i < TileType.values().length - 2; i++) {
            for (int j = 0; j < Rotation.values().length; j++) {
                neighborTile.setRotation(Rotation.values()[j]);
                neighborTile.setType(TileType.values()[i]);
                boolean allConnectionsValid = true;
                // Check if the tempTile has valid connections to all trains
                if (neighborTile.getVisitedByTrains() != null) {
                    for (Tile road : neighborTile.getVisitedByTrains()) {
                        if (!isValidConnection(road, neighborTile)) {
                            allConnectionsValid = false;
                            break;
                        }
                    }
                }
                // If all connections are valid and tempTile is valid for the currentTile
                if (allConnectionsValid && isValidConnection(currentTile, neighborTile)) {
                    return neighborTile;
                }
            }
        }
        // If no valid configuration is found, set the tile to CROSS
        neighborTile.setType(TileType.CROSS);
        return neighborTile;
    }


    public static boolean isValidConnection(Tile current, Tile neighbor) {
        int currentX = current.getX();
        int currentY = current.getY();
        int neighborX = neighbor.getX();
        int neighborY = neighbor.getY();

        // Special cases for TRAIN and STATION
        if (neighbor.getType() == TileType.TRAIN || current.getType() == TileType.TRAIN) {
            return false; // Cannot connect to a train
        }
        if (neighbor.getType() == TileType.STATION || current.getType() == TileType.STATION) {
            return true; // Always connect to a station
        }

        // Determine the direction of the neighbor relative to the current tile
        int dir;
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

        // Map rotations to the connections they enable
        int[][] connections = new int[][]{
                {0b0101, 0b1010, 0b0101, 0b1010}, // STRAIGHT
                {0b0011, 0b0110, 0b1100, 0b1001}, // TURN
                {0b0111, 0b1110, 0b1101, 0b1011}, // THREEWAY
                {0b1111, 0b1111, 0b1111, 0b1111}  // CROSS
        };

        // Validate indices
        int currentType = current.getTypeIndex() - 1;
        int neighborType = neighbor.getTypeIndex() - 1;
        int currentRotation = current.getRotationIndex();
        int neighborRotation = neighbor.getRotationIndex();

        if (currentType < 0 || currentType >= connections.length ||
                neighborType < 0 || neighborType >= connections.length) {
            System.out.println("Invalid connection: TileType index out of bounds " + currentType + " " + neighborType);
            return false; // Invalid type indices
        }

        if (currentRotation < 0 || currentRotation >= 4 || neighborRotation < 0 || neighborRotation >= 4) {
            System.out.println("Invalid connection: Rotation index out of bounds");
            return false; // Invalid rotation indices
        }

        // Check if the connection is valid in both directions
        boolean currentToNeighbor = (connections[currentType][currentRotation] & (1 << dir)) != 0;
        boolean neighborToCurrent = (connections[neighborType][neighborRotation] & (1 << oppDir)) != 0;

        return currentToNeighbor && neighborToCurrent;
    }


}