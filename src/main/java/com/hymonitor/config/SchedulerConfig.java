package com.hymonitor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Scheduler configuration for health check monitoring
 * Enables scheduled tasks and configures thread pool
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {

    /**
     * Configure TaskScheduler bean with thread pool
     * @return configured TaskScheduler for monitoring tasks
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("monitor-");
        scheduler.initialize();
        return scheduler;
    }
}
