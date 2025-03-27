package org.example;

import mpi.MPI;
import mpi.MPIException;
import java.util.*;

public class DistributedMain {

    public static final int ROOT = 0;
    public static final int iterations = 100;
    public static final int mapSize = 20;
    public static final int populationSize = 50;
    public static final int numberOfTrains = 5;
    public static MapSolution[] population = new MapSolution[populationSize];

    public static void main(String[] args) throws MPIException {
        // Initialize the game before MPI.Init.
        // Use mode 1 (sequential GA) so that GA settings match your sequential/parallel versions.
        Game game = new Game(1, iterations, mapSize, numberOfTrains, 1234);
        game.init();

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
            population = GeneticAlgorithm.initPopulation(populationSize).toArray(new MapSolution[0]);
            System.out.println("ROOT FINISHED population of size: " + population.length);
            int[] signal = new int[]{1};
            for (int i = 1; i < size; i++) {
                sendBuffer = population;
                MPI.COMM_WORLD.Send(signal, 0, 1, MPI.INT, i, 0);
                System.out.println("ROOT: Signal sent to process " + i);
            }
        } else {
            int[] signal = new int[1];
            System.out.println("Process " + rank + " waiting for signal from ROOT");
            MPI.COMM_WORLD.Recv(signal, 0, 1, MPI.INT, ROOT, 0);
            System.out.println("Process " + rank + " received signal from ROOT, continuing.");
        }

        // Start of the evolving iterations.
        for (int iter = 0; iter < iterations; iter++) {

            MPI.COMM_WORLD.Scatter(sendBuffer, 0, partitionSize, MPI.OBJECT, receiveBuffer, 0, partitionSize, MPI.OBJECT, ROOT);

            // NOW WE EACH MACHINE EVALUATES ITS PARTITION
            if (rank == ROOT) {
                for (MapSolution mapSolution : receiveBuffer) {
                    mapSolution.evaluateFitness();
                }
                for (int i = 1; i < size; i++) {
                    int[] signal = new int[1];
                    MPI.COMM_WORLD.Recv(signal, 0, 1, MPI.INT, i, 0);
                    System.out.println("ROOT: Signal received signal form process " + i);
                }
                System.out.println("ROOT HAS RECEIVED ALL SIGNALS");
            } else {
                int[] signal = new int[]{1};
                for (MapSolution mapSolution : receiveBuffer) {
                    mapSolution.evaluateFitness();
                }
                MPI.COMM_WORLD.Send(signal, 0, 1, MPI.INT, ROOT, 0);
                System.out.println("NODE" + rank + " HAS FINISHED EVALUATION (SIGNAL SENT)");
            }

            MPI.COMM_WORLD.Gather(receiveBuffer, 0, partitionSize, MPI.OBJECT,
                    sendBuffer, 0, partitionSize, MPI.OBJECT, ROOT);

            if (rank == ROOT) {
                for (MapSolution mapSolution : sendBuffer) {
                    System.out.println(" Fitness is " + mapSolution.getFitness());
                }
                List<MapSolution> sendBufferList = new ArrayList<>(Arrays.asList(sendBuffer));
                List<MapSolution> returnedList = GeneticAlgorithm.creatingNewGeneration(sendBufferList, populationSize, new Random(12345));
                sendBuffer = returnedList.toArray(new MapSolution[0]);
            }
        }

        if (rank == ROOT) {
            long endTime = System.currentTimeMillis();
            for (MapSolution solution : sendBuffer) {
                solution.evaluateFitness();
            }
            List<MapSolution> finalPopulation = new ArrayList<>(Arrays.asList(sendBuffer));
            finalPopulation.sort(Comparator.comparingInt(MapSolution::getFitness));
            MapSolution bestMap = finalPopulation.getFirst();
            System.out.println("Best Map fitness: " + bestMap.getFitness());
            System.out.println("Time: " + (endTime - startTime) + "ms");
        }

        MPI.Finalize();
    }
}
