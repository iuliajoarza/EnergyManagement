package com.example.websocket.controller;

import com.example.websocket.model.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class WebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Receives chat messages from clients and forwards to chat service via RabbitMQ
     */
    @MessageMapping("/chat.send")
    public void sendMessageToChat(@Payload ChatMessage chatMessage, 
                                   SimpMessageHeaderAccessor headerAccessor) {
        try {
            logger.info("Received chat message from user {}: {}", 
                        chatMessage.getUserId(), chatMessage.getMessage());
            
            // Set timestamp if not provided
            if (chatMessage.getTimestamp() == null) {
                chatMessage.setTimestamp(LocalDateTime.now()
                        .format(DateTimeFormatter.ISO_DATE_TIME));
            }
            
            // Set sender as user
            chatMessage.setSender("user");
            
            // Forward to chat service via RabbitMQ
            String messageJson = objectMapper.writeValueAsString(chatMessage);
            rabbitTemplate.convertAndSend("chat.user.messages", messageJson);
            
            logger.info("Forwarded chat message to RabbitMQ queue: chat.user.messages");
            
        } catch (Exception e) {
            logger.error("Error forwarding chat message: {}", e.getMessage(), e);
        }
    }

    /**
     * Admin response handler - forwards admin replies to chat service
     */
    @MessageMapping("/chat.admin.reply")
    public void adminReply(@Payload ChatMessage chatMessage) {
        try {
            // Use username if userId is not set
            String targetUser = chatMessage.getUserId() != null ? 
                               chatMessage.getUserId() : chatMessage.getUsername();
            
            logger.info("Admin reply to user {}: {}", 
                        targetUser, chatMessage.getMessage());
            
            // Ensure userId is set for downstream processing
            if (chatMessage.getUserId() == null && chatMessage.getUsername() != null) {
                chatMessage.setUserId(chatMessage.getUsername());
            }
            
            // Set timestamp if not provided
            if (chatMessage.getTimestamp() == null) {
                chatMessage.setTimestamp(LocalDateTime.now()
                        .format(DateTimeFormatter.ISO_DATE_TIME));
            }
            
            // Set sender as admin
            chatMessage.setSender("admin");
            
            // Forward to chat service via RabbitMQ
            String messageJson = objectMapper.writeValueAsString(chatMessage);
            rabbitTemplate.convertAndSend("admin.messages", messageJson);
            
            logger.info("Forwarded admin reply to RabbitMQ queue: admin.messages");
            
        } catch (Exception e) {
            logger.error("Error forwarding admin reply: {}", e.getMessage(), e);
        }
    }

    /**
     * Request admin support - forwards user message to admin queue
     */
    @MessageMapping("/chat.request.admin")
    public void requestAdmin(@Payload ChatMessage chatMessage) {
        try {
            // Use username if userId is not set
            String userId = chatMessage.getUserId() != null ? 
                           chatMessage.getUserId() : chatMessage.getUsername();
            
            logger.info("User {} requesting admin support: {}", userId, chatMessage.getMessage());
            
            // Ensure userId is set for downstream processing
            if (chatMessage.getUserId() == null && chatMessage.getUsername() != null) {
                chatMessage.setUserId(chatMessage.getUsername());
            }
            
            // Set timestamp if not provided
            if (chatMessage.getTimestamp() == null) {
                chatMessage.setTimestamp(LocalDateTime.now()
                        .format(DateTimeFormatter.ISO_DATE_TIME));
            }
            
            // Forward to admin notification queue
            String messageJson = objectMapper.writeValueAsString(chatMessage);
            rabbitTemplate.convertAndSend("admin.user.messages", messageJson);
            
            logger.info("Forwarded user message to admin queue: admin.user.messages");
            
        } catch (Exception e) {
            logger.error("Error forwarding to admin: {}", e.getMessage(), e);
        }
    }

    /**
     * Test endpoint for notifications
     */
    @MessageMapping("/notification.test")
    @SendTo("/topic/notifications/test")
    public String testNotification(@Payload String message) {
        logger.info("Test notification: {}", message);
        return message;
    }
}
