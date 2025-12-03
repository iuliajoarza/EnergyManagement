package com.example.demo.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String SYNC_EXCHANGE = "sync.events.exchange";
    public static final String USER_COMMANDS_QUEUE = "user.commands";
    public static final String AUTH_COMMANDS_QUEUE = "auth.commands";

    @Bean
    public FanoutExchange syncExchange() {
        return new FanoutExchange(SYNC_EXCHANGE, true, false);
    }

    @Bean
    public Queue userCommandsQueue() {
        return new Queue(USER_COMMANDS_QUEUE, true); // durable
    }

    @Bean
    public Queue authCommandsQueue() {
        return new Queue(AUTH_COMMANDS_QUEUE, true); // durable
    }
}
