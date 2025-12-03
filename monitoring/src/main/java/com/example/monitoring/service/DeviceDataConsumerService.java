package com.example.monitoring.service;

import com.example.monitoring.entity.DeviceData;
import com.example.monitoring.entity.HourlyEnergyConsumption;
import com.example.monitoring.repository.DeviceDataRepository;
import com.example.monitoring.repository.HourlyEnergyConsumptionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class DeviceDataConsumerService {
    private final DeviceDataRepository repository;
    private final HourlyEnergyConsumptionRepository hourlyRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DeviceDataConsumerService(DeviceDataRepository repository, HourlyEnergyConsumptionRepository hourlyRepository) {
        this.repository = repository;
        this.hourlyRepository = hourlyRepository;
    }

    public void receiveMessage(byte[] messageBytes) {
        String message = new String(messageBytes, StandardCharsets.UTF_8);
        System.out.println("=== Received message from RabbitMQ: " + message);
        try {
            JsonNode node = objectMapper.readTree(message);
            String deviceId = node.get("device_id").asText();
            Double measurementValue = node.get("measurement_value").asDouble();
            LocalDateTime timestamp = LocalDateTime.parse(node.get("timestamp").asText(), DateTimeFormatter.ISO_DATE_TIME);
            
            // Save raw device data
            DeviceData data = new DeviceData();
            data.setDeviceId(deviceId);
            data.setMeasurementValue(measurementValue);
            data.setTimestamp(timestamp);
            repository.save(data);
            System.out.println("=== Successfully saved device data: " + deviceId + " - " + measurementValue);
            
            // Aggregate hourly energy consumption
            aggregateHourlyConsumption(deviceId, measurementValue, timestamp);
            
        } catch (Exception e) {
            System.err.println("=== Error processing message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void aggregateHourlyConsumption(String deviceId, Double measurementValue, LocalDateTime timestamp) {
        // Truncate to hour (e.g., 2025-12-01T15:23:45 -> 2025-12-01T15:00:00)
        LocalDateTime hourTimestamp = timestamp.withMinute(0).withSecond(0).withNano(0);
        
        // Each measurement represents 10 minutes of consumption
        // Convert to kWh (measurement is in kW for 10 minutes = kW * 10/60 hours)
        Double energyKwh = measurementValue * (10.0 / 60.0);
        
        // Find or create hourly record
        HourlyEnergyConsumption hourly = hourlyRepository
            .findByDeviceIdAndHourTimestamp(deviceId, hourTimestamp)
            .orElse(new HourlyEnergyConsumption(deviceId, 0.0, hourTimestamp));
        
        // Add to total
        hourly.setTotalEnergyKwh(hourly.getTotalEnergyKwh() + energyKwh);
        hourlyRepository.save(hourly);
        
        System.out.println("=== Updated hourly consumption for " + deviceId + " at " + hourTimestamp + ": " + hourly.getTotalEnergyKwh() + " kWh");
    }
}
