package com.trip.diary.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@EnableAsync
@Configuration
public class AsyncConfig {
    @Bean
    public Executor executor(){
        int processorSize = Runtime.getRuntime().availableProcessors();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(processorSize);
        executor.setMaxPoolSize(processorSize * 2);
        executor.setQueueCapacity(processorSize * 6);
        executor.initialize();
        return executor;
    }
}
