package com.example.websocket.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    
    @JsonProperty("user_id")
    private String userId;
    
    @JsonProperty("username")
    private String username;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("timestamp")
    private String timestamp;
    
    @JsonProperty("sender")
    private String sender; // "user" or "admin" or "bot"
    
    @JsonProperty("session_id")
    private String sessionId;
}
