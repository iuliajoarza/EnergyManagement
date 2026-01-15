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
public class AdminChatService {

    private static final Logger logger = LoggerFactory.getLogger(AdminChatService.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${admin.queue.outgoing}")
    private String outgoingQueue;

    /**
     * Listen to admin chat messages from RabbitMQ (admin replies to users)
     */
    @RabbitListener(queues = "${admin.queue.incoming}")
    public void handleAdminMessage(String message) {
        try {
            logger.info("Received admin message: {}", message);
            
            ChatMessage chatMessage = objectMapper.readValue(message, ChatMessage.class);
            String adminMessage = chatMessage.getMessage();
            
            // Use username if userId is not set
            String targetUserId = chatMessage.getUserId() != null ? 
                                 chatMessage.getUserId() : chatMessage.getUsername();
            
            // Ensure userId is set if only username was provided
            if (chatMessage.getUserId() == null && chatMessage.getUsername() != null) {
                targetUserId = chatMessage.getUsername();
            }

            // Create response message to forward to user
            ChatMessage responseMessage = new ChatMessage();
            responseMessage.setUserId(targetUserId);
            responseMessage.setUsername(targetUserId);
            responseMessage.setMessage(adminMessage);
            responseMessage.setSender("admin");
            responseMessage.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            responseMessage.setSessionId(chatMessage.getSessionId());

            // Send admin response to websocket service for delivery to user
            String responseJson = objectMapper.writeValueAsString(responseMessage);
            rabbitTemplate.convertAndSend(outgoingQueue, responseJson);
            
            logger.info("Forwarded admin message to user {}: {}", targetUserId, adminMessage);

        } catch (Exception e) {
            logger.error("Error processing admin message: {}", e.getMessage(), e);
        }
    }

    /**
     * Forward user message to admin queue for admin to see and reply
     */
    public void forwardToAdmin(ChatMessage userMessage) {
        try {
            // Mark as forwarded to admin
            ChatMessage adminNotification = new ChatMessage();
            adminNotification.setUserId(userMessage.getUserId());
            adminNotification.setMessage(userMessage.getMessage());
            adminNotification.setSender("user");
            adminNotification.setTimestamp(userMessage.getTimestamp());
            adminNotification.setSessionId(userMessage.getSessionId());

            String messageJson = objectMapper.writeValueAsString(adminNotification);
            rabbitTemplate.convertAndSend("admin.user.messages", messageJson);
            
            logger.info("Forwarded user message to admin queue: {}", userMessage.getMessage());
        } catch (Exception e) {
            logger.error("Error forwarding message to admin: {}", e.getMessage(), e);
        }
    }
}
