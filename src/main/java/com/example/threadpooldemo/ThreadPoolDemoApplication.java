package com.example.threadpooldemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author: zh4ngyj
 * @date: 2025/7/11 17:40
 * @des:
 */
@SpringBootApplication
@EnableAsync
public class ThreadPoolDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ThreadPoolDemoApplication.class, args);
    }
}
