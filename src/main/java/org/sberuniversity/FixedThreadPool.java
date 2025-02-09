package org.sberuniversity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class FixedThreadPool implements ThreadPool {
    private BlockingDeque<Runnable> queue = new LinkedBlockingDeque<>();
    private final List<Worker> workers;
    private boolean isQueueBlocked = false;

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
        if(isQueueBlocked) {
            throw new IllegalStateException("Can't add new task while waiting for tasks");
        }
        queue.offer(task);
    }

    @Override
    public void waitForTasks() {
        isQueueBlocked = true;
        while (!queue.isEmpty()) {
            while(isAnyWorkerBusy()){
            }
        }
        isQueueBlocked = false;
    }

    private boolean isAnyWorkerBusy() {
        for (Worker worker : workers) {
            if (worker.isWorking()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void shutdown() {
        waitForTasks();
        isQueueBlocked = true;
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
        private boolean isWorking = false;

        public Worker(BlockingDeque<Runnable> queue) {
            this.queue = queue;
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    isWorking = false;
                    Runnable task = queue.take();
                    isWorking = true;
                    task.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        public boolean isWorking() {
            return isWorking;
        }
    }
}