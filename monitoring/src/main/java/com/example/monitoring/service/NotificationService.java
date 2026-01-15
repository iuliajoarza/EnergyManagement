package com.example.monitoring.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Send overconsumption alert to WebSocket microservice via RabbitMQ
     */
    public void sendOverconsumptionAlert(String userId, String deviceId, 
                                        Double consumptionValue, Double maxConsumption) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("user_id", userId);
            notification.put("device_id", deviceId);
            notification.put("consumption_value", consumptionValue);
            notification.put("max_consumption", maxConsumption);
            notification.put("type", "overconsumption");
            notification.put("message", String.format(
                "ALERT: Device %s has exceeded maximum consumption! Current: %.2f kW, Max: %.2f kW",
                deviceId, consumptionValue, maxConsumption
            ));
            notification.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

            String message = objectMapper.writeValueAsString(notification);
            
            rabbitTemplate.convertAndSend(
                "overconsumption.exchange", 
                "overconsumption.alert", 
                message
            );
            
            System.out.println("=== Sent overconsumption alert: " + message);
        } catch (Exception e) {
            System.err.println("=== Error sending overconsumption alert: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
