package com.tbs.bookingservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async executor configuration for booking flow background threads.
 * Used by BookingAttemptService and compensation threads in BookingService.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    // Dedicated thread pool isolates booking async work from the default Spring executor
    @Bean(name = "bookingAsyncExecutor")
    public Executor bookingAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("booking-async-");
        executor.initialize();
        return executor;
    }
}
