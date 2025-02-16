package org.sberuniversity;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class ScalableThreadPool implements ThreadPool {
    private final BlockingDeque<Runnable> queue = new LinkedBlockingDeque<>();
    private final List<Worker> workers = Collections.synchronizedList(new LinkedList<>());
    private final int minThreads;
    private final int maxThreads;
    private AtomicInteger freeThreadsCount;
    private WorkerDoneCallback workerDoneCallback;
    private final Object workerDoneCallbackLock = new Object();
    private volatile boolean isQueueBlocked;

    public ScalableThreadPool(int minThreads, int maxThreads) {
        this.minThreads = minThreads;
        this.maxThreads = maxThreads;
        freeThreadsCount = new AtomicInteger(minThreads);
        workerDoneCallback = (workerRef) -> {
            if (queue.isEmpty()) {
                synchronized (workerDoneCallbackLock) {
                    if (workers.size() > this.minThreads) {
                        System.out.println("Был удален поток " + Thread.currentThread().getName() + " из списка рабочих потоков");
                        workers.remove((Worker) Thread.currentThread());
                        freeThreadsCount.decrementAndGet();
                        Thread.currentThread().interrupt();
                    }
                    if(isQueueBlocked && freeThreadsCount.get() == workers.size()){
                        for(Worker worker : workers){
                            if(worker != workerRef){
                                worker.interrupt();
                            }
                        }
                        workerRef.interrupt();
                    }
                }
            }
        };
        for (int i = 0; i < minThreads; i++) {
            workers.add(new Worker(queue, freeThreadsCount, workerDoneCallback));
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
        if (isQueueBlocked) {
            throw new IllegalStateException("Can't add new task while waiting for tasks");
        }
        if (freeThreadsCount.get() == 0 && workers.size() < maxThreads) {
            // Если количество рабочих потоков меньше максимального, добавляем новый
            Worker worker = new Worker(queue, freeThreadsCount, workerDoneCallback);
            workers.add(worker);
            freeThreadsCount.incrementAndGet();
            worker.start();
            System.out.println(worker.getName() + " добавлен в список рабочих потоков");
        }
        queue.offer(task);
    }


    @Override
    public void shutdown() {
        isQueueBlocked = true;
    }

    private  interface WorkerDoneCallback {
        void onDone(Worker worker);
    }

    private static class Worker extends Thread {
        private final BlockingDeque<Runnable> queue;
        private final AtomicInteger freeThreadsCount;
        private final WorkerDoneCallback workerDoneCallback;
        private boolean isWorking;


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
