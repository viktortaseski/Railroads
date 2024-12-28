package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.HashSet;
import java.util.Set;

public class Game {
    public static int size;
    public static int numTrains;
    public static Tile[][] map;
    public static List<Train> TRAINS;

    public Game(int size, int numTrains) {
        Game.size = size;
        Game.numTrains = numTrains;
    }

    public void init(){
        map = new Tile[size][size];
        TRAINS = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                map[i][j] = Tile.randomRoad(i,j);
            }
        }

        for (int i = 0; i < numTrains; i++) {
            Set<String> usedCoordinates = new HashSet<>(); // To track used coordinates

            // Generate unique start coordinates for the train
            int x = new Random().nextInt(size);
            int y = new Random().nextInt(size);
            while (usedCoordinates.contains(x + "," + y)) {  // Ensure the coordinate is unique
                x = new Random().nextInt(size);
                y = new Random().nextInt(size);
            }
            usedCoordinates.add(x + "," + y);  // Add the coordinate to the used set
            Tile start = new Tile(x, y, Rotation.ZERO, TileType.TRAIN);
            map[x][y] = start;

            // Generate unique end coordinates for the station
            x = new Random().nextInt(size);
            y = new Random().nextInt(size);
            while (usedCoordinates.contains(x + "," + y)) {  // Ensure the coordinate is unique
                x = new Random().nextInt(size);
                y = new Random().nextInt(size);
            }
            usedCoordinates.add(x + "," + y);  // Add the coordinate to the used set
            Tile end = new Tile(x, y, Rotation.ZERO, TileType.STATION);
            map[x][y] = end;

            // Create a train with unique start and end positions
            Train train = new Train(start, end);
            TRAINS.add(train);
        }

        int index = 0;
        for (Train train : TRAINS) {
            index++;
            train.setId(index);
            System.out.println("Train[" + index + "] (" + train.getStartTile().getX() + ", " + train.getStartTile().getY() + ") (" + train.getEndTile().getX() + ", " + train.getEndTile().getY() + ")");
        }
    }

}
