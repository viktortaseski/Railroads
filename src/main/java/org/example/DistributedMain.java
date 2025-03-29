package org.example;

import mpi.MPI;
import mpi.MPIException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DistributedMain {

    public static final int ROOT = 0;
    public static final int iterations = 100;
    public static final int mapSize = 20;
    public static final int populationSize = 50;
    public static final int numberOfTrains = 5;
    public static MapSolution[] population = new MapSolution[populationSize];
    static ExecutorService executor = Executors.newFixedThreadPool(populationSize);

    public static void main(String[] args) throws MPIException {
        // Initialize the game before MPI.Init.
        // Use mode 1 (sequential GA) so that GA settings match your sequential/parallel versions.
        Game game = new Game(1, iterations, mapSize, numberOfTrains, 1234);
        game.init();
        Random random = new Random(12345);

        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int numberOfNodes = MPI.COMM_WORLD.Size();
        int partitionSize = populationSize / numberOfNodes;

        MapSolution[] sendBuffer = new MapSolution[populationSize];
        MapSolution[] receiveBuffer = new MapSolution[partitionSize];

        long startTime = 0;

        // Have to initialize the population and give it to the send buffer.
        if (rank == ROOT) {
            startTime = System.currentTimeMillis();
            List<MapSolution> initialPopulation = GeneticAlgorithm.initPopulation(populationSize);
            population = initialPopulation.toArray(new MapSolution[populationSize]);
            sendBuffer = population;
        }

        // Don't have to wait for the population to be initialized for everyone
        // Scatter will send it anyway

        // Start of the evolving iterations.
        // When iter = 6 I get the right result?????????????
        // It is not EVALUATION
        // It is not SORTING
        // Noticed that when I lower size to 10 then I have to have iter = 15/16 to give the right answer.
        for (int iter = 0; iter < iterations; iter++) {

            // Scatter sends each machine its partition and the root gives itself the first partition. (partitionSize => 50 / 5 = 10)
            MPI.COMM_WORLD.Scatter(sendBuffer, 0, partitionSize, MPI.OBJECT,
                    receiveBuffer, 0, partitionSize, MPI.OBJECT, ROOT);

            // NOW EACH MACHINE EVALUATES ITS PARTITION
            if (rank == ROOT) {
                List<MapSolution> partitionOfPopulation = new ArrayList<>(Arrays.asList(receiveBuffer));
                GeneticAlgorithm.parallelFitnessEvaluation(partitionOfPopulation, executor);
                receiveBuffer = partitionOfPopulation.toArray(new MapSolution[partitionSize]);
            } else {
                List<MapSolution> partitionOfPopulation = new ArrayList<>(Arrays.asList(receiveBuffer));
                GeneticAlgorithm.parallelFitnessEvaluation(partitionOfPopulation, executor);
                receiveBuffer = partitionOfPopulation.toArray(new MapSolution[partitionSize]);
            }

            // Get all the partitions (receiveBuffer) and store them in the sendBuffer
            // Root gathers all the partitions and generates the new population
            MPI.COMM_WORLD.Gather(receiveBuffer, 0, partitionSize, MPI.OBJECT,
                    sendBuffer, 0, partitionSize, MPI.OBJECT, ROOT);


            if (rank == ROOT) {
                List<MapSolution> sendBufferList = new ArrayList<>(Arrays.asList(sendBuffer));
                sendBufferList.sort(Comparator.comparingInt(MapSolution::getFitness));
                List<MapSolution> returnedList = GeneticAlgorithm.creatingNewGeneration(sendBufferList, populationSize, random);
                sendBuffer = returnedList.toArray(new MapSolution[populationSize]);
            }

            // HAVE TO WAIT FOR ROOT TO CREATE NEW GEN SO THAT THE OTHER NODES DON'T EVALUATE THE SAME PARTITIONS AGAIN
            // All machines have to reach this point at the same time.
            // Because we want all machines in the same iteration (synchronized)
            MPI.COMM_WORLD.Barrier();
        }


        if (rank == ROOT) {
            long endTime = System.currentTimeMillis();

            List<MapSolution> finalPopulation = new ArrayList<>(Arrays.asList(sendBuffer));
            GeneticAlgorithm.parallelFitnessEvaluation(finalPopulation, executor);
            finalPopulation.sort(Comparator.comparingInt(MapSolution::getFitness));
            MapSolution bestMap = finalPopulation.getFirst();
            Game.setBoard(bestMap.getMapLayout());

            System.out.println("==========================================");
            System.out.println("Population size " + finalPopulation.size());
            System.out.println("Best Map fitness: " + bestMap.getFitness());
            System.out.println("Time: " + (endTime - startTime) + "ms " + (endTime - startTime)/1000 + " seconds");
        }

        MPI.Finalize();
    }
}
