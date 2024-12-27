package org.example;

import java.util.*;

public class Fitness {

    public static int evaluate(Tile[][] map) {
        int fitness = 0;

        for (Train train : Game.TRAINS) {
            PathResult result = findPath(map, train.startTile, train.endTile);
            train.pathCost = result.pathCost;
            train.setPath(result.path);

            if (!result.pathExists) {
                fitness -= 100; // Penalty for no path
            } else {
                fitness += 100; // Reward for path existence
                fitness -= result.pathCost; // Subtract cost
            }
        }

        return fitness;
    }

    private static PathResult findPath(Tile[][] map, Tile start, Tile end) {
        Set<Tile> visited = new HashSet<>();
        List<String> path = new ArrayList<>();
        return dfs(map, start, end, visited, 0, 0, path);
    }

    private static PathResult dfs(Tile[][] roadMap, Tile start, Tile end, Set<Tile> visited, int depth, int pathCost, List<String> path) {
        if (start.equals(end)) {
            return new PathResult(true, pathCost, new ArrayList<>(path)); // Path found
        }

        visited.add(start);

        Tile bestNeighbor = null;
        int minDistance = Integer.MAX_VALUE;


        for (Tile neighbor : start.getNeighbors()) {
            if (!visited.contains(neighbor) && isValid(neighbor, roadMap)) {
                int distance = calculateDistance(neighbor, end);
                if (distance < minDistance) {
                    bestNeighbor = neighbor;
                    minDistance = distance;
                }
            }
        }

        // If no valid connection neighbors found, check all other neighbors (with tile changes if necessary)
        if (bestNeighbor == null) {
            for (Tile neighbor : start.getNeighbors()) {
                if (!visited.contains(neighbor) && isValid(neighbor, roadMap) && isValidConnection(start, neighbor)) {
                    int distance = calculateDistance(neighbor, end);
                    if (distance < minDistance) {
                        bestNeighbor = neighbor;
                        minDistance = distance;
                    }
                }
            }
        }

        if (bestNeighbor != null) {
            Tile currentTile = roadMap[start.x][start.y];
            Tile neighborTile = roadMap[bestNeighbor.x][bestNeighbor.y];

            boolean requiresChange = !isValidConnection(currentTile, neighborTile);

            if (requiresChange) {
                Tile changedRoad = changeRoad(currentTile, neighborTile);
                roadMap[neighborTile.x][neighborTile.y] = changedRoad;
                Game.map[neighborTile.x][neighborTile.y] = changedRoad;
                pathCost += changedRoad.getTypeIndex();
            }

            path.add(determineMove(start, bestNeighbor)); // Add move to path
            bestNeighbor.visitedByTrain = true;

            PathResult result = dfs(roadMap, neighborTile, end, visited, depth + 1, pathCost, path);
            if (result.pathExists) {
                return result;
            }

            path.remove(path.size() - 1); // Backtrack
        }

        return new PathResult(false, pathCost, path); // No path found
    }

    private static int calculateDistance(Tile a, Tile b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    private static String determineMove(Tile from, Tile to) {
        if (to.x == from.x && to.y == from.y - 1) return "N";
        if (to.x == from.x && to.y == from.y + 1) return "S";
        if (to.x == from.x - 1 && to.y == from.y) return "W";
        if (to.x == from.x + 1 && to.y == from.y) return "E";
        return ""; // Invalid move
    }

    private static boolean isValid(Tile tile, Tile[][] roadMap) {
        return tile.getX() >= 0 && tile.getX() < roadMap.length &&
                tile.getY() >= 0 && tile.getY() < roadMap[0].length &&
                tile.getType() != TileType.TRAIN; // Avoid TRAIN tiles
    }

    private static Tile changeRoad(Tile currentTile, Tile neighborTile) {
        TileType neighborType = neighborTile.getType();


        if (neighborType == TileType.STRAIGHT || neighborType == TileType.TURN || neighborType == TileType.THREEWAY) {
            if (needsThreeWayConnection(currentTile, neighborTile)) {
                if (neighborType != TileType.THREEWAY) {
                    neighborTile.setType(TileType.THREEWAY);
                    adjustRotationForThreeWay(currentTile, neighborTile);
                }
            }
        }

        for (int i = 0; i < TileType.values().length - 2; i++) {
            for (int j = 0; j < Rotation.values().length; j++) {
                Tile tempTile = new Tile(neighborTile.x, neighborTile.y, Rotation.values()[j], TileType.values()[i]);
                if (isValidConnection(currentTile, tempTile)) {
                    return tempTile;
                }
            }
        }

        neighborTile.setType(TileType.CROSS);
        return neighborTile;
    }

    private static boolean needsThreeWayConnection(Tile currentTile, Tile neighborTile) {
        // Logic to determine if a three-way connection is needed
        if (neighborTile.getType() == TileType.CROSS || neighborTile.getType() == TileType.THREEWAY || neighborTile.getType() == TileType.TRAIN || neighborTile.getType() == TileType.STATION) {
            return false;
        }
        return isValidConnection(currentTile, neighborTile); // If there doesn't exist a connection
    }

    private static void adjustRotationForThreeWay(Tile currentTile, Tile neighborTile) {
        List<Tile> neighbors = neighborTile.getNeighbors();
        List<Tile> neighboursConnectedToNeighbor = new ArrayList<>();
        for (Tile neighbor : neighbors) {
            if (isValidConnection(neighborTile, neighbor)) {
                neighboursConnectedToNeighbor.add(neighbor);
            }
        }

        // Rotate the neighborTile until all conditions are satisfied
        int maxRotations = 4; // Assuming each tile has 4 possible rotations (e.g., 0°, 90°, 180°, 270°)
        for (int i = 0; i < maxRotations; i++) {
            boolean allValidConnections = true;

            // Check if the neighborTile is validly connected to all neighboursConnectedToNeighbor
            for (Tile connectedNeighbor : neighboursConnectedToNeighbor) {
                if (!isValidConnection(neighborTile, connectedNeighbor)) {
                    allValidConnections = false;
                    break;
                }
            }

            // Check if the neighborTile is validly connected to the currentTile
            if (!isValidConnection(currentTile, neighborTile)) {
                allValidConnections = false;
            }

            // If all connections are valid, stop rotating
            if (allValidConnections) {
                break;
            }

            // Rotate the neighborTile for the next iteration
            neighborTile.setRotation(Rotation.values()[i]);
            Game.map[neighborTile.x][neighborTile.y] = neighborTile;
        }
    }


    public static boolean isValidConnection(Tile current, Tile neighbor) {
        int currentX = current.x;
        int currentY = current.y;
        int neighborX = neighbor.x;
        int neighborY = neighbor.y;

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

    public static class PathResult {
        boolean pathExists;
        int pathCost;
        List<String> path;

        PathResult(boolean pathExists, int pathCost, List<String> path) {
            this.pathExists = pathExists;
            this.pathCost = pathCost;
            this.path = path;
        }
    }

}