package org.sberuniversity;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class ScalableThreadPool implements ThreadPool {
    private final BlockingDeque<Runnable> queue = new LinkedBlockingDeque<>();
    private final List<Worker> workers = new LinkedList<>();
    private final int minThreads;
    private final int maxThreads;

    public ScalableThreadPool(int minThreads, int maxThreads) {
        this.minThreads = minThreads;
        this.maxThreads = maxThreads;
        for (int i = 0; i < minThreads; i++) {
            workers.add(new Worker(queue));
        }
    }

    @Override
    public void start() {
        for (Worker worker : workers) {
            worker.start();
        }
    }

    @Override
    public void execute(Runnable task) {
        if (workers.size() < maxThreads) {
            // Если количество рабочих потоков меньше максимального, добавляем новый
            Worker worker = new Worker(queue);
            workers.add(worker);
            worker.start();
        }
        try {
            queue.put(task);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void shutdown() {
        for (Worker worker : workers) {
            worker.interrupt();
        }
    }

    private static class Worker extends Thread {
        private final BlockingDeque<Runnable> queue;

        public Worker(BlockingDeque<Runnable> queue) {
            this.queue = queue;
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    Runnable task = queue.take();
                    task.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
