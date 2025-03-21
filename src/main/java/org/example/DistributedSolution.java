package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

// This class implements the distributed solution strategy
public class DistributedSolution {

    private static final int WORKER_COUNT = 10;
    // Assuming that populationSize and seed are provided or configured similarly to the other modes.
    private final int populationSize;
    private final long seed;

    public DistributedSolution(int populationSize, long seed) {
        this.populationSize = populationSize;
        this.seed = seed;
    }

    // Main method for testing the distributed solution
    public static void main(String[] args) {
        // Example usage: these parameters should be set consistently with your sequential and parallel modes.
        int populationSize = 1_000_000;
        long seed = 12345L;
        DistributedSolution ds = new DistributedSolution(populationSize, seed);
        MapSolution best = ds.solve();
        System.out.println("Best distributed solution fitness: " + best.getFitness());
    }

    public MapSolution solve() {
        // Generate the complete list of genetic solutions using the same seed as the other methods
        List<MapSolution> solutions = GeneticAlgorithm.initPopulation(200);

        // Calculate partition size for each worker
        int partitionSize = solutions.size() / WORKER_COUNT;
        ExecutorService executor = Executors.newFixedThreadPool(WORKER_COUNT);
        List<Future<MapSolution>> futures = new ArrayList<>();

        // Submit each partition to a separate worker
        for (int i = 0; i < WORKER_COUNT; i++) {
            int start = i * partitionSize;
            int end = (i == WORKER_COUNT - 1) ? solutions.size() : (i + 1) * partitionSize;
            List<MapSolution> partition = solutions.subList(start, end);

            Callable<MapSolution> workerTask = () -> {
                MapSolution best = null;
                for (MapSolution sol : partition) {
                    if (best == null || sol.getFitness() < best.getFitness()) {
                        best = sol;
                    }
                }
                return best;
            };
            futures.add(executor.submit(workerTask));
        }

        // Shutdown the executor and combine the results
        executor.shutdown();
        MapSolution bestOverall = null;
        for (Future<MapSolution> future : futures) {
            try {
                MapSolution candidate = future.get();
                if (bestOverall == null || candidate.getFitness() < bestOverall.getFitness()) {
                    bestOverall = candidate;
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                // You might want to handle the exception more gracefully in production code
            }
        }

        return bestOverall;
    }

}
