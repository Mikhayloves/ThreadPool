package org.sberuniversity;


public interface ThreadPool {
    void start();
    void execute(Runnable task);
    void shutdown();
}