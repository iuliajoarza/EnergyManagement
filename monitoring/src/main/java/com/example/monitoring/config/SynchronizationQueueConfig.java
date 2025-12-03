package com.example.monitoring.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SynchronizationQueueConfig {
    public static final String SYNC_QUEUE = "sync.events";

    @Bean
    public Queue syncQueue() {
        return new Queue(SYNC_QUEUE, true);
    }
}
