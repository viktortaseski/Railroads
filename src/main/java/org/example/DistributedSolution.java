package org.example;

import java.util.*;
import java.util.concurrent.*;

public class DistributedSolution {

    /**
     * Runs the distributed genetic algorithm.
     * The population is partitioned among custom Worker threads.
     * Each Worker calls the helper function GeneticAlgorithm.parallelFitnessEvaluation
     * to evaluate its partition concurrently.
     *
     * @param iterations     Number of generations to run.
     * @param populationSize The population size per generation.
     * @param random         The seeded Random instance (e.g. new Random(12345)).
     */
    public static void runDistributed(int iterations, int populationSize, Random random) {
        // Generate the initial population.
        List<MapSolution> population = GeneticAlgorithm.initPopulation(populationSize);

        // Evolve the population for the given number of iterations.
        for (int i = 0; i < iterations; i++) {
            // Evaluate the current population in a distributed manner.
            evaluatePopulationDistributed(population);
            // Create a new generation.
            population = GeneticAlgorithm.creatingNewGeneration(population, populationSize, random);
        }

        // Final evaluation: sort the last generation to select the best solution.
        population.sort(Comparator.comparingInt(MapSolution::getFitness));
        Game.setBoard(population.get(0).getMapLayout());
        System.out.println("Distributed Best Solution Fitness: " + population.get(0).getFitness());
    }

    /**
     * Partitions the population among Workers and waits for all Workers to finish.
     *
     * @param population The list of MapSolution instances to evaluate.
     */
    private static void evaluatePopulationDistributed(List<MapSolution> population) {
        int numWorkers = Runtime.getRuntime().availableProcessors();
        int partitionSize = (int) Math.ceil((double) population.size() / numWorkers);
        List<Worker> workers = new ArrayList<>();

        // Partition the population and start a Worker for each partition.
        for (int i = 0; i < population.size(); i += partitionSize) {
            int start = i;
            int end = Math.min(i + partitionSize, population.size());
            List<MapSolution> partition = population.subList(start, end);
            Worker worker = new Worker(partition);
            workers.add(worker);
            worker.start();
        }

        // Wait for all Workers to complete.
        for (Worker worker : workers) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Custom Worker class that extends Thread.
     * Each Worker evaluates the fitness of its partition by calling the helper method.
     */
    private static class Worker extends Thread {
        private final List<MapSolution> partition;

        public Worker(List<MapSolution> partition) {
            this.partition = partition;
        }

        @Override
        public void run() {
            // Create a temporary executor service for concurrent evaluation in this worker.
            ExecutorService workerExecutor = Executors.newCachedThreadPool();
            // Evaluate the fitness for this partition using the helper method.
            GeneticAlgorithm.parallelFitnessEvaluation(partition, workerExecutor);
            workerExecutor.shutdown();
            try {
                workerExecutor.awaitTermination(1, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
