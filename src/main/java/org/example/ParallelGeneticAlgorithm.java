package org.example;

import java.util.*;
import java.util.concurrent.*;

public class ParallelGeneticAlgorithm {

    public static void runParallel(Game game) {
        int iterations = 50;
        int populationSize = 10;
        List<Train> trains = game.getTrains();

        for (Train train : trains) {
            runParallel(iterations, populationSize, train);
            System.out.println("========================================");
            System.out.println("Path exists: " + train.getResult().isPathExists());
            System.out.print("Path for Train[" + train.getId() + "]: ");
            for (Tile path : train.getResult().getPath()) {
                System.out.print("(" + path.getX() + "," + path.getY() + ") ");
            }
            System.out.println("\n========================================");
        }

        System.out.println("Best map cost is: " + Game.getBoardFitness());
    }

    public static void runParallel(int iterations, int populationSize, Train train) {
        Random random = new Random(12345);
        List<MapSolution> population = GeneticAlgorithm.initPopulation(train, populationSize);

        // Create a thread pool with the number of available processors
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < iterations; i++) {
            List<MapSolution> nextGeneration = new ArrayList<>();

            // Evaluate fitness of the current population in parallel
            List<Future<Void>> futures = new ArrayList<>();
            for (MapSolution solution : population) {
                futures.add(executor.submit(() -> {
                    solution.evaluateFitness();
                    return null;
                }));
            }

            // Wait for all fitness evaluations to complete
            for (Future<Void> future : futures) {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }

            // Sort population by fitness (lower cost is better)
            population.sort(Comparator.comparingInt(MapSolution::getFitness));

            // Elitism: Retain the top-performing solutions
            nextGeneration.addAll(population.subList(0, 2));

            // Generate offspring in parallel
            while (nextGeneration.size() < populationSize) {
                MapSolution parent1 =  GeneticAlgorithm.selectParent(population, random);
                MapSolution parent2 =  GeneticAlgorithm.selectParent(population, random);

                // Perform diagonal crossover
                List<MapSolution> offspring =  GeneticAlgorithm.crossover(parent1, parent2, random);

                // Mutate and evaluate offspring in parallel
                for (MapSolution child : offspring) {
                    executor.submit(() -> {
                        GeneticAlgorithm.mutate(child, random);
                        child.evaluateFitness();
                        synchronized (nextGeneration) {
                            nextGeneration.add(child);
                        }
                        return null;
                    });
                }
            }

            population = nextGeneration;
        }

        // Shutdown the executor
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Set the best solution to the game board
        population.sort(Comparator.comparingInt(MapSolution::getFitness));
        Game.setBoard(population.get(0).getMapLayout());
    }


}