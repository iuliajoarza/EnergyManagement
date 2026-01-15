package com.example.monitoring.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "device_info")
public class DeviceInfo {
    @Id
    @Column(name = "device_id")
    private String deviceId;
    
    @Column(name = "user_id")
    private String userId;
    
    @Column(name = "username")
    private String username;
    
    @Column(name = "max_consumption")
    private Double maxConsumption;
    
    private String description;
    private String address;

    public DeviceInfo() {}

    public DeviceInfo(String deviceId, String userId, Double maxConsumption) {
        this.deviceId = deviceId;
        this.userId = userId;
        this.maxConsumption = maxConsumption;
    }

    public DeviceInfo(String deviceId, String userId, String username, Double maxConsumption) {
        this.deviceId = deviceId;
        this.userId = userId;
        this.username = username;
        this.maxConsumption = maxConsumption;
    }

    // Getters and setters
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public Double getMaxConsumption() { return maxConsumption; }
    public void setMaxConsumption(Double maxConsumption) { this.maxConsumption = maxConsumption; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
