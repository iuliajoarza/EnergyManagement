package com.example.websocket.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String OVERCONSUMPTION_QUEUE = "overconsumption.notifications";
    public static final String OVERCONSUMPTION_EXCHANGE = "overconsumption.exchange";
    public static final String OVERCONSUMPTION_ROUTING_KEY = "overconsumption.alert";
    
    // Admin chat queues
    public static final String ADMIN_USER_MESSAGES_QUEUE = "admin.user.messages";
    public static final String ADMIN_MESSAGES_QUEUE = "admin.messages";

    @Bean
    Queue overconsumptionQueue() {
        return new Queue(OVERCONSUMPTION_QUEUE, true);
    }

    @Bean
    TopicExchange overconsumptionExchange() {
        return new TopicExchange(OVERCONSUMPTION_EXCHANGE);
    }

    @Bean
    Binding overconsumptionBinding(Queue overconsumptionQueue, TopicExchange overconsumptionExchange) {
        return BindingBuilder.bind(overconsumptionQueue)
                .to(overconsumptionExchange)
                .with(OVERCONSUMPTION_ROUTING_KEY);
    }

    // Admin chat queues
    @Bean
    Queue adminUserMessagesQueue() {
        return new Queue(ADMIN_USER_MESSAGES_QUEUE, true);
    }

    @Bean
    Queue adminMessagesQueue() {
        return new Queue(ADMIN_MESSAGES_QUEUE, true);
    }
}
