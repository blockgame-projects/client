package com.james090500.utils;

import lombok.Getter;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ThreadUtil {

    private static ExecutorService queue;

    public static ExecutorService getQueue() {
        if(queue == null || queue.isShutdown()) {
            int cores = Runtime.getRuntime().availableProcessors();
            queue = Executors.newFixedThreadPool(cores - 1);
        }
        return queue;
    }

    @Getter
    private static final ConcurrentLinkedQueue<Runnable> mainQueue = new ConcurrentLinkedQueue<>();

    public static void runMainQueue() {
        if(!mainQueue.isEmpty()) {
            mainQueue.poll().run();
        }
    }

    public static void shutdown() {
        ThreadUtil.getQueue().shutdown();
        try {
            if (!ThreadUtil.getQueue().awaitTermination(3, TimeUnit.SECONDS)) {
                ThreadUtil.getQueue().shutdownNow(); // Force shutdown
            }
        } catch (InterruptedException e) {
            ThreadUtil.getQueue().shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
