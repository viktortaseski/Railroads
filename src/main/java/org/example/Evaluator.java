package org.example;

public class Evaluator {
    private static Game game;
    private static int mode;

    public Evaluator(Game game, int mode) {
        Evaluator.game = game;
        Evaluator.mode = mode;
    }

     int evaluate() {
        int iterations = 10;
        int totalPathCost = 0;

        for (int i = 0; i < Game.getMap().length; i++) {
            for (int j = 0; j < Game.getMap()[0].length; j++) {
                System.out.print("[" + game.getMap()[i][j].getType() + " " + game.getMap()[i][j].getRotation() + " (" + game.getMap()[i][j].getX() + ", " + game.getMap()[i][j].getY() + ")]" + "    ");
            }
            System.out.println();
        }

        long startTime = System.currentTimeMillis();
        for (Train train: Game.TRAINS){
            totalPathCost += GeneticAlgorithm.run(game, iterations, mode, train);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Running time: " + (endTime - startTime) + " ms | " + iterations + " iterations");

        for (Train train: Game.TRAINS){
            System.out.println("Train[" + train.getId() + "] (" + train.getStartTile().getX() + ", " + train.getStartTile().getY() + ") Cost = " + (train.getPathCost() - 5) + " Path = " + train.getPath());
        }
        System.out.println("Map state:");
        for (Tile[] row : Game.getMap()) {
            for (Tile tile : row) {
                System.out.print("[" + tile.getType() + " " + tile.getRotation() + " (" + tile.getX() + ", " + tile.getY() + ")]" + "    ");
            }
            System.out.println();
        }


        return totalPathCost;
    }

}
