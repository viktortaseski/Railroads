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
            // Add parallel mode implementation here if needed.
        } else if (mode == 3) {
            System.out.println("DISTRIBUTED MODE!!");
            // Add distributed mode implementation here if needed.
        }
    }

    public static void runSequential(Game game) {
        int iterations = 50;
        int populationSize = 10;
        List<Train> trains = game.getTrains();

        for (Train train : trains) {
            run(iterations, populationSize, train);
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

    public static void run(int iterations, int populationSize, Train train) {
        Random random = new Random(1234);
        List<MapSolution> population = initPopulation(train, populationSize);

        for (int i = 0; i < iterations; i++) {
            List<MapSolution> nextGeneration = new ArrayList<>();

            // Evaluate fitness of the current population
            for (MapSolution solution : population) {
                solution.evaluateFitness();
                System.out.println("Solution: " + solution.getFitness());
            }
            System.out.println("========================");
            // Sort population by fitness (lower cost is better)
            population.sort(Comparator.comparingInt(MapSolution::getFitness));
            // Elitism: Retain the top-performing solutions
            int eliteCount = Math.max(1, populationSize / 2);
            nextGeneration.addAll(population.subList(0, eliteCount));

            // Generate offspring
            while (nextGeneration.size() < populationSize) {
                MapSolution parent1 = selectParent(population, random);
                MapSolution parent2 = selectParent(population, random);

                // Perform diagonal crossover
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

            // Log generation details using the best solution (first element)
            for (MapSolution solution : population) {
                solution.evaluateFitness();
            }
            population.sort(Comparator.comparingInt(MapSolution::getFitness));
//
//            System.out.println("========================");
//            System.out.println("Generation [" + i + "] ");
//            System.out.println("Best Generation Solution Cost: " + population.get(0).getFitness());
//            System.out.println("Path exists: " + train.getResult().isPathExists());
//            System.out.println("========================");
        }
        for (MapSolution solution : population) {
            solution.evaluateFitness();
        }
        Game.setBoard(population.getFirst().getMapLayout());
    }

    public static List<MapSolution> initPopulation(Train train, int populationSize) {
        List<MapSolution> population = new ArrayList<>();
        Random random = new Random(1234);
        for (int i = 0; i < populationSize; i++) {
            MapSolution solution = new MapSolution(HelperFunctions.createRandomMap(train, Game.size, random), 0, false);
            solution.setMapCost(solution.calculateMapCost());
            population.add(solution);
        }
        return population;
    }

    public static MapSolution selectParent(List<MapSolution> population, Random random) {
        // Improved Roulette Wheel Selection for minimization:
        // Lower fitness (cost) should have a higher chance of selection.
        int worstFitness = population.stream().mapToInt(MapSolution::getFitness).max().orElse(1);
        List<Integer> weights = new ArrayList<>();
        int totalWeight = 0;
        for (MapSolution sol : population) {
            // Inverse weight: better (lower) fitness gives higher weight.
            int weight = worstFitness - sol.getFitness() + 1; // +1 ensures a non-zero weight
            weights.add(weight);
            totalWeight += weight;
        }
        int randomValue = random.nextInt(totalWeight);
        int runningSum = 0;
        for (int i = 0; i < population.size(); i++) {
            runningSum += weights.get(i);
            if (runningSum > randomValue) {
                return population.get(i);
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

        // Diagonal crossover: choose a random pivot point in the matrix
        int pivotRow = random.nextInt(rows);
        int pivotCol = random.nextInt(cols);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // For cells in the bottom-right (relative to the pivot), swap values.
                if (i >= pivotRow && j >= pivotCol) {
                    Tile temp = childLayout1[i][j];
                    childLayout1[i][j] = childLayout2[i][j];
                    childLayout2[i][j] = temp;
                }
            }
        }

        MapSolution child1 = new MapSolution(childLayout1, 0, false);
        MapSolution child2 = new MapSolution(childLayout2, 0, false);

        return List.of(child1, child2);
    }

    // Utility method to deep copy a 2D array of Tiles
    private static Tile[][] deepCopy(Tile[][] original) {
        Tile[][] copy = new Tile[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = original[i].clone();
        }
        return copy;
    }

    public static void mutate(MapSolution solution, Random random) {
        // Use deep copy to avoid modifying the original array reference.
        Tile[][] map = deepCopy(solution.getMapLayout());
        int i = random.nextInt(map.length);
        int j = random.nextInt(map[0].length);
        // If the tile is not a TRAIN or STATION, change it to one of the low-cost road types.
        if (map[i][j].getType() != TileType.STATION && map[i][j].getType() != TileType.TRAIN) {
            // Assuming the last two enum values represent STATION and TRAIN, select from the others.
            TileType[] possibleTypes = Arrays.copyOf(TileType.values(), TileType.values().length - 2);
            map[i][j].setType(possibleTypes[random.nextInt(possibleTypes.length)]);
        }
        solution.setMapLayout(map);
    }
}
