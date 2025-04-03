package org.example;

import mpi.MPI;
import mpi.MPIException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DistributedMain {

    public static final int ROOT = 0;
    public static final int iterations = 1000;
    public static final int mapSize = 20;
    public static final int numberOfTrains = 20;
    public static final int populationSize = 50;
    public static MapSolution[] population = new MapSolution[populationSize];
    static ExecutorService executor = Executors.newFixedThreadPool(populationSize);
    public static Random random = new Random(12345);

    public static void main(String[] args) throws MPIException {
        // Initialize the game before MPI.Init.
        // Use mode 1 (sequential GA) so that GA settings match your sequential/parallel versions.
        Game game = new Game(1, iterations, mapSize, numberOfTrains, 1234);
        game.init();

        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int numberOfNodes = MPI.COMM_WORLD.Size();
        int partitionSize = populationSize / numberOfNodes;

        MapSolution[] sendBuffer = new MapSolution[populationSize];
        MapSolution[] receiveBuffer = new MapSolution[partitionSize];

        long startTime = 0;

        // Initialize the population and give it to the sendBuffer.
        if (rank == ROOT) {
            startTime = System.currentTimeMillis();
            List<MapSolution> initialPopulation = GeneticAlgorithm.initPopulation(populationSize);
            population = initialPopulation.toArray(new MapSolution[populationSize]);
            sendBuffer = population;
        }

        // Evolution loop
        for (int iter = 0; iter < iterations; iter++) {
            // Scatter: distribute partitions of the population from the root to all processes.
            MPI.COMM_WORLD.Scatter(sendBuffer, 0, partitionSize, MPI.OBJECT,
                    receiveBuffer, 0, partitionSize, MPI.OBJECT, ROOT);

            // Each process evaluates its partition (partition is stored into receiveBuffer).
            List<MapSolution> partitionOfPopulation = new ArrayList<>(Arrays.asList(receiveBuffer));
            GeneticAlgorithm.parallelFitnessEvaluation(partitionOfPopulation, executor);
            receiveBuffer = partitionOfPopulation.toArray(new MapSolution[partitionSize]);


            // Gather: collect evaluated partitions back to the root. (population is stored into sendBuffer)
            MPI.COMM_WORLD.Gather(receiveBuffer, 0, partitionSize, MPI.OBJECT,
                    sendBuffer, 0, partitionSize, MPI.OBJECT, ROOT);

            // Only the root generates the new population.
            if (rank == ROOT) {
                List<MapSolution> sendBufferList = new ArrayList<>(Arrays.asList(sendBuffer));
                sendBufferList.sort(Comparator.comparingInt(MapSolution::getFitness));
                List<MapSolution> newGeneration = GeneticAlgorithm.creatingNewGeneration(sendBufferList, populationSize, random);
                sendBuffer = newGeneration.toArray(new MapSolution[populationSize]);
            }

            MPI.COMM_WORLD.Barrier();
        }

        // Final evaluation and printing results on the root.
        if (rank == ROOT) {
            long endTime = System.currentTimeMillis();
            List<MapSolution> finalPopulation = new ArrayList<>(Arrays.asList(sendBuffer));
            GeneticAlgorithm.parallelFitnessEvaluation(finalPopulation, executor);
            finalPopulation.sort(Comparator.comparingInt(MapSolution::getFitness));
            MapSolution bestMap = finalPopulation.getFirst();
            Game.setBoard(bestMap.getMapLayout());

            System.out.println("==========================================");
            System.out.println("Population size: " + finalPopulation.size());
            System.out.println("Best Map fitness: " + bestMap.getFitness());
            System.out.println("Time: " + (endTime - startTime) + "ms or " + ((endTime - startTime) / 1000) + " seconds");
        }

        MPI.Finalize();
    }
}
