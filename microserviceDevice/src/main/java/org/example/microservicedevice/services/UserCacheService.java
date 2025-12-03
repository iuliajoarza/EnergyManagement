package org.example.microservicedevice.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.microservicedevice.entities.UserCache;
import org.example.microservicedevice.repositories.UserCacheRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserCacheService {
    private final UserCacheRepository userCacheRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public UserCacheService(UserCacheRepository userCacheRepository) {
        this.userCacheRepository = userCacheRepository;
    }

    @RabbitListener(queues = "sync.events.device")
    @Transactional
    public void handleUserSync(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String type = node.get("type").asText();
            
            if ("user".equals(type)) {
                String userId = node.get("user_id").asText();
                String username = node.has("attributes") && node.get("attributes").has("name") 
                    ? node.get("attributes").get("name").asText() 
                    : null;
                
                UUID userUuid = UUID.fromString(userId);
                
                UserCache userCache = new UserCache();
                userCache.setUserId(userUuid);
                userCache.setUsername(username);
                userCacheRepository.save(userCache);
                
                System.out.println("=== Saved user to cache DB: " + userId + " (" + username + ")");
            } else if ("user_deleted".equals(type)) {
                String userId = node.get("user_id").asText();
                UUID userUuid = UUID.fromString(userId);
                userCacheRepository.deleteById(userUuid);
                System.out.println("=== Deleted user from cache DB: " + userId);
            }
        } catch (Exception e) {
            System.err.println("=== Error processing user sync for cache: " + e.getMessage());
        }
    }

    public boolean userExists(UUID userId) {
        return userCacheRepository.existsByUserId(userId);
    }

    public UUID getUserIdByUsername(String username) {
        Optional<UserCache> userCache = userCacheRepository.findByUsername(username);
        return userCache.map(UserCache::getUserId).orElse(null);
    }

    public UserCache getUserInfo(UUID userId) {
        return userCacheRepository.findById(userId).orElse(null);
    }

    public java.util.List<UserCache> getAllUsers() {
        return userCacheRepository.findAll();
    }
}
