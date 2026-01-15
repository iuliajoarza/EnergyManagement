package com.example.chat.service;

import com.example.chat.model.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    @Autowired
    private RuleBasedChatbotService chatbotService;

    @Autowired
    private AIService aiService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${chat.queue.outgoing}")
    private String outgoingQueue;

    @Value("${ai.enabled:true}")
    private boolean aiEnabled;

    /**
     * Listen to user chat messages from RabbitMQ
     */
    @RabbitListener(queues = "${chat.queue.incoming}")
    public void handleUserMessage(String message) {
        try {
            logger.info("Received chat message: {}", message);
            
            ChatMessage chatMessage = objectMapper.readValue(message, ChatMessage.class);
            String userMessage = chatMessage.getMessage();
            String userId = chatMessage.getUserId();

            // Process message through rule-based system with userId for AI context
            String botResponse = chatbotService.processMessage(userMessage, userId);

            // Create response message
            ChatMessage responseMessage = new ChatMessage();
            responseMessage.setUserId(userId);
            responseMessage.setMessage(botResponse);
            responseMessage.setSender("bot");
            responseMessage.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            responseMessage.setSessionId(chatMessage.getSessionId());

            // Send response back via RabbitMQ to WebSocket service
            String responseJson = objectMapper.writeValueAsString(responseMessage);
            rabbitTemplate.convertAndSend(outgoingQueue, responseJson);
            
            logger.info("Sent bot response to user {}: {}", userId, botResponse);

        } catch (Exception e) {
            logger.error("Error processing chat message: {}", e.getMessage(), e);
        }
    }
}
