package org.example.microservicedevice.services;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

@Service
public class SyncPublisherService {
    private static final String SYNC_EXCHANGE = "sync.events.exchange";
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SyncPublisherService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishDeviceSync(String deviceId, String name) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("type", "device");
            event.put("device_id", deviceId);
            
            Map<String, String> attributes = new HashMap<>();
            if (name != null) {
                attributes.put("name", name);
            }
            event.put("attributes", attributes);
            
            String message = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(SYNC_EXCHANGE, "", message);
            System.out.println("Published device sync event: " + message);
        } catch (Exception e) {
            System.err.println("Error publishing device sync event: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void publishDeviceDeleted(String deviceId) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("type", "device_deleted");
            event.put("device_id", deviceId);
            
            String message = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(SYNC_EXCHANGE, "", message);
            System.out.println("Published device_deleted event: " + message);
        } catch (Exception e) {
            System.err.println("Error publishing device_deleted event: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
