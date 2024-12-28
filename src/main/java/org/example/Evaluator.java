package org.example;

public class Evaluator {
    private static Game game;
    private static int mode;

    public Evaluator(Game game, int mode) {
        Evaluator.game = game;
        Evaluator.mode = mode;
    }

    public static int evaluate() {
        int iterations = 1_000;
        int totalPathCost = 0;
        long startTime = System.currentTimeMillis();
        for (Train train: Game.TRAINS){
            totalPathCost += GeneticAlgorithm.run(game, iterations, mode, train);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Running time: " + (endTime - startTime) + " ms | " + iterations + " iterations");

        for (Train train: Game.TRAINS){
            System.out.println("Train[" + train.getId() + "] Cost = " + train.getPathCost() + " Path = " +train.getPath());
        }

        return totalPathCost;
    }

}
