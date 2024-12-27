package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GeneticAlgorithm {

    public static int fitness;

    public static int run(Game game, int iterations, int mode){
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        Random random = new Random(1000);

        if ( mode == 1 ){   // Sequential execution
            for (int i = 0; i < iterations; i++){
                fitness = Fitness.evaluate(Game.map);
                mutateMap(Game.map, random);
                System.out.println("Sequential Iteration, Fitness: " + fitness);
            }
        } else if ( mode == 2 ) {   // Parallel execution
            List<Callable<Void>> tasks = new ArrayList<>();
            for (int i = 0; i < iterations; i++) {
                tasks.add(() -> {
                   mutateMap(Game.map, random);
                   fitness = Fitness.evaluate(Game.map);
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

    private static void mutateMap(Tile[][] map, Random random) {
        for (Train train : Game.TRAINS) {
            // Ensure the train's path is initialized
            if (train.getPath() == null) {
                System.out.println("Train path is null. Skipping train mutation.");
                continue;
            }

            List<String> path = train.getPath();
            if (path.isEmpty()) {
                System.out.println("Train path is empty. Skipping train mutation.");
                continue;
            }

            boolean reachedStation = false;
            Tile currentTile = train.startTile; // Assuming startTile is the starting point
            Tile endStation = train.endTile;   // Assuming endTile is the station to reach

            for (String direction : path) {
                if (currentTile.equals(endStation)) {
                    reachedStation = true;
                    break;
                }
                // Move the currentTile based on direction
                currentTile = move(currentTile, direction, map);
            }

            if (reachedStation) {
                System.out.println("Train has already reached the station. Skipping mutation.");
                continue;
            }

            // Mutate the path to try reaching the station
            int mutationType = random.nextInt(3); // Choose mutation type: 0 = add, 1 = remove, 2 = replace

            switch (mutationType) {
                case 0: // Add a direction to the path
                    String randomDirection = getRandomDirection(random);
                    path.add(randomDirection);
                    System.out.println("Added direction to train path: " + randomDirection);
                    break;

                case 1: // Remove a direction from the path
                    if (path.size() > 1) { // Ensure at least one direction remains
                        String removedDirection = path.remove(path.size() - 1);
                        System.out.println("Removed direction from train path: " + removedDirection);
                    }
                    break;

                case 2: // Replace a direction in the path
                    int indexToReplace = random.nextInt(path.size());
                    String oldDirection = path.get(indexToReplace);
                    String newDirection = getRandomDirection(random);
                    path.set(indexToReplace, newDirection);
                    System.out.println("Replaced direction in train path: " + oldDirection + " -> " + newDirection);
                    break;
            }
        }
    }

    private static String getRandomDirection(Random random) {
        String[] directions = {"N", "E", "S", "W"};
        return directions[random.nextInt(directions.length)];
    }

    private static Tile move(Tile currentTile, String direction, Tile[][] map) {
        int newX = currentTile.x;
        int newY = currentTile.y;

        switch (direction) {
            case "N": newY -= 1; break;
            case "E": newX += 1; break;
            case "S": newY += 1; break;
            case "W": newX -= 1; break;
        }

        // Check if the move is within bounds
        if (newX >= 0 && newX < map.length && newY >= 0 && newY < map[0].length) {
            return map[newX][newY];
        }
        return currentTile; // Return the current tile if the move is invalid
    }


}
