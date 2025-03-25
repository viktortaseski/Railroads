package org.example;

import java.util.*;
import java.util.concurrent.*;

public class GeneticAlgorithm {

    public static void start() {
        Random random = new Random(12345);
        long startTime = 0;
        long endTime = 0;
        int iterations = Game.getIterations();

        if (Game.getMode() == 1) {
            System.out.println("Running Sequential Genetic Algorithm... \nSize: " + Game.getSize() + "\nTrains: " + Game.getTrains().size() + "\nIterations: " + iterations);
            startTime = System.currentTimeMillis();
            runSequential(iterations, 50, random);
            endTime = System.currentTimeMillis();
        } else if (Game.getMode() == 2) {
            System.out.println("Running GA with Parallel Evaluation... \nSize: " + Game.getSize() + "\nTrains: " + Game.getTrains().size() + "\nIterations: " + iterations);
            startTime = System.currentTimeMillis();
            runParallel(iterations, 50, random);
            endTime = System.currentTimeMillis();
        }

        // Print results for each train.

//        for (Train train : Game.getTrains()) {
//            System.out.println("Reached station: " + train.getResult().isPathExists() +
//                    " | Path for Train[" + train.getId() + "]: ");
//            for (Tile path : train.getResult().getPath()) {
//                System.out.print("(" + path.getX() + "," + path.getY() + ") ");
//            }
//            System.out.println("\n==========================================");
//        }

        System.out.println("\nMapSolution with best cost is: " + Game.getBoardFitness());
        System.out.println("Time: " + (endTime - startTime) + "ms or " + ((endTime - startTime)/1000) + "s.");
        System.out.println("==========================================");
    }

    public static void runSequential(int iterations, int populationSize, Random random) {
        List<MapSolution> population = initPopulation(populationSize);

        for (int i = 0; i < iterations; i++) {
            // Track if we are progressing
            if (i % 50 == 0) {  // Print "|" every 50 iterations
                if (i % (50 * 5) == 0 && i != 0) {  // Add a tab every 5th "|"
                    System.out.print("\t");
                }
                System.out.print("|");
            }
            // Evaluate fitness for current generation.
            for (MapSolution solution : population) {
                solution.evaluateFitness();
            }
            // Generate new generation.
            population = creatingNewGeneration(population, populationSize, random);
        }

        // Set the best solution on the game board.
        population.sort(Comparator.comparingInt(MapSolution::getFitness));
        Game.setBoard(population.getFirst().getMapLayout());
    }

    public static void runParallel(int iterations, int populationSize, Random random) {
        List<MapSolution> population = initPopulation(populationSize);
        ExecutorService executor = Executors.newFixedThreadPool(populationSize);

        for (int i = 0; i < iterations; i++) {
            if (i % 50 == 0) {  // Print "|" every 50 iterations
                if (i % (50 * 5) == 0 && i != 0) {  // Add a tab every 5th "|"
                    System.out.print("\t");
                }
                System.out.print("|");
            }
            // Evaluate fitness for current generation in parallel.
            parallelFitnessEvaluation(population, executor);
            // Generate new generation.
            population = creatingNewGeneration(population, populationSize, random);
        }
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Set the best solution on the game board.
        population.sort(Comparator.comparingInt(MapSolution::getFitness));
        Game.setBoard(population.getFirst().getMapLayout());
    }

    /**
     * Creates the next generation from the given population.
     * The population is first sorted. Then 10% (10% of 50 = 5 MapSolutions) of elite solutions are carried over directly.
     * The remainder of the next generation is produced by selecting parents using weighted random selection that professor Domen suggested.
     * (using ranking weights) and then applying crossover and mutation.
     * After reaching 50 MapSolutions we return the nextGeneration.
     */
    static List<MapSolution> creatingNewGeneration(List<MapSolution> population, int populationSize, Random random) {
        // Sort population by fitness (lower is better).
        population.sort(Comparator.comparingInt(MapSolution::getFitness));

        // Elitism: retain a small percentage of the best solutions.
        int eliteCount = Math.max(1, populationSize / 10); // 10% elitism
        List<MapSolution> nextGeneration = new ArrayList<>(population.subList(0, eliteCount));

        // Generate the rest of the population using weighted random selection.
        List<MapSolution> offspringList = new ArrayList<>();
        while (offspringList.size() < (populationSize - eliteCount)) {
            MapSolution parent1 = selectParent(population, random);
            MapSolution parent2 = selectParent(population, random);

            // Diagonal crossover (sequential) returns two offspring.
            List<MapSolution> offspring = crossover(parent1, parent2, random);
            for (MapSolution child : offspring) {
                if (offspringList.size() < (populationSize - eliteCount)) {
                    mutate(child, random);
                    offspringList.add(child);
                }
            }
        }
        nextGeneration.addAll(offspringList);
        return nextGeneration;
    }

    /**
     * Weighted random selection based on ranking.
     * Assumes the population is sorted in ascending order of fitness.
     * The best candidate (index 0) gets the highest weight.
     *
     * @param population the sorted list of solutions
     * @param random     the seeded Random instance
     * @return a selected MapSolution
     */
    public static MapSolution selectParent(List<MapSolution> population, Random random) {
        int n = population.size();
        int totalWeight = n * (n + 1) / 2; // sum of weights 1...n
        int randomWeight = random.nextInt(totalWeight);
        for (int i = 0; i < n; i++) {
            int weight = n - i; // best candidate gets highest weight (n)
            if (randomWeight < weight) {
                return population.get(i);
            }
            randomWeight -= weight;
        }
        return population.get(n - 1);
    }

    public static List<MapSolution> initPopulation(int populationSize) {
        List<MapSolution> population = new ArrayList<>();
        // Add the original map as a starting solution.
        population.add(new MapSolution(Game.getMap(), 0));
        // Use a dedicated RNG for population initialization.
        Random initRandom = new Random(1234);
        for (int i = 0; i < populationSize - 1; i++) {
            MapSolution solution = new MapSolution(
                    HelperFunctions.createRandomMap(Game.getSize(), initRandom),
                    0
            );
            solution.setMapCost(solution.calculateMapCost());
            population.add(solution);
        }
        return population;
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

        MapSolution child1 = new MapSolution(childLayout1, 0);
        MapSolution child2 = new MapSolution(childLayout2, 0);
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

    // Helper to evaluate fitness in parallel.
    static void parallelFitnessEvaluation(List<MapSolution> solutions, ExecutorService executor) {
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
}
