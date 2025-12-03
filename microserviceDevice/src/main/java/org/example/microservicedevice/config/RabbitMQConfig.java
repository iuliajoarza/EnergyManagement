package org.example.microservicedevice.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String SYNC_EXCHANGE = "sync.events.exchange";
    public static final String DEVICE_SYNC_QUEUE = "sync.events.device";
    public static final String USER_COMMANDS_QUEUE = "user.commands";

    @Bean
    public FanoutExchange syncExchange() {
        return new FanoutExchange(SYNC_EXCHANGE, true, false);
    }

    @Bean
    public Queue deviceSyncQueue() {
        return new Queue(DEVICE_SYNC_QUEUE, true); // durable
    }

    @Bean
    public Binding deviceSyncBinding(Queue deviceSyncQueue, FanoutExchange syncExchange) {
        return BindingBuilder.bind(deviceSyncQueue).to(syncExchange);
    }

    @Bean
    public Queue userCommandsQueue() {
        return new Queue(USER_COMMANDS_QUEUE, true); // durable
    }
}
