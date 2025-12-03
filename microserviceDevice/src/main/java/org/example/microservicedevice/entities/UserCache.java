package org.example.microservicedevice.entities;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "user_cache")
public class UserCache {
    @Id
    private UUID userId;
    
    private String username;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
