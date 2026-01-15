package com.example.websocket.listener;

import com.example.websocket.model.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Listens for admin messages and forwards them to appropriate users
 */
@Component
public class AdminMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(AdminMessageListener.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Listen to user messages that need admin attention
     */
    @RabbitListener(queues = "admin.user.messages")
    public void handleUserMessageForAdmin(String message) {
        try {
            logger.info("User message for admin: {}", message);
            
            ChatMessage chatMessage = objectMapper.readValue(message, ChatMessage.class);
            
            // Broadcast to all connected admins on the admin topic
            messagingTemplate.convertAndSend("/topic/admin/messages", chatMessage);
            
            logger.info("Broadcasted user message to admin interface");
            
        } catch (Exception e) {
            logger.error("Error handling user message for admin: {}", e.getMessage(), e);
        }
    }
}
