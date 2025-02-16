package org.sberuniversity;

import javax.xml.datatype.DatatypeConstants;
import java.util.Random;
import java.util.Scanner;


public class ThreadWork {
    Scanner scanner = new Scanner(System.in);

    public void launch() {
        while (true) {
            menu();
            int enter = scanner.nextInt();
            switch (enter) {
                case 1:
                    FixedThreadPoolTask();
                    break;
                case 2:
                    scalableThreadPoolTask();
                    break;
            }
        }
    }


    public static void scalableThreadPoolTask() {
        // Инициализируем ScalableThreadPool с минимальным количеством потоков 2 и максимальным 5
        ThreadPool scalableThreadPool = new ScalableThreadPool(2, 5);

        // Запускаем пул потоков
        scalableThreadPool.start();


        for (int i = 0; i < 10; i++) {
            int taskId = i;
            try{
                Thread.sleep(500);
            }catch (InterruptedException ignored){}
            scalableThreadPool.execute(() -> {
                System.out.println("Task " + taskId + " is running on thread " + Thread.currentThread().getName());
                try {
                    Random random = new Random();
                    // Симулируем работу задачи с задержкой
                    Thread.sleep(random.nextInt(1000) + 5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("Task " + taskId + " has completed.");
            });
        }
        System.out.println("Добавили все задачи");

        scalableThreadPool.shutdown();
        System.out.println("Thread pool has been shut down.");
    }

    public static void FixedThreadPoolTask() {
        ThreadPool threadPool = getThreadPool(true);

        // Запускаем пул потоков
        threadPool.start();

        // Добавляем 10 задач для выполнения
        for (int i = 0; i <30;i++) {
            int taskId = i;
            threadPool.execute(() -> {
                System.out.println("Task " + taskId + " is running on thread " + Thread.currentThread().getName());
                try {
                    Random random = new Random();
                    // Симулируем работу задачи с задержкой
                    Thread.sleep(random.nextInt(3000) + 1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("Task " + taskId + " has completed.");
            });
        }
        System.out.println("Добавили все задачи");
        // Завершаем работу пула потоков
        threadPool.shutdown();
        System.out.println("Thread pool has been shut down.");
    }

    private static ThreadPool getThreadPool(boolean useScalablePool) {
        if (useScalablePool) {
            // Создаем масштабируемый пул с минимальным количеством потоков 2 и максимальным 5
            return new FixedThreadPool(15);
        } else {
            // Создаем фиксированный пул с 3 потоками
            return new ScalableThreadPool(2, 5);
        }
    }

    public static void menu() {
        System.out.println("Выберите пункт меню:\n1 - проверить работу FixedThreadPool\n2 - проверить работу ScalableThreadPool");
    }
}

