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
        long startTime = System.currentTimeMillis();
        int score = GeneticAlgorithm.run(game, iterations, mode);
        long endTime = System.currentTimeMillis();
        System.out.println("Running time: " + (endTime - startTime) + " ms | " + iterations + " iterations");
        int totalPathCost = 0;
        for (Train train: Game.TRAINS){
            System.out.println("Train[" + train.id + "] " + train.getPath() + " | path cost: " + train.getPathCost());
            totalPathCost += train.getPathCost();
        }

        return totalPathCost;
    }

}
