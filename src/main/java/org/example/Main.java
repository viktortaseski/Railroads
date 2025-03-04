package org.example;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static ExecutorService pool = Executors.newFixedThreadPool(3);
    public static void main(String[] args) throws IOException {
        System.out.println("\t\t\tHELLO WORLD");
        System.out.println("\t=========== RULES ===========");
        System.out.println("1. For the program to work start it in Mode = 1, Size = 4 and Trains = 1");
        System.out.println("2. After that press on the GUI with your cursor and then press 's' on your keyboard");
        System.out.println("3. If you completed steps 1. and 2. the algorithm 'should' start :)");
        System.out.println("4. If a train doesn't reach the station a map is given +20 penalty.");
        System.out.println("5. Straight roads are considered of cost 0.");
        System.out.println("\t=============================\n");
        Scanner scanner = new Scanner(System.in);
        int mode;
        int mapSize;
        int numberOfTrains;
        do {
            System.out.println("Enter mode (1)Sequential (2)Parallel (3)Distributed");
            mode = scanner.nextInt();
        }while(mode < 1 || mode > 3);
        do {
            System.out.println("Enter map size: 'max: 50, min: 3' ");
            mapSize = scanner.nextInt();
        }while (mapSize < 3 || mapSize > 50);
        do {
            System.out.println("Enter trains: 'max: " + mapSize*mapSize/2 + ", min: 1' ");
            numberOfTrains  = scanner.nextInt();
        }while(numberOfTrains < 1 || numberOfTrains > mapSize*mapSize / 2);
        System.out.println("==============================");
        System.out.println("Mode: " + mode + "\nMap size: " + mapSize + "\ntrains: " + numberOfTrains);
        System.out.println("==============================");
        System.out.println("========= Starting.. =========");
        Game game = new Game(mode, mapSize, numberOfTrains, 1234);
        game.init();
        pool.submit(new InputHandler());
        pool.submit(new GameLoop(game));
        pool.submit(new Gui());
        System.out.println("hello world");
    }
}