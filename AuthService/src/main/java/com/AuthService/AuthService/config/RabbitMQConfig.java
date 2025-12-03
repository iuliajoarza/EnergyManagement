package com.AuthService.AuthService.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String AUTH_COMMANDS_QUEUE = "auth.commands";

    @Bean
    public Queue authCommandsQueue() {
        return new Queue(AUTH_COMMANDS_QUEUE, true);
    }
}
