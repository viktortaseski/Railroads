package org.example;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class GenerateTests {

    // Inner class to represent a test configuration.
    public static class Pair {
        int size, trains;
        public Pair(int size, int trains) {
            this.size = size;
            this.trains = trains;
        }
        public int getSize() {
            return size;
        }
        public int getTrains() {
            return trains;
        }
    }

    // Array of test configurations.
    public static Pair[] tests = {
            new Pair(2, 1),
            new Pair(5, 2),
            new Pair(10, 5),
            new Pair(20, 20),
            //new Pair(30, 50),
            //new Pair(40, 100),
            //new Pair(50, 200),
    };

    public static void runTests() throws IOException {
        StringBuilder output = new StringBuilder();
        // Define column widths.
        int w1 = 20; // Test Type
        int w2 = 10; // Size
        int w3 = 10; // Trains
        int w4 = 15; // Time (ms)
        int w5 = 15; // Map Cost
        int w6 = 12; // Iterations

        // Build header and separator lines.
        String header = formatRow("Test Type", "Size", "Trains", "Time (ms)", "Map Cost", "Iterations", w1, w2, w3, w4, w5, w6);
        String separator = "+" + "-".repeat(w1) + "+" + "-".repeat(w2) + "+" + "-".repeat(w3) + "+" + "-".repeat(w4) + "+" + "-".repeat(w5) + "+" + "-".repeat(w6) + "+\n";

        output.append(separator);
        output.append(header);
        output.append(separator);

        // Run all sequential tests with 100 iterations.
        output.append(runTestsForMode(1, 100, "Sequential", w1, w2, w3, w4, w5, w6));
        // Run all parallel tests with 100 iterations.
        output.append(runTestsForMode(2, 100, "Parallel", w1, w2, w3, w4, w5, w6));
        // Run distributed tests with 100 iterations.
        output.append(runTestsForMode(3, 100, "Distributed", w1, w2, w3, w4, w5, w6));

        // Add a separator line between 100 iterations and 1000 iterations tests.
        output.append(separator);

        // Run All tests again but with 1000 iterations.
        output.append(runTestsForMode(1, 1000, "Sequential", w1, w2, w3, w4, w5, w6));
        output.append(runTestsForMode(2, 1000, "Parallel", w1, w2, w3, w4, w5, w6));
        output.append(runTestsForMode(3, 1000, "Distributed", w1, w2, w3, w4, w5, w6));

        output.append(separator);

        // Save all results to a file.
        saveResults(output.toString(), "results.txt");
    }

    /**
     * Runs tests for a given mode and returns formatted rows.
     *
     * @param mode  1 for sequential, 2 for parallel, 3 for distributed.
     * @param iterations Number of iterations to run.
     * @param label The label for the test type.
     * @param w1    Column width for Test Type.
     * @param w2    Column width for Size.
     * @param w3    Column width for Trains.
     * @param w4    Column width for Time (ms).
     * @param w5    Column width for Map Cost.
     * @param w6    Column width for Iterations.
     * @return A formatted string with the results for all test configurations.
     */
    private static String runTestsForMode(int mode, int iterations, String label, int w1, int w2, int w3, int w4, int w5, int w6) {
        StringBuilder sb = new StringBuilder();
        for (Pair test : tests) {
            // Create a new game with the given mode, size, and number of trains.
            Game game = new Game(mode, iterations, test.getSize(), test.getTrains(), 1234);
            game.init();
            long startTime = System.currentTimeMillis();
            GeneticAlgorithm.start();
            long elapsedTime = System.currentTimeMillis() - startTime;
            // Retrieve the best map cost after running the test.
            int bestMapCost = Game.getBoardFitness();

            sb.append(formatRow(
                    label + " Test",
                    String.valueOf(test.getSize()),
                    String.valueOf(test.getTrains()),
                    elapsedTime + "ms",
                    String.valueOf(bestMapCost),
                    String.valueOf(iterations),
                    w1, w2, w3, w4, w5, w6
            ));
        }
        return sb.toString();
    }

    /**
     * Formats a row with centered text in each of six columns.
     */
    private static String formatRow(String col1, String col2, String col3, String col4, String col5, String col6,
                                    int w1, int w2, int w3, int w4, int w5, int w6) {
        return "|" + center(col1, w1)
                + "|" + center(col2, w2)
                + "|" + center(col3, w3)
                + "|" + center(col4, w4)
                + "|" + center(col5, w5)
                + "|" + center(col6, w6)
                + "|\n";
    }

    private static String center(String s, int width) {
        if (s == null) s = "";
        int len = s.length();
        if (len >= width) return s;
        int padSize = width - len;
        int padStart = padSize / 2;
        int padEnd = padSize - padStart;
        return " ".repeat(padStart) + s + " ".repeat(padEnd);
    }

    public static void saveResults(String content, String filename) throws IOException {
        File file = new File(filename);
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(content);
        writer.close();
        System.out.println("Results saved to " + filename);
    }
}
