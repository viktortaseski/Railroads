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
        System.out.println("1. Tested settings: Mode = 1, Size = 4 and Trains = 1");
        System.out.println("2. After that press on the GUI with your cursor. For running the game press 's' on your keyboard");
        System.out.println("3. If you want to make changes to the map you can press any valid number then ENTER key.");
        System.out.println("4. The lower the score the better. e.g. -20 > 30 ");
        System.out.println("5. Straight roads are considered of cost 0. Turns of 1 and so on.");
        System.out.println("\t=============================\n");

        Scanner scanner = new Scanner(System.in);
        int mode;
        do {
            System.out.println("Enter mode (1)Sequential (2)Parallel (3)Distributed (4)Automatic Testing");
            mode = scanner.nextInt();
        } while (mode < 1 || mode > 4);

        // If automatic testing mode is selected, run tests and exit.
        if (mode == 4) {
            GenerateTests.runTests();
            System.exit(0);
        }

        int mapSize;
        int numberOfTrains;
        do {
            System.out.println("Enter map size: 'max: 50, min: 3' ");
            mapSize = scanner.nextInt();
        } while (mapSize < 3 || mapSize > 50);
        do {
            System.out.println("Enter trains: 'max: " + (mapSize * mapSize / 2) + ", min: 1' ");
            numberOfTrains = scanner.nextInt();
        } while (numberOfTrains < 1 || numberOfTrains > mapSize * mapSize / 2);

        System.out.println("=========================");
        System.out.println("=\t\tMode: " + mode + "\t\t\t=");
        System.out.println("=\t\tMap size: " + mapSize + "\t\t=");
        System.out.println("=\t\tTrains: " + numberOfTrains + "\t\t=");
        System.out.println("=========================\n");
        System.out.println("========= Starting.. =========");

        Game game = new Game(mode, mapSize, numberOfTrains, 1234);
        game.init();

        // Start game threads.
        pool.submit(new InputHandler());
        pool.submit(new GameLoop(game));
        pool.submit(new Gui());
    }
}
