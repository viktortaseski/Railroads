package org.example;

import java.util.*;
import java.util.concurrent.*;

public class DistributedSolution {

    /**
     * Runs the distributed genetic algorithm.
     * This version uses a fixed thread pool (with a size equal to the available processors)
     * and partitions the population so that each worker evaluates only its portion.
     * Sorting is deferred until after evaluation—ensuring that we only combine partitions and
     * sort the entire population when needed.
     *
     * @param iterations     Number of generations to run.
     * @param populationSize The population size per generation.
     * @param random         The seeded Random instance (e.g. new Random(12345)).
     */
    public static void runDistributed(int iterations, int populationSize, Random random) {
        // Generate the initial population.
        List<MapSolution> population = GeneticAlgorithm.initPopulation(populationSize);

        // Create a fixed thread pool based on available processors.
        int threads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        // Evaluate the initial population in parallel using partitioned evaluation.
        // parallelFitnessEvaluation(population, executor);     // Don't need to do this since we already evaluate when iterarating

        // Evolve the population for the given number of iterations.
        for (int i = 0; i < iterations; i++) {
            // Evaluate the current population partition-wise.
            parallelFitnessEvaluation(population, executor);
            // Create a new generation.
            population = GeneticAlgorithm.creatingNewGeneration(population, populationSize, random);
        }

        // Final evaluation: sort the last generation to select the best solution.
        population.sort(Comparator.comparingInt(MapSolution::getFitness));
        Game.setBoard(population.getFirst().getMapLayout());
        System.out.println("Distributed Best Solution Fitness: " + population.getFirst().getFitness());

        // Shutdown the executor service.
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    /**
     * Evaluates the fitness of all MapSolution objects in the population in parallel.
     * The method partitions the population based on the number of available threads in the executor.
     * Each worker thread processes its assigned partition.
     *
     * @param population The list of MapSolution instances to evaluate.
     * @param executor   The ExecutorService used for parallel evaluation.
     */
    private static void parallelFitnessEvaluation(List<MapSolution> population, ExecutorService executor) {
        int numThreads = ((ThreadPoolExecutor) executor).getCorePoolSize();
        int partitionSize = (int) Math.ceil((double) population.size() / numThreads);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < population.size(); i += partitionSize) {
            int start = i;
            int end = Math.min(i + partitionSize, population.size());
            List<MapSolution> partition = population.subList(start, end);
            futures.add(executor.submit(() -> {
                for (MapSolution solution : partition) {
                    solution.evaluateFitness();
                }
            }));
        }

        // Wait for all submitted tasks to complete.
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }
}
