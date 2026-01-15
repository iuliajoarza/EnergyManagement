package com.example.monitoring.config;

import com.example.monitoring.service.SynchronizationConsumerService;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SynchronizationQueueConfig {
    public static final String SYNC_QUEUE = "sync.events.monitoring";
    public static final String SYNC_EXCHANGE = "sync.events.exchange";

    @Bean
    public Queue syncQueue() {
        return new Queue(SYNC_QUEUE, true);
    }
    
    @Bean
    public FanoutExchange syncExchange() {
        return new FanoutExchange(SYNC_EXCHANGE, true, false);
    }
    
    @Bean
    public Binding syncBinding(Queue syncQueue, FanoutExchange syncExchange) {
        return BindingBuilder.bind(syncQueue).to(syncExchange);
    }
    
    @Bean
    public SimpleMessageListenerContainer syncContainer(ConnectionFactory connectionFactory,
                                                       MessageListenerAdapter syncListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(SYNC_QUEUE);
        container.setMessageListener(syncListenerAdapter);
        return container;
    }

    @Bean
    public MessageListenerAdapter syncListenerAdapter(SynchronizationConsumerService receiver) {
        return new MessageListenerAdapter(receiver, "processSyncEvent");
    }
}

