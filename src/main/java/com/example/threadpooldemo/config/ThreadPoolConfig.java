package com.example.threadpooldemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;

/**
 * @author: zh4ngyj
 * @date: 2025/7/11 17:41
 * @des:
 */
@Configuration
public class ThreadPoolConfig {

    // 问题线程池 - 只有2个线程（用于演示问题）
    @Bean(name = "problemPool")
    public ExecutorService problemThreadPool() {
        return new ForkJoinPool(
                2,
                ForkJoinPool.defaultForkJoinWorkerThreadFactory,
                null,
                true
        );
    }

    // 优化线程池 - 合理配置（解决方案）
    @Bean(name = "optimizedPool")
    public ThreadPoolTaskExecutor optimizedThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(corePoolSize * 2);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("optimized-pool-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    // 非阻塞线程池 - 用于异步返回
    @Bean(name = "asyncExecutor")
    public ThreadPoolTaskExecutor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("async-executor-");
        executor.initialize();
        return executor;
    }

}
