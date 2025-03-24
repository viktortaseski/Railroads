package org.example;

import java.util.*;
import java.util.concurrent.*;

public class DistributedSolution {

    /**
     * Runs the distributed genetic algorithm.
     * This version uses the same deterministic evolution process (with a fixed seeded Random)
     * as in the sequential and parallel versions so that the same set of solutions is produced.
     * The fitness of all solutions is evaluated in parallel—each solution is given its own task,
     * just like in the parallel implementation—and then the overall best solution is selected.
     *
     * @param iterations     Number of generations to run.
     * @param populationSize The population size per generation.
     * @param random         The seeded Random instance (e.g. new Random(12345)).
     */
    public static void runDistributed(int iterations, int populationSize, Random random) {
        // Generate the initial population.
        List<MapSolution> population = GeneticAlgorithm.initPopulation(populationSize);

        // Evaluate the initial population in parallel.
        ExecutorService executor = Executors.newFixedThreadPool(populationSize);
        GeneticAlgorithm.parallelFitnessEvaluation(population, executor);
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Evolve the population for the given number of iterations.
        for (int i = 0; i < iterations; i++) {
            executor = Executors.newFixedThreadPool(populationSize);
            GeneticAlgorithm.parallelFitnessEvaluation(population, executor);
            executor.shutdown();
            try {
                executor.awaitTermination(1, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            population = GeneticAlgorithm.creatingNewGeneration(population, populationSize, random);
        }
        // Final evaluation: sort the last generation and pick the best solution.
        population.sort(Comparator.comparingInt(MapSolution::getFitness));
        Game.setBoard(population.get(0).getMapLayout());
        System.out.println("Distributed Best Solution Fitness: " + population.get(0).getFitness());
    }
}
