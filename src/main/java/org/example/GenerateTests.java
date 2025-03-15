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
            new Pair(10, 10),
            new Pair(20, 20),
            new Pair(30, 50)
            // You can add more test configurations as needed.
    };

    public static void runTests() throws IOException {
        StringBuilder output = new StringBuilder();
        // Define column widths
        int w1 = 20; // Test Type
        int w2 = 10; // Trains
        int w3 = 10; // Size
        int w4 = 15; // Time (ms)

        // Build header and separator lines.
        String header = formatRow("Test Type", "Trains", "Size", "Time (ms)", w1, w2, w3, w4);
        String separator = "+" + "-".repeat(w1) + "+" + "-".repeat(w2) + "+" + "-".repeat(w3) + "+" + "-".repeat(w4) + "+\n";

        output.append(separator);
        output.append(header);
        output.append(separator);

        // Run all sequential tests.
        output.append(runTestsForMode(1, "Sequential", w1, w2, w3, w4));

        // Run all parallel tests.
        output.append(runTestsForMode(2, "Parallel", w1, w2, w3, w4));

        output.append(separator);

        // Save all results to a file.
        saveResults(output.toString(), "results.txt");
    }

    /**
     * Runs tests for the given mode and returns the formatted output.
     *
     * @param mode  1 for sequential, 2 for parallel
     * @param label The label for the test type.
     * @param w1    Column width for Test Type.
     * @param w2    Column width for Trains.
     * @param w3    Column width for Size.
     * @param w4    Column width for Time.
     * @return A formatted string with the results for all test configurations.
     */
    private static String runTestsForMode(int mode, String label, int w1, int w2, int w3, int w4) {
        StringBuilder sb = new StringBuilder();
        for (Pair test : tests) {
            // Create a new game with the given mode, size, and number of trains.
            Game game = new Game(mode, test.getSize(), test.getTrains(), 1234);
            game.init();
            long startTime = System.currentTimeMillis();
            GeneticAlgorithm.start(game);
            long elapsedTime = System.currentTimeMillis() - startTime;

            sb.append(formatRow(
                    label + " Test",
                    String.valueOf(test.getTrains()),
                    String.valueOf(test.getSize()),
                    elapsedTime + "ms",
                    w1, w2, w3, w4
            ));
        }
        return sb.toString();
    }

    /**
     * Formats a row with centered text in each column.
     *
     * @param col1 First column text.
     * @param col2 Second column text.
     * @param col3 Third column text.
     * @param col4 Fourth column text.
     * @param w1   Width for first column.
     * @param w2   Width for second column.
     * @param w3   Width for third column.
     * @param w4   Width for fourth column.
     * @return A formatted row string.
     */
    private static String formatRow(String col1, String col2, String col3, String col4,
                                    int w1, int w2, int w3, int w4) {
        return "|" + center(col1, w1)
                + "|" + center(col2, w2)
                + "|" + center(col3, w3)
                + "|" + center(col4, w4)
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
