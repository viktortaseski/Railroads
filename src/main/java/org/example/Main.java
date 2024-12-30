package org.example;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static ExecutorService pool = Executors.newFixedThreadPool(3);
    public static void main(String[] args) throws IOException {

        pool.submit(new InputHandler());
        pool.submit(new GameLoop());


    }
}