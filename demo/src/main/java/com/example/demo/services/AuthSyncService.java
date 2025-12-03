package com.example.demo.services;

import com.example.demo.dtos.AuthUserDTO;
import org.springframework.stereotype.Service;

@Service
public class AuthSyncService {

    private final org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
    private static final String AUTH_COMMANDS_QUEUE = "auth.commands";

    public AuthSyncService(org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void createAuthUser(AuthUserDTO dto) {
        try {
            java.util.Map<String, Object> command = new java.util.HashMap<>();
            command.put("command", "create_auth_user");
            command.put("data", dto);
            
            String message = objectMapper.writeValueAsString(command);
            rabbitTemplate.convertAndSend(AUTH_COMMANDS_QUEUE, message);
            System.out.println("Published create auth user command: " + message);
        } catch (Exception ex) {
            System.err.println("Failed to publish create auth user command: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void updateAuthUser(Long id, AuthUserDTO dto) {
        try {
            java.util.Map<String, Object> command = new java.util.HashMap<>();
            command.put("command", "update_auth_user");
            command.put("id", id);
            command.put("data", dto);
            
            String message = objectMapper.writeValueAsString(command);
            rabbitTemplate.convertAndSend(AUTH_COMMANDS_QUEUE, message);
            System.out.println("Published update auth user command: " + message);
        } catch (Exception ex) {
            System.err.println("Failed to publish update auth user command: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void deleteAuthUser(Long id) {
        try {
            java.util.Map<String, Object> command = new java.util.HashMap<>();
            command.put("command", "delete_auth_user");
            command.put("id", id);
            
            String message = objectMapper.writeValueAsString(command);
            rabbitTemplate.convertAndSend(AUTH_COMMANDS_QUEUE, message);
            System.out.println("Published delete auth user command: " + message);
        } catch (Exception ex) {
            System.err.println("Failed to publish delete auth user command: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
