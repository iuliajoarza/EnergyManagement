package org.example.microservicedevice.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class UserCommandsConsumerService {
    private final DeviceService deviceService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public UserCommandsConsumerService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @RabbitListener(queues = "user.commands")
    public void receiveUserCommand(String message) {
        System.out.println("=== Received user command: " + message);
        try {
            JsonNode node = objectMapper.readTree(message);
            String command = node.get("command").asText();
            
            if ("delete_user_devices".equals(command)) {
                String userId = node.get("user_id").asText();
                System.out.println("=== Processing delete_user_devices for user: " + userId);
                deviceService.deleteByUserId(UUID.fromString(userId));
                System.out.println("=== Successfully deleted devices for user: " + userId);
            } else {
                System.err.println("=== Unknown command: " + command);
            }
        } catch (Exception e) {
            System.err.println("=== Error processing user command: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
