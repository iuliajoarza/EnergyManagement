package org.example.microservicedevice.dtos.validators;

import java.util.Objects;
import java.util.UUID;

public class DeviceDTO {
    private UUID id;
    private String name;
    private Double maxConsumption;
    private UUID userId;

    public DeviceDTO() {}

    public DeviceDTO(UUID id, String name, Double maxConsumption, UUID userId) {
        this.id = id;
        this.name = name;
        this.maxConsumption = maxConsumption;
        this.userId = userId;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getMaxConsumption() { return maxConsumption; }
    public void setMaxConsumption(Double maxConsumption) { this.maxConsumption = maxConsumption; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceDTO deviceDTO = (DeviceDTO) o;
        return Objects.equals(name, deviceDTO.name) && Objects.equals(maxConsumption, deviceDTO.maxConsumption) && Objects.equals(userId, deviceDTO.userId);
    }

    @Override
    public int hashCode() { return Objects.hash(name, maxConsumption, userId); }
}

