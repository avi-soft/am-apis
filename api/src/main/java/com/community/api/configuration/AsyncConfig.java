package com.community.api.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

    @Bean(name = "customAsyncExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Core pool size - minimum number of threads to keep alive
        executor.setCorePoolSize(8);  // Adjusted for higher concurrency

        // Max pool size - maximum number of threads to allow
        executor.setMaxPoolSize(32);  // Adjusted to allow more concurrent tasks

        // Queue capacity - limit for the task queue
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(60);// Adjusted to prevent overload

        // Custom thread name prefix for easier debugging and identification
        executor.setThreadNamePrefix("CustomAsyncExecutor-");

        // Initialize the executor
        executor.initialize();
        return executor;
    }
}