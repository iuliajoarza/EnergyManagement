package com.example.websocket.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {
    
    @JsonProperty("user_id")
    private String userId;
    
    @JsonProperty("device_id")
    private String deviceId;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("timestamp")
    private String timestamp;
    
    @JsonProperty("consumption_value")
    private Double consumptionValue;
    
    @JsonProperty("max_consumption")
    private Double maxConsumption;
    
    @JsonProperty("type")
    private String type; // "overconsumption", "warning", "info"
}
