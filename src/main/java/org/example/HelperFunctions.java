package org.example;

import java.util.*;

class HelperFunctions {

    // Map rotations to the connections they enable
    static int[][] connections = new int[][]{
            {0b0101, 0b1010, 0b0101, 0b1010}, // STRAIGHT
            {0b0011, 0b0110, 0b1100, 0b1001}, // TURN
            {0b0111, 0b1110, 0b1101, 0b1011}, // THREEWAY
            {0b1111, 0b1111, 0b1111, 0b1111}  // CROSS
    };

    // Idea is to find the best path for the given train and return the map.
    // Idea is to find the best path for the given train and return the map.
    public static PathResult dfs(Train train, MapSolution solution) {

        Tile[][] map = solution.getMapLayout();
        PathResult pathSolution = train.getResult();
        solution.pathResult = pathSolution;

        if (pathSolution == null) {
            pathSolution = new PathResult(false, 0, new ArrayList<>(), 0, train.getStartTile());
            train.setResult(pathSolution);
            solution.pathResult = pathSolution;
        }

        List<Tile> path = new ArrayList<>();
        Tile end = train.getEndTile();
        Tile start = train.getStartTile();
        HashSet<Tile> visited = new HashSet<>();
        Stack<Tile> stack = new Stack<>();

        // Stack will hold the tile and its path up to this point
        stack.push(start);

        while (!stack.isEmpty()) {
            Tile current = stack.peek();

            // If we have reached the end tile
            if (current.equals(end) || (current.getX() == end.getX() && current.getY() == end.getY())) {
                path.add(current);
                return new PathResult(true, path.size(), new ArrayList<>(path),
                        calculateDistance(path.get(path.size() - 1), end),
                        path.get(path.size() - 1));
            }

            if (!visited.contains(current)) {
                visited.add(current);
                path.add(current);
            }

            boolean hasUnvisitedNeighbor = false;

            // Explore neighbors
            for (Tile neighbor : current.getNeighbors(map)) {
                if (!visited.contains(neighbor) && isValidConnection(current, neighbor) && isValidTile(neighbor)) {
                    stack.push(neighbor); // Add the neighbor to the stack
                    hasUnvisitedNeighbor = true;
                    break; // Continue DFS with this neighbor
                }
            }

            // If no unvisited neighbors exist, backtrack
            if (!hasUnvisitedNeighbor) {
                stack.pop();
                path.remove(path.size() - 1); // Remove the current tile from the path
            }
        }

        // If no path is found
        return new PathResult(false, path.size(), new ArrayList<>(path),
                calculateDistance(path.isEmpty() ? start : path.get(path.size() - 1), end),
                path.isEmpty() ? null : path.get(path.size() - 1));
    }

    private static int calculateDistance(Tile a, Tile b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }

    static boolean isValidTile(Tile tile) {
        return tile.getX() >= 0 && tile.getX() < Game.size && tile.getY() >= 0 && tile.getY() < Game.size;
    }


    public static boolean isValidConnection(Tile current, Tile neighbor) {
        int currentX = current.getX();
        int currentY = current.getY();
        int neighborX = neighbor.getX();
        int neighborY = neighbor.getY();

        // Determine the direction of the neighbor relative to the current tile
        int dir;
        if (neighborY == currentY + 1) {
            dir = 1; // Right
        } else if (neighborY == currentY - 1) {
            dir = 3; // Left
        } else if (neighborX == currentX + 1) {
            dir = 2; // Down
        } else if (neighborX == currentX - 1) {
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
                if (neighbor.getType() == TileType.STATION) {
                    neighborToCurrent = true;
                } else if (neighbor.getType() == TileType.TRAIN) {
                    neighborToCurrent = false;
                } else {
                    neighborToCurrent = (connections[neighbor.getTypeIndex()][neighbor.getRotationIndex()] & (1 << oppDir)) != 0;
                }
                return currentToNeighbor && neighborToCurrent;
            } else {
                // Any other encounter with a TRAIN tile is invalid
                return false;
            }
        }


        // Validate indices for non-TRAIN tiles
        int currentType = current.getTypeIndex();
        int neighborType = neighbor.getTypeIndex();
        int currentRotation = current.getRotationIndex();
        int neighborRotation = neighbor.getRotationIndex();

        //
        if (current.getType() == TileType.STATION && neighbor.getType() != TileType.TRAIN && neighbor.getType() != TileType.STATION) {
            return (connections[neighborType][neighborRotation] & (1 << oppDir)) != 0;  // Just check if the current tile is connecting to the station.
        }

        if (neighbor.getType() == TileType.STATION && current.getType() != TileType.TRAIN && current.getType() != TileType.STATION) {
            return (connections[currentType][currentRotation] & (1 << dir)) != 0;
        }

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

    public static Tile[][] createRandomMap(Train train, int size) {
        Random rand = new Random();
        Tile[][] map = new Tile[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                map[i][j] = Tile.randomRoad(i, j, rand); // Assuming Tile.randomRoad() is deterministic
            }
        }
        for (Train train1 : Game.TRAINS) {
            Tile start = map[train1.getStartTile().getX()][train1.getStartTile().getY()];
            Tile end = map[train1.getEndTile().getX()][train1.getEndTile().getY()];
            start.setType(TileType.TRAIN);
            end.setType(TileType.STATION);
            start.setRotation(Rotation.ZERO);
            end.setRotation(Rotation.ZERO);
        }

        return map;
    }

}