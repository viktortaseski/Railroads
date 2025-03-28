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
        int size = MPI.COMM_WORLD.Size();
        long startTime = 0;
        int partitionSize = populationSize / size;

        MapSolution[] sendBuffer = new MapSolution[populationSize];
        MapSolution[] receiveBuffer = new MapSolution[partitionSize];


        // CAN'T SKIP THIS SINCE THE INITIALIZED POPULATION HAS NULL GENERATIONS.
        if (rank == ROOT) {
            startTime = System.currentTimeMillis();
            population = GeneticAlgorithm.initPopulation(populationSize).toArray(new MapSolution[populationSize]);
            // System.out.println("ROOT FINISHED population of size: " + population.length);
            int[] signal = new int[]{1};
            for (int i = 1; i < size; i++) {
                sendBuffer = population;
                MPI.COMM_WORLD.Send(signal, 0, 1, MPI.INT, i, 0);
                //System.out.println("ROOT: Signal sent to process " + i);
            }
        } else {
            int[] signal = new int[1];
            //System.out.println("Process " + rank + " waiting for signal from ROOT");
            MPI.COMM_WORLD.Recv(signal, 0, 1, MPI.INT, ROOT, 0);
            //System.out.println("Process " + rank + " received signal from ROOT, continuing.");
        }

        // Start of the evolving iterations.
        for (int iter = 0; iter < iterations; iter++) {

            MPI.COMM_WORLD.Scatter(sendBuffer, 0, partitionSize, MPI.OBJECT, receiveBuffer, 0, partitionSize, MPI.OBJECT, ROOT);

            // NOW WE EACH MACHINE EVALUATES ITS PARTITION
            if (rank == ROOT) {
                List<MapSolution> partitionOfPopulation = new ArrayList<>(Arrays.asList(receiveBuffer));
                GeneticAlgorithm.parallelFitnessEvaluation(partitionOfPopulation, executor);
                receiveBuffer = partitionOfPopulation.toArray(new MapSolution[partitionSize]);

                System.out.println("Me " + rank + " has finished evaluating population of size " + receiveBuffer.length);
                for (int i = 1; i < size; i++) {
                    int[] signal = new int[1];
                    // Have to receive 4 signals to continue.
                    MPI.COMM_WORLD.Recv(signal, 0, 1, MPI.INT, i, 0);
                }
                // Root has received all signals can advance forward
            } else {
                int[] signal = new int[]{1};
                List<MapSolution> partitionOfPopulation = new ArrayList<>(Arrays.asList(receiveBuffer));
                GeneticAlgorithm.parallelFitnessEvaluation(partitionOfPopulation, executor);
                receiveBuffer = partitionOfPopulation.toArray(new MapSolution[partitionSize]);
                System.out.println("Me " + rank + " has finished evaluating population of size " + receiveBuffer.length);
                // Node has finished evaluation send signal that is ready to continue
                MPI.COMM_WORLD.Send(signal, 0, 1, MPI.INT, ROOT, 0);
            }

            MPI.COMM_WORLD.Gather(receiveBuffer, 0, partitionSize, MPI.OBJECT,
                    sendBuffer, 0, partitionSize, MPI.OBJECT, ROOT);

            // HAVE TO WAIT FOR ROOT TO CREATE NEW GEN SO THAT THE OTHER NODES DON'T EVALUATE THE SAME PARTITIONS AGAIN
            if (rank == ROOT) {
                List<MapSolution> sendBufferList = new ArrayList<>(Arrays.asList(sendBuffer));
                List<MapSolution> returnedList = GeneticAlgorithm.creatingNewGeneration(sendBufferList, populationSize, random);
                sendBuffer = returnedList.toArray(new MapSolution[populationSize]);
            }

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
            System.out.println("==========================================");
            System.out.println("Population size " + finalPopulation.size());
            System.out.println("Best Map fitness: " + bestMap.getFitness());
            System.out.println("Time: " + (endTime - startTime) + "ms " + (endTime - startTime)/1000 + " seconds");
        }

        MPI.Finalize();
    }
}
