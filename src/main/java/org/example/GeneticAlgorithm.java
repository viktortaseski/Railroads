package org.example;

import java.util.*;
import java.util.concurrent.*;

import static org.example.Fitness.*;

public class GeneticAlgorithm {

    public static PathResult run(int iterations, int populationSize, Train train) {
        Random random = new Random();
        List<PathResult> population = initializePopulation(train, populationSize);

        for (PathResult pathResult : population) {
            System.out.println("POPULATION: " + pathResult.getPath());
        }

        for (int i = 0; i < iterations; i++) {
            List<PathResult> nextGeneration = new ArrayList<>();
            for (PathResult pathResult : population) {
                Fitness.setFitness(pathResult, train);
                Fitness.evaluate(pathResult, train);
                System.out.println("Generation " + i + ": Train[" + train.getId() + "] Fitness: " + pathResult.getFitness() + " Path[" + pathResult.getId() + "] " + pathResult.getPath() );
            }
            System.out.println();
            // Elitism: Retain the best solutions
            int eliteCount = Math.max(1, populationSize / 10);
            population.sort(Comparator.comparingInt(PathResult::getFitness).reversed());
            nextGeneration.addAll(population.subList(0, eliteCount));

            // Generate offspring
            while (nextGeneration.size() < populationSize) {
                PathResult parent1 = selectParent(population, random);
                PathResult parent2 = selectParent(population, random);

                List<PathResult> offspring = crossover(parent1, parent2, random);

                for (PathResult child : offspring) {
                    if (!child.isPathExists()) {
                        mutate(child, random);
                    }
                    Fitness.setFitness(child, train);
                    Fitness.evaluate(child, train);
                    nextGeneration.add(child);
                }

            }

            population = nextGeneration;
        }

        return getBestSolution(population);
    }

    private static List<PathResult> initializePopulation(Train train, int populationSize) {
        List<PathResult> population = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            Tile[][] copyMap = copyMap(Game.getMap());
            PathResult solution = Fitness.findPath(copyMap, train);
            solution.setId(i);
            Fitness.setFitness(solution, train);
            Fitness.evaluate(solution, train);
            population.add(solution);
        }
        for (PathResult solution : population) {
            System.out.println("Solution[" + solution.getId() + "]: " + solution.getPath());
        }
        return population;
    }

    private static PathResult selectParent(List<PathResult> population, Random random) {
        // Roulette Wheel Selection with fallback for zero fitness
        int totalFitness = population.stream().mapToInt(PathResult::getFitness).sum();

        if (totalFitness <= 0) {
            // Fallback: Select a random individual if all fitness values are zero
            return population.get(random.nextInt(population.size()));
        }

        int pick = random.nextInt(totalFitness);
        int current = 0;

        for (PathResult individual : population) {
            current += individual.getFitness();
            if (current > pick) {
                return individual;
            }
        }

        // Should not reach here if logic is correct, fallback to a random individual
        return population.get(random.nextInt(population.size()));
    }

    private static List<PathResult> crossover(PathResult parent1, PathResult parent2, Random random) {
        List<String> path1 = parent1.getPath();
        List<String> path2 = parent2.getPath();

        int minLength = Math.min(path1.size(), path2.size());
        if (minLength < 2) {
            return Arrays.asList(new PathResult(parent1), new PathResult(parent2));
        }

        int crossoverPoint = random.nextInt(minLength - 1) + 1;

        List<String> childPath1 = new ArrayList<>(path1.subList(0, crossoverPoint));
        childPath1.addAll(path2.subList(crossoverPoint, path2.size()));

        List<String> childPath2 = new ArrayList<>(path2.subList(0, crossoverPoint));
        childPath2.addAll(path1.subList(crossoverPoint, path1.size()));

        return Arrays.asList(
                new PathResult(false, 0, childPath1, 0),
                new PathResult(false, 0, childPath2, 0)
        );
    }

    private static void mutate(PathResult solution, Random random) {
        List<String> path = solution.getPath();
        Set<Tile> visited = solution.getVisited();
        if (path == null) {
            return;
        }

        int mutationType = random.nextInt(2);

        if (path.isEmpty()){
            path.add(0, getRandomDirection(random));   // If path is empty generate a move.
        }

        switch (mutationType) {
            case 0: // Add a valid random move
                int addIndex = random.nextInt(path.size());
                path.add(addIndex, getRandomDirection(random));
                break;
            case 1: // Replace a move with a valid random move
                int replaceIndex = random.nextInt(path.size());
                path.set(replaceIndex, getRandomDirection(random));
                break;
        }
    }

    private static String getRandomDirection(Random random) {
        String[] directions = {"N", "E", "S", "W"};
        return directions[random.nextInt(directions.length)];
    }

    private static PathResult getBestSolution(List<PathResult> population) {
        return population.stream().max(Comparator.comparingInt(PathResult::getFitness)).orElse(null);
    }

    static Tile[][] copyMap(Tile[][] original) {
        Tile[][] copy = new Tile[original.length][original[0].length];
        for (int i = 0; i < original.length; i++) {
            for (int j = 0; j < original[i].length; j++) {
                copy[i][j] = original[i][j];
            }
        }
        return copy;
    }

    public static void applyBestPathToMap(Tile[][] map, PathResult bestResult, Train train) {
        Tile currentTile = null;

        currentTile = train.getStartTile();

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
                if (currentTile.getType() != TileType.TRAIN) {
                    updateTileConnection(currentTile, nextTile);
                    bestResult.setPathCost(bestResult.getPathCost() + nextTile.getTypeIndex());
                }
            }

            currentTile = nextTile;
        }
    }

    static Tile getNextTile(Tile[][] map, Tile current, String direction) {
        int x = current.getX();
        int y = current.getY();
        Tile nextTile = null;
        switch (direction) {
            case "N": // North
                nextTile = (x > 0) ? map[x - 1][y] : null;
                break;
            case "S": // South
                nextTile = (x < map.length - 1) ? map[x + 1][y] : null;
                break;
            case "E": // East
                nextTile = (y < map[0].length - 1) ? map[x][y + 1] : null;
                break;
            case "W": // West
                nextTile = (y > 0) ? map[x][y - 1] : null;
                break;
        }

        return nextTile;
    }

    static void updateTileConnection(Tile current, Tile next) {
        for (int i = 0; i < TileType.values().length - 2; i++) {
            for (int j = 0; j < Rotation.values().length; j++) {
                if (current.getType() == TileType.TRAIN || next.getType() == TileType.STATION) {
                    return;
                }
                current.setType(TileType.values()[i]);
                current.setRotation(Rotation.values()[j]);

                boolean connections = true;
                if (current.getVisitedByTrains() != null) {
                    for (Tile connected : current.getVisitedByTrains()) {
                        if (!isValidConnection(current, connected)) {
                            connections = false;
                        }
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
