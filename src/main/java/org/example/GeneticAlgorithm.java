package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class GeneticAlgorithm {

    public static int fitness;

    public static int run(Game game, int iterations, int mode, Train train){
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        Random random = new Random(1000);
        PathResult[] solutions = new PathResult[iterations];
        PathResult bestFitness = new PathResult(false, Integer.MAX_VALUE, null);

        if ( mode == 1 ){   // Sequential execution
            for (int i = 0; i < iterations; i++){
                solutions[i] = Fitness.evaluate(Game.getMap(), train);
                if (solutions[i].getPathCost() < bestFitness.getPathCost()){
                    System.out.println("Found better path.");
                    bestFitness = solutions[i];
                    mutatePath(bestFitness.getPath(), random);
                    fitness = bestFitness.getPathCost();
                }
                System.out.println("Sequential Iteration, Fitness: " + fitness);
            }
        } else if ( mode == 2 ) {   // Parallel execution
            List<Callable<Void>> tasks = new ArrayList<>();
            for (int i = 0; i < iterations; i++) {
                tasks.add(() -> {
                   //mutatePath(Game.map, random);
                    //fitness = Fitness.evaluate(Game.map);
                    System.out.println("Parallel Iteration, Fitness: " + fitness);
                    return null;
                });

            }
            try {
                executor.invokeAll(tasks);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        } else if ( mode == 3 ) {   // Distributed
            System.out.println("Distributed Iteration, Fitness: " + fitness);
        }else{
            System.out.println("Invalid mode");
        }

        executor.shutdown();
        return fitness;
    }

    private static void mutatePath(List<String> path, Random random) {
        if (path == null || path.isEmpty()) {
            System.out.println("Path is null or empty. Skipping mutation.");
            return;
        }

        // Choose a mutation type: 0 = add, 1 = remove, 2 = replace
        int mutationType = random.nextInt(3);

        switch (mutationType) {
            case 0: // Add a direction to the path
                String newDirection = getRandomDirection(random);
                path.add(newDirection);
                System.out.println("Added direction to path: " + newDirection);
                break;

            case 1: // Remove a direction from the path
                if (path.size() > 1) { // Ensure at least one direction remains
                    String removedDirection = path.remove(random.nextInt(path.size()));
                    System.out.println("Removed direction from path: " + removedDirection);
                } else {
                    System.out.println("Path is too short to remove a direction. Skipping mutation.");
                }
                break;

            case 2: // Replace a direction in the path
                int indexToReplace = random.nextInt(path.size());
                String oldDirection = path.get(indexToReplace);
                String replacementDirection = getRandomDirection(random);
                path.set(indexToReplace, replacementDirection);
                System.out.println("Replaced direction in path: " + oldDirection + " -> " + replacementDirection);
                break;
        }
    }


    private static String getRandomDirection(Random random) {
        String[] directions = {"N", "E", "S", "W"};
        return directions[random.nextInt(directions.length)];
    }


}
