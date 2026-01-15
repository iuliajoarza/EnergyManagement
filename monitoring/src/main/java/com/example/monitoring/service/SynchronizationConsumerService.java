package com.example.monitoring.service;

import com.example.monitoring.entity.DeviceInfo;
import com.example.monitoring.repository.DeviceInfoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class SynchronizationConsumerService {
    
    private final DeviceInfoRepository deviceInfoRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SynchronizationConsumerService(DeviceInfoRepository deviceInfoRepository) {
        this.deviceInfoRepository = deviceInfoRepository;
    }

    /**
     * Process sync events for device information (including max_consumption and username)
     */
    public void processSyncEvent(byte[] messageBytes) {
        String message = new String(messageBytes, StandardCharsets.UTF_8);
        System.out.println("=== Received sync event: " + message);
        
        try {
            JsonNode node = objectMapper.readTree(message);
            String eventType = node.get("type").asText();
            
            if ("device".equals(eventType)) {
                String deviceId = node.get("device_id").asText();
                JsonNode attributes = node.get("attributes");
                
                if (attributes != null) {
                    String userId = attributes.has("user_id") ? attributes.get("user_id").asText() : null;
                    String username = attributes.has("username") ? attributes.get("username").asText() : null;
                    Double maxConsumption = attributes.has("max_consumption") ? 
                                          attributes.get("max_consumption").asDouble() : null;
                    String description = attributes.has("description") ? 
                                       attributes.get("description").asText() : null;
                    String address = attributes.has("address") ? 
                                   attributes.get("address").asText() : null;
                    
                    // Save or update device info
                    DeviceInfo deviceInfo = deviceInfoRepository.findByDeviceId(deviceId)
                        .orElse(new DeviceInfo(deviceId, userId, username, maxConsumption));
                    
                    if (userId != null) deviceInfo.setUserId(userId);
                    if (username != null) deviceInfo.setUsername(username);
                    if (maxConsumption != null) deviceInfo.setMaxConsumption(maxConsumption);
                    if (description != null) deviceInfo.setDescription(description);
                    if (address != null) deviceInfo.setAddress(address);
                    
                    deviceInfoRepository.save(deviceInfo);
                    System.out.println("=== Saved device info: " + deviceId + 
                                     " (user: " + username + ", max: " + maxConsumption + " kW)");
                }
            } else if ("device_deleted".equals(eventType)) {
                String deviceId = node.get("device_id").asText();
                deviceInfoRepository.deleteById(deviceId);
                System.out.println("=== Deleted device info: " + deviceId);
            }
            
        } catch (Exception e) {
            System.err.println("=== Error processing sync event: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
