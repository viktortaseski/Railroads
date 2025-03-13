package org.example;

import java.util.*;
import java.util.concurrent.*;

public class GeneticAlgorithm {

    public static void start(Game game) {
        Random random = new Random(12345);
        System.out.println("Running Genetic Algorithm with parallel fitness evaluation");
        long startTime = System.currentTimeMillis();

        // For each train, run the GA (all genetic operations except fitness evaluation are sequential).
        for (Train train : Game.TRAINS) {
            run(50, 10, train, random);
        }

        long endTime = System.currentTimeMillis();

        // Print results for each train.
        for (Train train : Game.TRAINS) {
            System.out.println("========================================");
            System.out.println("Path exists: " + train.getResult().isPathExists());
            System.out.print("Path for Train[" + train.getId() + "]: ");
            for (Tile path : train.getResult().getPath()) {
                System.out.print("(" + path.getX() + "," + path.getY() + ") ");
            }
            System.out.println("\n========================================");
        }
        System.out.println("Best map cost is: " + Game.getBoardFitness());
        System.out.println("Time: " + (endTime - startTime) + "ms");
    }

    public static void run(int iterations, int populationSize, Train train, Random random) {
        List<MapSolution> population = initPopulation(train, populationSize);

        // Create a fixed thread pool with as many threads as solutions per generation.
        ExecutorService executor = Executors.newFixedThreadPool(populationSize);

        for (int i = 0; i < iterations; i++) {
            // 1. Evaluate fitness for current generation in parallel.
            parallelFitnessEvaluation(population, executor);

            // 2. Sort population by fitness (lower is better).
            population.sort(Comparator.comparingInt(MapSolution::getFitness));

            // 3. Elitism: retain the top 2 solutions.
            List<MapSolution> nextGeneration = new ArrayList<>();
            nextGeneration.addAll(population.subList(0, 2));

            // 4. Generate offspring sequentially to ensure deterministic random behavior.
            List<MapSolution> offspringList = new ArrayList<>();
            while (offspringList.size() < (populationSize - 2)) {
                MapSolution parent1 = selectParent(population, random);
                MapSolution parent2 = selectParent(population, random);

                // Diagonal crossover (sequential).
                List<MapSolution> offspring = crossover(parent1, parent2, random);
                for (MapSolution child : offspring) {
                    if (offspringList.size() < (populationSize - 2)) {
                        // Mutation is also sequential.
                        mutate(child, random);
                        offspringList.add(child);
                    }
                }
            }

            // 5. Evaluate offspring fitness in parallel.
            parallelFitnessEvaluation(offspringList, executor);

            // 6. Form the next generation.
            nextGeneration.addAll(offspringList);
            population = nextGeneration;
        }

        // Shutdown the executor.
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Set the best solution on the game board.
        population.sort(Comparator.comparingInt(MapSolution::getFitness));
        Game.setBoard(population.get(0).getMapLayout());
    }

    // Helper to evaluate fitness in parallel.
    private static void parallelFitnessEvaluation(List<MapSolution> solutions, ExecutorService executor) {
        List<Callable<Void>> tasks = new ArrayList<>();
        for (MapSolution solution : solutions) {
            tasks.add(() -> {
                solution.evaluateFitness();
                return null;
            });
        }
        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static List<MapSolution> initPopulation(Train train, int populationSize) {
        List<MapSolution> population = new ArrayList<>();
        // Use a dedicated RNG for population initialization.
        Random initRandom = new Random(1234);
        for (int i = 0; i < populationSize; i++) {
            MapSolution solution = new MapSolution(
                    HelperFunctions.createRandomMap(train, Game.size, initRandom),
                    0, false
            );
            solution.setMapCost(solution.calculateMapCost());
            population.add(solution);
        }
        return population;
    }

    public static MapSolution selectParent(List<MapSolution> population, Random random) {
        // Select a parent from the top 50% of the population.
        return population.get(random.nextInt(population.size() / 2));
    }

    public static List<MapSolution> crossover(MapSolution parent1, MapSolution parent2, Random random) {
        Tile[][] layout1 = parent1.getMapLayout();
        Tile[][] layout2 = parent2.getMapLayout();
        int rows = layout1.length;
        int cols = layout1[0].length;

        // Create offspring layouts as deep copies.
        Tile[][] childLayout1 = deepCopy(layout1);
        Tile[][] childLayout2 = deepCopy(layout2);

        // Diagonal crossover: choose a random pivot point.
        int pivotRow = random.nextInt(rows);
        int pivotCol = random.nextInt(cols);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // Swap cells in the bottom-right region relative to the pivot.
                if (i >= pivotRow && j >= pivotCol) {
                    Tile temp = childLayout1[i][j];
                    childLayout1[i][j] = childLayout2[i][j];
                    childLayout2[i][j] = temp;
                }
            }
        }

        MapSolution child1 = new MapSolution(childLayout1, 0, false);
        MapSolution child2 = new MapSolution(childLayout2, 0, false);
        return List.of(child1, child2);
    }

    // Utility method to deep copy a 2D array of Tiles.
    private static Tile[][] deepCopy(Tile[][] original) {
        Tile[][] copy = new Tile[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = new Tile[original[i].length];
            for (int j = 0; j < original[i].length; j++) {
                Tile originalTile = original[i][j];
                copy[i][j] = new Tile(
                        originalTile.getX(),
                        originalTile.getY(),
                        originalTile.getRotation(),
                        originalTile.getType()
                );
            }
        }
        return copy;
    }

    public static void mutate(MapSolution solution, Random random) {
        // Use a deep copy to avoid modifying the original array reference.
        Tile[][] map = deepCopy(solution.getMapLayout());
        int i = random.nextInt(map.length);
        int j = random.nextInt(map[0].length);

        // If the tile is not a TRAIN or STATION, change it to one of the low-cost road types.
        if (map[i][j].getType() != TileType.STATION && map[i][j].getType() != TileType.TRAIN) {
            // Assuming the last two enum values represent STATION and TRAIN, select from the others.
            TileType[] possibleTypes = Arrays.copyOf(TileType.values(), TileType.values().length - 2);
            map[i][j].setType(possibleTypes[random.nextInt(possibleTypes.length)]);
        }

        solution.setMapLayout(map);
    }
}
