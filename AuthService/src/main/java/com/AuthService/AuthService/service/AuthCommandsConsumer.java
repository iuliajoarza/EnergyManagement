package com.AuthService.AuthService.service;

import com.AuthService.AuthService.entity.Role;
import com.AuthService.AuthService.entity.UserCredential;
import com.AuthService.AuthService.repository.UserCredentialRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthCommandsConsumer {
    private final UserCredentialRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthCommandsConsumer(UserCredentialRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @RabbitListener(queues = "auth.commands")
    public void handleAuthCommand(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String command = node.has("command") ? node.get("command").asText() : "";
            JsonNode data = node.get("data");

            switch (command) {
                case "create_auth_user" -> createOrUpdateByUsername(data);
                case "update_auth_user" -> createOrUpdateByUsername(data);
                case "delete_auth_user" -> deleteByNode(node, data);
                default -> System.out.println("[auth.commands] Unknown command: " + command);
            }
        } catch (Exception e) {
            System.err.println("[auth.commands] Error processing message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createOrUpdateByUsername(JsonNode data) {
        if (data == null || !data.hasNonNull("username")) {
            System.err.println("[auth.commands] Missing username in data");
            return;
        }
        String username = data.get("username").asText();
        String password = data.hasNonNull("password") ? data.get("password").asText() : null;
        String roleStr = data.hasNonNull("role") ? data.get("role").asText() : null;

        Role role = Role.ROLE_USER;
        if (roleStr != null && !roleStr.isEmpty()) {
            try {
                String s = roleStr.toUpperCase();
                if (!s.startsWith("ROLE_")) s = "ROLE_" + s;
                role = Role.valueOf(s);
            } catch (IllegalArgumentException ignored) {}
        }

        var existing = repository.findByUsername(username);
        if (existing.isPresent()) {
            UserCredential u = existing.get();
            if (password != null && !password.isEmpty()) {
                u.setPassword(passwordEncoder.encode(password));
            }
            u.setRole(role);
            repository.save(u);
            System.out.println("[auth.commands] Updated auth user: " + username);
        } else {
            if (password == null || password.isEmpty()) {
                System.err.println("[auth.commands] Cannot create user without password: " + username);
                return;
            }
            UserCredential u = new UserCredential();
            u.setUsername(username);
            u.setPassword(passwordEncoder.encode(password));
            u.setRole(role);
            repository.save(u);
            System.out.println("[auth.commands] Created auth user: " + username);
        }
    }

    private void deleteByNode(JsonNode node, JsonNode data) {
        // Prefer username if provided; fallback to id (Long) if available
        if (data != null && data.hasNonNull("username")) {
            String username = data.get("username").asText();
            repository.findByUsername(username).ifPresent(u -> {
                repository.deleteById(u.getId());
                System.out.println("[auth.commands] Deleted auth user by username: " + username);
            });
            return;
        }
        if (node.hasNonNull("id")) {
            long id = node.get("id").asLong();
            repository.deleteById(id);
            System.out.println("[auth.commands] Deleted auth user by id: " + id);
        } else {
            System.err.println("[auth.commands] Missing username/id for delete_auth_user");
        }
    }
}
