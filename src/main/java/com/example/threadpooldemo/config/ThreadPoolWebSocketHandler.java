package com.example.threadpooldemo.config;

import com.example.threadpooldemo.service.ThreadPoolMonitorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;


import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author zh4ngyj
 * @date 2025/7/13
 * @description
 */
@Component
@RequiredArgsConstructor
public class ThreadPoolWebSocketHandler extends TextWebSocketHandler {

    private final ThreadPoolMonitorService monitorService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                List<Map<String, Object>> statsList = monitorService.getThreadPoolStats();
                String json = objectMapper.writeValueAsString(statsList);
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(json));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.SECONDS); // 每秒推送一次线程池状态
    }

}
