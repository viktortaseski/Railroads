package org.example;

import java.util.*;
import java.util.concurrent.*;

public class DistributedSolution {

    private static final int WORKER_COUNT = 10;

    /**
     * Runs the distributed genetic algorithm.
     *
     * @param iterations     The number of generations to iterate.
     * @param populationSize The size of the population.
     * @param random         The random number generator (seeded for reproducibility).
     */
    public static void runDistributed(int iterations, int populationSize, Random random) {
        // Initialize population using the same helper from GeneticAlgorithm.
        List<MapSolution> population = GeneticAlgorithm.initPopulation(populationSize);

        // Create a fixed thread pool representing our distributed workers.
        ExecutorService executor = Executors.newFixedThreadPool(WORKER_COUNT);

        for (int iter = 0; iter < iterations; iter++) {
            // Partition population among WORKER_COUNT workers.
            int partitionSize = population.size() / WORKER_COUNT;
            List<Callable<Void>> tasks = new ArrayList<>();

            for (int i = 0; i < WORKER_COUNT; i++) {
                int start = i * partitionSize;
                // Ensure the last worker picks up any remaining solutions.
                int end = (i == WORKER_COUNT - 1) ? population.size() : (i + 1) * partitionSize;
                List<MapSolution> partition = population.subList(start, end);

                // Each task evaluates fitness for its partition.
                tasks.add(() -> {
                    for (MapSolution solution : partition) {
                        solution.evaluateFitness();
                    }
                    return null;
                });
            }

            try {
                // Wait for all workers to finish evaluation.
                executor.invokeAll(tasks);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Create new generation using the evaluated population.
            population = creatingNewGeneration(population, populationSize, random);
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // After all iterations, pick the best solution and update the game board.
        population.sort(Comparator.comparingInt(MapSolution::getFitness));
        Game.setBoard(population.get(0).getMapLayout());
    }

    /**
     * Helper method that mimics the creation of a new generation.
     * It retains a few elite solutions and generates offspring by crossover and mutation.
     *
     * @param population     The current generation.
     * @param populationSize The target population size.
     * @param random         The random number generator.
     * @return The new generation population.
     */
    private static List<MapSolution> creatingNewGeneration(List<MapSolution> population, int populationSize, Random random) {
        // Sort the current generation by fitness (lower is better).
        population.sort(Comparator.comparingInt(MapSolution::getFitness));

        // Retain a few top solutions as elite (here, top 5 are kept).
        List<MapSolution> nextGeneration = new ArrayList<>(population.subList(0, Math.min(5, population.size())));

        // Generate offspring until the population size is reached.
        List<MapSolution> offspringList = new ArrayList<>();
        while (offspringList.size() < (populationSize - 2)) {
            MapSolution parent1 = GeneticAlgorithm.selectParent(population, random);
            MapSolution parent2 = GeneticAlgorithm.selectParent(population, random);

            // Diagonal crossover as in your implementation.
            List<MapSolution> offspring = GeneticAlgorithm.crossover(parent1, parent2, random);
            for (MapSolution child : offspring) {
                if (offspringList.size() < (populationSize - 2)) {
                    // Perform mutation.
                    GeneticAlgorithm.mutate(child, random);
                    offspringList.add(child);
                }
            }
        }

        nextGeneration.addAll(offspringList);
        return nextGeneration;
    }
}
