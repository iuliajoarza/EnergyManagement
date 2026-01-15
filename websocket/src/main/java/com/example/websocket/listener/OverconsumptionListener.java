package com.example.websocket.listener;

import com.example.websocket.model.NotificationMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class OverconsumptionListener {

    private static final Logger logger = LoggerFactory.getLogger(OverconsumptionListener.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Listens to overconsumption notifications from RabbitMQ
     * and forwards them to connected WebSocket clients
     */
    @RabbitListener(queues = "overconsumption.notifications")
    public void receiveOverconsumptionAlert(String message) {
        try {
            logger.info("Received overconsumption alert: {}", message);
            
            // Parse the notification message
            NotificationMessage notification = objectMapper.readValue(message, NotificationMessage.class);
            
            // Send to specific user via WebSocket
            String userId = notification.getUserId();
            if (userId != null && !userId.isEmpty()) {
                messagingTemplate.convertAndSend("/topic/notifications/" + userId, notification);
                logger.info("Sent overconsumption notification to user: {}", userId);
            } else {
                // Broadcast to all if no specific user
                messagingTemplate.convertAndSend("/topic/notifications/all", notification);
                logger.info("Broadcast overconsumption notification to all users");
            }
            
        } catch (Exception e) {
            logger.error("Error processing overconsumption alert: {}", e.getMessage(), e);
        }
    }
}
