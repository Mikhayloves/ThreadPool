package org.sberuniversity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class FixedThreadPool implements ThreadPool {
    private BlockingDeque<Runnable> queue = new LinkedBlockingDeque<>();
    private List<Worker> workers;
    private boolean isQueueBlocked = false;
    private AtomicInteger freeThreadsCount;
    private WorkerDoneCallback workerDoneCallback;
    private final Object workerDoneCallbackLock = new Object();

    public FixedThreadPool(int countThread) {
        freeThreadsCount = new AtomicInteger(countThread);
        workerDoneCallback = (workerRef) -> {
            if (queue.isEmpty()) {
                synchronized (workerDoneCallbackLock) {
                    if (isQueueBlocked && freeThreadsCount.get() == workers.size()) {
                        System.out.println("Threads termination...");
                        for (Worker worker : workers) {
                            if (worker != workerRef) {
                                worker.interrupt();
                            }
                        }
                        workerRef.interrupt();
                    }
                }
            }
        };
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
        if (isQueueBlocked) {
            throw new IllegalStateException("Can't add new task while waiting for tasks");
        }
        queue.offer(task);
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
        isQueueBlocked = true;
    }

    private List<Worker> createWorker(int countThread) {
        List<Worker> workers = new ArrayList<>();
        for (int i = 0; i < countThread; i++) {
            workers.add(new Worker(queue, freeThreadsCount, workerDoneCallback));
        }
        return workers;
    }

    private interface WorkerDoneCallback {
        void onDone(Worker worker);
    }


    private static class Worker extends Thread {
        private final BlockingDeque<Runnable> queue;
        private boolean isWorking = false;
        private final WorkerDoneCallback workerDoneCallback;
        private final AtomicInteger freeThreadsCount;

        public Worker(BlockingDeque<Runnable> queue,
                      AtomicInteger freeThreadsCount,
                      WorkerDoneCallback workerDoneCallback) {
            this.queue = queue;
            this.freeThreadsCount = freeThreadsCount;
            this.workerDoneCallback = workerDoneCallback;
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    Runnable task = queue.take();
                    isWorking = true;
                    freeThreadsCount.decrementAndGet();
                    task.run();
                    freeThreadsCount.incrementAndGet();
                    isWorking = false;
                    workerDoneCallback.onDone(this);
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