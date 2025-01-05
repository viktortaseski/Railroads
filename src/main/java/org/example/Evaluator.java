package org.example;

public class Evaluator {
    private static Game game;
    private static int mode;

    public Evaluator(Game game, int mode) {
        Evaluator.game = game;
        Evaluator.mode = mode;
    }

     int evaluate() {
        int iterations = 1000;
        int totalPathCost = 0;
         PathResult result = null;

        for (int i = 0; i < Game.getMap().length; i++) {
            for (int j = 0; j < Game.getMap()[0].length; j++) {
                System.out.print("[" + Game.getMap()[i][j].getType() + " " + Game.getMap()[i][j].getRotation() + " (" + Game.getMap()[i][j].getX() + ", " + Game.getMap()[i][j].getY() + ")]" + "    ");
            }
            System.out.println();
        }

        long startTime = System.currentTimeMillis();
        for (Train train: Game.TRAINS){
            result = GeneticAlgorithm.run(iterations, 10, train);
            totalPathCost = result.getPathCost();
            train.setBestResult(result);
            GeneticAlgorithm.applyBestPathToMap(game.getMap(), result);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Running time: " + (endTime - startTime) + " ms | " + iterations + " iterations");

        for (Train train: Game.TRAINS){
            System.out.println("Train[" + train.getId() + "] (" + train.getStartTile().getX() + ", " + train.getStartTile().getY() + ") Cost = " + train.getBestResult().getPathCost() + " Distance = " + result.getDistance() + " Path = " + train.getBestResult().getPath());
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
