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
         PathResult result = null;

        for (int i = 0; i < Game.getMap().length; i++) {
            for (int j = 0; j < Game.getMap()[0].length; j++) {
                System.out.print("[" + game.getMap()[i][j].getType() + " " + game.getMap()[i][j].getRotation() + " (" + game.getMap()[i][j].getX() + ", " + game.getMap()[i][j].getY() + ")]" + "    ");
            }
            System.out.println();
        }

        long startTime = System.currentTimeMillis();
        for (Train train: Game.TRAINS){
            result = GeneticAlgorithm.run(game, iterations, mode, train);
            totalPathCost = result.getPathCost();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Running time: " + (endTime - startTime) + " ms | " + iterations + " iterations");

        for (Train train: Game.TRAINS){
            System.out.println("Train[" + train.getId() + "] (" + train.getStartTile().getX() + ", " + train.getStartTile().getY() + ") Cost = " + train.getPathCost() + " Changes = " + result.getChanges() + " Path = " + train.getPath());
        }
        System.out.println("Map state:");
        Tile[][] map = Game.getMap();
         for (int i = 0; i < map.length; i++) {
             for (int j = 0; j < map[0].length; j++) {
                 System.out.print("[" + map[i][j].getType() + " " + map[i][j].getRotation() + " (" + map[i][j].getX() + ", " + map[i][j].getY() + ")]" + "    ");
             }
             System.out.println();
         }


        return totalPathCost;
    }

}
