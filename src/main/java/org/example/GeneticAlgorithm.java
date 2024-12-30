package org.example;

import java.util.*;
import java.util.concurrent.*;

import static org.example.Fitness.isValidConnection;

public class GeneticAlgorithm {

    public static PathResult run(Game game, int iterations, int mode, Train train) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        Random random = new Random();
        PathResult bestResult = new PathResult(false, Integer.MAX_VALUE, null, Integer.MAX_VALUE);

        if (mode == 1) { // Sequential execution
            for (int i = 0; i < iterations; i++) {
                Tile[][] mapCopy = copyMap(Game.getMap());
                PathResult result = Fitness.evaluate(mapCopy, train);
                System.out.println("Generated Path: " + result.getPath());

                // Update bestResult only if the new path is valid and has a lower cost
                if (result.isPathExists() && (result.getPathCost() < bestResult.getPathCost()) && result.getChanges() < bestResult.getChanges()) {
                    bestResult = result;
                    train.setPath(bestResult.getPath());
                    train.setPathCost(bestResult.getPathCost());
                    return bestResult;
                } else {
                    // Mutate the current path for exploration
                    mutatePath(train.getPath(), random);
                }

                System.out.println("Iteration " + i + ", Fitness: " + result.getPathCost());
            }
        } else if (mode == 2) { // Parallel execution
            List<Callable<PathResult>> tasks = new ArrayList<>();
            for (int i = 0; i < iterations; i++) {
                tasks.add(() -> {
                    Tile[][] mapCopy = copyMap(Game.getMap());
                    PathResult result = Fitness.evaluate(mapCopy, train);
                    if (!result.isPathExists()) {
                        mutatePath(train.getPath(), random);
                    }
                    return result;
                });
            }

            try {
                List<Future<PathResult>> futures = executor.invokeAll(tasks);
                for (Future<PathResult> future : futures) {
                    PathResult result = future.get();
                    if (result.isPathExists() && result.getPathCost() < bestResult.getPathCost()) {
                        bestResult = result;
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Invalid mode");
        }

        executor.shutdown();

        // Apply the best path to the map if it exists
        if (bestResult.isPathExists()) {
            applyBestPathToMap(Game.getMap(), bestResult);
        }

        train.setPathCost(bestResult.getPathCost());
        train.setPath(bestResult.getPath());
        return bestResult;
    }

    private static void mutatePath(List<String> path, Random random) {

        if (path == null || path.isEmpty()) {
            return;
        }

        int mutationType = random.nextInt(3);
        switch (mutationType) {
            case 0: // Add a random direction
                path.add(getRandomDirection(random));
                break;
            case 1: // Remove a random direction
                if (path.size() > 1) {
                    path.remove(random.nextInt(path.size()));
                }
                break;
            case 2: // Replace a random direction
                int index = random.nextInt(path.size());
                path.set(index, getRandomDirection(random));
                break;
        }

    }

    private static String getRandomDirection(Random random) {
        String[] directions = {"N", "E", "S", "W"};
        return directions[random.nextInt(directions.length)];
    }

    private static Tile[][] copyMap(Tile[][] original) {
        Tile[][] copy = new Tile[original.length][original[0].length];
        for (int i = 0; i < original.length; i++) {
            for (int j = 0; j < original[i].length; j++) {
                copy[i][j] = original[i][j];
            }
        }
        return copy;
    }

    private static void applyBestPathToMap(Tile[][] map, PathResult bestResult) {
        Tile currentTile = null;
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                if (map[i][j].isStartTile()) {
                    currentTile = map[i][j];
                    break;
                }
            }
            if (currentTile != null) break;
        }

        if (currentTile == null) {
            throw new IllegalStateException("Start tile not found in the map.");
        }

        for (String direction : bestResult.getPath()) {
            Tile nextTile = getNextTile(map, currentTile, direction);
            if (nextTile == null) {
                System.out.println("Invalid (end of) path: Unable to find next tile.");
                return;
            }

            if (!isValidConnection(currentTile, nextTile)) {
                updateTileConnection(currentTile, nextTile);
                bestResult.setPathCost(bestResult.getPathCost() + nextTile.getTypeIndex());
            }

            currentTile = nextTile;
        }
    }

    private static Tile getNextTile(Tile[][] map, Tile current, String direction) {
        int x = current.getX();
        int y = current.getY();
        Tile nextTile = null;
        switch (direction) {
            case "N":
                nextTile = (x > 0) ? map[x - 1][y] : null;
                break;
            case "S":
                nextTile = (x < map.length - 1) ? map[x + 1][y] : null;
                break;
            case "E":
                nextTile = (y < map[0].length - 1) ? map[x][y + 1] : null;
                break;
            case "W":
                nextTile = (y > 0) ? map[x][y - 1] : null;
                break;
        }
        if (nextTile == null) {
            System.out.println("Invalid direction: " + direction + " from (" + x + ", " + y + ")");
        }
        return nextTile;
    }


    static void updateTileConnection(Tile current, Tile next) {
        for (int i = 0; i < TileType.values().length - 2; i++) {
            for (int j = 0; j < Rotation.values().length; j++) {
                current.setType(TileType.values()[i]);
                current.setRotation(Rotation.values()[j]);
                boolean connections = true;
                for (Tile connected : current.getVisitedByTrains()) {
                    if (!isValidConnection(current, connected)) {
                        connections = false;
                    }
                }
                if (isValidConnection(current, next) && connections) {
                    return;
                }
            }
        }
        current.setType(TileType.CROSS);
        current.setRotation(Rotation.values()[0]);
    }
}
