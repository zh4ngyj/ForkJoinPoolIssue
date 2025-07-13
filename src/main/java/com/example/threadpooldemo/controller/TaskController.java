package com.example.threadpooldemo.controller;

import com.example.threadpooldemo.service.TaskService;
import com.example.threadpooldemo.service.ThreadPoolMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;
import java.util.Map;

/**
 * @author: zh4ngyj
 * @date: 2025/7/11 17:42
 * @des:
 */
@RestController
@RequestMapping("/api")
public class TaskController {

    private final TaskService taskService;

    private final ThreadPoolMonitorService threadPoolMonitorService;

    @Autowired
    public TaskController(TaskService taskService,
                          ThreadPoolMonitorService threadPoolMonitorService) {
        this.taskService = taskService;
        this.threadPoolMonitorService = threadPoolMonitorService;
    }

    // 问题端点：会阻塞Tomcat线程
    @GetMapping("/problem")
    public String problemEndpoint() {
        return taskService.runBlockingTasks();
    }

    // 优化端点1：使用合理线程池
    @GetMapping("/optimized")
    public String optimizedEndpoint() {
        return taskService.runOptimizedTasks();
    }

    // 优化端点2：异步非阻塞
    @GetMapping("/async")
    public DeferredResult<String> asyncEndpoint() {
        DeferredResult<String> deferredResult = new DeferredResult<>(30000L);

        taskService.runAsyncTasks().whenComplete((result, ex) -> {
            if (ex != null) {
                deferredResult.setErrorResult("异步任务执行出错: " + ex.getMessage());
            } else {
                deferredResult.setResult(result);
            }
        });

        return deferredResult;
    }

    // 健康检查端点
    @GetMapping("/health")
    public List<Map<String, Object>> healthCheck() {
        return threadPoolMonitorService.getThreadPoolStats();
    }
}
