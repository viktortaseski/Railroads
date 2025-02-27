package org.example;

import java.util.*;

public class GeneticAlgorithm {


    public static void start(Game game) {
        int mode = game.getMode();

        if (mode == 1) {
            System.out.println("Running Genetic Algorithm in Sequential Mode :) ");
            runSequential(game);
        } else if (mode == 2) {
            System.out.println("PARALLEL MODE!!!");
        } else if (mode == 3) {
            System.out.println("DISTRIBUTED MODE!!");
        }

    }

    public static void runSequential(Game game) {
        int iterations = 50;
        int populationSize = 10;
        List<Train> trains = game.getTrains();

        for (Train train : trains) run(iterations, populationSize, train);

        // =============================
        //          TESTING
        // =============================
        /*
        Tile[][] map = Game.getMap();
        Tile currentTest = map[3][3];
        List<Tile> neighbours = currentTest.getNeighbors(map);
        for (Tile neighbour : neighbours){
            System.out.println("Neighbor["+neighbour.getX() + "," + neighbour.getY() + "]: " + neighbour.getType() + " " + neighbour.getRotation() + " connection is " + isValidConnection(currentTest, neighbour) );
            if (!isValidConnection(currentTest, neighbour)){
                updateTileConnection(currentTest, neighbour);
                System.out.println("After update Neighbor["+neighbour.getX() + "," + neighbour.getY() + "]: " + neighbour.getType() + " " + neighbour.getRotation() + " connection is " + isValidConnection(currentTest, neighbour) );
            }
        }
         */
        // =============================
    }

    public static void run(int iterations, int populationSize, Train train) {
        Random random = new Random();
        List<MapSolution> population = initPopulation(train, populationSize);

        // Print initial population
        System.out.println("Initial Population:");
        int c = 0;
        for (MapSolution solution : population) {
            System.out.println("Solution " + c + ": " + solution.getFitness());
            c++;
        }
        System.out.println("=========== Evolving ===========");
        for (int i = 0; i < iterations; i++) {
            List<MapSolution> nextGeneration = new ArrayList<>();

            // Evaluate fitness of the current population
            for (MapSolution solution : population) {
                solution.evaluateFitness();
                System.out.println("Solution: " + solution.getFitness());
            }

            // Sort population by fitness (lower cost is better)
            population.sort(Comparator.comparingInt(MapSolution::getFitness));
            // Elitism: Retain the top-performing solutions
            int eliteCount = Math.max(1, populationSize/2);
            nextGeneration.addAll(population.subList(0, eliteCount));

            // Generate offspring
            while (nextGeneration.size() < populationSize) {
                MapSolution parent1 = selectParent(population, random);
                MapSolution parent2 = selectParent(population, random);

                // Perform crossover
                List<MapSolution> offspring = crossover(parent1, parent2, random);

                // Mutate and evaluate offspring
                for (MapSolution child : offspring) {
                    mutate(child, random);
                    child.evaluateFitness();
                    nextGeneration.add(child);
                    if (nextGeneration.size() >= populationSize) {
                        break;
                    }
                }
            }

            population = nextGeneration;

            population.getFirst().evaluateFitness();
            // Log generation details
            System.out.println("========================");
            System.out.println("Generation [" + i + "] ");
            System.out.println("Best Generation Solution Cost: " + population.getFirst().getFitness());
            System.out.println("Path exists: " + train.getResult().isPathExists());
            System.out.print("Path: ");
            for (Tile path : train.getResult().getPath()) {
                System.out.print("(" + path.getX() + "," + path.getY() + ") ");
            }
            System.out.println("\n=======================");
        }


        Game.setBoard(population.getFirst().getMapLayout());
    }


    public static List<MapSolution> initPopulation(Train train, int populationSize) {
        List<MapSolution> population = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            MapSolution solution = new MapSolution(HelperFunctions.createRandomMap(train, Game.size), 0, false);
            solution.setMapCost(solution.calculateMapCost());
            population.add(solution);
        }
        return population;
    }

    public static MapSolution selectParent(List<MapSolution> population, Random random) {
        // Roulette Wheel Selection
        int totalFitness = population.stream().mapToInt(MapSolution::getFitness).sum();
        int randomValue = random.nextInt(totalFitness);
        int runningSum = 0;
        for (MapSolution solution : population) {
            runningSum += solution.getFitness();
            if (runningSum >= randomValue) {
                return solution;
            }
        }
        return population.get(population.size() - 1); // Fallback
    }

    public static List<MapSolution> crossover(MapSolution parent1, MapSolution parent2, Random random) {
        Tile[][] layout1 = parent1.getMapLayout();
        Tile[][] layout2 = parent2.getMapLayout();
        int rows = layout1.length;
        int cols = layout1[0].length;

        // Create offspring with deep copies of parent layouts
        Tile[][] childLayout1 = deepCopy(layout1);
        Tile[][] childLayout2 = deepCopy(layout2);

        // Example: Row-wise crossover
        int crossoverRow = random.nextInt(rows);
        for (int i = crossoverRow; i < rows; i++) {
            Tile[] temp = childLayout1[i];
            childLayout1[i] = childLayout2[i];
            childLayout2[i] = temp;
        }

        // Create offspring MapSolution objects
        MapSolution child1 = new MapSolution(childLayout1, 0, false);
        MapSolution child2 = new MapSolution(childLayout2, 0, false);

        return List.of(child1, child2);
    }

    // Utility method to deep copy a 2D array
    private static Tile[][] deepCopy(Tile[][] original) {
        Tile[][] copy = new Tile[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = original[i].clone();
        }
        return copy;
    }


    public static void mutate(MapSolution solution, Random random) {
        // Example mutation: Randomly change one part of the map layout
        int i = random.nextInt(solution.getMapLayout().length);
        int j = random.nextInt(solution.getMapLayout()[0].length);
        Tile[][] map = solution.getMapLayout().clone();
        // If it is not a train nor a station change it to the lowest cost road.
        if (map[i][j].getType() != TileType.STATION && map[i][j].getType() != TileType.TRAIN) {
            map[i][j].setType(TileType.values()[random.nextInt(TileType.values().length-2)]);
            //map[i][j].setType(TileType.STRAIGHT);
        }
        solution.setMapLayout(map);
    }

}
