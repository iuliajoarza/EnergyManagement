package com.example.monitoring.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String DEVICE_QUEUE = "energy_data";

    @Bean
    public Queue deviceDataQueue() {
        return new Queue(DEVICE_QUEUE, false);
    }

    @Bean
    public SimpleMessageListenerContainer deviceDataContainer(ConnectionFactory connectionFactory,
                                                               MessageListenerAdapter deviceListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(DEVICE_QUEUE);
        container.setMessageListener(deviceListenerAdapter);
        return container;
    }

    @Bean
    public MessageListenerAdapter deviceListenerAdapter(com.example.monitoring.service.DeviceDataConsumerService receiver) {
        return new MessageListenerAdapter(receiver, "receiveMessage");
    }
}
