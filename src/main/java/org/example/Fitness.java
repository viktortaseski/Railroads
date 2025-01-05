package org.example;

import java.util.*;

import static org.example.GeneticAlgorithm.copyMap;
import static org.example.GeneticAlgorithm.updateTileConnection;


class Fitness {

    // Map rotations to the connections they enable
    static int[][] connections = new int[][]{
            {0b0101, 0b1010, 0b0101, 0b1010}, // STRAIGHT
            {0b0011, 0b0110, 0b1100, 0b1001}, // TURN
            {0b0111, 0b1110, 0b1101, 0b1011}, // THREEWAY
            {0b1111, 0b1111, 0b1111, 0b1111}  // CROSS
    };

    public static PathResult evaluate(PathResult solution, Train train) {
        int fitness = solution.getFitness();
        if (solution.isPathExists()) {
            fitness += 1000;
        }
        fitness -= solution.getPathCost();
        fitness -= solution.getDistance();  // Reward closer to station (Smaller the distance smaller the subtraction)
        solution.setFitness(fitness);
        train.setBestResult(solution);
        return solution;
    }

    public static void setFitness(PathResult solution, Train train) {
        Tile current = train.getStartTile();
        Tile[][] map = copyMap(Game.getMap());
        for (String direction : solution.getPath()){
            Tile next = GeneticAlgorithm.getNextTile(map, current, direction);
            if (next == null) {
                return;
            }

            if (!isValidConnection(current, next)) {
                if (current.getType() != TileType.TRAIN){
                    solution.setPathCost(solution.getPathCost() + calculateUpdateCost(current, next));
                }
            }
            current = next;
        }
        solution.setDistance(calculateDistance(current, train.getEndTile()));
        if (current.equals(train.getEndTile())){
            solution.setPathExists(true);
        }
    }

    static PathResult findPath(Tile[][] map, Train train) {
        Set<Tile> visited = new HashSet<>();
        List<String> path = new ArrayList<>();
        Tile startTile = train.getStartTile();
        Tile endTile = train.getEndTile();
        PathResult pathResult = new PathResult(false, 0, path, calculateDistance(startTile, endTile));
        return dfs(map, startTile, endTile, visited, pathResult.getPathCost(), pathResult);
    }

    private static PathResult dfs(Tile[][] map, Tile current, Tile end, Set<Tile> visited, int cost, PathResult solution) {
        // Base case: invalid start or end tile
        List<String> path = solution.getPath();
        if (current == null || end == null || !isValidTile(current) || !isValidTile(end)) {
            return new PathResult(false, Integer.MAX_VALUE, path, Integer.MAX_VALUE);
        }

        // Log current state
        System.out.println("Visiting: (" + current.getX() + ", " + current.getY() + ") Current Path" + solution.getId() + ": " + solution.getPath());

        // Base case: reached the end tile
        if (current.equals(end)) {
            path.add(determineMove(current,end));
            solution.setPath(path);
            return new PathResult(true, cost, solution.getPath(), 0);
        }

        visited.add(current); // Mark the current tile as visited

        int x = current.getX();
        int y = current.getY();
        int direction = new Random().nextInt(2); // Randomly follow x or y-axis

        // Define potential neighbors safely
        Tile[] xNeighbors = new Tile[2];
        xNeighbors[0] = (x + 1 < map.length) ? map[x + 1][y] : null;    // Right    E
        xNeighbors[1] = (x - 1 >= 0) ? map[x - 1][y] : null;            // Left     W

        Tile[] yNeighbors = new Tile[2];
        yNeighbors[0] = (y + 1 < map[0].length) ? map[x][y + 1] : null; // Down     S
        yNeighbors[1] = (y - 1 >= 0) ? map[x][y - 1] : null;            // Up       N

        Tile closerNeighbor;

        if (direction == 0) { // Explore X neighbors
            closerNeighbor = getCloserNeighbor(xNeighbors[0], xNeighbors[1], end);
            if (closerNeighbor != null && isValidConnection(current, closerNeighbor)) {
                path.add(determineMove(current, closerNeighbor));
                solution.setPath(path);
                solution = dfs(map, closerNeighbor, end, visited, cost + 1, solution);
                if (solution.isPathExists()){
                    return solution;
                }
            }
        } else { // Explore Y neighbors
            closerNeighbor = getCloserNeighbor(yNeighbors[0], yNeighbors[1], end);
            if (closerNeighbor != null && isValidConnection(current, closerNeighbor)) {
                path.add(determineMove(current, closerNeighbor));
                solution.setPath(path);
                solution = dfs(map, closerNeighbor, end, visited, cost + 1, solution);
                if (solution.isPathExists()){
                    return solution;
                }
            }
        }

        if (solution.isPathExists()) {
            return new PathResult(true, solution.getDistance(), path, solution.getPathCost());
        }

        visited.remove(current); // Backtrack: unmark the current tile
        return new PathResult(false, cost, solution.getPath(), solution.getDistance()); // No path found
    }


