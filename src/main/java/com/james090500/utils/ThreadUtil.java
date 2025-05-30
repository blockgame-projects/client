package com.james090500.utils;

import lombok.Getter;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ThreadUtil {

    @Getter
    private static final ConcurrentLinkedQueue<Runnable> mainQueue = new ConcurrentLinkedQueue<>();

    private static HashMap<String, ExecutorService> queues = new HashMap<>();

    /**
     * Create a new queue
     * @param name
     * @return
     */
    public static ExecutorService getQueue(String name) {
        ExecutorService queue = queues.get(name);
        if(queue == null || queue.isShutdown()) {
            int cores = Runtime.getRuntime().availableProcessors();
            ExecutorService newQueue = Executors.newFixedThreadPool(cores - 1);
            queues.put(name, newQueue);
            return newQueue;
        }
        return queue;
    }

    /**
     * Run an item in the main queue
     */
    public static void runMainQueue() {
        if(!mainQueue.isEmpty()) {
            mainQueue.poll().run();
        }
    }

    /**
     * Shutdown the thread queue
     */
    public static void shutdown() {
        for(ExecutorService queue : queues.values()) {
            queue.shutdown();
            try {
                if (!queue.awaitTermination(3, TimeUnit.SECONDS)) {
                    queue.shutdownNow(); // Force shutdown
                }
            } catch (InterruptedException e) {
                queue.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
