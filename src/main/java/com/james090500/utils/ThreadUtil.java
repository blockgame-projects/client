package com.james090500.utils;

import lombok.Getter;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadUtil {

    @Getter
    private static final ExecutorService queue;

    @Getter
    private static final ConcurrentLinkedQueue<Runnable> mainQueue = new ConcurrentLinkedQueue<>();

    static {
        int cores = Runtime.getRuntime().availableProcessors();
        queue = Executors.newFixedThreadPool(cores - 1);
    }

    public static void runMainQueue() {
        if(!mainQueue.isEmpty()) {
            mainQueue.poll().run();
        }
    }
}
