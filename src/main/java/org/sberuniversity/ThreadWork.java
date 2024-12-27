package org.sberuniversity;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class ThreadWork {
    Scanner scanner = new Scanner(System.in);

    public void launch() {
        while (true) {
            menu();
            int enter = scanner.nextInt();
            switch (enter) {
                case 1:
                    scalableThreadPoolTask();
                    break;
                case 2:
                    FixedThreadPoolTask();
                    break;
            }
        }
    }


    public static void scalableThreadPoolTask() {
        // Инициализируем ScalableThreadPool с минимальным количеством потоков 2 и максимальным 5
        ThreadPool scalableThreadPool = new ScalableThreadPool(2, 5);

        // Запускаем пул потоков
        scalableThreadPool.start();

        // Добавляем 10 задач для выполнения
        for (int i = 0; i < 10; i++) {
            int taskId = i;
            scalableThreadPool.execute(() -> {
                System.out.println("Task " + taskId + " is running on thread " + Thread.currentThread().getName());
                try {
                    // Симулируем работу задачи с задержкой
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("Task " + taskId + " has completed.");
            });
        }

        // Ждем выполнения всех задач (время ожидания должно быть больше, чем общее время выполнения всех задач)
        try {
            TimeUnit.SECONDS.sleep(25); // Время может варьироваться в зависимости от задания
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Завершаем работу пула потоков
        scalableThreadPool.shutdown();
        System.out.println("Thread pool has been shut down.");
    }

    public static void FixedThreadPoolTask() {
        ThreadPool threadPool = getThreadPool(true);

        // Запускаем пул потоков
        threadPool.start();

        // Добавляем 10 задач для выполнения
        for (int i = 0; i < 10; i++) {
            int taskId = i;
            threadPool.execute(() -> {
                System.out.println("Task " + taskId + " is running on thread " + Thread.currentThread().getName());
                try {
                    // Симулируем работу задачи с задержкой
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("Task " + taskId + " has completed.");
            });
        }

        // Ждем выполнения всех задач
        try {
            TimeUnit.SECONDS.sleep(15);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Завершаем работу пула потоков
        threadPool.shutdown();
        System.out.println("Thread pool has been shut down.");
    }

    private static ThreadPool getThreadPool(boolean useScalablePool) {
        if (useScalablePool) {
            // Создаем масштабируемый пул с минимальным количеством потоков 2 и максимальным 5
            return new ScalableThreadPool(2, 5);
        } else {
            // Создаем фиксированный пул с 3 потоками
            return new FixedThreadPool(3);
        }
    }

    public static void menu() {
        System.out.println("Выберите пункт меню:\n1 - проверить работу FixedThreadPool\n2 - проверить работу ScalableThreadPool");
    }
}