    private static Tile getCloserNeighbor(Tile neighbor1, Tile neighbor2, Tile end) {
        if (neighbor1 != null && neighbor2 != null) {
            return calculateDistance(neighbor1, end) < calculateDistance(neighbor2, end) ? neighbor1 : neighbor2;
        } else if (neighbor1 != null) {
            return neighbor1;
        } else if (neighbor2 != null) {
            return neighbor2;
        }
        return null;
    }

    static int calculateUpdateCost(Tile current, Tile next) {
        Tile copyCurrent = new Tile(current);
        Tile copyNext = new Tile(next);

        updateTileConnection(copyCurrent, copyNext);
        return copyCurrent.getTypeIndex();
    }

    private static int calculateDistance(Tile a, Tile b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }

    private static boolean isValidTile(Tile tile) {
        return tile.getX() >= 0 && tile.getX() < Game.size && tile.getY() >= 0 && tile.getY() < Game.size;
    }

    private static String determineMove(Tile from, Tile to) {
        if (to.getX() == from.getX() && to.getY() == from.getY() - 1) return "W";
        if (to.getX() == from.getX() && to.getY() == from.getY() + 1) return "E";
        if (to.getX() == from.getX() - 1 && to.getY() == from.getY()) return "N";
        if (to.getX() == from.getX() + 1 && to.getY() == from.getY()) return "S";
        return "";
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


        if (neighbor.getType() == TileType.STATION || current.getType() == TileType.STATION) {
            return (0b1111 & (1 << dir)) != 0 && (0b1111 & (1 << oppDir)) != 0;  // Just check if the current tile is connecting to the station.
        }


        // Validate indices for non-TRAIN tiles
        int currentType = current.getTypeIndex();
        int neighborType = neighbor.getTypeIndex();
        int currentRotation = current.getRotationIndex();
        int neighborRotation = neighbor.getRotationIndex();

//        System.out.println("Current Type Index: " + currentType);
//        System.out.println("Neighbor Type Index: " + neighborType);
//        System.out.println("Current Rotation Index: " + currentRotation);
//        System.out.println("Neighbor Rotation Index: " + neighborRotation);
//        System.out.println("Connections Array Dimensions: " + connections.length + "x" + connections[0].length);
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

    public static List<Tile> getNeighboringTile(Tile[][] map, Tile current) {
        List<Tile> neighbors = new ArrayList<>();
        int x = current.getX();
        int y = current.getY();
        // Check left neighbor (x-1, y)
        if (x > 0) {
            neighbors.add(map[x - 1][y]);
        }
        // Check right neighbor (x+1, y)
        if (x < map.length - 1) {
            neighbors.add(map[x + 1][y]);
        }
        // Check top neighbor (x, y+1)
        if (y < map[0].length - 1) {
            neighbors.add(map[x][y + 1]);
        }
        // Check bottom neighbor (x, y-1)
        if (y > 0) {
            neighbors.add(map[x][y - 1]);
        }
        return neighbors;
    }


}