package com.example.monitoring.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "hourly_energy_consumption")
public class HourlyEnergyConsumption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String deviceId;
    private Double totalEnergyKwh;
    private LocalDateTime hourTimestamp; // Start of the hour

    // Constructors
    public HourlyEnergyConsumption() {}

    public HourlyEnergyConsumption(String deviceId, Double totalEnergyKwh, LocalDateTime hourTimestamp) {
        this.deviceId = deviceId;
        this.totalEnergyKwh = totalEnergyKwh;
        this.hourTimestamp = hourTimestamp;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public Double getTotalEnergyKwh() { return totalEnergyKwh; }
    public void setTotalEnergyKwh(Double totalEnergyKwh) { this.totalEnergyKwh = totalEnergyKwh; }

    public LocalDateTime getHourTimestamp() { return hourTimestamp; }
    public void setHourTimestamp(LocalDateTime hourTimestamp) { this.hourTimestamp = hourTimestamp; }
}
