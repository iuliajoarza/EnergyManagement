package com.example.websocket.listener;

import com.example.websocket.model.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class ChatResponseListener {

    private static final Logger logger = LoggerFactory.getLogger(ChatResponseListener.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @RabbitListener(queues = "chat.bot.responses")
    public void handleChatResponse(String message) {
        try {
            logger.info("Received chat bot response: {}", message);
            
            ChatMessage chatMessage = objectMapper.readValue(message, ChatMessage.class);
            
            // Use username if userId is not set
            String userId = chatMessage.getUserId();
            if (userId == null && chatMessage.getUsername() != null) {
                userId = chatMessage.getUsername();
                chatMessage.setUserId(userId);
            }
            
            logger.info("Processing chat response for userId: {} (username: {})", userId, chatMessage.getUsername());

            // Only send if we have a valid userId
            if (userId != null && !userId.isEmpty()) {
                // Send to user-specific topic
                messagingTemplate.convertAndSend("/topic/chat/" + userId, chatMessage);
                logger.info("Forwarded chat response to user: {}", userId);
            } else {
                logger.warn("Cannot forward message - both userId and username are null/empty. Message: {}", message);
            }

        } catch (Exception e) {
            logger.error("Error processing chat response: {}", e.getMessage(), e);
        }
    }
}
