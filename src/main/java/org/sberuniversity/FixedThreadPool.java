package org.sberuniversity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class FixedThreadPool implements ThreadPool {
    private BlockingDeque<Runnable> queue = new LinkedBlockingDeque<>();
    private final List<Worker> workers;

    public FixedThreadPool(int countThread) {
        this.workers = createWorker(countThread);
    }

    @Override
    public void start() {
        for (Worker worker : workers) {
            worker.start();
        }
    }

    @Override
    public void execute(Runnable task) {
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

    private List<Worker> createWorker(int countThread) {
        List<Worker> workers = new ArrayList<>();
        for (int i = 0; i < countThread; i++) {
            workers.add(new Worker(queue));
        }
        return workers;
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