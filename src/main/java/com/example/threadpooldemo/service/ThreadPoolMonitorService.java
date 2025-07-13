package com.example.threadpooldemo.service;

import org.apache.tomcat.util.threads.ThreadPoolExecutor; // 使用 Tomcat 的 ThreadPoolExecutor
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.AbstractProtocol;
import org.apache.coyote.ProtocolHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

/**
 * @author zh4ngyj
 * @date 2025/7/13
 * @description
 */
@Service
public class ThreadPoolMonitorService {

    private final ServletWebServerApplicationContext applicationContext;

    private final ExecutorService problemPool;

    private final ThreadPoolTaskExecutor optimizedPool;

    @Autowired
    public ThreadPoolMonitorService(ServletWebServerApplicationContext applicationContext,
                                    @Qualifier("problemPool") ExecutorService problemPool,
                                    @Qualifier("optimizedPool") ThreadPoolTaskExecutor optimizedPool) {
        this.applicationContext = applicationContext;
        this.problemPool = problemPool;
        this.optimizedPool = optimizedPool;
    }

    /**
     * 获取所有线程池的状态（Tomcat + 自定义）
     *
     * @return List of thread pool stats
     */
    public List<Map<String, Object>> getThreadPoolStats() {
        List<Map<String, Object>> statsList = new ArrayList<>();

        // Tomcat 线程池
        ThreadPoolExecutor tomcatExecutor = getTomcatThreadPoolExecutor();
        statsList.add(getThreadPoolStats(tomcatExecutor, "Tomcat"));

        // 问题线程池（problemPool）
        if (problemPool instanceof ForkJoinPool)
            statsList.add(getThreadPoolStats((ForkJoinPool)problemPool,"problemPool"));

        return statsList;
    }

    /**
     * 获取特定线程池的状态信息（适用于 ThreadPoolExecutor）
     */
    private Map<String, Object> getThreadPoolStats(ThreadPoolExecutor executor, String name) {

        Map<String, Object> stats = new HashMap<>();
        stats.put("name", name);

        if (executor == null) {
            stats.put("status", "DOWN");
            stats.put("error", "Error retrieving thread pool stats");
            return stats;
        }

        stats.put("status", "UP");
        stats.put("activeThreads", executor.getActiveCount());
        stats.put("maxThreads", executor.getMaximumPoolSize());
        stats.put("corePoolSize", executor.getCorePoolSize());
        stats.put("queueSize", executor.getQueue().size());
        stats.put("completedTasks", executor.getCompletedTaskCount());
        stats.put("poolSize", executor.getPoolSize());
        stats.put("largestPoolSize", executor.getLargestPoolSize());

        // 计算使用率
        double usage = (double) executor.getActiveCount() / executor.getMaximumPoolSize() * 100;
        stats.put("usagePercentage", String.format("%.2f%%", usage));

        return stats;
    }

    /**
     * 获取 ForkJoinPool 的状态信息
     *
     * @param executor ForkJoinPool 实例
     * @param name     线程池名称
     * @return 状态 Map
     */
    private Map<String, Object> getThreadPoolStats(ForkJoinPool executor, String name) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("name", name);

        if (executor == null) {
            stats.put("status", "DOWN");
            stats.put("error", "Executor instance is null");
            return stats;
        }

        stats.put("status", "UP");

        // ForkJoinPool 专有指标
        int parallelism = executor.getParallelism();
        int activeCount = executor.getActiveThreadCount();
        long queuedTasks = executor.getQueuedTaskCount();
        long stealCount = executor.getStealCount();
        int poolSize = executor.getPoolSize();
        int runningThreads = executor.getRunningThreadCount();

        stats.put("parallelism", parallelism);
        stats.put("activeThreads", activeCount);
        stats.put("runningThreads", runningThreads);
        stats.put("poolSize", poolSize);
        stats.put("queuedTasks", queuedTasks);
        stats.put("stealCount", stealCount);

        // 使用率：活跃线程 / 并行度
        double usage = parallelism > 0
                ? (double) activeCount / parallelism * 100
                : 0.0;
        stats.put("usagePercentage", String.format("%.2f%%", usage));

        return stats;
    }

    private ThreadPoolExecutor getTomcatThreadPoolExecutor() {
        try {
            // 获取 TomcatWebServer
            Object webServer = applicationContext.getWebServer();

            // 使用反射获取 Tomcat 实例
            java.lang.reflect.Method getTomcat = webServer.getClass().getMethod("getTomcat");
            Object tomcat = getTomcat.invoke(webServer);

            // 获取连接器
            java.lang.reflect.Method getConnector = tomcat.getClass().getMethod("getConnector");
            Connector connector = (Connector) getConnector.invoke(tomcat);

            // 获取协议处理器
            ProtocolHandler handler = connector.getProtocolHandler();

            if (handler instanceof AbstractProtocol) {
                AbstractProtocol<?> protocol = (AbstractProtocol<?>) handler;

                // 使用反射获取线程池
                java.lang.reflect.Method getExecutor = protocol.getClass().getMethod("getExecutor");
                Object executor = getExecutor.invoke(protocol);

                // 直接返回 Tomcat 的 ThreadPoolExecutor
                if (executor instanceof ThreadPoolExecutor) {
                    return (ThreadPoolExecutor) executor;
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to get Tomcat thread pool:" + e.getMessage());
            return null;
        }
        return null;
    }
}
