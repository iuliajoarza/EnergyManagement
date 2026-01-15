package com.example.chat.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${chat.queue.incoming}")
    private String incomingQueue;

    @Value("${chat.queue.outgoing}")
    private String outgoingQueue;

    @Value("${chat.exchange}")
    private String chatExchange;

    @Bean
    public Queue incomingChatQueue() {
        return new Queue(incomingQueue, true);
    }

    @Bean
    public Queue outgoingChatQueue() {
        return new Queue(outgoingQueue, true);
    }

    @Bean
    public TopicExchange chatExchange() {
        return new TopicExchange(chatExchange);
    }

    @Bean
    public Binding incomingBinding(Queue incomingChatQueue, TopicExchange chatExchange) {
        return BindingBuilder.bind(incomingChatQueue).to(chatExchange).with("chat.user.#");
    }

    @Bean
    public Binding outgoingBinding(Queue outgoingChatQueue, TopicExchange chatExchange) {
        return BindingBuilder.bind(outgoingChatQueue).to(chatExchange).with("chat.bot.#");
    }

    // Admin chat queues
    @Bean
    public Queue adminUserMessagesQueue() {
        return new Queue("admin.user.messages", true);
    }

    @Bean
    public Queue adminMessagesQueue() {
        return new Queue("admin.messages", true);
    }
}
