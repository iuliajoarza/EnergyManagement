package com.example.demo.services;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

@Service
public class SyncPublisherService {
    private static final String SYNC_EXCHANGE = "sync.events.exchange";
    private static final String USER_COMMANDS_QUEUE = "user.commands";
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SyncPublisherService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishUserSync(String userId, String name) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("type", "user");
            event.put("user_id", userId);
            
            Map<String, String> attributes = new HashMap<>();
            if (name != null) {
                attributes.put("name", name);
            }
            event.put("attributes", attributes);
            
            String message = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(SYNC_EXCHANGE, "", message);
            System.out.println("Published user sync event: " + message);
        } catch (Exception e) {
            System.err.println("Error publishing user sync event: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void publishDeleteUserDevices(String userId) {
        try {
            Map<String, Object> command = new HashMap<>();
            command.put("command", "delete_user_devices");
            command.put("user_id", userId);
            
            String message = objectMapper.writeValueAsString(command);
            rabbitTemplate.convertAndSend(USER_COMMANDS_QUEUE, message);
            System.out.println("Published delete user devices command: " + message);
        } catch (Exception e) {
            System.err.println("Error publishing delete user devices command: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void publishUserDeleted(String userId) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("type", "user_deleted");
            event.put("user_id", userId);
            event.put("attributes", new HashMap<>()); // placeholder for consistency
            String message = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(SYNC_EXCHANGE, "", message);
            System.out.println("Published user deleted event: " + message);
        } catch (Exception e) {
            System.err.println("Error publishing user deleted event: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
