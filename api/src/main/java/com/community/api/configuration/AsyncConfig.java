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

        executor.setCorePoolSize(8); // 8 threads to handle the average expected concurrent tasks

        // Max pool size: Allows for handling spikes in traffic, but should not be too high
        executor.setMaxPoolSize(32); // Up to 32 threads for handling load spikes

        // Queue capacity: Should be large enough to handle incoming tasks without rejecting them
        executor.setQueueCapacity(200); // Queue up to 200 tasks before new tasks are rejected

        // Keep alive time: Time for idle threads to wait before they are terminated
        executor.setKeepAliveSeconds(60); // 60 seconds for idle threads before termination
        // Custom thread name prefix for easier debugging and identification
        executor.setThreadNamePrefix("CustomAsyncExecutor-");

        // Initialize the executor
        executor.initialize();
        return executor;
    }
}