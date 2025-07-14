package com.example.threadpooldemo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author: zh4ngyj
 * @date: 2025/7/11 17:41
 * @des:
 */
@Service
public class TaskService {

    private final ExecutorService problemPool;

    private final ThreadPoolTaskExecutor optimizedPool;

    @Autowired
    public TaskService(
            @Qualifier("problemPool") ExecutorService problemPool,
            @Qualifier("optimizedPool") ThreadPoolTaskExecutor optimizedPool,
            ServletWebServerApplicationContext applicationContext) {
        this.problemPool = problemPool;
        this.optimizedPool = optimizedPool;
    }

    // 问题方法：使用阻塞调用
    public String runBlockingTasks() {
        long startTime = System.currentTimeMillis();

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // 创建5个任务（超过线程池大小）
        for (int i = 1; i <= 5; i++) {
            final int taskId = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(
                    () -> simulateLongTask(taskId),
                    problemPool
            );
            futures.add(future);
        }

        // 阻塞等待所有任务完成（问题根源）
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(30, TimeUnit.SECONDS); // 超时设置为30秒

            long endTime = System.currentTimeMillis();
            System.out.println("耗时：" + (endTime-startTime)/1000);

            return "所有任务已完成";
        } catch (Exception e) {
            return "任务执行失败: " + e.getMessage();
        }
    }

    // 优化方法1：使用合理大小的线程池
    public String runOptimizedTasks() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            final int taskId = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(
                    () -> simulateLongTask(taskId),
                    optimizedPool
            );
            futures.add(future);
        }

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(10, TimeUnit.SECONDS);
            return "所有任务已完成（优化线程池）";
        } catch (Exception e) {
            return "任务执行失败: " + e.getMessage();
        }
    }

    // 优化方法2：异步非阻塞返回
    @Async("asyncExecutor")
    public CompletableFuture<String> runAsyncTasks() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            final int taskId = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(
                    () -> simulateLongTask(taskId),
                    optimizedPool
            );
            futures.add(future);
        }

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(10, TimeUnit.SECONDS);
            return CompletableFuture.completedFuture("所有任务已完成（异步非阻塞）");
        } catch (Exception e) {
            return CompletableFuture.completedFuture("任务执行失败: " + e.getMessage());
        }
    }

    // 模拟长时间运行的任务（3秒）
    private void simulateLongTask(int taskId) {
        try {
            Thread.sleep(3000); // 模拟3秒耗时操作
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
